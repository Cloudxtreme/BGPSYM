package nl.nlnetlabs.bgpsym01.route.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.nlnetlabs.bgpsym01.callback.Callback;
import nl.nlnetlabs.bgpsym01.main.EL;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.OutputAddEntity;
import nl.nlnetlabs.bgpsym01.primitives.OutputEntity;
import nl.nlnetlabs.bgpsym01.primitives.OutputEntityType;
import nl.nlnetlabs.bgpsym01.primitives.OutputRemoveEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;
import nl.nlnetlabs.bgpsym01.route.MRAIStore;
import nl.nlnetlabs.bgpsym01.route.MRAITimer;
import nl.nlnetlabs.bgpsym01.route.Policy;
import nl.nlnetlabs.bgpsym01.route.output.OutputState.UpdateToSendType;

import org.apache.log4j.Logger;

public class OutputBufferImpl implements OutputBuffer {

    public Policy getPolicy() {
        return policy;
    }

    private static final int UPDATE_COUNT_THRESHOLD = 50;

    private static Logger log = Logger.getLogger(OutputBufferImpl.class);

    private Neighbors neighbors;

    private Policy policy;

    private ASIdentifier asIdentifier;

    private Callback callback;

    /**
     * MRAIStore - if the MRAITimer for a particular neighbor is not expired we
     * register such a timer in this store
     */
    private MRAIStore mraiStore;

    private OutputBufferStore bufferStore;

    private OutputState outputState;

    public OutputState getOutputState() {
        return outputState;
    }

    public void setOutputState(OutputState outputState) {
        this.outputState = outputState;
    }

    public OutputBufferImpl(ASIdentifier asIdentifier) {
        this.asIdentifier = asIdentifier;
    }

    public void add(OutputEntity entity) {
        callback.addEntity(entity);

        // bad
        boolean isWithdrawal = entity.getType() == OutputEntityType.WITHDRAWAL;

        if (isWithdrawal) {
            addWithdrawal((OutputRemoveEntity) entity);
        } else {
            addAnnouncement((OutputAddEntity) entity);
        }
    }

    private void addWithdrawal(OutputRemoveEntity entity) {
        bufferStore.addWithdrawal(entity);
    }

    private void addAnnouncement(OutputAddEntity entity) {
        bufferStore.addAnnouncement(entity);
    }

    public void flush(ASIdentifier asIdentifier) {
        Neighbor neighbor = neighbors.getNeighbor(asIdentifier);
        try {
            processForANeighbor(neighbor, bufferStore.announcementsIterator(neighbor), true);
            bufferStore.clearAnnouncements(neighbor);
        } catch (Exception e) {
            // it's ok - there was nothing to send, happens during tests
        }
    }

    public void flush() {
        processWithdrawals();
        processAnnouncements();
    }

    private void processAnnouncements() {
        for (Neighbor neighbor : neighbors) {
            processForANeighbor(neighbor, bufferStore.announcementsIterator(), false);
        }
        // clear the list...
        bufferStore.clearAnnouncements();
    }

    private int allAnn;
    private int defAnn;

    private void annProcess(boolean deferred) {
        allAnn++;
        if (deferred) {
            defAnn++;
        }
        if (allAnn % 1000000 == 0) {
            if (EL.queueDebug && log.isInfoEnabled()) {
                log.info("X7, defAnn: " + defAnn + " / " + allAnn);
            }
        }
    }

    private void processForANeighbor(Neighbor neighbor, Iterator<OutputAddEntity> iterator, boolean unflushing) {
        // if timer - rewrite it all to store

        if (EL.queueDebug && log.isInfoEnabled()) {
            log.info("X7, flushing..., hasNext()=" + iterator.hasNext());
        }

        if (!iterator.hasNext()) {
            return;
        }

        MRAITimer timer = neighbor.getTimer();

        // deferring
        if ((!timer.canSendNow()) || timer.isTicking()) {
            if (EL.queueDebug && log.isInfoEnabled()) {
                log.info("X7, can: " + timer.canSendNow() + ", tick=" + timer.isTicking() + ", id=" + System.identityHashCode(timer));
            }
            if (unflushing) {
                log.warn("unflushing, but cannot send; asId=" + neighbor.getASIdentifier() + ", timer=" + timer);
            }
            while (iterator.hasNext()) {
                annProcess(true);
                OutputAddEntity entity = iterator.next();

                // remove the old one
                bufferStore.removeAnnouncement(neighbor, entity.getPrefix());
                outputState.deferred(neighbor, entity.getPrefix(), entity.getLastRoute());

                bufferStore.addAnnouncement(neighbor, entity);
            }
            mraiStore.register(neighbor.getASIdentifier(), timer);
            return;
        }

        // else
        // send it...

        HashMap<Route, ArrayList<Prefix>> map = new LinkedHashMap<Route, ArrayList<Prefix>>();
        boolean sent = false;
        ArrayList<Prefix> withdrawals = new ArrayList<Prefix>();
        while (iterator.hasNext()) {
            OutputAddEntity entity = iterator.next();
            //log.info("processing: "+entity.getPrefix()+ " route: "+entity.getRoute() + "last route: "+entity.getLastRoute());
            UpdateToSendType updateType = outputState.getUpdateType(neighbor, entity.getPrefix(), entity.getRoute(), entity.getLastRoute());


            switch (updateType) {
            case NONE:
                outputState.sent(neighbor, entity.getPrefix());
                break;

            case ANNOUNCE:

                outputState.sent(neighbor, entity.getPrefix());
                Route route = entity.getRoute();
                ArrayList<Prefix> list = map.get(route);
                if (list == null) {
                    list = new ArrayList<Prefix>();
                    map.put(route, list);
                }
                list.add(entity.getPrefix());

                callback.prefixAdvertised(neighbor.getASIdentifier(), entity.getPrefix(), entity.getRoute());
                if (list.size() > UPDATE_COUNT_THRESHOLD) {
                    sent |= createAndSendUpdate(neighbor, route, list, withdrawals);
                    list.clear();
                }
                break;
            case WITHDRAWAL:

                outputState.sent(neighbor, entity.getPrefix());
                // throw new NotImplementedException();
                // update.addWithdrawal(addEntity.getPrefix());
                withdrawals.add(entity.getPrefix());
                callback.withdrawalSent(neighbor.getASIdentifier(), entity.getPrefix(), null);
                break;
            }
        }

        for (Map.Entry<Route, ArrayList<Prefix>> entry : map.entrySet()) {
            if (entry.getValue().size() > 0) {
                sent |= createAndSendUpdate(neighbor, entry.getKey(), entry.getValue(), withdrawals);
                withdrawals.clear();
            }
        }

        if (withdrawals.size() > 0) {
            sent |= createAndSendUpdate(neighbor, null, null, withdrawals);
            withdrawals.clear();
        }

        if (sent) {
            timer.sent();
        }
    }

    //@SuppressWarnings("unused")
	private boolean createAndSendUpdate(Neighbor neighbor, Route route, List<Prefix> list, List<Prefix> withdrawals) {
        if (log.isInfoEnabled()) {
            log.info("X7, send " + list + " and withdrawals: "+withdrawals+" to " + neighbor.getASIdentifier() + "route: "+route);
        }
        
        BGPUpdate update = new BGPUpdate(asIdentifier, route == null ? null : route.copyWithMeOnPath(asIdentifier));
        update.setPrefixes(list);
        update.setWithdrawals(withdrawals);
        callback.updateSend(neighbor.getASIdentifier(), update);
        neighbor.send(update);
        return true;
    }

    private void processWithdrawals() {

        if (!bufferStore.withdrawalsIterator().hasNext()) {
            // don't go other neighbors if there is nothing to do...
            return;
        }

        // timers don't matter for withdrawals...
        for (Neighbor neighbor : neighbors) {
            Iterator<OutputRemoveEntity> iterator = bufferStore.withdrawalsIterator();
            ArrayList<Prefix> withdrawals = new ArrayList<Prefix>();
            while (iterator.hasNext()) {
                OutputRemoveEntity entity = iterator.next();


                // just remove it - don't think what's there - if something was,
                // it was already deferred
                bufferStore.removeAnnouncement(neighbor, entity.getPrefix());

                UpdateToSendType updateType = outputState.getUpdateType(neighbor, entity.getPrefix(), null, entity.getLastRoute());
                if (updateType == UpdateToSendType.WITHDRAWAL) {
                    if (withdrawals == null) {
                        withdrawals = new ArrayList<Prefix>();
                    }
                    withdrawals.add(entity.getPrefix());
                    outputState.sent(neighbor, entity.getPrefix());
                    callback.withdrawalSent(neighbor.getASIdentifier(), entity.getPrefix(), null);
                }
            }
            if (withdrawals.size() > 0) {
                createAndSendUpdate(neighbor, null, null, withdrawals);
            }
        }
        bufferStore.clearWithdrawals();
    }

    public void setNeighbors(Neighbors neighbors) {
        this.neighbors = neighbors;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public MRAIStore getMraiStore() {
        return mraiStore;
    }

    public void setMraiStore(MRAIStore store) {
        this.mraiStore = store;
    }

    public OutputBufferStore getBufferStore() {
        return bufferStore;
    }

    public void setBufferStore(OutputBufferStore bufferStore) {
        this.bufferStore = bufferStore;
    }

    public Neighbors getNeighbors() {
        return neighbors;
    }

    public void invalidate(Neighbor neighbor, List<Prefix> prefixList) {
        /*
         * Things that need to be done:
         *  1. all pending announcements for this neighbor for this prefix have to be canceled
         *  2. withdrawal has to be sent
         *  3. OutputState does not need to be notified
         */

        // 1
        for (Prefix prefix: prefixList) {
            bufferStore.removeAnnouncement(neighbor, prefix);
        }
        // 2
        createAndSendUpdate(neighbor, null, null, prefixList);
    }

    void addAnnouncements(Neighbor neighbor, List<Pair<Prefix, Route>> prefixes) {
        for (Pair<Prefix, Route> pair : prefixes) {
            Prefix prefix = pair.key;
            Route route = pair.value;

            // ////////////////
            // this is needed because the updates for the prefix might have been
            // able make it way to the queue (they are thrown out during
            // processing before the sending, not during deferring)
            bufferStore.removeAnnouncement(neighbor, prefix);

            if (route == null) {
                // it is withdrawal - nothing needs to be sent (this route was
                // already blocked)
            } else {
                // last route was null because the prefix was suppressed
                Route lastRoute = null;
                bufferStore.addAnnouncement(neighbor, new OutputAddEntity(prefix, route, lastRoute));
            }
        }
    }

    public void validate(Neighbor neighbor, List<Pair<Prefix, Route>> prefixes) {
        /*
         * Things that need to be done:
         * for each prefix/route pair:
         *  1. check what type of update needs to be sent
         *  2. with:
         *         pass
         *     ann:
         *         add to the buffer
         *  3. flush the user if possible
         */
        addAnnouncements(neighbor, prefixes);
        if (neighbor.getTimer().canSendNow()) {
            flush(neighbor.getASIdentifier());
        }
    }

}

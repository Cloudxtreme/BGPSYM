package nl.nlnetlabs.bgpsym01.primitives.timers;

import nl.nlnetlabs.bgpsym01.callback.Callback;
import nl.nlnetlabs.bgpsym01.main.EL;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;
import nl.nlnetlabs.bgpsym01.primitives.types.PriorityMutableQueue;
import nl.nlnetlabs.bgpsym01.primitives.types.UnflapUpdate;

import org.apache.log4j.Logger;

public class FlapStoreImpl implements FlapStore {

    private static Logger log = Logger.getLogger(FlapStoreImpl.class);

    PriorityMutableQueue<Pair<ASIdentifier, Prefix>, FlapTimer> queue = new PriorityMutableQueue<Pair<ASIdentifier, Prefix>, FlapTimer>();

    private TimeController timeController;

    private Callback callback;

    private long readyTime = -1;

    public void register(Prefix prefix, ASIdentifier asId, FlapTimer timer) {
        if (EL.queueDebug && log.isInfoEnabled()) {
            log.info("flap register...");
        }

        /*        if (log.isInfoEnabled()) {
                    long unflap = timer.getUnflapTime();
                    long sleep = unflap - timeController.getCurrentTime();
                    log.info("FLAP registered for " + prefix + ", unsupTime=" + unflap + ", sleep=" + sleep + ", value=" + timer.getValue() + ", id="
                            + System.identityHashCode(timer));
                }*/
        queue.add(new Pair<ASIdentifier, Prefix>(asId, prefix), timer);
        readyTime = timeController.getRealMS(queue.peekValue().getUnflapTime());
        if (EL.flapLogging && log.isInfoEnabled()) {
            log.info("FLAP registered for " + prefix + ", id=" + System.identityHashCode(timer) + ", size=" + queue.size() + ", realWait="
                    + timeController.realWaitingTime(readyTime, false));
        }

        if (EL.timersLogging) {
            callback.flapRegister(asId, prefix, timeController.getRealMS(timer.getUnflapTime()), readyTime);
        }
    }

    public long getReadyTime() {
        return readyTime;
    }

    public boolean hasSomething() {
        return readyTime != -1;
    }

    // NOCOMMIT
    public FlapTimer getFirstTimer() {
        return queue.peekValue();
    }

    public Pair<ASIdentifier, Prefix> next() {
        FlapTimer timer = queue.peekValue();
        long u1 = timer.getUnflapTime();
        Pair<ASIdentifier, Prefix> pair = queue.pop();
        if (queue.size() > 0) {
            long u2 = queue.peekValue().getUnflapTime();
            if (u1 > u2) {
                // sanity check
                log.warn("u1=" + u1 + " > u2=" + u2);
            }
            readyTime = timeController.getRealMS(u2);
        } else {
            readyTime = -1;
        }
        if (EL.flapLogging && log.isInfoEnabled()) {
            log.info("returning timer, id=" + System.identityHashCode(timer) + ", curr=" + timeController.getCurrentTime() + ", unflap=" + u1 + ", size="
                    + queue.size() + ", realWait=" + timeController.realWaitingTime(readyTime, false));
        }
        if (EL.timersLogging) {
            callback.flapTrigger(pair.key, pair.value, readyTime);
        }
        return pair;
    }

    public TimeController getTimeController() {
        return timeController;
    }

    public void setTimeController(TimeController timeController) {
        this.timeController = timeController;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public int size() {
        return queue.size();
    }

    public Update getUpdate() {
        Pair<ASIdentifier, Prefix> pair = next();
        UnflapUpdate update = new UnflapUpdate();
        update.setPair(pair);
        return update;
    }

}

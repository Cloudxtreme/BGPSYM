package nl.nlnetlabs.bgpsym01.route;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import nl.nlnetlabs.bgpsym01.cache.PrefixCache;
import nl.nlnetlabs.bgpsym01.cache.PrefixCacheImplBlock;
import nl.nlnetlabs.bgpsym01.cache.PrefixInfo;
import nl.nlnetlabs.bgpsym01.callback.Callback;
import nl.nlnetlabs.bgpsym01.main.EL;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.OutputAddEntity;
import nl.nlnetlabs.bgpsym01.primitives.OutputRemoveEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapStore;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimer;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimerFactory;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimerImpl;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeController;
import nl.nlnetlabs.bgpsym01.route.output.OutputBuffer;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

public class PrefixStoreMapImpl implements PrefixStore {

    private static Logger log = Logger.getLogger(PrefixStoreMapImpl.class);

    private OutputBuffer outputBuffer;

    private Policy policy;

    private FlapTimerFactory flapTimerFactory;

    private ASIdentifier asIdentifier;

    private Neighbors neighbors;

    private Callback callback;

    PrefixCache cache;

    private FlapStore flapStore;
    
    TimeController timeController;

    private XProperties properties;
    int prefixesReceived = 0;
    
    int noBest = 0;

    public PrefixStoreMapImpl() {
        properties = XProperties.getInstance();
    }
    
    public ArrayList<Prefix> removePrefixesFromSender(ASIdentifier sender) {

		ArrayList<Prefix> prefixesToRemove = new ArrayList<Prefix>();

		if (cache instanceof PrefixCacheImplBlock) {
			LinkedHashMap<Prefix, PrefixInfo> prefixes = (LinkedHashMap<Prefix, PrefixInfo>) ((PrefixCacheImplBlock) cache).getTable();

			synchronized (prefixes) {
				Iterator<Entry<Prefix, PrefixInfo>> iterator = prefixes.entrySet().iterator();

				while (iterator.hasNext()) {
					Entry<Prefix, PrefixInfo> current = iterator.next();
					PrefixInfo info = current.getValue();
					Prefix currentPrefix = current.getKey();

					PrefixTableEntry entry = info.getCurrentEntry();
					if (entry != null) {
						Route route = entry.getRoute();
						if (route != null && route.isFrom(sender)) {
							prefixesToRemove.add(currentPrefix);
						}
					}
				}
			}
			

			if (prefixesToRemove.size() > 0) {
				// log.info("prefixes to delete: "+prefixesToDelete);
				prefixRemove(sender, prefixesToRemove);
			}
		}

		return prefixesToRemove;
	}

    public void prefixRemove(ASIdentifier asIdentifier, Collection<Prefix> prefixes) {
        assert prefixes != null && prefixes.size() > 0;
        for (Prefix prefix : prefixes) {
            prefixRemove(asIdentifier, prefix);
        }
        outputBuffer.flush();
    }

    boolean prefixRemove(ASIdentifier originator, Prefix prefix) {
        callback.withdrawalReceived(originator, prefix);

        PrefixInfo prefixInfo = cache.getPrefixInfo(prefix);

        Map<ASIdentifier, PrefixTableEntry> neighborsForPrefix = prefixInfo.getNeighborsMap();

        /* we don't know anything about this particular prefix therefore we cannot withdraw this data
         * still it's weird that we have received info about it
         */
        if (neighborsForPrefix == null) {
            log.warn("neighborsForPrefix==null, prefix=" + prefix);
            return false;
        }

        /*
         * if we don't have info about this prefix from specified AS we cannot withdraw it
         * 
         * This is a legitimate if the entry is not valid because we were on the AS path.
         * We check if this the case. If it is not, we output a warning.
         */
        PrefixTableEntry entry = neighborsForPrefix.get(originator);
        if (entry == null || !entry.isValid()) {
            if (entry == null || !entry.isContainsMe()) {
                log.warn("don't have entry for " + prefix + ", entry=" + entry + ", me=" + (entry == null ? "ERROR" : entry.isContainsMe()));
            }
            return false;
        }

        FlapTimer flapTimer = entry.getFlapTimer();
        flapTimer.withdraw();
        registerFlapIfNeeded(originator, prefix, flapTimer);

        boolean wasCurrent = prefixInfo.getCurrentEntry() == entry;

        // write test for it!!!
        Route oldRoute = prefixInfo.getCurrentEntry() == null ? null : prefixInfo.getCurrentEntry().getRoute();
        entry.invalidate(false);

        if (wasCurrent) {
            if (log.isDebugEnabled()) {
                log.debug("removing prefix " + prefix + " from " + originator + ", is current, size=" + neighborsForPrefix.size());
            }

            runDecision(originator, prefixInfo, oldRoute);

            return true;
        } else {
            return false;
        }

    }

    /**
     * Replaces oldEntry with newEntry. Adds information to outputBuffer (for
     * sending).
     * 
     * @param prefixInfo
     * @param oldEntry
     * @param newEntry
     * @param addToBuffer
     */
    void replaceRoute(PrefixInfo prefixInfo, PrefixTableEntry newEntry, boolean addToBuffer) {

        Route oldRoute = null;
        if (prefixInfo.getCurrentEntry() != null) {
            oldRoute = prefixInfo.getCurrentEntry().getRoute();
        }

        prefixInfo.setCurrentEntry(newEntry);
        if (addToBuffer) {
            outputBuffer.add(new OutputAddEntity(prefixInfo, newEntry.getRoute(), oldRoute));
        }      
    }

    /**
     * Called when a prefix is to be replaced or deregistered, not when a truly
     * new prefix comes.
     * 
     * Runs decision process: 1. search for best route for given prefix 2.
     * install that route 3. add appropriate info to the output buffer
     * 
     * @param originator
     * @param prefixInfo
     *            prefix for which we are working
     * @param currentRoute
     *            TODO
     * @return TODO
     */
    boolean runDecision(ASIdentifier originator, PrefixInfo prefixInfo, Route currentRoute) {
        /*
         * 1. remove entry from current prefix store
         * 2. find new entry for the prefix store and put it in place
         */

        // this where we will store the new best route - if it stays null there
        // is totally nothing to send
        PrefixTableEntry best = null;
        boolean output = false;

        Prefix prefix = prefixInfo.getPrefix();
        Set<ASIdentifier> neighborSet = prefixInfo.getNeighborsMap().keySet();
        for (ASIdentifier asId : neighborSet) {
            PrefixTableEntry pte = prefixInfo.getNeighborsMap().get(asId);

            if (!pte.isValid()) {
                continue;
            }

            if (log.isDebugEnabled()) {
                log.debug("flapped=" + pte.getFlapTimer().isFlapped());
            }

            if (pte.getFlapTimer().isFlapped()) {
                continue;
            }

            /*
             * check whether the new one is better
             */

            if (policy.isBetter(asIdentifier, prefix, best == null ? null : best.getRoute(), getNeighbor(best), pte.getRoute(), getNeighbor(pte))) {
                best = pte;
            }
        }

        // install the new route
        if (best != null) {
            // debugging purposes - this "if" is slowing us down, but it is
            // useful to have it
            if (EL.checkWarnings && best == prefixInfo.getCurrentEntry() && best.getRoute().equals(currentRoute)) {

                /*
                 * we treat situation when we start the decision process and end up with exactly the same stuff as erroneous!
                 */
                log.warn("best==currentEntry, prefix=" + prefix + ", s=" + prefixInfo.getNeighborsMap().size() + ", r=" + currentRoute);
            }
            callback.prefixUnregistered(originator, prefix, currentRoute, best.getRoute());
            replaceRoute(prefixInfo, best, false);
            outputBuffer.add(new OutputAddEntity(prefixInfo, best.getRoute(), currentRoute));
            output = true;
        } else if (currentRoute != null) {
            callback.prefixUnregistered(originator, prefix, currentRoute, null);
            prefixInfo.setCurrentEntry(null);
            outputBuffer.add(new OutputRemoveEntity(prefixInfo, currentRoute));
            output = true;
        }
        return output;

    }

    private Neighbor getNeighbor(PrefixTableEntry entry) {
        return entry == null ? null : neighbors.getNeighbor(entry.getOriginator());
    }

    void prefixReceived(ASIdentifier originator, Prefix prefix, Route route) {
        callback.prefixReceived(originator, prefix, route);
        prefixesReceived++;
                
        PrefixInfo prefixInfo = cache.getPrefixInfo(prefix);
        // prefixInfo.setLastSeen(timeController.getCurrentTime());
        Map<ASIdentifier, PrefixTableEntry> neighborsForPrefix = prefixInfo.getNeighborsMap();
 
        PrefixTableEntry neighborEntry = neighborsForPrefix.get(originator);
        boolean isValid = false;
        if (neighborEntry == null) {
            neighborEntry = getNewNeighborEntry(originator, route);
            neighborsForPrefix.put(originator, neighborEntry);
        } else {
            isValid = neighborEntry.isValid();
        }

        boolean containsMe = route.containsMe(asIdentifier);

        FlapTimer flapTimer = neighborEntry.getFlapTimer();
        
        if (!containsMe) {
            if (isValid) {
                flapTimer.reannounce();
            } else {
                flapTimer.announce();
            }

            registerFlapIfNeeded(originator, prefix, flapTimer);
        }
                
        if (prefixInfo.getCurrentEntry() == neighborEntry) {

            // this is our current route, it might not be the best one anymore
            // now...
            Route currentRoute = neighborEntry.getRoute();
            if (EL.checkWarnings && currentRoute.equals(route)) {
                log.warn("already seen this one!, p=" + prefix + ", r=" + route + ", sender=" + originator);
            }

            if (containsMe) {
                neighborEntry.invalidate(true);
            } else {
                neighborEntry.setRoute(route);
            }

            runDecision(originator, prefixInfo, currentRoute);

        } else {
            PrefixTableEntry currentEntry = prefixInfo.getCurrentEntry();

            // PrefixTableEntry newEntry = tryToAddNewRoute(originator,
            // prefixInfo, route);
            if (containsMe) {
                neighborEntry.invalidate(true);
            } else {
                neighborEntry.setRoute(route);
            }
            
            // don't use newEntry if it is flapped
            if (neighborEntry.isValid()
                    && !flapTimer.isFlapped()
                    && policy.isBetter(asIdentifier, prefix, currentEntry == null ? null : currentEntry.getRoute(), getNeighbor(currentEntry), neighborEntry
                            .getRoute(), getNeighbor(neighborEntry))) {
                replaceRoute(prefixInfo, neighborEntry, true);
                callback.prefixRegistered(originator, prefix, currentEntry == null ? null : currentEntry.getRoute(), route);
            } else {

            }
        }
    }

    private void registerFlapIfNeeded(ASIdentifier originator, Prefix prefix, FlapTimer flapTimer) {
        if (flapTimer.isFlapped()) {
            flapStore.register(prefix, originator, flapTimer);
        }
    }

    private PrefixTableEntry getNewNeighborEntry(ASIdentifier originator, Route route) {
        PrefixTableEntry neighborEntry;
        neighborEntry = new PrefixTableEntry();
        neighborEntry.setOrignator(originator);
        neighborEntry.setRoute(route);
        try {
            neighborEntry.setFlapTimer(flapTimerFactory.getFlapTimer());
        } catch (Exception e) {
            log.error(e);
            throw new BGPSymException(e);
        }
        return neighborEntry;
    }

    public void storePrefixesPermanent() throws IOException {
        cache.storePrefixesPermanent();
    }

    public void setOutputBuffer(OutputBuffer outputBuffer) {
        this.outputBuffer = outputBuffer;
    }

    public void setAsIdentifier(ASIdentifier myId) {
        this.asIdentifier = myId;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setCache(PrefixCache cache) {
        this.cache = cache;
    }

    public void prefixReceived(ASIdentifier asIdentifier, Collection<Prefix> prefixes, Route route) {

        assert prefixes != null && prefixes.size() > 0;

        long start = System.currentTimeMillis();
        long longest = 0;
        for (Prefix prefix : prefixes) {
            long sx = System.currentTimeMillis();
            prefixReceived(asIdentifier, prefix, route);
            long sx2 = System.currentTimeMillis();
            if (sx2 - sx > longest) {
                longest = sx2 - sx;
            }
        }
        long s2 = System.currentTimeMillis();
        outputBuffer.flush();
        long s3 = System.currentTimeMillis();
        if (s3 - start > 300) {
            if (log.isInfoEnabled()) {
                //log.info(s2 - start + ", " + (s3 - s2) + ", count=" + prefixes.size() + ", longest=" + longest);
            }
        }

    }

    public void flush(ASIdentifier asId) {
        outputBuffer.flush(asId);
    }

    public void unflap(Prefix prefix, ASIdentifier neighborsAsId) {

        if (EL.flapLogging && log.isInfoEnabled()) {
            log.info("unflapping " + prefix + " for " + neighborsAsId);
        }
        PrefixInfo prefixInfo = cache.getPrefixInfo(prefix);
        PrefixTableEntry entry = prefixInfo.getNeighborsMap().get(neighborsAsId);
        if (entry == null || !entry.getFlapTimer().isFlapped()) {
            log.warn(prefix + " is not flapped=" + entry);
            return;
        }
        entry.getFlapTimer().unflap(prefix);
        PrefixTableEntry currentEntry = prefixInfo.getCurrentEntry();
        Route currentRoute = currentEntry == null ? null : currentEntry.getRoute();
        if (entry.isValid() && policy.isBetter(asIdentifier, prefix, currentRoute, getNeighbor(currentEntry), entry.getRoute(), getNeighbor(entry))) {
            if (runDecision(neighborsAsId, prefixInfo, currentRoute)) {
                outputBuffer.flush();
                if (EL.flapINFO && prefix.getNum() < properties.bogusPrefixMin && log.isInfoEnabled()) {
                    String type = entry.getFlapTimer() instanceof FlapTimerImpl ? ((FlapTimerImpl) entry.getFlapTimer()).getTimerType().toString() : "unknown";
                    log.info("really unflapped " + prefix + " for " + neighborsAsId + ", type=" + type);
                }
            }
        }
        // callback.arbitrary("ARB2 " + prefix + " c=" + currentEntry + " e.r="
        // + entry.getRoute() + ", isBetter=" + isBetter + ", dec=" +
        // decisionOutput);
    }

    public void setFlapStore(FlapStore flapStore) {
        this.flapStore = flapStore;
    }

    public PrefixCache getCache() {
        return cache;
    }

    public void setNeighbors(Neighbors neighbors) {
        this.neighbors = neighbors;
    }

    public OutputBuffer getOutputBuffer() {
        return outputBuffer;
    }

    public PrefixStoreType getType() {
        return PrefixStoreType.MAP;
    }

    public FlapTimerFactory getFlapTimerFactory() {
        return flapTimerFactory;
    }

    public void setFlapTimerFactory(FlapTimerFactory flapTimerFactory) {
        this.flapTimerFactory = flapTimerFactory;
    }

    public Neighbors getNeighbors() {
        return neighbors;
    }

    public void setTimeController(TimeController timeController) {
		this.timeController = timeController;
	}

	public TimeController getTimeController() {
		return timeController;
	}

}

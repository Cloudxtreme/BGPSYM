package nl.nlnetlabs.bgpsym01.coordinator.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.StaticThread;
import nl.nlnetlabs.bgpsym01.xstream.XPrefix;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

/**
 * Takes responsibility over prefix propagation.
 * 
 * Either propagates one prefix after another or loads information about
 * prefixes from a file.
 */
public class PropagationHelperImpl implements PropagationHelper {

    /*
     *  how many prefix do we require in one message to double the sleeping time
     */
    private int prefixAggreagationSleeperMultiplier = 6;

    private static Logger log = Logger.getLogger(PropagationHelperImpl.class);

    private volatile boolean finished;

    // private Coordinator coordinator;

    private CommandSenderHelper commandSenderHelper;

    private XProperties properties = XProperties.getInstance();

    public int prefixAggregationSize = 1;

    private int overload = 0;

    private Object overloadMonitor = new Object();

    private Collection<XPrefix> prefixes;

    private long sleepingTime = properties.sleepingTime;

    private double getSleepingTimeMultiplier(List<Prefix> prefixList) {
        return (1 + ((double) (prefixList.size() - 1) / (double) prefixAggreagationSleeperMultiplier));
    }

    /**
     * Waits as long as the system is overloaded.
     */
    private void waitIfNeeded() {
        long start = System.currentTimeMillis();
        synchronized (overloadMonitor) {
            while (overload != 0) {
                try {
                    if (finished) {
                        break;
                    }
                    if (log.isInfoEnabled()) {
                        log.info("stop, ov=" + overload);
                    }
                    overloadMonitor.wait();
                    if (overload == 0 && log.isInfoEnabled()) {
                        log.info("start, time=" + (System.currentTimeMillis() - start));
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Generate prefixes map - we can send prefixes from one neighbor together
     * 
     * @return map of prefixes {@link ASIdentifier} -> {@link ArrayList}
     */
    private Map<ASIdentifier, ArrayList<XPrefix>> generatePrefixMap() {
        int count = properties.prefixStartingPoint;
        // tests make use of the fact that this is LinkedHashMap - change with
        // care
        Map<ASIdentifier, ArrayList<XPrefix>> prefixesMap = new LinkedHashMap<ASIdentifier, ArrayList<XPrefix>>();

        // skip first X prefixes
        Iterator<XPrefix> iterator = prefixes.iterator();
        for (int i = 0; i < properties.prefixStartingPoint; i++) {
            iterator.next();
        }

        while (iterator.hasNext() && count < properties.prefixCount) {
            XPrefix prefix = iterator.next();
            if (prefix.getPrefixNum() > properties.prefixCount) {
                log.warn("prefixes are not numbered correctly in the file");
                break;
            }
            ASIdentifier asId = ASFactory.getInstance(prefix.getAsInternalId());
            ArrayList<XPrefix> list = prefixesMap.get(asId);
            if (list == null) {
                list = new ArrayList<XPrefix>();
                prefixesMap.put(asId, list);
            }
            list.add(prefix);
            count++;
        }
        return prefixesMap;
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.coordinator.helpers.PropagationHelper#propagatePrefixes()
     */
    public void propagatePrefixes() {
        XProperties properties = XProperties.getInstance();
        
        log.info("Propagate prefixes");
        
        if (prefixes == null) {
            propagateEasy(properties);
        } else {
            propagateFromList(properties);
        }
    }

    private void propagateFromList(XProperties properties) {
        // just propagate one after another

        log.info("Propagate prefixes from List");
        
        Map<ASIdentifier, ArrayList<XPrefix>> prefixesMap = generatePrefixMap();

        int sent = 0;
        int count = 0;
        int all = 0;
        List<Prefix> prefixList = new ArrayList<Prefix>(prefixAggregationSize);

        for (Map.Entry<ASIdentifier, ArrayList<XPrefix>> entry : prefixesMap.entrySet()) {
            waitIfNeeded();
            if (finished) {
                break;
            }
            Iterator<XPrefix> iterator = entry.getValue().iterator();
            XPrefix last = null;
            while (iterator.hasNext()) {
                last = iterator.next();
                prefixList.add(Prefix.getInstance(last.getPrefixNum()));
                log.info("Added prefix " + last.getPrefixNum());

                /* send it if the queue is already full or we have nothing more to send
                 * clear the queue
                 */
                if (prefixList.size() == prefixAggregationSize || !iterator.hasNext()) {
                    commandSenderHelper.sendUpdate(prefixList, null, entry.getKey());
                    sent += prefixList.size();
                    all += prefixList.size();
                    long sleepTime = (long) (sleepingTime * getSleepingTimeMultiplier(prefixList));
                    // log.info("sleep time: " + sleepTime + ", sent=" +
                    // prefixList.size() + ", as=" + entry.getKey());
                    prefixList.clear();
                    StaticThread.sleep(sleepTime);
                    waitIfNeeded();
                }
                if ((sent * 10 / 8) >= XProperties.getInstance().prefixCacheSize) {
                    // commandSenderHelper.waitForEmptyQueues();
                    // commandSenderHelper.sendSyncCommand();
                    sent = 0;
                }
            }

            // if (count % 500 == 0) {
            if (count % properties.getIntroducedLoggingInterval() == 0) {
                if (log.isInfoEnabled()) {
                    log.info("I " + (count + 1) + ", (a=" + all + "), L: " + last);
                }
            }
            count++;
        }
    }

    private void propagateEasy(XProperties properties) {
        int sent = 0;
        int startingPoint = properties.prefixStartingPoint;
        for (int i = startingPoint; i < properties.prefixCount; i++) {
            waitIfNeeded();
            if (finished) {
                break;
            }
            commandSenderHelper.sendUpdate(i);
            if (i % properties.getIntroducedLoggingInterval() == 0) {
                if (log.isInfoEnabled()) {
                    log.info("introduced " + (i + 1) + " messages");
                }
            }
            sent++;
            if (sent >= XProperties.getInstance().prefixCacheSize) {
                // commandSenderHelper.sendSyncCommand();
                sent = 0;
            }
            StaticThread.sleep(properties.sleepingTime);
        }
        long sleepTime = 100000;
        log.info("going to a big sleep... (" + sleepTime + ") ms");
        StaticThread.sleep(sleepTime);
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.coordinator.helpers.PropagationHelper#end()
     */
    public void end() {
        synchronized (overloadMonitor) {
            finished = true;
            overloadMonitor.notify();
        }
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.coordinator.helpers.PropagationHelper#changeLoad(int)
     */
    public void changeLoad(int value) {
        synchronized (overloadMonitor) {
            overload += value;
            // TODO for some reason we can't call it only if it's 0... why?
            overloadMonitor.notify();

            sleepingTime = (properties.sleepingTime * (overload * 3 + 1));
            log.info("overload=" + overload);

        }
    }

    public void setPrefixAggreagationSleeperMultiplier(int prefixAggreagationSleeperMultiplier) {
        this.prefixAggreagationSleeperMultiplier = prefixAggreagationSleeperMultiplier;
    }

    public void setPrefixes(Collection<XPrefix> prefixes) {
        this.prefixes = prefixes;
    }

    public void setPrefixAggregationSize(int prefixAggregationSize) {
        this.prefixAggregationSize = prefixAggregationSize;
    }

    public CommandSenderHelper getCommandSenderHelper() {
        return commandSenderHelper;
    }

    public void setCommandSenderHelper(CommandSenderHelper commandSenderHelper) {
        this.commandSenderHelper = commandSenderHelper;
    }

}

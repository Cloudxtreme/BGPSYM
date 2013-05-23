/*package nl.nlnetlabs.bgpsym01.primitives.types;

import nl.nlnetlabs.bgpsym01.main.EL;
import nl.nlnetlabs.bgpsym01.main.tcp.OverloadMonitor;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update.UpdateType;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapStore;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeController;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.route.MRAIStore;
import nl.nlnetlabs.bgpsym01.route.MRAITimer;

import org.apache.log4j.Logger;

*//**
 * MessageQueue taking care of {@link Update}'s to process as well as
 * looking at the timers.
 * 
 * If there is an {@link MRAITimer} ready for execution it will be executed
 * before any update in the queue.
 */
/*
public class MessageQueueOld {

    public MessageQueueOld() {

    }

    private boolean isLogOn;

    // set this!
    private TimeController timeController;

    // this definitely shuld not be here - this class should be split into many
    // smaller ones --- don't have time for it right now :(
    private IBGPModel ibgpModel;

    private Update immediateUpdate;

    private FlushUpdate flushUpdate = new FlushUpdate();

    private UnflapUpdate unflapUpdate = new UnflapUpdate();

    private FIFOQueue<BGPUpdate> updates = new FIFOQueue<BGPUpdate>();

    private Logger log = Logger.getLogger(MessageQueueOld.class);

    private OverloadMonitor monitor;

    private boolean shutdown = false;

    private boolean overloaded;

    private MRAIStore mraiStore;

    private FlapStore flapStore;

    public void addMessage(Update update) {
        synchronized (this) {
            if (shutdown) {
                notifyAll();
                return;
            }

            
             * TODO: if this is BGPUpdate set the timer
             * 
             * if it's put it as immediate update...
             * 
             
            if (update.getType() != UpdateType.BGPUPDATE) {
                if (immediateUpdate != null) {
                    String msg = "immediateUpdate != null: " + immediateUpdate + " ,new=" + update;
                    log.error(msg);
                    throw new BGPSymException(msg);
                }
                immediateUpdate = update;
            } else {
                BGPUpdate bgpUpdate = (BGPUpdate) update;
                bgpUpdate.setReadyTime(timeController.getCurrentTime() + ibgpModel.getConvergenceTime());
                int size = updates.add(bgpUpdate);
                if (!overloaded && size > monitor.getQueueBigValue()) {
                    log.warn("queue overload, update=" + update);
                    monitor.overload(size);
                    overloaded = true;
                }
            }
            notifyAll();
        }
        Thread.yield();
    }

    *//**
 * @return how long do we want to wait, -1 if we are ready and 0 if we
 *         want to wait forever
 */
/*
    long getStoreWaitingTime() {
        // check MRAIwaitingTime
        boolean wait = false;
        long mraiReadyTime = Long.MAX_VALUE;
        long flapReadyTime = Long.MAX_VALUE;
        if (mraiStore.hasSomething()) {
            mraiReadyTime = mraiStore.getReadyTime();
            if (log.isDebugEnabled()) {
                log.debug("mrai readyTime: " + mraiReadyTime);
            }
            wait = true;
        }
        if (flapStore.hasSomething()) {
            flapReadyTime = flapStore.getReadyTime();

            if (log.isDebugEnabled()) {
                log.debug("flap waitingTime: " + flapReadyTime);
            }
            wait = true;
        }
        if (wait) {
            long waitTime = (mraiReadyTime <= flapReadyTime) ? mraiReadyTime : flapReadyTime;
            if (waitTime <= 0) {
                return -1;
            }
            return waitTime;
        } else {
            return 0;
        }

    }

    boolean hasSomething() {
        throw new NotImplementedException();
    }

    long getWaitingTime() {
        if (immediateUpdate != null) {
            return -1;
        }
        long question = getStoreWaitingTime();
        if (question == -1) {
            return -1;
        } else {
            long updatesWaitingTime = 0;
            long storeTime = 0;
            if (updates.size() > 0) {
                updatesWaitingTime = timeController.realWaitingTime(updates.peek().getReadyTime());
            }
            if (question > 0) {
                storeTime = timeController.realWaitingTime(question);
                if (log.isDebugEnabled()) {
                    log.debug("question: " + question + ", waiting: " + storeTime + ", time=" + timeController.getCurrentTime());
                }
                if (EL.flapLogging && log.isInfoEnabled()) {
                    log.info("waitingTime=" + storeTime + ", question=" + question);
                }
            }
            if (updatesWaitingTime < 0 || storeTime < 0) {
                return -1;
            }
            if (updatesWaitingTime == 0) {
                return storeTime;
            } else if (storeTime == 0) {
                return updatesWaitingTime;
            } else {
                return Math.min(updatesWaitingTime, storeTime);
            }
//            return waitingTime;
        }
    }

    public synchronized Update getMessage() {
        TimeController timeController = TimeControllerFactory.getTimeController();
        boolean mrai = false;
        boolean flap = false;
        while ((!shutdown) && updates.size() == 0 && immediateUpdate == null) {
            try {
                // if there is immediateUpdate we are more than happy to let it
                // go :)
                if (immediateUpdate == null) {
                     
                     * first check how long we have to sleep - this depends on the updates
                     

                    long waitingTime = getWaitingTime();
                    if (waitingTime > 0) {
                        wait(waitingTime);
                    } else if (waitingTime == 0) {
                        wait();
                    }

                    // TODO - empty cycles
                    mrai = mraiStore.getReadyTime() != -1 && timeController.realWaitingTime(mraiStore.getReadyTime()) < 0;
                    flap = flapStore.getReadyTime() != -1 && timeController.realWaitingTime(flapStore.getReadyTime()) < 0;

                    // String msg = "size: " + updates.size() + ", mrai=" + mrai
                    // +
                    // ", flap=" + flap;
                    // if ((log.isInfoEnabled() && isLogOn) || flap) {
                    // log.info(msg);
                    // }
                }

                if (immediateUpdate != null || updates.size() > 0 || mrai || flap) {
                    break;
                }

            } catch (InterruptedException e) {
                log.warn("interrupted", e);
            }
        }
        if (shutdown) {
            notifyAll();
            return null;
        }

        if (immediateUpdate != null) {
            Update ret = immediateUpdate;
            immediateUpdate = null;
            return ret;
        }
        if (mrai) {
            flushUpdate.setAsId(mraiStore.next());
            return flushUpdate;
        }
        if (flap) {
            Pair<ASIdentifier, Prefix> pair = flapStore.next();
            unflapUpdate.setPair(pair);
            return unflapUpdate;
        }
        if (updates.size() > 0) {
            Update myUpdate = updates.remove();
            int size = updates.size();
            if (overloaded && size < monitor.getQueueLowValue()) {
                monitor.backOK();
                overloaded = false;
            }
            notifyAll();
            return myUpdate;
        }
        throw new NotImplementedException();
    }

    public synchronized void waitForEmpty() {
        while (size() != 0) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized void shutdown() {
        shutdown = true;
        notifyAll();
    }

    public int size() {
        return updates.size() + (immediateUpdate == null ? 0 : 1);
    }

    public OverloadMonitor getMonitor() {
        return monitor;
    }

    public void setMonitor(OverloadMonitor monitor) {
        this.monitor = monitor;
    }

    public MRAIStore getMraiStore() {
        return mraiStore;
    }

    public void setMraiStore(MRAIStore mraiStore) {
        this.mraiStore = mraiStore;
    }

    public FlapStore getFlapStore() {
        return flapStore;
    }

    public void setFlapStore(FlapStore flapStore) {
        this.flapStore = flapStore;
    }

    public boolean isLogOn() {
        return isLogOn;
    }

    public void setLogOn(boolean isLogOn) {
        this.isLogOn = isLogOn;
    }

    public IBGPModel getIbgpModel() {
        return ibgpModel;
    }

    public void setIbgpModel(IBGPModel ibgpModel) {
        this.ibgpModel = ibgpModel;
    }

    public TimeController getTimeController() {
        return timeController;
    }

    public void setTimeController(TimeController timeController) {
        this.timeController = timeController;
    }

}
*/
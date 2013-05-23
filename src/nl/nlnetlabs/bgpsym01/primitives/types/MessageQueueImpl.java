package nl.nlnetlabs.bgpsym01.primitives.types;

import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update.UpdateType;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeController;

import org.apache.log4j.Logger;

public class MessageQueueImpl implements MessageQueue {

    private static final int MAX_SIMULATION_DISCREPANCY = 3000;

    private static Logger log = Logger.getLogger(MessageQueueImpl.class);

    private int overloaded;

    // ArrayList<InputGenerator> list = new ArrayList<InputGenerator>();
    InputGenerator[] list = new InputGenerator[0];

    private TimeController timeController;

    private boolean shutdown;

    long readyTime = 0;

    private long maxDiscrepancy;

    int size() {
        return list.length;
    }

    boolean hasSomething() {
        boolean hasSomething = false;
        boolean found = false;

        for (InputGenerator input : list) {
            if (input.hasSomething()) {
                hasSomething = true;
                long tmp = input.getReadyTime();
                if (!found || tmp < readyTime) {
                    readyTime = tmp;
                    found = true;
                }
            }
        }
        return hasSomething;
    }

    public long getWaitingTime() {
        return readyTime == -1 ? -1 : readyTime;
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.primitives.types.MessageQueue#getNext()
     */
    public synchronized Update getNext() {
        if (shutdown) {
            return null;
        }

        while (!shutdown && !hasSomething()) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        long waitingTime = -1;
        while (!shutdown && (waitingTime = timeController.realWaitingTime(getWaitingTime(), false)) > 0) {
            try {
                wait(waitingTime);
                // recompute...
                hasSomething();
            } catch (InterruptedException e) {
                if (!shutdown) {
                    log.warn("interrupted!!!");
                }
            }
        }

        if (shutdown) {
            return null;
        }

        /*
         * TODO: make it live!
         */
        Update outUpdate = null;
        boolean hasSomething = false;
        for (InputGenerator input : list) {
            if (input.hasSomething() && timeController.realWaitingTime(input.getReadyTime(), false) == -1) {
                outUpdate = input.getUpdate();
                hasSomething = input.hasSomething();
                break;
            }
        }

        if (outUpdate == null) {
            throw new BGPSymException("no update to give..., w=" + waitingTime);
        }
        if (outUpdate.getType() == UpdateType.BGPUPDATE) {
            adjustPriority((BGPUpdate) outUpdate, hasSomething);
        }
        return outUpdate;
    }

    private void adjustPriority(BGPUpdate update, boolean hasSomething) {
        long time = update.getReadyTime();
        long diff = System.currentTimeMillis() - time;
        if (diff > maxDiscrepancy) {
            if (overloaded == 0) {
                log.debug("real world diff=" + diff + ", time=" + time + ", now=" + System.currentTimeMillis());
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                overloaded++;
                if (log.isDebugEnabled()) {
                    log.debug("overloading...");
                }
            } else {
                overloaded++;
            }
        } else if (overloaded > 0 && (!hasSomething || diff < maxDiscrepancy / 64)) {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
            if (log.isDebugEnabled()) {
                log.debug("UNoverloading..., count=" + overloaded);
            }
            overloaded = 0;
        }
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.primitives.types.MessageQueue#shutdown()
     */
    public synchronized void shutdown() {
        shutdown = true;
        notifyAll();
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.primitives.types.MessageQueue#addInputGenerator(nl.nlnetlabs.bgpsym01.primitives.types.InputGenerator)
     */
    public void addInputGenerator(InputGenerator inputGenerator) {
        // list.add(inputGenerator);

        InputGenerator[] newList = new InputGenerator[list.length + 1];
        for (int i = 0; i < list.length; i++) {
            newList[i] = list[i];
        }
        newList[newList.length - 1] = inputGenerator;
        list = newList;
    }

    public TimeController getTimeController() {
        return timeController;
    }

    public void setTimeController(TimeController timeController) {
        this.timeController = timeController;
        maxDiscrepancy = timeController.getRealWorldDiscrepancy(MAX_SIMULATION_DISCREPANCY);
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.primitives.types.MessageQueue#ping()
     */
    public synchronized void ping() {
        notifyAll();
    }

}

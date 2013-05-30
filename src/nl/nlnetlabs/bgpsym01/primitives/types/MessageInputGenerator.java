package nl.nlnetlabs.bgpsym01.primitives.types;

import nl.nlnetlabs.bgpsym01.main.EL;
import nl.nlnetlabs.bgpsym01.main.tcp.OverloadMonitor;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update.UpdateType;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeController;

import org.apache.log4j.Logger;

public class MessageInputGenerator implements InputGenerator {

    private static Logger log = Logger.getLogger(MessageInputGenerator.class);

    private FIFOQueue<BGPUpdate> updates = new FIFOQueueImpl<BGPUpdate>(2048);

    private MessageQueue messageQueue;

    private OverloadMonitor monitor;

    private ASIdentifier asId;

    private boolean overloaded;

    private IBGPModel iBGPmodel;

    private TimeController timeController;

    private FIFOQueue<Update> immediateUpdates = new FIFOQueueImpl<Update>();

    @SuppressWarnings("unused")
	public void addMessage(Update update) {
        synchronized (this) {
            if (update.getType() == UpdateType.BGPUPDATE) {
                addBGPUpdate((BGPUpdate) update);
            } else {
                immediateUpdates.add(update);
            }
        }
        if (EL.queueDebug && log.isInfoEnabled()) {
            log.info("X7, ping...");
        }
        messageQueue.ping();
    }

    public int size() {
        return updates.size();
    }

    private void addBGPUpdate(BGPUpdate update) {
        long readyTime = timeController.getRealMS(timeController.getCurrentTime() + getiBGPConvergenceTime());
        update.setReadyTime(readyTime);
        updates.add(update);
        if (!overloaded && updates.size() > monitor.getQueueBigValue() && updates.peek().getReadyTime() == -1) {
            log.warn("overload, size=" + updates.size() + " / " + immediateUpdates.size() + ", " + asId + ", r="
                    + timeController.realWaitingTime(getReadyTime(), false));
            // monitor.overload(updates.size());
            overloaded = true;
        } else if (overloaded && updates.size() < monitor.getQueueLowValue()) {
            log.warn("backOK, " + asId);
            // monitor.backOK();
            overloaded = false;
        } else if (overloaded && updates.size() % (monitor.getQueueBigValue() * 4) == 0) {
            log.warn("overload2, size=" + updates.size() + " for " + asId + ", r=" + timeController.realWaitingTime(getReadyTime(), false));
        }
    }

    private long getiBGPConvergenceTime() {
        return iBGPmodel.getConvergenceTime();
    }

    public synchronized long getReadyTime() {
        long readyTime = immediateUpdates.size() > 0 ? -1 : updates.peek().getReadyTime();
        return readyTime;
    }

    public synchronized Update getUpdate() {
        if (immediateUpdates.size() > 0) {
            return immediateUpdates.remove();
        }
        return updates.remove();
    }

    public synchronized boolean hasSomething() {
        return updates.size() + immediateUpdates.size() > 0;
    }

    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    public long getIBGPConvergenceTime() {
        return getiBGPConvergenceTime();
    }

    public TimeController getTimeController() {
        return timeController;
    }

    public void setTimeController(TimeController timeController) {
        this.timeController = timeController;
    }

    public OverloadMonitor getMonitor() {
        return monitor;
    }

    public void setMonitor(OverloadMonitor monitor) {
        this.monitor = monitor;
    }

    public ASIdentifier getAsId() {
        return asId;
    }

    public void setAsId(ASIdentifier asId) {
        this.asId = asId;
    }

    public IBGPModel getIBGPmodel() {
        return iBGPmodel;
    }

    public void setIBGPmodel(IBGPModel pmodel) {
        iBGPmodel = pmodel;
    }

}

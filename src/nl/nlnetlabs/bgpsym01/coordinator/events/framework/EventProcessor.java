package nl.nlnetlabs.bgpsym01.coordinator.events.framework;

import nl.nlnetlabs.bgpsym01.coordinator.events.Event;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.ShutdownadbleThread;
import nl.nlnetlabs.bgpsym01.primitives.types.StaticThread;

import org.apache.log4j.Logger;

/**
 * TODO: - teach him how to get along with objects that are not capable of being
 * executed at the start - do some king of sorting, right now it truly suXXs
 */
public class EventProcessor extends ShutdownadbleThread {

    private static Logger log = Logger.getLogger(EventProcessor.class);

    private static final String EVENT_PROCESSOR_THREAD_NAME = "eventProcessor";

    private boolean shutdown;

    private DataMeasurement dataMeasurement;

    private EventStream eventStream;

    private int eventCounter;

    private boolean isFinished;

    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public void run() {
        setName(EVENT_PROCESSOR_THREAD_NAME);
        BIG: while (eventStream.hasNext() && shutdown == false) {
            if (shutdown) {
                return;
            }

            long waitingTime = eventStream.getWaitingTime();
			log.info("waitingTime: "+waitingTime);
            if (waitingTime > 0) {
                // eventStream.wait(waitingTime);
                StaticThread.sleep(waitingTime);
            }
            if (shutdown) {
                break BIG;
            }
            if (!eventStream.isReady()) {
                continue;
            }
            Event event = eventStream.next();

            if (event.isShowOnScreen()) {
                log.info("@" + TimeControllerFactory.getTimeController().getCurrentTime() + ";#" + eventCounter + ";" + event.toString());
            }
            dataMeasurement.eventSent(event);
            event.process();
            eventCounter++;
        }
        isFinished = true;
        eventStream.shutdown();
    }

    @Override
    public void shutdown() {
        synchronized (eventStream) {
            shutdown = true;
            eventStream.notifyAll();
            this.interrupt();
        }
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    public EventStream getEventStream() {
        return eventStream;
    }

    public void setEventStream(EventStream eventStream) {
        this.eventStream = eventStream;
    }

    public DataMeasurement getDataMeasurement() {
        return dataMeasurement;
    }

    public void setDataMeasurement(DataMeasurement dataMeasurement) {
        this.dataMeasurement = dataMeasurement;
    }

}

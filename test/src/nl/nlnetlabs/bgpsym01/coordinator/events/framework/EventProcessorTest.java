package nl.nlnetlabs.bgpsym01.coordinator.events.framework;

import java.util.ArrayList;
import java.util.Iterator;

import nl.nlnetlabs.bgpsym01.coordinator.events.Event;
import nl.nlnetlabs.bgpsym01.coordinator.events.EventAdaptor;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.types.StaticThread;

public class EventProcessorTest extends AbstractTest {

    private final class EventStreamMock implements EventStream {
        Iterator<Event> iterator;

        public EventStreamMock(Iterator<Event> iterator) {
            super();
            this.iterator = iterator;
        }

        public long getWaitingTime() {
            return 0;
        }

        public boolean isFinished() {
            return !iterator.hasNext();
        }

        public boolean isReady() {
            return true;
        }

        public void shutdown() {
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Event next() {
            return iterator.next();
        }

        public void remove() {
            iterator.remove();
        }
    }

    public void testEventGetMeasured() {
        DataMeasurementMock dm = new DataMeasurementMock();

        EventProcessor ep = new EventProcessor();
        ep.setDataMeasurement(dm);
        ArrayList<Event> events = new ArrayList<Event>();
        int count = 10;
        for (int i = 0; i < count; i++) {
            events.add(new EventAdaptor());
        }

        assertEquals(0, dm.sent);

        ep.setEventStream(new EventStreamMock(events.iterator()));
        ep.start();

        int maxAlive = 10;
        while (ep.isAlive() && maxAlive-- > 0) {
            StaticThread.sleep(10);
        }
        assertTrue("maxAlive=" + maxAlive, ep.isFinished());

        assertEquals(count, dm.sent);
    }

}

package nl.nlnetlabs.bgpsym01.coordinator.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventBackend;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventStreamImpl;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.mock.TimeControllerMock;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeController;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;

import org.apache.log4j.Logger;

/**
 * Tests whether {@link EventStreamImpl} works fine with {@link TimeController}
 */
public class EventStreamImplTest extends AbstractTest {

    private static Logger log = Logger.getLogger(EventStreamImplTest.class);
    private TimeControllerMock timeControllerMock;

    @Override
    protected void tearDown() throws Exception {
        TimeControllerFactory.reload();
    }

    @Override
    protected void setUp() throws Exception {
        timeControllerMock = new TimeControllerMock();
        TimeControllerFactory.setTimeController(timeControllerMock);
    }

    public void testSchedule() throws InterruptedException {
        int size = 5;
        // how often they want to go
        final long sleepTime = 40;
        Collection<Event> events = generateEvents(size, sleepTime);

        timeControllerMock.currentTime = 1;

        EventBackend backend = getBackend(events);
        // test with calling esi.hasNext()
        final Collection<Event> collection2 = new ArrayList<Event>();
        EventStreamImpl esi = new EventStreamImpl();
        esi.setBackend(backend);

        for (int i = 0; i < size; i++) {
            log.info("i=" + i);
            assertTrue("ready before time", esi.hasNext());
            assertFalse(esi.isReady());
            timeControllerMock.oneToOne = false;
            timeControllerMock.answer = 1029;
            assertEquals(1029, esi.getWaitingTime());
            timeControllerMock.oneToOne = true;

            timeControllerMock.currentTime += sleepTime;

            assertTrue("no element after timeout", esi.hasNext());
            assertTrue(esi.isReady());
            Event tmp = esi.next();
            assertNotNull(tmp);
            collection2.add(tmp);
        }

        assertEquals(events, collection2);
    }

    private Collection<Event> generateEvents(int size, final long sleepTime) {
        Collection<Event> events = new ArrayList<Event>();

        for (int i = 0; i < size; i++) {
            final int num = i;
            Event event = new EventAdaptor() {

                @Override
                public EventSchedule getEventSchedule() {
                    return new EventSchedule() {

                        @Override
                        public long getLaunchTime() {
                            return (num + 1) * sleepTime;
                        }

                        @Override
                        public boolean timeIsKnown() {
                            return true;
                        }

                    };
                }

            };

            events.add(event);
        }
        return events;
    }

    private EventBackend getBackend(Collection<Event> collection) {
        final Iterator<Event> iterator = collection.iterator();

        EventBackend backend = new EventBackend() {

            public Event getNext() {
                if (!iterator.hasNext()) {
                    return null;
                }
                return iterator.next();
            }
        };
        return backend;
    }

}

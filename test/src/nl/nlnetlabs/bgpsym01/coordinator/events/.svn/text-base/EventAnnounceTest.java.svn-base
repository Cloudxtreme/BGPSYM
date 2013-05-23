package nl.nlnetlabs.bgpsym01.coordinator.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import nl.nlnetlabs.bgpsym01.coordinator.events.framework.DataMeasurementMock;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventBackend;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventProcessor;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventStreamImpl;
import nl.nlnetlabs.bgpsym01.coordinator.helpers.CommandSenderHelperMock;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;
import nl.nlnetlabs.bgpsym01.primitives.types.StaticThread;

/**
 * This is not a unit test! <br>
 * <br>
 * Tested classes:<br> {@link EventStreamImpl}<br> {@link EventBackendXStreamTest}<br>
 * {@link EventAnnounce}<br> {@link EventProcessor}<br>
 */
public class EventAnnounceTest extends AbstractTest {

    private CommandSenderHelperMock commandSenderHelper;

    @Override
    protected void setUp() throws Exception {
        generateASes(1000);
        Prefix.init(1000);
    }

    /*
     * create announce events, put them into the processor (one every 20 milis) and check
     * whether CommandSenderHelper receives appropriate data
     */
    public void testAnnouncing() throws InterruptedException {
        int size = 10;
        long sleepTime = 20;

        // genereate announces
        Collection<Event> events = generateEvents(size, sleepTime);

        // set up the system

        commandSenderHelper = new CommandSenderHelperMock();

        EventBackend backend = getBackend(events);

        EventStreamImpl esi = new EventStreamImpl();
        esi.setCommandSenderHelper(commandSenderHelper);
        esi.setBackend(backend);

        EventProcessor ep = new EventProcessor();
        ep.setEventStream(esi);
        ep.setDataMeasurement(new DataMeasurementMock());
        ep.start();

        int counter = 30;
        while (!esi.isFinished() && counter-- > 0) {
            StaticThread.sleep(10);
        }
        ep.join(200);
        assertFalse("processor is not dead", ep.isAlive());

        // check whether appropriate prefix would have been propagated
        assertEquals(size, commandSenderHelper.getReceived().size() + commandSenderHelper.getWithdrawn().size());
        for (int i = 0; i < size; i++) {
            if (i % 2 == 0) {
                Pair<List<Prefix>, ASIdentifier> pair = commandSenderHelper.getReceived().get(i / 2);
                assertEquals(getList(i), pair.key);
                assertEquals(getASByNumber(i), pair.value);
            } else {
                Pair<List<Prefix>, ASIdentifier> pair = commandSenderHelper.getWithdrawn().get(i / 2);
                assertEquals(getList(i), pair.key);
                assertEquals(getASByNumber(i), pair.value);
            }
        }

    }

    public void testToString() {
        // if this test starts to fail, just throw it out!
        EventAnnounce ea = new EventAnnounce();
        ea.setAsId(getAS(2));
        ea.setEventSchedule(new EventSchedule() {

            @Override
            public String toString() {
                return "eventSch";
            }

            @Override
            public long getLaunchTime() throws IllegalStateException {
                throw new NotImplementedException();
            }

            @Override
            public boolean timeIsKnown() {
                throw new NotImplementedException();
            }

        });
        ea.setPrefixList(getPrefixList(1, 2, 3));

        assertEquals("ANN;eventSch;as=AS2|2|24; prefs=msg_1, msg_2, msg_3, ; with=NULL", ea.toString());
        ea.setWithdrawals(getPrefixList(6, 7, 8));
        assertEquals("ANN;eventSch;as=AS2|2|24; prefs=msg_1, msg_2, msg_3, ; with=msg_6, msg_7, msg_8, ", ea.toString());
        ea.setPrefixList(null);
        assertEquals("ANN;eventSch;as=AS2|2|24; prefs=NULL; with=msg_6, msg_7, msg_8, ", ea.toString());

    }

    private Collection<Event> generateEvents(int size, final long sleepTime) {
        Collection<Event> events = new ArrayList<Event>();

        for (int i = 0; i < size; i++) {
            final int num = i;
            EventAnnounce event = new EventAnnounce();
            if (i % 2 == 0) {
                event.setPrefixList(getList(num));
            } else {
                event.setWithdrawals(getList(num));
            }

            event.setAsId(getASByNumber(num));
            event.setEventSchedule(new EventSchedule() {

                @Override
                public long getLaunchTime() {
                    return (num + 1) * sleepTime;
                }

                @Override
                public boolean timeIsKnown() {
                    return true;
                }
            });

            events.add(event);
        }
        return events;
    }

    private ASIdentifier getASByNumber(int i) {
        return getAS(i * 3);
    }

    private List<Prefix> getList(int i) {
        return getPrefixList(i, i + 1, i * 2, i + 43);
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

    public void setSimulationStartTime(long simulationStartTime) {
    }
}

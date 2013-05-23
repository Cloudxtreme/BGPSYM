package nl.nlnetlabs.bgpsym01.coordinator.events.framework;

import java.util.List;

import nl.nlnetlabs.bgpsym01.coordinator.events.EventAnnounce;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.mock.TimeControllerMock;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;

public class DataMeasurementImplTest extends AbstractTest {

    private TimeControllerMock controller;

    @Override
    protected void tearDown() throws Exception {
        TimeControllerFactory.reload();
    }

    @Override
    protected void setUp() throws Exception {
        Prefix.init(10);
        controller = new TimeControllerMock();
        TimeControllerFactory.setTimeController(controller);
    }

    /**
     * Tests whether EventAnnounce keeps good times for prefixes
     */
    public void testAnnounces() {

        DataMeasurement dmi = new DataMeasurementImpl();

        send(dmi, 101, getPrefixList(1, 2, 3));
        assertEquals(101, dmi.getLastSeen(getPrefix(2)));
        assertEquals(101, dmi.getLastSeen(getPrefix(3)));

        send(dmi, 103, getPrefixList(4, 5, 1));
        assertEquals(101, dmi.getLastSeen(getPrefix(2)));
        assertEquals(103, dmi.getLastSeen(getPrefix(4)));
        assertEquals(103, dmi.getLastSeen(getPrefix(1)));

        assertEquals(-1, dmi.getLastSeen(getPrefix(9)));
    }

    private void send(DataMeasurement dmi, int time, List<Prefix> list) {
        EventAnnounce ea1 = new EventAnnounce();
        ea1.setPrefixList(list);
        ea1.setEventSchedule(new EventScheduleImpl(time));
        dmi.eventSent(ea1);
    }

}

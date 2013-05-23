package nl.nlnetlabs.bgpsym01.coordinator.events.framework;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.mock.TimeControllerMock;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeController;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;

public class EventScheduleImplTest extends AbstractTest {

    TimeControllerMock timeControllerMock;

    @Override
    protected void tearDown() throws Exception {
        TimeControllerFactory.reload();
    }

    @Override
    protected void setUp() throws Exception {
        timeControllerMock = new TimeControllerMock();
        TimeControllerFactory.setTimeController(timeControllerMock);
    }

    /**
     * Tests whether {@link EventScheduleImpl#getLaunchTime()} does not depend
     * on {@link TimeController}
     */
    public void testLaunchTime() {
        int expected = 10234;
        EventScheduleImpl esi = new EventScheduleImpl(expected);
        timeControllerMock.oneToOne = false;
        timeControllerMock.answer = 1023;
        assertEquals(expected, esi.getLaunchTime());
    }

}

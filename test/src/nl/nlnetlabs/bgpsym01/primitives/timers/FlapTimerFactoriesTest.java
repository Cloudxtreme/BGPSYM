package nl.nlnetlabs.bgpsym01.primitives.timers;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.mocks.FlapTimerAdapter;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimer.FlapTimerType;

public class FlapTimerFactoriesTest extends AbstractTest {

    private static final int COUNT = 1000;

    /**
     * Tests Factory Mock
     */
    public void testFactoryMock() {
        FlapTimerFactory factory = new FlapTimerFactoryMock();
        for (int i = 0; i < COUNT; i++) {
            assertTrue(factory.getFlapTimer() instanceof FlapTimerAdapter);
        }
    }

    public void testFactoryReal() {
        doTimerTypeTest(FlapTimerType.CISCO, new FlapTimerFactoryReal(true));
        doTimerTypeTest(FlapTimerType.JUNIPER, new FlapTimerFactoryReal(false));
    }

    private void doTimerTypeTest(FlapTimerType type, FlapTimerFactory factory1) {
        for (int i = 0; i < COUNT; i++) {
            FlapTimer timer1 = factory1.getFlapTimer();
            assertTrue(timer1 instanceof FlapTimerImpl);
            assertEquals(type, ((FlapTimerImpl) timer1).getTimerType());
        }
    }

}

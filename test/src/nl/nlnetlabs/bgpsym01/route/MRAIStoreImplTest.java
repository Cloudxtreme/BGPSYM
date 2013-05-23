package nl.nlnetlabs.bgpsym01.route;

import nl.nlnetlabs.bgpsym01.callback.CallbackMock;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.mock.TimeControllerMock;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;

/*
 * lacks:
 *  - if the timer passes during the run - something might not be run
 *  - we want to have it on per-neighbor basis
 *  - invalidation during full scan (if it is run)
 * 
 */

public class MRAIStoreImplTest extends AbstractTest {

    private TimeControllerMock timeControllerMock;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateASes(1000);
        timeControllerMock = new TimeControllerMock();
        TimeControllerFactory.setTimeController(timeControllerMock);
        timeControllerMock.currentTime = 0;
    }

    @Override
    protected void tearDown() throws Exception {
        TimeControllerFactory.reload();
    }

    /**
     * Tests whether ticking property is set properly
     */
    public void testTicking() {
        MRAIStoreImpl mraiStore = new MRAIStoreImpl();
        mraiStore.setCallback(CallbackMock.getInstance());
        MRAITimerImpl timer = getMRAITimer(1000, getAS(0));
        assertFalse(timer.isTicking());

        mraiStore.register(getAS(0), timer);
        assertTrue(timer.isTicking());
        mraiStore.register(getAS(0), timer);

        assertTrue(timer.isTicking());
        mraiStore.next();
        assertFalse(timer.isTicking());
        assertFalse(mraiStore.hasSomething());
    }

    // TODO: rewrite this test!
    /*    *//**
     * Tests whether buffer gets flushed when the timeout expires and
     * whether MRAITimers get order well
     */
    /*
        public void testAutoExpiry2() {
             flow is as follows:
     * 1. register 3 timers with different timers for 3 different ASes
     *      a. make sure that all of them get flushed in good order
     * 


            log.info("-------------------testExpiry2");
            final MessageQueue queue = new MessageQueue();
            queue.setFlapStore(new FlapStoreMock());
            MRAIStoreImpl mraiStore = new MRAIStoreImpl();
            mraiStore.setCallback(CallbackMock.getInstance());
            queue.setMraiStore(mraiStore);

            registerTimer(mraiStore, 10, getAS(2));
            registerTimer(mraiStore, 7, getAS(4));
            registerTimer(mraiStore, 12, getAS(3));

            // we don't wanna wait :)
            timeControllerMock.currentTime = 15;

            for (int i = 0; i < 3; i++) {
                if (log.isInfoEnabled()) {
                    log.info("dupa");
                }
                Update update = queue.getMessage();
                if (log.isDebugEnabled()) {
                    log.debug("dupa2");
                }
                // fail(((FlushUpdate) update).getAsId().toString());
                assertEquals("wrong update", Update.UpdateType.RUNNABLE_UPDATE, update.getType());
                assertTrue("wrong update", update instanceof FlushUpdate);
                assertEquals("wrong guy", getAS(i == 0 ? 4 : i == 1 ? 2 : 3), ((FlushUpdate) update).getAsId());
            }

        }

    private void registerTimer(MRAIStoreImpl mraiStore, int t, ASIdentifier as) {
        MRAITimerMock timer1 = new MRAITimerMock();
        timer1.asId = as;
        timer1.startTime = t;
        mraiStore.register(as, timer1);
    }
     */

    private MRAITimerImpl getMRAITimer(long time, ASIdentifier as2) {
        MRAITimerImpl mraiTimer1 = new MRAITimerImpl();
        mraiTimer1.setThreshold(time);
        mraiTimer1.setAsIdentifier(as2);
        mraiTimer1.sent();
        return mraiTimer1;
    }

}

package nl.nlnetlabs.bgpsym01.primitives.timers;

import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.cache.PrefixInfo;
import nl.nlnetlabs.bgpsym01.callback.CallbackMock;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.mock.TimeControllerMock;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.mocks.FlapTimerMock;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;

public class FlapStoreTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        Prefix.init(1000);
        generateASes(1000);
    }

    public void testTiming() {
        /*
         * 1. add FlapTimer and check the absolute time
         * 
         * was more complicated but some things got deleted...
         */

        FlapStoreImpl store = new FlapStoreImpl();
        store.setCallback(CallbackMock.getInstance());
        TimeControllerMock timeController = new TimeControllerMock();
        store.setTimeController(timeController);
        assertFalse(store.hasSomething());

        // 1
        int waitTime = 949;
        FlapTimerMock timer1 = new FlapTimerMock(true, waitTime);
        PrefixInfo pi = new PrefixInfo();
        pi.setPrefix(getPrefix(12));
        store.register(pi.getPrefix(), getAS(1), timer1);
        assertTrue(store.hasSomething());
        assertEquals(store.getReadyTime(), timer1.getUnflapTime());
        assertNotNull(store.next());

        // 2
        store.register(pi.getPrefix(), getAS(1), timer1);
        store.setTimeController(timeController);
        assertEquals(waitTime, store.getReadyTime());

    }

    public void testOrdering() {
        /*
         * 1. test that nothing is ready
         * 2. register new timer and see that that store shows it
         * 3. register worse timer and see that the peek didn't change
         * 4. move the first to the end
         * 5. register 100 timers, read 10, change rest, read 10, and so on - order has to be OK :) 
         */

        // 1
        FlapStoreImpl store = new FlapStoreImpl();
        store.setCallback(CallbackMock.getInstance());
        TimeControllerMock tmock = new TimeControllerMock();
        store.setTimeController(tmock);
        assertFalse(store.hasSomething());

        // 2
        PrefixInfo pi1 = new PrefixInfo();
        pi1.setPrefix(getPrefix(12));
        FlapTimerMock timer1 = new FlapTimerMock(true, 124);
        store.register(pi1.getPrefix(), getAS(1), timer1);
        assertTrue(store.hasSomething());
        assertSame(store.getFirstTimer(), timer1);

        // 3
        PrefixInfo pi2 = new PrefixInfo();
        pi2.setPrefix(getPrefix(2));
        FlapTimerMock timer2 = new FlapTimerMock(true, 224); // is worse
        store.register(pi2.getPrefix(), getAS(1), timer2);
        assertTrue(store.hasSomething());
        assertSame(store.getFirstTimer(), timer1);

        // 4
        timer1.unflapTime = 300;
        store.register(pi1.getPrefix(), getAS(1), timer1);
        assertSame(store.getFirstTimer(), timer2);
        assertSame(pi2.getPrefix(), store.next().value);
        assertSame(pi1.getPrefix(), store.next().value);
        assertFalse(store.hasSomething());

        // 5
        ArrayList<Pair<PrefixInfo, FlapTimerMock>> list = new ArrayList<Pair<PrefixInfo, FlapTimerMock>>();
        int count = 50;
        for (int i = 0; i < count; i++) {
            PrefixInfo pi = new PrefixInfo();
            pi.setPrefix(getPrefix(12 + i));
            FlapTimerMock timer = new FlapTimerMock(true, 100 + i);
            store.register(pi.getPrefix(), getAS(1), timer);
            list.add(new Pair<PrefixInfo, FlapTimerMock>(pi, timer));
        }

        for (int i = 0; i < count; i++) {
            Pair<PrefixInfo, FlapTimerMock> pair = list.get(i);
            pair.value.unflapTime = 10000 - pair.value.unflapTime;
            store.register(pair.key.getPrefix(), getAS(1), pair.value);
        }

        for (int i = 0; i < count; i++) {
            assertSame(list.get(count - i - 1).key.getPrefix(), store.next().value);
        }

        assertFalse(store.hasSomething());
    }

}

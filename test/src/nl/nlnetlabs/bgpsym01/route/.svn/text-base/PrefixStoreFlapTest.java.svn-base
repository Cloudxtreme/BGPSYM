package nl.nlnetlabs.bgpsym01.route;

import java.util.Map;
import java.util.TreeMap;

import nl.nlnetlabs.bgpsym01.cache.PrefixInfo;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.mock.FlapStoreMock;
import nl.nlnetlabs.bgpsym01.mock.TimeControllerMock;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.mocks.FlapTimerMock;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimer;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimerFactoryReal;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimerImpl;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class PrefixStoreFlapTest extends AbstractTest {

    private PrefixStoreMapImpl store;
    private PrefixCacheMock cache;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Prefix.init(1000);
        TimeControllerFactory.getTimeController();

        XProperties properties = XProperties.getInstance();
        properties.setPrefixArraySize(10000);
        XProperties.setInstance(properties);

        generateASes(10000);
        this.store = MockedPrefixStoreFactory.getStore();
        this.cache = (PrefixCacheMock) this.store.cache;
        this.store.setFlapTimerFactory(new FlapTimerFactoryReal(true));

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TimeControllerFactory.reload();
    }

    /**
     * Tests whether a flap is registered in flapStore
     */
    public void testRegisterInFlapStore() {

        PrefixStoreMapImpl store = this.store;
        store.setFlapTimerFactory(new FlapTimerFactoryMockForTests());
        FlapStoreMock flapStoreMock = new FlapStoreMock();
        store.setFlapStore(flapStoreMock);

        Prefix prefix = getPrefix(2);
        store.prefixReceived(getAS(1), prefix, createRoute(1, 2, 3, 4));
        PrefixInfo prefixInfo = cache.getPrefixInfo(prefix);

        FlapTimerMock flapTimer = (FlapTimerMock) prefixInfo.getNeighborsMap().get(getAS(1)).getFlapTimer();
        assertFalse(flapTimer.isFlapped());

        assertEquals(0, flapStoreMock.list.size());

        flapTimer.reactWithFlap = true;
        store.prefixReceived(getAS(1), prefix, createRoute(1, 2, 3, 4, 5));
        assertTrue(flapTimer.isFlapped());
        assertEquals(1, flapStoreMock.list.size());
        Pair<Prefix, FlapTimer> pair = flapStoreMock.list.get(0);
        assertSame(prefixInfo.getPrefix(), pair.key);
        assertSame(flapTimer, pair.value);

        flapStoreMock.list.clear();

        // no flaps -> flapStore stays untouched
        flapTimer.flapped = false;
        for (int i = 0; i < 20; i++) {
            store.prefixReceived(getAS(100 + i), prefix, createRoute(1, 2, 3, 4, 5));
        }
        assertEquals(0, flapStoreMock.list.size());

    }

    /**
     * Tests whether a route really gets flapped (using {@link FlapTimerImpl}
     * when it is received too many times by PrefixStore
     */
    public void testFlap() {

        TimeControllerMock tcm = new TimeControllerMock();
        TimeControllerFactory.setTimeController(tcm);
        PrefixStoreMapImpl store = this.store;

        int times = 12; // this should be enough to flap a route :)

        Prefix prefix = getPrefix(12);
        ASIdentifier as1 = getAS(1);
        for (int i = 0; i < times; i++) {
            store.prefixReceived(as1, prefix, createRoute(102, 203, 301, 492, i + 1000));
            PrefixInfo prefixInfo = cache.getPrefixInfo(prefix);
            if (i == 0) {
                assertNotNull("i=" + i, prefixInfo.getCurrentEntry());
            }
        }

        PrefixInfo prefixInfo = cache.getPrefixInfo(prefix);
        assertNull(prefixInfo.getCurrentEntry());

        PrefixTableEntry entry = prefixInfo.getNeighborsMap().get(as1);
        FlapTimer flapTimer = entry.getFlapTimer();
        assertTrue(flapTimer.isFlapped());

        long unflapTime = flapTimer.getUnflapTime();
        /* 
         * adjust the time controller, unflap the route, check that it is installed
         */

        tcm.currentTime = unflapTime + 1;
        assertTrue(flapTimer.isFlapped());
        store.unflap(prefix, getAS(1));

        assertSame(prefixInfo, cache.getPrefixInfo(prefix));
        assertFalse(flapTimer.isFlapped());
        assertNotNull(prefixInfo.getCurrentEntry());

    }

    /**
     * Tests whether {@link PrefixStoreMapImpl} treats incoming flaps correctly
     */
    public void testIncomingFlap() {
        /*
         * 1. check that announce, withdrawal and reannounce generate appropriate calls
         * 2. check that if current route is getting flapped it will be
         *      - removed
         *      - decision will be made
         *      - fbf'ed
         * 3. check that if incoming prefix is about fbf, fbf would be called
         * 4. check unflapping :)
         */

        // 1
        PrefixStoreMapImpl store = this.store;

        Prefix prefix = getPrefix(12);
        store.prefixReceived(getAS(1), prefix, createRoute(100, 102));
        store.prefixReceived(getAS(2), prefix, createRoute(100, 103, 105, 106));
        PrefixInfo pi = cache.getPrefixInfo(prefix);
        pi.setPrefix(prefix);
        PrefixTableEntry pte1 = pi.getNeighborsMap().get(getAS(1));
        PrefixTableEntry pte2 = pi.getNeighborsMap().get(getAS(2));
        store.prefixRemove(getAS(1), prefix);
        FlapTimerMock flapTimer1 = new FlapTimerMock(false, -1);
        FlapTimerMock flapTimer2 = new FlapTimerMock(false, -1);
        pte1.setFlapTimer(flapTimer1);
        pte2.setFlapTimer(flapTimer1);

        // FlapTimerMock flapTimer1 = new FlapTimerMock(false, -1);
        // FlapTimerMock flapTimer2 = new FlapTimerMock(false, -1);
        // FlapTimerMock flapTimer3 = new FlapTimerMock(false, -1);
        // FlapTimerMock flapTimer4 = new FlapTimerMock(false, -1);
        // pte1.setFlapTimer(flapTimer1);
        // PrefixTableEntry pte2 = new PrefixTableEntry(createRoute(100, 103,
        // 105));
        // pte1.setFlapTimer(flapTimer2);
        // PrefixTableEntry pte3 = new PrefixTableEntry(createRoute(100, 104,
        // 109, 123));
        // pte1.setFlapTimer(flapTimer3);
        // PrefixTableEntry pte4 = new PrefixTableEntry(createRoute(100, 102,
        // 112, 98));
        // pte1.setFlapTimer(flapTimer4);

        // announce
        store.prefixReceived(getAS(1), prefix, createRoute(12, 23, 45));
        assertEquals(0, flapTimer1.reannounced);
        assertEquals(1, flapTimer1.announced);
        assertEquals(0, flapTimer1.withdrawn);
        assertEquals(0, flapTimer2.announced);

        // reannounce twice
        store.prefixReceived(getAS(1), getPrefixList(12), createRoute(12, 23, 46));
        store.prefixReceived(getAS(1), getPrefixList(12), createRoute(12, 23, 47));
        assertEquals(1, flapTimer1.announced);
        assertEquals(2, flapTimer1.reannounced);
        assertEquals(0, flapTimer1.withdrawn);
        assertEquals(0, flapTimer2.announced);

        // withdraw
        store.prefixRemove(getAS(1), getPrefixList(12));
        assertEquals(1, flapTimer1.announced);
        assertEquals(2, flapTimer1.reannounced);
        assertEquals(1, flapTimer1.withdrawn);
        assertEquals(0, flapTimer2.announced);

        // 2
        assertSame(pte1.getFlapTimer(), flapTimer1);
        assertSame(pte1, pi.getNeighborsMap().get(getAS(1)));
        flapTimer1.reactWithFlap = true;
        store.prefixReceived(getAS(1), prefix, createRoute(1, 2, 3));
        assertTrue(pte2.getFlapTimer().isFlapped());
        assertSame(pte2, pi.getCurrentEntry());

        // 3
        // pte1 is fbf
        // it's too hard to do right now - just believe it's there...

        // 4
        store.unflap(prefix, getAS(1));
        assertEquals(pte1, pi.getCurrentEntry());

    }

    /**
     * Tests whether flapped prefixes aren't taken into account by
     * {@link PrefixStoreMapImpl#runDecision(ASIdentifier, PrefixInfo, Route)}
     */
    public void testDontUseFlappedPrefixes() {
        PrefixInfo pi = new PrefixInfo();
        pi.setPrefix(getPrefix(12));
        Map<ASIdentifier, PrefixTableEntry> neighborsMap = new TreeMap<ASIdentifier, PrefixTableEntry>();
        pi.setNeighborsMap(neighborsMap);

        PrefixTableEntry pteBest = new PrefixTableEntry(createRoute(1, 2));
        FlapTimerMock bestFlapTimer = new FlapTimerMock(false, -10);
        pteBest.setFlapTimer(bestFlapTimer);
        neighborsMap.put(getAS(1), pteBest);
        pteBest.setOrignator(getAS(1));

        for (int i = 0; i < 10; i++) {
            PrefixTableEntry pte = new PrefixTableEntry(createRoute(1, 2, 3, 4, i));
            neighborsMap.put(getAS(100 + i), pte);
            pte.setFlapTimer(bestFlapTimer);
            pte.setOrignator(getAS(100 + i));
            pi.setCurrentEntry(pte);
        }

        store.runDecision(null, pi, createRoute(1, 2, 3, 4, 10));
        assertSame(pi.getCurrentEntry(), pteBest);

        // set the route as flapped and be sure it won't be chosen
        bestFlapTimer.flapped = true;

        store.runDecision(null, pi, createRoute(1, 2));
        assertNotSame(pi.getCurrentEntry(), pteBest);

        neighborsMap.clear();
        pi.setCurrentEntry(pteBest);
        neighborsMap.put(getAS(1), pteBest);

        store.runDecision(null, pi, createRoute(1, 2, 3, 4, 0));
        assertNull(pi.getCurrentEntry());

        bestFlapTimer.flapped = false;

        store.runDecision(null, pi, null);
        assertNotNull(pi.getCurrentEntry());

    }

}

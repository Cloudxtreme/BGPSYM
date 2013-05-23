package nl.nlnetlabs.bgpsym01.route;

import java.util.Collection;
import java.util.List;

import nl.nlnetlabs.bgpsym01.cache.PrefixInfo;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.OutputAddEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.mocks.FlapTimerMock;
import nl.nlnetlabs.bgpsym01.route.output.OutputBufferMock;
import nl.nlnetlabs.bgpsym01.route.output.PolicyEasyMock;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

/**
 * Tests whether {@link PrefixStoreMapImpl} behaves correctly
 * 
 * @see PrefixStoreMapImpl
 */
public class PrefixStoreTest extends AbstractTest {

    private PrefixStoreMapImpl store;
    private PrefixCacheMock cache;
    ASIdentifier as1;
    ASIdentifier as2;
    ASIdentifier as3;
    private OutputBufferMock outputBuffer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Prefix.init(1000);

        XProperties properties = XProperties.getInstance();
        properties.setPrefixArraySize(10000);

        generateASes(1000);
        as1 = ASFactory.getInstance(1);
        as2 = ASFactory.getInstance(2);
        as3 = ASFactory.getInstance(3);
        this.store = getStore();
    }

    @Override
    protected void tearDown() throws Exception {
    }

    /**
     * Nothing should happen if the route isn't there (withdrawal for a
     * non-existen route)
     */
    public void testWithdrawInvalidRoute() {
        Prefix prefix = getPrefix(12);
        ASIdentifier as = getAS(1);
        store.prefixReceived(as, prefix, createRoute(12));
        PrefixInfo prefixInfo = cache.getPrefixInfo(prefix);
        FlapTimerMock flapTimerMock = new FlapTimerMock();
        prefixInfo.getCurrentEntry().setFlapTimer(flapTimerMock);
        assertTrue(store.prefixRemove(as, prefix));
        assertEquals(1, flapTimerMock.withdrawn);
        assertFalse(store.prefixRemove(as, prefix));
        assertEquals(1, flapTimerMock.withdrawn);
    }

    /**
     * If we get a removal for current entry but have another one the standard
     * behavior should be to create OutputAddEntity ( and not OutputRemoveEntity
     * like it was before)
     * 
     * @see PrefixStore#prefixRemove(ASIdentifier, Collection)
     */
    public void testPrefixChange() {
        Route route1 = createRoute(2, 3, 4, 5);
        Route route2 = createRoute(1, 2, 3);
        Route route3 = createRoute(2, 3, 4, 8, 9);
        List<Prefix> prefix = getPrefixList(1);

        // propagated further (1)
        store.prefixReceived(as1, prefix, route1);
        // propagated further (2)
        store.prefixReceived(as2, prefix, route2);
        // not propagated further (3)
        store.prefixReceived(as3, prefix, route3);
        assertEquals(2, outputBuffer.getAdded());
        assertEquals(0, outputBuffer.getRemoved());

        assertEquals(route2, gp(1).getCurrentEntry().getRoute());
        assertEquals(3, gp(1).getNeighborsMap().size());

        // remove the best route - we should propagate the new one (3)
        store.prefixRemove(as2, prefix);
        assertEquals(3, outputBuffer.getAdded());
        assertEquals(0, outputBuffer.getRemoved());

        // remove not the best route - we shouldn't propagate anything
        store.prefixRemove(as3, prefix);
        assertEquals(3, outputBuffer.getAdded());
        assertEquals(0, outputBuffer.getRemoved());

        // remove the last route - we should propagate removal
        store.prefixRemove(as1, prefix);
        assertEquals(3, outputBuffer.getAdded());
        assertEquals(1, outputBuffer.getRemoved());
    }

    /**
     * Just adds and changes prefixes and checks whether the prefixCache state
     * behaves correctly.
     * 
     * @see PrefixStore#prefixReceived(ASIdentifier, Collection, Route)
     */
    public void testAddPrefix() {

        // add one prefix
        Route route = createRoute(1, 2, 3, 4);
        List<Prefix> prefix = getPrefixList(1);
        store.prefixReceived(as1, prefix, route);
        assertEquals(1, cache.size());
        assertEquals(1, gp(1).getNeighborsMap().size());
        assertEquals(1, outputBuffer.getFlushed());

        // change its route
        route = createRoute(2, 3, 4);
        store.prefixReceived(as1, prefix, route);

        // check whether the store state has changed
        assertEquals(1, cache.size());
        assertEquals(route, gp(1).getCurrentEntry().getRoute());
        assertEquals(1, gp(1).getNeighborsMap().size());
        assertEquals(2, outputBuffer.getFlushed());

        // add a worse route from second
        Route route2 = createRoute(2, 3, 4, 5, 7);
        store.prefixReceived(as2, prefix, route2);

        // check that current route is not changed
        assertEquals(1, cache.size());
        assertEquals(route, gp(1).getCurrentEntry().getRoute());
        assertEquals(2, gp(1).getNeighborsMap().size());
        assertEquals(3, outputBuffer.getFlushed());

        // add a better route from third neighbor
        Route route3 = createRoute(1, 2);
        store.prefixReceived(as3, prefix, route3);
        // check the state
        assertEquals(1, cache.size());
        assertEquals(route3, gp(1).getCurrentEntry().getRoute());
        assertEquals(3, gp(1).getNeighborsMap().size());
        assertEquals(outputBuffer.getFlushed(), 4);

        // add a second prefix, also from a3
        prefix = getPrefixList(2);
        store.prefixReceived(as3, prefix, route3);
        // check the state
        assertEquals(2, cache.size());
        assertEquals(route3, gp(2).getCurrentEntry().getRoute());
        assertEquals(1, gp(2).getNeighborsMap().size());
        assertEquals(outputBuffer.getFlushed(), 5);
    }

    /**
     * Tests whether withdrawing prefixes leads to the good state (on small
     * piece of data). Do only if {@link #testAddPrefix()} works.
     * 
     * @see PrefixStore#prefixRemove(ASIdentifier, Collection)
     */
    public void testWithdrawPrefix() {

        // add one prefix
        Route route1 = createRoute(1, 2, 3, 4);
        List<Prefix> prefixList = getPrefixList(1);
        store.prefixReceived(as1, prefixList, route1);
        assertEquals(1, cache.size());
        assertEquals(1, gp(1).getNeighborsMap().size());
        assertEquals(outputBuffer.getFlushed(), 1);

        // -----------------
        // add the same prefix from another neighbor
        Route route2 = createRoute(2, 3, 4);
        store.prefixReceived(as2, prefixList, route2);

        // check whether the store state has changed
        assertEquals(1, cache.size());
        assertEquals(route2, gp(1).getCurrentEntry().getRoute());
        assertEquals(2, gp(1).getNeighborsMap().size());
        assertEquals(outputBuffer.getFlushed(), 2);

        // remove the prefix from the second neighbor
        store.prefixRemove(as2, prefixList);
        // check whether the store state has changed
        assertEquals(1, cache.size());
        assertEquals(route1, gp(1).getCurrentEntry().getRoute());
        checkValidPrefixesCount(gp(1).getNeighborsMap(), 1, 1);
        assertEquals(outputBuffer.getFlushed(), 3);

        // -------------
        // make it two prefixes again
        store.prefixReceived(as2, prefixList, route2);
        assertEquals(4, outputBuffer.getFlushed());

        // remove the prefix from the first neighbor (the worse one)
        store.prefixRemove(as1, prefixList);

        // check that prefix route is the same but neighbor state has changed
        assertEquals(1, cache.size());
        assertEquals(route2, gp(1).getCurrentEntry().getRoute());
        assertTrue(gp(1).getCurrentEntry().isValid());
        checkValidPrefixesCount(gp(1).getNeighborsMap(), 1, 1);
        assertEquals(5, outputBuffer.getFlushed());
    }

    /**
     * Tests adding information about many prefixes at the same time. Test only
     * if {@link #testAddPrefix()} works.
     * 
     * @see PrefixStore#prefixReceived(ASIdentifier, Collection, Route)
     */
    public void testAddPrefixArray() {
        // add one prefix (just to be sure that we know that we're doing)
        Route route1 = createRoute(1, 2, 3, 4);
        List<Prefix> prefix = getPrefixList(1);
        store.prefixReceived(as1, prefix, route1);
        assertEquals(1, cache.size());
        assertEquals(1, gp(1).getNeighborsMap().size());
        assertEquals(outputBuffer.getFlushed(), 1);

        // -----
        /*
         * start the fun:
         */
        // add few prefixes together
        List<Prefix> ar = getPrefixList(2, 3, 4, 5, 6);
        store.prefixReceived(as2, ar, route1);
        /*
         * check whether they are really together:
         *  1. each one should be available as one
         */
        assertEquals(6, cache.size());
        // 2. value for 2 and for 4 should not be the same
        assertNotSame(gp(2), cache.getPrefixInfo(getPrefix(4)));
        assertEquals(2, outputBuffer.getFlushed());

        // -----
        // change route for 4, 5 and 6
        Route route2 = createRoute(4, 5, 6);
        List<Prefix> ar2 = getPrefixList(4, 5);
        store.prefixReceived(as2, ar2, route2);
        // 1. still each prefix should be available for us
        assertEquals(6, cache.size());
        // 2. value for 2 and 3 should be equal
        assertEquals(gp(2).getCurrentEntry(), gp(3).getCurrentEntry());
        // 3. value for 4 and 5 should be equal
        assertEquals(gp(2).getNeighborsMap(), gp(3).getNeighborsMap());
        // 4. value for 2 and 4 should not be equal
        assertFalse(gp(2).getCurrentEntry().equals(gp(4).getCurrentEntry()));
        // 5. route for 2 should not have changed
        assertEquals(route1, gp(2).getCurrentEntry().getRoute());
        // 6. whereas for 5 should have
        assertEquals(route2, gp(5).getCurrentEntry().getRoute());
        assertEquals(outputBuffer.getFlushed(), 3);

        // ---------
        /*
         * advertise a better route for the whole prefix array and check how it behaves :)
         */
        Route route3 = createRoute(2);
        List<Prefix> ar3 = getPrefixList(2, 3, 4, 5);
        store.prefixReceived(as2, ar3, route3);
        // 1. still each prefix should be available for us
        assertEquals(6, cache.size());
        // 2. value for 2 and 3 should be equal
        assertEquals(gp(2).getCurrentEntry(), gp(3).getCurrentEntry());
        // 3. value for 4 and 5 should be equal
        assertEquals(gp(2).getNeighborsMap(), gp(3).getNeighborsMap());
        // 4. value for 2 and 4 should be equal
        assertFalse(gp(2).getCurrentEntry().equals(gp(4)));
        // 5. value for 2 and 6 should not be equal
        assertFalse(gp(2).getNeighborsMap().equals(gp(6).getNeighborsMap()));
        // 6. route for 2 should have changed
        assertEquals(route3, gp(2).getCurrentEntry().getRoute());
        // 7. whereas for 6 shouldn't from the beginning
        assertEquals(route1, gp(6).getCurrentEntry().getRoute());
        assertEquals(outputBuffer.getFlushed(), 4);

    }

    private PrefixInfo gp(int i) {
        return cache.getPrefixInfo(getPrefix(i));
    }

    /**
     * Tests whether removing array of prefixes works as it should.
     * 
     * @see PrefixStore#prefixRemove(ASIdentifier, Collection)
     */
    public void testWithdrawPrefixArray() {
        // add 5 prefixes from 3 different neighbors
        Route route1 = createRoute(1, 2, 3, 4);
        Route route2 = createRoute(2, 3, 4, 5, 6);
        Route route3 = createRoute(3, 4, 5, 6, 7, 8);
        List<Prefix> prefixes = getPrefixList(1, 2, 3, 4, 5);
        store.prefixReceived(as1, prefixes, route1);
        store.prefixReceived(as2, prefixes, route2);
        store.prefixReceived(as3, prefixes, route3);
        // sanity checks
        assertEquals(5, cache.size());
        for (Prefix p : prefixes) {
            assertEquals(gp(p.getNum()).getNeighborsMap().size(), 3);
        }
        assertEquals(outputBuffer.getFlushed(), 3);

        // ----
        // withdraw first neighbor from first 3 prefixes
        store.prefixRemove(as1, getPrefixList(1, 2, 3));
        // check whether everything's OK
        assertEquals(5, cache.size());

        // check neighbors count
        checkValidPrefixesCount(gp(1).getNeighborsMap(), 2, 1);
        checkValidPrefixesCount(gp(2).getNeighborsMap(), 2, 1);
        checkValidPrefixesCount(gp(3).getNeighborsMap(), 2, 1);
        assertEquals(gp(4).getNeighborsMap().size(), 3);
        assertEquals(gp(5).getNeighborsMap().size(), 3);

        // check current routes
        assertEquals(gp(1).getCurrentEntry().getRoute(), route2);
        assertEquals(gp(2).getCurrentEntry().getRoute(), route2);
        assertEquals(gp(3).getCurrentEntry().getRoute(), route2);
        assertEquals(gp(4).getCurrentEntry().getRoute(), route1);
        assertEquals(gp(5).getCurrentEntry().getRoute(), route1);

        assertEquals(outputBuffer.getFlushed(), 4);

        // ---------------
        // remove third neighbor form last 3 prefixes
        store.prefixRemove(as3, getPrefixList(3, 4, 5));

        // check neighbors count
        checkValidPrefixesCount(gp(1).getNeighborsMap(), 2, 1);
        checkValidPrefixesCount(gp(2).getNeighborsMap(), 2, 1);
        checkValidPrefixesCount(gp(3).getNeighborsMap(), 1, 2);
        checkValidPrefixesCount(gp(4).getNeighborsMap(), 2, 1);
        checkValidPrefixesCount(gp(5).getNeighborsMap(), 2, 1);

        // check current routes
        assertEquals(gp(1).getCurrentEntry().getRoute(), route2);
        assertEquals(gp(2).getCurrentEntry().getRoute(), route2);
        assertEquals(gp(3).getCurrentEntry().getRoute(), route2);
        assertEquals(gp(4).getCurrentEntry().getRoute(), route1);
        assertEquals(gp(5).getCurrentEntry().getRoute(), route1);

        assertEquals(outputBuffer.getFlushed(), 5);

        // ---------------
        // remove second neighbor from all prefixes
        store.prefixRemove(as2, getPrefixList(1, 2, 3, 4, 5));

        // check neighbors count
        checkValidPrefixesCount(gp(1).getNeighborsMap(), 1, 2);
        checkValidPrefixesCount(gp(2).getNeighborsMap(), 1, 2);
        checkValidPrefixesCount(gp(3).getNeighborsMap(), 0, 3);
        checkValidPrefixesCount(gp(4).getNeighborsMap(), 1, 2);
        checkValidPrefixesCount(gp(5).getNeighborsMap(), 1, 2);

        // check current routes
        assertEquals(gp(1).getCurrentEntry().getRoute(), route3);
        assertEquals(gp(2).getCurrentEntry().getRoute(), route3);
        assertNull(gp(3).getCurrentEntry());
        assertEquals(gp(4).getCurrentEntry().getRoute(), route1);
        assertEquals(gp(5).getCurrentEntry().getRoute(), route1);

        assertEquals(outputBuffer.getFlushed(), 6);

    }

    private PrefixStoreMapImpl getStore() {
        PrefixStoreMapImpl store2 = MockedPrefixStoreFactory.getStore();
        cache = (PrefixCacheMock) store2.getCache();
        outputBuffer = (OutputBufferMock) store2.getOutputBuffer();
        return store2;
        /*PrefixStoreMapImpl store = new PrefixStoreMapImpl();
        store.setAsIdentifier(ASFactory.getInstance(0));
        store.setCallback(CallbackMock.getInstance());
        store.setTimeController(new TimeControllerMock());
        store.setFlapStore(new FlapStoreMock());

        // TODO test callback
        store.setCallback(CallbackMock.getInstance());
        outputBuffer = new OutputBufferMock();
        store.setOutputBuffer(outputBuffer);
        cache = new PrefixCacheMock();
        store.setCache(cache);
        store.setPolicy(new PolicyImpl());
        return store;*/
    }

    /**
     * Tests whether info about old route is attached to {@link OutputAddEntity}
     */
    public void testOldRoute() {
        PrefixStore store = getStore();
        ((PrefixStoreMapImpl) store).setPolicy(new PolicyEasyMock());

        store.prefixReceived(getAS(11), getPrefixList(1), createRoute(1, 2, 3, 4, 11));
        assertEquals(1, outputBuffer.getAddedEntities().size());
        assertNull(outputBuffer.getAddedEntities().get(0).getLastRoute());

        store.prefixReceived(getAS(12), getPrefixList(1), createRoute(1, 2, 3, 4, 12));
        assertEquals(1, outputBuffer.getAddedEntities().size());

        store.prefixReceived(getAS(12), getPrefixList(1), createRoute(1, 2, 12));
        assertEquals(2, outputBuffer.getAddedEntities().size());
        assertNotNull(outputBuffer.getAddedEntities().get(1).getLastRoute());
    }

}

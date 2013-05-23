package nl.nlnetlabs.bgpsym01.route;

import nl.nlnetlabs.bgpsym01.callback.CallbackMock;
import nl.nlnetlabs.bgpsym01.command.RouteViewDataResponse;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.mock.TimeControllerMock;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

/**
 * Tests whether {@link PrefixStoreRouteView} gathers info about prefixes
 * correctly
 */
public class PrefixStoreRouteViewTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        generateASes(1000);
        Prefix.init(1000);
    }

    @Override
    protected void tearDown() throws Exception {
    }

    /**
     * Asserts that {@link PrefixStoreRouteView} returns good collection
     */
    public void testGetPrefixDataList() {
        // don't get to deep - just check the size of the list...
        PrefixStoreRouteView store = getStore();
        TimeControllerMock timeController = (TimeControllerMock) store.timeController;
        ASIdentifier as1 = getAS(1);
        ASIdentifier as2 = getAS(2);

        int time1 = 7;
        timeController.currentTime = time1;
        int time2 = 12;
        store.prefixReceived(as1, getPrefixList(12, 13, 15), createRoute(1, 2, 34)); // 12
        // ,
        // 13,
        // 15 -
        // count
        // 3
        assertEquals(3, store.getPrefixDataList().size());
        timeController.currentTime = time2;
        store.prefixReceived(as1, getPrefixList(16, 3, 15), createRoute(1, 2, 34)); // 16
        // ,
        // 3 -
        // count
        // 5
        assertEquals(5, store.getPrefixDataList().size());

        long time5 = 102;
        timeController.currentTime = time5;
        int time6 = 207;
        assertNull(store.getPrefixData(as2, getPrefix(1)));
        store.prefixReceived(as2, getPrefixList(1, 10), createRoute()); // 1, 10
        // -
        // count
        // 7
        timeController.currentTime = time6;
        store.prefixRemove(as2, getPrefixList(1, 12)); // 12 - count8
        assertEquals(8, store.getPrefixDataList().size());
    }

    /**
     * Tests whether map gets correctly updated when prefixes are comming
     */
    public void testUpdateMap() {
        PrefixStoreRouteView store = getStore();
        TimeControllerMock timeController = (TimeControllerMock) store.timeController;
        ASIdentifier as1 = getAS(1);
        ASIdentifier as2 = getAS(2);

        // add few prefixes
        int time1 = 7;
        timeController.currentTime = time1;
        int time2 = 12;
        store.prefixReceived(as1, getPrefixList(12, 13, 15), createRoute(1, 2, 34));
        timeController.currentTime = time2;
        store.prefixReceived(as1, getPrefixList(16, 3, 15), createRoute(1, 2, 34));

        assertNull(store.getPrefixData(as1, getPrefix(1)));
        assertEquals(new RouteViewDataResponse(as1, getPrefix(12), time1, time1), store.getPrefixData(as1, getPrefix(12)));
        assertEquals(new RouteViewDataResponse(as1, getPrefix(15), time1, time2), store.getPrefixData(as1, getPrefix(15)));
        assertEquals(new RouteViewDataResponse(as1, getPrefix(16), time2, time2), store.getPrefixData(as1, getPrefix(16)));

        // now some withdrawals
        // add few prefixes
        int time3 = 27;
        timeController.currentTime = time3;
        int time4 = 42;
        store.prefixRemove(as1, getPrefixList(12, 13, 15));
        timeController.currentTime = time4;
        store.prefixRemove(as1, getPrefixList(16, 15, 1));
        assertEquals(new RouteViewDataResponse(as1, getPrefix(1), time4, time4), store.getPrefixData(as1, getPrefix(1)));
        assertEquals(new RouteViewDataResponse(as1, getPrefix(15), time1, time4), store.getPrefixData(as1, getPrefix(15)));
        assertEquals(new RouteViewDataResponse(as1, getPrefix(12), time1, time3), store.getPrefixData(as1, getPrefix(12)));

        // and some other guy

        long time5 = 102;
        timeController.currentTime = time5;
        int time6 = 207;
        assertNull(store.getPrefixData(as2, getPrefix(1)));
        store.prefixReceived(as2, getPrefixList(1, 10), createRoute());
        timeController.currentTime = time6;
        store.prefixRemove(as2, getPrefixList(1, 12));

        // for as1 nothing should have changed (we were working only on as2)
        assertEquals(new RouteViewDataResponse(as1, getPrefix(1), time4, time4), store.getPrefixData(as1, getPrefix(1)));
        assertEquals(new RouteViewDataResponse(as1, getPrefix(15), time1, time4), store.getPrefixData(as1, getPrefix(15)));
        assertEquals(new RouteViewDataResponse(as1, getPrefix(12), time1, time3), store.getPrefixData(as1, getPrefix(12)));

        // for as2 there should have been some changes
        assertEquals(new RouteViewDataResponse(as2, getPrefix(1), time5, time6), store.getPrefixData(as2, getPrefix(1)));
        assertEquals(new RouteViewDataResponse(as2, getPrefix(10), time5, time5), store.getPrefixData(as2, getPrefix(10)));
        assertEquals(new RouteViewDataResponse(as2, getPrefix(12), time6, time6), store.getPrefixData(as2, getPrefix(12)));
    }

    public void testResetPrefixData() {
        PrefixStoreRouteView store = getStore();
        TimeControllerMock timeController = (TimeControllerMock) store.timeController;
        ASIdentifier as1 = getAS(1);
        ASIdentifier as2 = getAS(2);

        // add few prefixes and check that everything is OK
        int time1 = 7;
        timeController.currentTime = time1;
        int time2 = 12;
        store.prefixReceived(as1, getPrefixList(12, 13, 15), createRoute(1, 2, 34));
        timeController.currentTime = time2;
        store.prefixReceived(as2, getPrefixList(16, 3, 15), createRoute(1, 2, 34));

        store.resetPrefixData(getPrefix(12));
        store.resetPrefixData(getPrefix(3));
        store.resetPrefixData(getPrefix(15));

        // some got reset
        assertEquals(new RouteViewDataResponse(as1, getPrefix(12), -1, -1), store.getPrefixData(as1, getPrefix(12)));
        assertEquals(new RouteViewDataResponse(as1, getPrefix(15), -1, -1), store.getPrefixData(as1, getPrefix(15)));
        assertEquals(new RouteViewDataResponse(as2, getPrefix(15), -1, -1), store.getPrefixData(as2, getPrefix(15)));
        assertEquals(new RouteViewDataResponse(as2, getPrefix(3), -1, -1), store.getPrefixData(as2, getPrefix(3)));

        // and some didn't
        assertEquals(new RouteViewDataResponse(as1, getPrefix(13), time1, time1), store.getPrefixData(as1, getPrefix(13)));

        // check if after reset we will behave correctly
        timeController.currentTime = 19;
        store.prefixReceived(as2, getPrefixList(3), createRoute(1, 2, 34));
        assertEquals(new RouteViewDataResponse(as2, getPrefix(3), 19, 19), store.getPrefixData(as2, getPrefix(3)));
    }

    private PrefixStoreRouteView getStore() {
        PrefixStoreRouteView store = new PrefixStoreRouteView();
        store.setCallback(new CallbackMock());
        TimeControllerMock timeController = new TimeControllerMock();
        store.setTimeController(timeController);
        return store;
    }

}

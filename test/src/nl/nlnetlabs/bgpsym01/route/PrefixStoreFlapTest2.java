package nl.nlnetlabs.bgpsym01.route;

import nl.nlnetlabs.bgpsym01.cache.PrefixInfo;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.mocks.FlapTimerMock;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class PrefixStoreFlapTest2 extends AbstractTest {

    private PrefixStoreMapImpl store;
    private PrefixCacheMock cache;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Prefix.init(1000);
        TimeControllerFactory.getTimeController();

        XProperties properties = XProperties.getInstance();
        properties.setPrefixArraySize(10000);

        generateASes(10000);
        this.store = MockedPrefixStoreFactory.getStore();
        this.cache = (PrefixCacheMock) this.store.cache;

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TimeControllerFactory.reload();
    }

    /**
     * Tests that the unflapped route can become current route (and that it may
     * not)
     */
    public void testUnflapCanBeBetter() {
        ASIdentifier as = getAS(1);
        Prefix prefix = getPrefix(12);
        Route goodRoute = createRoute(1, 2, 3);
        store.prefixReceived(as, prefix, goodRoute);
        PrefixInfo prefixInfo = cache.getPrefixInfo(prefix);
        FlapTimerMock flapTimer1 = (FlapTimerMock) prefixInfo.getCurrentEntry().getFlapTimer();

        flapTimer1.reactWithFlap = true;
        store.prefixReceived(as, prefix, goodRoute);
        assertNull(prefixInfo.getCurrentEntry());

        Route worseRoute = createRoute(1, 2, 3, 4, 5);
        store.prefixReceived(getAS(2), prefix, worseRoute);
        assertNotNull(prefixInfo.getCurrentEntry());
        assertEquals(prefixInfo.getCurrentEntry().getRoute(), worseRoute);

        // this one is bad - no one cares
        Route worstRoute = createRoute(1, 2, 3, 4, 5, 6, 7);
        store.prefixReceived(getAS(3), prefix, worstRoute);
        assertNotNull(prefixInfo.getCurrentEntry());
        assertEquals(prefixInfo.getCurrentEntry().getRoute(), worseRoute);
        FlapTimerMock flapTimer3 = (FlapTimerMock) prefixInfo.getNeighborsMap().get(getAS(3)).getFlapTimer();

        flapTimer3.reactWithFlap = true;
        store.prefixReceived(getAS(3), prefix, worstRoute);
        assertTrue(flapTimer3.isFlapped());

        // start unflapping

        // unflap a better one - should be exchanged
        flapTimer1.reactWithUnflap = true;
        store.prefixReceived(as, prefix, goodRoute);
        assertFalse(flapTimer1.isFlapped());
        assertNotNull(prefixInfo.getCurrentEntry());
        assertEquals(prefixInfo.getCurrentEntry().getRoute(), goodRoute);

        // unflap the worst one - should not be exchanged
        flapTimer3.reactWithUnflap = true;
        store.prefixReceived(getAS(3), prefix, worstRoute);
        assertNotNull(prefixInfo.getCurrentEntry());
        assertEquals(prefixInfo.getCurrentEntry().getRoute(), goodRoute);
        assertFalse(flapTimer3.isFlapped());

    }

}

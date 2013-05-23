package nl.nlnetlabs.bgpsym01.route;

import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.mock.TimeControllerMock;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.route.PrefixStore.PrefixStoreType;
import nl.nlnetlabs.bgpsym01.route.output.NeighborMock;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class PrefixStroreRISTest extends AbstractTest {

    private static final int BOGUS_PREFIX_MIN = 1000;
    private TimeControllerMock timeController;
    private NeighborMock n1;
    private NeighborMock n2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateASes(1000);
        Prefix.init(1100);
        XProperties.getInstance().bogusPrefixMin = BOGUS_PREFIX_MIN;
        timeController = new TimeControllerMock();
        TimeControllerFactory.setTimeController(timeController);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TimeControllerFactory.reload();
    }

    public void testGetType() {
        PrefixStoreRIS store = getStore();
        assertEquals(PrefixStoreType.RIS, store.getType());
    }

    private PrefixStoreRIS getStore() {
        PrefixStoreRIS store = new PrefixStoreRIS();
        Neighbors neighbors = new Neighbors(getAS(0));
        n1 = new NeighborMock(getAS(1));
        neighbors.addNeighbor(n1);
        n2 = new NeighborMock(getAS(2));
        neighbors.addNeighbor(n2);
        store.setNeighbors(neighbors);
        return store;
    }

    /**
     * If the update is received from myself, we should propagate it instead of
     * storing it. This method tests if it is done properly.
     */
    public void testPropation() {
        ASIdentifier asTo = getAS(124);
        ASIdentifier me = asTo;
        PrefixStoreRIS store = getStore();
        store.setAsId(asTo);

        ArrayList<NabsirUpdate> list = new ArrayList<NabsirUpdate>();
        updateReceived(asTo, store, list, me, getPrefixList(123), createRoute());
        updateReceived(asTo, store, list, me, getPrefixList(19, 12), createRoute());
        updateReceived(asTo, store, list, me, getPrefixList(1), createRoute());

        // this is withdrawal
        updateReceived(asTo, store, list, me, getPrefixList(13, 34), null);

        // assert that nothing was saved
        assertEquals(0, store.getList().size());

        // assert that things were propagated
        assertEquals(4, n1.getUpdates().size());
        assertEquals(4, n2.getUpdates().size());

        // check 1st from n1 and 4th update from n3 (random check to be sure)
        {
            BGPUpdate n1Update = n1.getUpdates().get(0);
            assertEquals(asTo, n1Update.getSender());
            assertEquals(getPrefixList(123), n1Update.getPrefixes());
            assertEmpty("", n1Update.getWithdrawals());
            assertEquals(createRoute(me.getInternalId()), n1Update.getRoute());
        }

        {
            BGPUpdate n2Update = n2.getUpdates().get(3);
            assertEquals(asTo, n2Update.getSender());
            assertEquals(getPrefixList(13, 34), n2Update.getWithdrawals());
            assertEmpty("", n2Update.getPrefixes());
            assertEquals(0, n2Update.getRoute().getPathLength());
        }
    }

    /**
     * Tests receiving normal updates:<br>
     * - should be logged<br>
     * - should not be propagated to the neighbors
     */
    public void testGetUpdate() {
        ASIdentifier asTo = getAS(124);
        PrefixStoreRIS store = getStore();
        store.setAsId(asTo);
        ArrayList<NabsirUpdate> list = new ArrayList<NabsirUpdate>();

        updateReceived(asTo, store, list, getAS(12), getPrefixList(123), createRoute(35, 432, 5));
        updateReceived(asTo, store, list, getAS(16), getPrefixList(19, 12), createRoute(35, 5));
        updateReceived(asTo, store, list, getAS(1), getPrefixList(1), createRoute(315, 432, 5));
        updateReceived(asTo, store, list, getAS(3), getPrefixList(13), null);

        assertEquals(list.size(), 5);
        assertEquals(list, store.getList());

        // this one should not be propagated - is bigger than bogus prefix
        // number
        updateReceived(asTo, store, list, getAS(1), getPrefixList(1010), createRoute(315, 432, 5));
        assertEquals(list.size(), 5);
        assertEquals(list, store.getList());

        // 39 from thsi should be taken, 1034 discarded
        updateReceived(asTo, store, list, getAS(1), getPrefixList(1034, 39), null);
        assertEquals(list.size(), 6);
        assertEquals(list, store.getList());

        // assert that nothing was propagated
        assertEquals(0, n1.getUpdates().size());
        assertEquals(0, n2.getUpdates().size());

    }

    private void updateReceived(ASIdentifier asTo, PrefixStoreRIS store, ArrayList<NabsirUpdate> list, ASIdentifier as, List<Prefix> prefixList, Route route) {

        timeController.currentTime += 17;

        if (route == null) {
            store.prefixRemove(as, prefixList);
        } else {
            store.prefixReceived(as, prefixList, route);
        }
        for (Prefix prefix : prefixList) {
            if (prefix.getNum() < BOGUS_PREFIX_MIN) {
                NabsirUpdate update = new NabsirUpdate();
                update.setTime(timeController.getCurrentTime());
                update.setFrom(as);
                update.setTo(asTo);
                update.setPrefix(prefix);
                update.setRoute(route);
                update.setWithdrawal(route == null);
                list.add(update);
            }
        }
    }

}

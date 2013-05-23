package nl.nlnetlabs.bgpsym01.route;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.OutputAddEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.route.output.NeighborMock;

/**
 * Tests whether PolicyImplRel gives proper answer for proper relations between
 * peers
 * 
 * @see PolicyImplRel
 */
public class PolicyImplRelTest extends AbstractTest {

    Policy policy;

    @Override
    protected void setUp() throws Exception {
        Prefix.init(1000);
        generateASes(32000);
        policy = new PolicyImplRel();
    }

    /**
     * Tests sibling and customer relations
     */
    public void testEasy() {
        ASIdentifier as0 = getAS(0);
        ASIdentifier as1 = getAS(1);

        NeighborMock nm = new NeighborMock(as1);
        Neighbors neighbors = new Neighbors(getAS(100));

        Route route1 = createRoute(1, 2, 3, 4);
        OutputAddEntity entity = new OutputAddEntity(getPrefix(0), route1, null);
        Route route2 = createRoute(1, 2, 3, 1);
        OutputAddEntity entity2 = new OutputAddEntity(getPrefix(0), route2, null);

        nm.attach(PeerRelation.SIBLING);
        assertTrue(policy.isAdvertisable(as0, nm, neighbors, entity.getRoute()));
        // I don't send his data even to him
        assertFalse(policy.isAdvertisable(as0, nm, neighbors, entity2.getRoute()));

        nm.attach(PeerRelation.CUSTOMER);
        assertTrue(policy.isAdvertisable(as0, nm, neighbors, entity.getRoute()));
        assertFalse(policy.isAdvertisable(as0, nm, neighbors, entity2.getRoute()));

        nm.attach(PeerRelation.PROVIDER);
        // I don't send his data even to him
        assertFalse(policy.isAdvertisable(as0, nm, neighbors, entity2.getRoute()));

    }

    public void testProvider() {

        /*
         * I am as1
         * 
         * the idea is:
         * as0 is provider for as1,
         * as1 is provider for as2
         * as3 is peer to as1
         * 
         * we want as1 to:
         * - send his prefixes to as0
         * - send prefixes from as1 to as0
         * - do not send prefixes from as3 to as0
         */

        ASIdentifier as0 = getAS(0);
        ASIdentifier as1 = getAS(1);
        ASIdentifier as2 = getAS(2);
        ASIdentifier as3 = getAS(3);

        NeighborMock nm0 = new NeighborMock(as0);
        NeighborMock nm2 = new NeighborMock(as2);
        NeighborMock nm3 = new NeighborMock(as3);

        nm0.attach(PeerRelation.PROVIDER);
        nm2.attach(PeerRelation.CUSTOMER);
        nm3.attach(PeerRelation.PEER);

        Neighbors neighbors = new Neighbors(getAS(100));
        neighbors.addNeighbor(nm0);
        neighbors.addNeighbor(nm2);
        neighbors.addNeighbor(nm3);

        // as0 is my provider
        runSlaveProviderTest(as1, as2, as3, nm0, neighbors);

        // if as0 he is my peer it should also hold
        nm0.attach(PeerRelation.PEER);
        runSlaveProviderTest(as1, as2, as3, nm0, neighbors);

        // but if as2 is my peer I don't want to propagate his prefixes
        nm2.attach(PeerRelation.PEER);
        // as1 does not sends prefixes from his peer as2
        OutputAddEntity entity = new OutputAddEntity(getPrefix(0), createRoute(1, 2, 3, 2), null);
        assertFalse(policy.isAdvertisable(as1, nm0, neighbors, entity.getRoute()));

    }

    /**
     * We always want to advertise to Route Views Monitors
     * 
     * No matter what route - just return <b>true</b>
     */
    public void testRouteViewMonitor() {
        ASIdentifier as3 = getAS(3);

        NeighborMock nm3 = new NeighborMock(as3);

        nm3.attach(PeerRelation.ROUTEVIEWMONITOR);

        Neighbors neighbors = new Neighbors(getAS(100));
        neighbors.addNeighbor(nm3);

        PolicyImplRel pir = new PolicyImplRel();
        ASIdentifier me = getAS(0);
        assertTrue(pir.isAdvertisable(me, nm3, null, createRoute(1, 2, 4)));
        assertTrue(pir.isAdvertisable(me, nm3, null, createRoute(5, 632, 1)));
        assertTrue(pir.isAdvertisable(me, nm3, null, createRoute(0)));
        assertTrue(pir.isAdvertisable(me, nm3, null, new Route()));

    }

    private void runSlaveProviderTest(ASIdentifier as1, ASIdentifier as2, ASIdentifier as3, Neighbor nm0, Neighbors neighbors) {
        // as1 sends his prefixes to as0
        assertTrue(policy.isAdvertisable(as1, nm0, neighbors, createRoute()));

        // as1 does not send prefixes from his peer as3
        assertFalse(policy.isAdvertisable(as1, nm0, neighbors, createRoute(1, 2, 3)));

        // as1 sends prefixes from his customer as2
        assertTrue(policy.isAdvertisable(as1, nm0, neighbors, createRoute(1, 2, 3, 2)));
    }

}

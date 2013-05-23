package nl.nlnetlabs.bgpsym01.route.output;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;
import nl.nlnetlabs.bgpsym01.route.PolicyImpl;
import nl.nlnetlabs.bgpsym01.route.output.OutputState.UpdateToSendType;

public class OutputStateImplTest extends AbstractTest {

    /**
     * Test written while looking for a bug, tests a case when a new route was suppressed
     * because of MRAI but the update was withdrawn (situation has not changed globally).  
     */
    public void testFromLife() {
        OutputStateImpl state = getState();
        NeighborMock n1 = new NeighborMock(getAS(1));
        
        Prefix prefix = getPrefix(1);
        Route route1 = createRoute(1);
        Route route2 = createRoute(2);
        state.deferred(n1, prefix, route1);
        
        assertEquals(UpdateToSendType.NONE, state.getUpdateType(n1, prefix, route1, route2));
    }

    public void testHasRegisteredPrefixes() {
        OutputStateImpl state = getState();
        NeighborMock n1 = new NeighborMock(getAS(1));
        NeighborMock n2 = new NeighborMock(getAS(2));

        assertFalse(state.hasRegisteredPrefixes(n1));
        assertFalse(state.hasRegisteredPrefixes(n2));
        state.registerPrefixes(n1, getPrefixList(1, 2, 3));
        assertTrue(state.hasRegisteredPrefixes(n1));
        state.deregisterPrefixes(n1, getPrefixList(1, 2, 3));
        assertFalse(state.hasRegisteredPrefixes(n1));
        state.registerPrefixes(n1, getPrefixList(5, 6, 8));
        assertTrue(state.hasRegisteredPrefixes(n1));

        assertFalse(state.hasRegisteredPrefixes(n2));

        state.deregisterPrefixes(n1, getPrefixList(6, 8, 9));
        assertTrue(state.hasRegisteredPrefixes(n1));
        state.deregisterPrefixes(n1, getPrefixList(11));
        assertTrue(state.hasRegisteredPrefixes(n1));
        state.deregisterPrefixes(n1, getPrefixList(5));
        assertFalse(state.hasRegisteredPrefixes(n1));
    }

    public void testDeregisterPrefixes() {
        OutputStateImpl state = getState();
        NeighborMock n1 = new NeighborMock(getAS(2));
        NeighborMock n2 = new NeighborMock(getAS(3));

        // deregister for unknonw guy - no error expected
        state.deregisterPrefixes(n1, getPrefixList(1, 2, 3, 4));

        assertEquals(0, state.filteredPrefixes.size());
        List<Prefix> prefixList1 = getPrefixList(1, 2, 3, 4);
        List<Prefix> prefixList2 = getPrefixList(101, 103, 105);

        // register
        state.filteredPrefixes.put(n1, prefixList1);
        state.filteredPrefixes.put(n2, prefixList2);

        // remove all at once for n1
        state.deregisterPrefixes(n1, prefixList1);
        assertEmpty("", state.filteredPrefixes.get(n1));

        // remove partially for n2
        state.deregisterPrefixes(n2, getPrefixList(101, 105));
        assertEquals(getPrefixList(103), state.filteredPrefixes.get(n2));
        state.deregisterPrefixes(n2, getPrefixList(103));
        assertEmpty("", state.filteredPrefixes.get(n2));

    }

    /**
     * For each prefix that is registered, answer should always be NONE
     */
    public void testNONEForRegistered() {
        OutputStateImpl state = getState();
        NeighborMock n1 = new NeighborMock(getAS(1));
        NeighborMock n2 = new NeighborMock(getAS(2));

        List<Prefix> prefixList1 = getPrefixList(1, 2, 3, 4);
        state.registerPrefixes(n1, prefixList1);
        n1.setValid(false);

        // sanity check for a different guy
        assertEquals(UpdateToSendType.ANNOUNCE, state.getUpdateType(n2, getPrefix(1), createRoute(3, 4), createRoute(5, 6)));
        assertEquals(UpdateToSendType.WITHDRAWAL, state.getUpdateType(n2, getPrefix(1), null, createRoute(5, 6)));

        // nothing for our guy
        assertEquals(UpdateToSendType.NONE, state.getUpdateType(n1, getPrefix(1), createRoute(3, 4), createRoute(5, 6)));
        assertEquals(UpdateToSendType.NONE, state.getUpdateType(n1, getPrefix(1), null, createRoute(5, 6)));

        // but cool for not registered prefixes
        assertEquals(UpdateToSendType.ANNOUNCE, state.getUpdateType(n1, getPrefix(11), createRoute(3, 4), createRoute(5, 6)));
        assertEquals(UpdateToSendType.WITHDRAWAL, state.getUpdateType(n1, getPrefix(11), null, createRoute(5, 6)));

        state.filteredPrefixes.clear();
        n1.setValid(true);
        // and OK if we remove him from the dictionary
        assertEquals(UpdateToSendType.ANNOUNCE, state.getUpdateType(n1, getPrefix(1), createRoute(3, 4), createRoute(5, 6)));
        assertEquals(UpdateToSendType.WITHDRAWAL, state.getUpdateType(n1, getPrefix(1), null, createRoute(5, 6)));
    }

    /**
     * If a prefix get registered (invalidated for a particular neighbor)
     * everything saved for this prefix should get discarded
     */
    public void testRemoveSavedStuff() {
        OutputStateImpl state = getState();
        NeighborMock n1 = new NeighborMock(getAS(1));
        NeighborMock n2 = new NeighborMock(getAS(2));

        state.deferred(n1, getPrefix(1), createRoute(1, 2, 3));
        state.deferred(n2, getPrefix(2), createRoute(2, 3, 4));
        state.deferred(n1, getPrefix(9), createRoute(2, 3, 4));

        // check that it is saved
        assertTrue(state.map.containsKey(new Pair<Neighbor, Prefix>(n1, getPrefix(1))));
        assertTrue(state.map.containsKey(new Pair<Neighbor, Prefix>(n1, getPrefix(9))));
        assertTrue(state.map.containsKey(new Pair<Neighbor, Prefix>(n2, getPrefix(2))));
        assertEquals(3, state.map.size());

        // deregister 1 2 3 and 4
        List<Prefix> prefixList1 = getPrefixList(1, 2, 3, 4);
        state.registerPrefixes(n1, prefixList1);
        n1.setValid(false);

        // and now check that stuff for n1 (for prefix 1) was forgotten while
        // for the other wasn't
        assertFalse(state.map.containsKey(new Pair<Neighbor, Prefix>(n1, getPrefix(1))));
        assertTrue(state.map.containsKey(new Pair<Neighbor, Prefix>(n2, getPrefix(2))));
        assertTrue(state.map.containsKey(new Pair<Neighbor, Prefix>(n1, getPrefix(9))));
        assertEquals(2, state.map.size());
    }

    /**
     * Tests that prefix get correctly registered (and deregistered) in the map
     */
    public void testRegisterPrefixes() {
        OutputStateImpl state = getState();
        NeighborMock n1 = new NeighborMock(getAS(2));

        assertEquals(0, state.filteredPrefixes.size());
        List<Prefix> prefixList1 = getPrefixList(1, 2, 3, 4);
        List<Prefix> prefixList2 = getPrefixList(101, 103);
        state.registerPrefixes(n1, prefixList1);
        assertEquals(1, state.filteredPrefixes.size());

        // we want this list to be added, not copied;
        assertNotSame(prefixList1, state.filteredPrefixes.get(n1));
        assertEquals(prefixList1, state.filteredPrefixes.get(n1));

        List<Prefix> sum = new ArrayList<Prefix>(prefixList1);
        sum.addAll(prefixList2);

        state.registerPrefixes(n1, prefixList2);
        assertEquals(1, state.filteredPrefixes.size());
        assertEquals(sum, state.filteredPrefixes.get(n1));

    }

    public void testWasAdvertised() {
        OutputStateImpl state = getState();

        PolicyMock policy = new PolicyMock();
        state.setPolicy(policy);
        policy.useAnswer = true;

        // check that the answer is the same as what policy says
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            boolean result = random.nextBoolean();
            policy.answer = result;
            assertEquals(result, state.wasAdvertised(null, null, createRoute(1, 2, 3)));
        }

        // check that no matter what the answer is, it is always true for route
        // == null
        Route route = null;
        for (int i = 0; i < 100; i++) {
            boolean result = random.nextBoolean();
            policy.answer = result;
            assertEquals(true, state.wasAdvertised(null, null, route));
        }

    }

    /**
     * If we want to set a prefix to the same thing that was saved, simply
     * remove it
     */
    public void testTheSame() {
        // first by hand
        OutputStateImpl state = getState();
        state.setPolicy(new PolicyMock(true));
        state.setAsIdentifier(getAS(0));
        Neighbor n1 = new NeighborMock(getAS(123));
        Prefix prefix = getPrefix(12);
        Route r1 = createRoute(1, 2, 3);
        assertEquals(UpdateToSendType.NONE, state.getUpdateType(n1, prefix, r1, r1));
        // it should matter if lastRoute is different
        assertEquals(UpdateToSendType.ANNOUNCE, state.getUpdateType(n1, prefix, r1, createRoute(5, 6, 7)));

        // and now be deferring first
        state.deferred(n1, prefix, r1);
        assertEquals(UpdateToSendType.NONE, state.getUpdateType(n1, prefix, r1, r1));
        // if should not matter what second route we have set (because of
        // deferred)
        assertEquals(UpdateToSendType.NONE, state.getUpdateType(n1, prefix, r1, createRoute(5, 6, 7)));

    }

    public void testLastSentRoute1() {
        OutputStateImpl state = getState();
        PolicyMock policy = new PolicyMock();
        state.setPolicy(policy);
        policy.useAnswer = true;
        Prefix prefix = getPrefix(123);

        Neighbor n = new NeighborMock(getAS(12));
        Route route1 = createRoute(1, 2, 3);
        /*        PrefixInfo pi = cache.getPrefixInfo(prefix);
                PrefixTableEntry currentEntry = new PrefixTableEntry(route1);
                pi.setCurrentEntry(currentEntry);
                pi.getNeighborsMap().put(n.getASIdentifier(), currentEntry);*/

        // nothing saved - return from cache
        assertEquals(route1, state.getLastSentRoute(n, prefix, route1));

        // is not advertisable - we want a null
        policy.answer = false;
        assertNull(state.getLastSentRoute(n, prefix, route1));
        policy.answer = true;

        // we should get null for a non-existent prefix
        assertNull(state.getLastSentRoute(n, getPrefix(194), null));

        /*
         * 1. save info
         * 2. change in cache
         * 3. check that we receive the saved
         * 4. remove the sent
         * 5. check that we receive from cache
         */
        assertEquals(route1, state.getLastSentRoute(n, prefix, route1));
        // 1
        state.deferred(n, prefix, route1);
        assertEquals(route1, state.getLastSentRoute(n, prefix, route1));

        // 2
        Route route2 = createRoute(3, 4, 5);

        // 3
        assertEquals(route1, state.getLastSentRoute(n, prefix, route2));

        // 4
        state.sent(n, prefix);

        // 5
        assertEquals(route2, state.getLastSentRoute(n, prefix, route2));

    }

    /**
     * Tests that deferred things actually get stored
     */
    public void testDeferredAndSent() {
        OutputStateImpl state = getState();
        Neighbor n = new NeighborMock(getAS(12));
        Prefix prefix = getPrefix(123);
        assertFalse(state.map.containsKey(new Pair<Neighbor, Prefix>(n, prefix)));
        assertNull(state.map.get(new Pair<Neighbor, Prefix>(n, prefix)));

        Route route = createRoute(124, 56, 3);
        state.deferred(n, prefix, route);
        assertEquals(route, state.map.get(new Pair<Neighbor, Prefix>(n, prefix)));

        // second route does not matter
        state.deferred(n, prefix, createRoute(1, 2, 3));
        assertEquals(route, state.map.get(new Pair<Neighbor, Prefix>(n, prefix)));

        // check that we save nulls
        Prefix prefix2 = getPrefix(124);
        route = null;
        state.deferred(n, prefix2, route);
        assertTrue(state.map.containsKey(new Pair<Neighbor, Prefix>(n, prefix2)));
        assertNull(state.map.get(new Pair<Neighbor, Prefix>(n, prefix2)));

        // if sent, the info is removed
        state.sent(n, prefix);
        assertFalse(state.map.containsKey(new Pair<Neighbor, Prefix>(n, prefix)));
        assertNull(state.map.get(new Pair<Neighbor, Prefix>(n, prefix)));
    }

    /**
     * @see OutputStateImpl#getUpdateTypeInternal(Neighbor, Prefix, Route,
     *      Route)
     */
    public void testGetUpdateType() {
        OutputStateImpl state = getState();
        PolicyMock policy = new PolicyMock();
        state.setPolicy(policy);
        policy.useAnswer = true;

        Neighbor neighbor = new NeighborMock(getAS(123));

        UpdateToSendType none = OutputState.UpdateToSendType.NONE;
        UpdateToSendType announce = OutputState.UpdateToSendType.ANNOUNCE;
        UpdateToSendType withdrawal = OutputState.UpdateToSendType.WITHDRAWAL;

        /* notation:
         * lastRoute ; route ; result
         */

        // null ; null ; NONE
        assertEquals(none, state.getUpdateTypeInternal(neighbor, getPrefix(123), null, null));

        // !null ; null ; WITHDRAWAL
        assertEquals(withdrawal, state.getUpdateTypeInternal(neighbor, getPrefix(123), null, createRoute(1, 2, 3)));

        // * ; good ; ANNOUNCE
        assertEquals(announce, state.getUpdateTypeInternal(neighbor, getPrefix(123), createRoute(2, 3, 4), createRoute(1, 2, 3)));
        assertEquals(announce, state.getUpdateTypeInternal(neighbor, getPrefix(123), createRoute(2, 3, 4), null));

        // null ; bad ; NONE
        policy.answer = false;
        assertEquals(none, state.getUpdateTypeInternal(neighbor, getPrefix(123), createRoute(1, 2, 3), null));

        // !null ; bad ; WITHDRAWAL
        assertEquals(withdrawal, state.getUpdateTypeInternal(neighbor, getPrefix(123), createRoute(9, 10), createRoute(2, 5)));

    }

    private OutputStateImpl getState() {
        OutputStateImpl state = new OutputStateImpl();
        state.setPolicy(new PolicyImpl());
        state.setAsIdentifier(getAS(0));
        return state;
    }

}

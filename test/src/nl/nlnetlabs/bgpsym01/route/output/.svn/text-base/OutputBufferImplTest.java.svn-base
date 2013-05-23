package nl.nlnetlabs.bgpsym01.route.output;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.OutputAddEntity;
import nl.nlnetlabs.bgpsym01.primitives.OutputEntity;
import nl.nlnetlabs.bgpsym01.primitives.OutputRemoveEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.mocks.MRAITimerMock;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;
import nl.nlnetlabs.bgpsym01.route.MRAITimer;
import nl.nlnetlabs.bgpsym01.route.MRAITimerImpl;
import nl.nlnetlabs.bgpsym01.route.Policy;
import nl.nlnetlabs.bgpsym01.route.PolicyImpl;

import org.apache.log4j.Logger;

/**
 * Tests whether {@link OutputBufferImpl} behaves correctly
 */
public class OutputBufferImplTest extends AbstractTest {

    private static Logger log = Logger.getLogger(OutputBufferImplTest.class);

    private OutputBufferImpl outputBuffer;
    ASIdentifier as0;
    ASIdentifier as1;
    ASIdentifier as2;
    NeighborMock n1;
    NeighborMock n2;
    NeighborMock n3;
    NeighborMock n4;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateASes(1000);
        Prefix.init(1000);
        as0 = ASFactory.getInstance(0);
        as1 = ASFactory.getInstance(1);
        as2 = ASFactory.getInstance(2);
    }

    /**
     * Tests whether things get added to MRAI store
     */
    public void testMRAIStore() {

        MRAIStoreMock store = new MRAIStoreMock();
        loadMocks(0, new PolicyImpl());
        outputBuffer.setMraiStore(store);
        MRAITimerImpl timer = new MRAITimerImpl();
        n1.setTimer(timer);

        outputBuffer.add(new OutputAddEntity(getPrefix(34), createRoute(2, 3, 4), null));
        outputBuffer.flush();

        // nothing should be added to MRAI timer
        assertEquals(0, store.list.size());

        outputBuffer.add(new OutputAddEntity(getPrefix(35), createRoute(2, 3, 4), null));
        outputBuffer.flush();

        // only info for n1 should be added (as he is the only one not on the as
        // path
        assertEquals(1, store.list.size());
        Pair<ASIdentifier, MRAITimer> pair = store.list.get(0);
        assertEquals(n1.getASIdentifier(), pair.key);
        assertSame(timer, pair.value);
    }

    /**
     * See {@link OutputStateImplTest#testFromLife()}
     * 
     * @throws Exception
     */
    public void testFromLife() throws Exception {
        loadMocks(2, new PolicyImpl());
        outputBuffer.flush();
        // OutputStateImpl state = getState(getAS(2),
        // outputBuffer.getNeighbors(), outputBuffer.getPolicy());
        MRAITimerMock timer = new MRAITimerMock();
        n1.setTimer(timer);
        timer.canSendNow = true;

        // we are interested only in n1
        Prefix prefix = getPrefix(101);
        Route route1 = createRoute(101);
        Route route2 = createRoute(102);
        outputBuffer.add(new OutputAddEntity(prefix, route1, null));
        outputBuffer.flush();
        ArrayList<BGPUpdate> updates = n1.getUpdates();
        assertEquals(1, updates.size());

        timer.canSendNow = false;
        outputBuffer.add(new OutputAddEntity(prefix, route2, route1));
        outputBuffer.flush();

        outputBuffer.add(new OutputAddEntity(prefix, route1, route2));
        outputBuffer.flush();

        timer.isTicking = false;
        timer.canSendNow = true;
        outputBuffer.flush(n1.getASIdentifier());
        assertEquals(1, updates.size());

    }

    /**
     * withdrawals should not be sent to guys we wouldn't send prefix to
     */
    public void testWithdrawalNoAnnounceWithMockPolicy() {
        loadMocks(2, new PolicyMock());
        outputBuffer.flush();

        Route route1 = createRoute(1, 2, 3, 4, 5);

        OutputAddEntity o1 = new OutputAddEntity(getPrefix(1), route1, null);
        OutputAddEntity o2 = new OutputAddEntity(getPrefix(3), route1, null);

        outputBuffer.add(o1);
        outputBuffer.add(o2);
        outputBuffer.flush();

        // we should have propagated to 3 but not to 1 or 2

        ArrayList<BGPUpdate> updates = n1.getUpdates();
        assertEquals(updates.size(), 0);

        updates = n3.getUpdates();
        assertEquals(updates.size(), 1);
        assertEquals(updates.get(0).getPrefixes().size(), 2);
        clearUpdates();

        OutputRemoveEntity e1 = new OutputRemoveEntity(getPrefix(1), route1);
        outputBuffer.add(e1);
        outputBuffer.flush();

        // we should have sent an withdrawal to 3 and not to 1
        updates = n3.getUpdates();
        assertEquals(1, updates.size());
        // nothings announced, one thing is withdrawn
        assertNull(updates.get(0).getPrefixes());
        assertEquals(1, updates.get(0).getWithdrawals().size());

        updates = n1.getUpdates();
        // he did not receive the prefix so he should not receive the withdrawal
        assertEquals(0, updates.size());

        // nothing for ourselves
        updates = n2.getUpdates();
        // he did not receive the prefix so he should not receive the withdrawal
        assertEquals(0, updates.size());

        // and everything for n4
        // nothing for ourselves
        updates = n4.getUpdates();
        // he did not receive the prefix so he should not receive the withdrawal
        assertEquals(1, updates.size());
        assertNull(updates.get(0).getPrefixes());
        assertEquals(1, updates.get(0).getWithdrawals().size());

    }

    /**
     * are the messages aggregated well?
     */
    public void testArrayAnnounce() {
        loadMocks(0, new PolicyImpl());

        // empty flush always has to be correct!
        outputBuffer.flush();
        ArrayList<BGPUpdate> updates = n2.getUpdates();
        assertEquals(updates.size(), 0);

        // announce prefixes
        Route route1 = createRoute(1, 2, 3, 4, 5);
        Route route2 = createRoute(3, 4, 5, 6);
        OutputAddEntity o1 = new OutputAddEntity(getPrefix(1), route1, null);
        OutputAddEntity o2 = new OutputAddEntity(getPrefix(2), route1, null);
        OutputAddEntity o3 = new OutputAddEntity(getPrefix(3), route1, null);
        OutputAddEntity o4 = new OutputAddEntity(getPrefix(4), route1, null);
        OutputAddEntity o5 = new OutputAddEntity(getPrefix(5), route1, null);

        OutputAddEntity o6 = new OutputAddEntity(getPrefix(7), route2, null);
        OutputAddEntity o7 = new OutputAddEntity(getPrefix(8), route2, null);

        // send 5 prefixes that should get together
        outputBuffer.add(o1);
        outputBuffer.add(o2);
        outputBuffer.add(o3);
        outputBuffer.add(o4);
        outputBuffer.add(o5);
        outputBuffer.flush();

        updates = n2.getUpdates();
        assertEquals(updates.size(), 1);
        assertEquals(updates.get(0).getPrefixes().size(), 5);
        clearUpdates();

        // send 5 prefixes but flush after two and then after all
        outputBuffer.add(o1);
        outputBuffer.add(o2);
        outputBuffer.flush();
        outputBuffer.add(o3);
        outputBuffer.add(o4);
        outputBuffer.add(o5);
        outputBuffer.flush();

        updates = n1.getUpdates();
        assertEquals(updates.size(), 2);
        assertEquals(updates.get(0).getPrefixes().size(), 2);
        assertEquals(updates.get(1).getPrefixes().size(), 3);
        clearUpdates();

        // send 7 prefixes: 5 the same and 2 different. Should be sent
        // separately.
        outputBuffer.add(o1);
        outputBuffer.add(o2);
        outputBuffer.add(o6);
        outputBuffer.add(o3);
        outputBuffer.add(o7);
        outputBuffer.add(o4);
        outputBuffer.add(o5);
        outputBuffer.flush();

        updates = n1.getUpdates();
        assertEquals(updates.size(), 2);
        LinkedList<Integer> awaited = new LinkedList<Integer>();
        awaited.add(2);
        awaited.add(5);

        BGPUpdate u1 = updates.get(0);
        BGPUpdate u2 = updates.get(1);

        assertTrue(awaited.remove((Integer) u1.getPrefixes().size()));
        assertTrue(awaited.remove((Integer) u2.getPrefixes().size()));

        BGPUpdate test1 = u1.getPrefixes().size() == 5 ? u1 : u2;
        BGPUpdate test2 = u1.getPrefixes().size() == 5 ? u2 : u1;
        assertEquals(test1.getPrefixes(), getPrefixList(1, 2, 3, 4, 5));
        assertEquals(test2.getPrefixes(), getPrefixList(7, 8));

        clearUpdates();
    }

    /**
     * are the messages aggregated well?
     */
    public void testArrayWithdrawal() {
        loadMocks(0, new PolicyImpl());
        Route route = createRoute(19, 21, 23, 56);
        OutputRemoveEntity o1 = new OutputRemoveEntity(getPrefix(0), route);
        OutputRemoveEntity o2 = new OutputRemoveEntity(getPrefix(1), route);
        OutputRemoveEntity o3 = new OutputRemoveEntity(getPrefix(2), route);
        OutputRemoveEntity o4 = new OutputRemoveEntity(getPrefix(3), route);

        outputBuffer.add(o1);
        outputBuffer.add(o2);
        outputBuffer.add(o3);
        outputBuffer.add(o4);
        outputBuffer.flush();

        ArrayList<BGPUpdate> updates = n2.getUpdates();
        assertEquals(1, updates.size());
        assertEquals(getPrefixList(0, 1, 2, 3), updates.get(0).getWithdrawals());
        clearUpdates();
    }

    private void clearUpdates() {
        n1.setUpdates(new ArrayList<BGPUpdate>());
        n2.setUpdates(new ArrayList<BGPUpdate>());
        n3.setUpdates(new ArrayList<BGPUpdate>());
        n4.setUpdates(new ArrayList<BGPUpdate>());
    }

    private void loadMocks(int asInternalId, Policy policy) {
        OutputBufferImpl buffer = MockedOutputBufferFactory.getInstance(asInternalId, policy, null, 1, 2, 3, 4);
        Neighbors neighbors = buffer.getNeighbors();
        n1 = (NeighborMock) neighbors.getNeighbor(getAS(1));
        n2 = (NeighborMock) neighbors.getNeighbor(getAS(2));
        n3 = (NeighborMock) neighbors.getNeighbor(getAS(3));
        n4 = (NeighborMock) neighbors.getNeighbor(getAS(4));
        outputBuffer = buffer;
    }

    /**
     * This test covers a special case:<br>
     * - I'm AS100 and AS101 is my neighbor<br>
     * - I have a route to AS1 through AS90 (default) but also through AS101<br>
     * - I receive withdrawal from AS90 and install AS101 (to AS1)<br>
     * - I have to send a withdrawal to AS101 so that he knows that he cannot
     * use me (as the path through AS90 does not exist anymore)<br>
     */
    public void testWithdrawalWithGuyOnEnd() {

        // ases 90 and 101
        outputBuffer = getTwoGuysNeighbors();

        Prefix prefix = getPrefix(1);
        Route startRoute = createRoute(1, 2, 3, 4, 5, 6);

        OutputAddEntity oad = new OutputAddEntity(prefix, createRoute(1, 2, 3, 4, 5, 90), startRoute);
        outputBuffer.add(oad);
        outputBuffer.flush();

        ArrayList<BGPUpdate> updates1 = n1.getUpdates();
        ArrayList<BGPUpdate> updates2 = n2.getUpdates();
        assertEquals("SB one update!", 1, updates2.size());

        // one announce for AS101
        assertEquals("SB one announce", 1, updates2.get(0).getPrefixes().size());
        assertEmpty("SB no withdrawal", updates2.get(0).getWithdrawals());

        // one withdrawal for AS101
        assertEquals("SB one update!", 1, updates1.size());
        assertEmpty("SB no announces", updates1.get(0).getPrefixes());
        assertEquals("SB one withdrawal", 1, updates1.get(0).getWithdrawals().size());

        // OutputRemoveEntity ore1 = new OutputRemoveEntity(getPrefix(1),
        // createRoute(1, 2, 3, 4, 90));

        /*        PrefixStoreMapImpl store = getStore();
                store.setAsIdentifier(getAS(100));

                Route route1 = createRoute(1, 2, 3, 4, 90);
                store.prefixReceived(getAS(90), getPrefixList(1), route1);
                Route route2 = createRoute(1, 2, 3, 4, 5, 6, 7, 100);
                store.prefixReceived(getAS(100), getPrefixList(1), route2);

                // check that the store is in correct state
                assertEquals("SB info about 1 prefix", 1, cache.size());
                assertEquals("SB route1 is deafult", gp(1).getCurrentEntry().getRoute(), route1);

                assertEquals("SB no with", 0, outputBuffer.getWith);
                assertEquals("SB 2 with", 0, outputBuffer.getAdded());*/
    }

    private OutputState getState(ASIdentifier as, Neighbors neighbors, Policy policy) {
        OutputStateImpl state = new OutputStateImpl();
        state.setAsIdentifier(as);
        state.setPolicy(policy);
        state.setNeighbors(neighbors);
        return state;
    }

    /**
     * Tests whether if we cannot send a route to a neighbor, a withdrawal is
     * being sent.
     */
    public void testNewRouteNotAdvertisable() {
        /*
         * 1. add 1 route - should be propagated
         * 2. add 2 route that cannot be propagated (according to policy...)
         * 3. he should receive a withdrawal
         * 
         */
        // my neighbors are 90 and 101
        outputBuffer = getTwoGuysNeighbors();

        Route previousRoute = createRoute(1, 2, 3, 4, 5);
        OutputAddEntity oad = new OutputAddEntity(getPrefix(1), previousRoute, null);
        outputBuffer.add(oad);
        outputBuffer.flush();

        ArrayList<BGPUpdate> updates1 = n1.getUpdates();
        assertEquals("SB one update!", 1, updates1.size());
        BGPUpdate update = n1.getUpdates().get(0);
        assertEquals("SB on announce", 1, update.getPrefixes().size());
        assertEmpty("SB no withdrawals", update.getWithdrawals());

        oad = new OutputAddEntity(getPrefix(1), createRoute(1, 2, 3, 4, 5, 90), previousRoute);

        outputBuffer.add(oad);
        outputBuffer.flush();

        updates1 = n1.getUpdates();
        assertEquals("SB one update!", 2, updates1.size());
        update = n1.getUpdates().get(1);
        assertEmpty(update.toString(), update.getPrefixes());
        assertEquals(update.toString(), 1, update.getWithdrawals().size());

    }

    /**
     * Tests whether {@link OutputEntity} is copied when setOldRoute is set
     */
    public void testEntityCopy() {
        if (log.isInfoEnabled()) {
            log.info("---------entityCopyStart---------");
        }
        outputBuffer = getTwoGuysNeighbors();

        MRAITimerMock timerMock1 = new MRAITimerMock();
        MRAITimerMock timerMock2 = new MRAITimerMock();
        n1.setTimer(timerMock1);
        n2.setTimer(timerMock2);

        outputBuffer.setMraiStore(new MRAIStoreMock());

        // n1 asId is 90
        // n2 asId is 101

        // this route is wrong for n1, so it will not be propagated
        Route r1 = createRoute(1, 2, 3, 4, 5, n1.getASIdentifier().getInternalId());
        Prefix prefix = getPrefix(1);

        outputBuffer.add(new OutputAddEntity(prefix, r1, null));
        outputBuffer.flush();

        ArrayList<BGPUpdate> updates1 = n1.getUpdates();
        ArrayList<BGPUpdate> updates2 = n2.getUpdates();
        assertEquals(0, updates1.size());
        assertEquals(1, updates2.size());

        // this route is OK for n1, so it will be propagated
        timerMock2.canSendNow = false;
        Route r2 = createRoute(1, 2, 3, 4, 6);

        // only n2 has r1
        outputBuffer.add(new OutputAddEntity(prefix, r2, r1));
        outputBuffer.flush();
        assertEquals(1, updates1.size());
        assertEquals(1, updates2.size());

        // this route is not ok for n1
        timerMock1.canSendNow = false;
        Route r3 = createRoute(1, 2, 3, 4, 5, 7, n1.getASIdentifier().getInternalId());
        outputBuffer.add(new OutputAddEntity(prefix, r3, r2));
        outputBuffer.flush();
        assertEquals(1, updates1.size());
        assertEquals(1, updates2.size());

        timerMock1.canSendNow = true;
        timerMock2.canSendNow = true;

        outputBuffer.flush(n1.getASIdentifier());
        // there should be a withdrawal (as n1 thinks that r2 is current route)
        assertEquals(2, updates1.size());

    }

    /**
     * Tests whether one announce and one withdrawal compute to zero (if mrai is
     * still on)
     */
    public void testAddAndWithdraw() {
        if (log.isInfoEnabled()) {
            log.info("---------addAndWithdrawStart---------");
        }
        outputBuffer = getTwoGuysNeighbors();

        MRAITimerMock timerMock1 = new MRAITimerMock();
        n1.setTimer(timerMock1);

        outputBuffer.setMraiStore(new MRAIStoreMock());

        // n1 asId is 90
        // n2 asId is 101

        timerMock1.canSendNow = false;

        // this route is wrong for n1, so it will not be propagated
        Route r1 = createRoute(1, 2, 3, 4, 5);
        Prefix prefix = getPrefix(1);
        outputBuffer.add(new OutputAddEntity(prefix, r1, null));
        outputBuffer.flush();

        outputBuffer.add(new OutputRemoveEntity(prefix, r1));
        outputBuffer.flush();

        timerMock1.canSendNow = true;
        outputBuffer.flush(n1.getASIdentifier());
    }

    private OutputBufferImpl getTwoGuysNeighbors() {
        OutputBufferImpl bufferImpl = MockedOutputBufferFactory.getInstance(100, null, null, 90, 101);
        Neighbors neighbors = bufferImpl.getNeighbors();
        n1 = (NeighborMock) neighbors.getNeighbor(getAS(90));
        n2 = (NeighborMock) neighbors.getNeighbor(getAS(101));

        OutputState outputState = getState(getAS(100), neighbors, new PolicyImpl());
        bufferImpl.setOutputState(outputState);

        return bufferImpl;
    }

    /**
     * See comment in
     * {@link OutputBufferImpl#validate(nl.nlnetlabs.bgpsym01.neighbor.Neighbor, java.util.List)}
     */
    public void testValidate() {

        /* we test just addAnouncements, as the only thing, the validate function does
         * is to flush
         */

        OutputBufferImpl buffer = getTwoGuysNeighbors();
        OutputBufferStore store = buffer.getBufferStore();

        List<Pair<Prefix, Route>> prefixes = new ArrayList<Pair<Prefix, Route>>();

        prefixes.add(new Pair<Prefix, Route>(getPrefix(1), null));
        Route route = createRoute(2, 3, 4);
        Prefix prefix = getPrefix(2);
        prefixes.add(new Pair<Prefix, Route>(prefix, route));

        buffer.addAnnouncements(n1, prefixes);
        // there shouldn't be any updates generated
        ArrayList<BGPUpdate> updates = n1.getUpdates();
        assertEquals(0, updates.size());

        // second should be added to the store
        assertTrue(store.announcementsIterator(n1).hasNext());
        OutputAddEntity entity = store.announcementsIterator(n1).next();
        assertEquals(prefix, entity.getPrefix());
        assertEquals(route, entity.getRoute());

        // //////////////////////
        // check that if something was in the buffer before, it has gotten
        // deleted
        store.addAnnouncement(n2, new OutputAddEntity(prefix, route, null));
        assertTrue(store.announcementsIterator(n2).hasNext());

        prefixes.clear();
        prefixes.add(new Pair<Prefix, Route>(prefix, null));
        buffer.addAnnouncements(n2, prefixes);

        assertFalse(store.announcementsIterator(n2).hasNext());
    }

    /**
     * See comment in {@link
     * OutputBufferImpl#invalidate(nl.nlnetlabs.bgpsym01.neighbor.Neighbor,
     * List<Prefix>)}
     */
    public void testInvalidate() {
        OutputBufferImpl buffer = getTwoGuysNeighbors();

        OutputBufferStore store = buffer.getBufferStore();

        MRAITimerMock timer = new MRAITimerMock();
        timer.canSendNow = false;
        n1.setTimer(timer);
        ArrayList<BGPUpdate> updates = n1.getUpdates();

        buffer.add(new OutputAddEntity(getPrefix(1), createRoute(5, 6), createRoute(9, 10)));
        buffer.add(new OutputAddEntity(getPrefix(5), createRoute(5, 6), createRoute(9, 10)));
        buffer.flush();

        // no updates sent up to now
        assertEquals(0, updates.size());

        // check that it has been added to the store
        assertTrue(store.announcementsIterator(n1).hasNext());

        buffer.invalidate(n1, getPrefixList(1));

        // withdrawal sent
        assertEquals(1, updates.size());
        assertEquals(updates.get(0).getWithdrawals(), getPrefixList(1));
        assertEmpty("", updates.get(0).getPrefixes());

        // flush normally, only prefix 5 should be sent (one is blocked because
        // of invalidation)
        timer.canSendNow = true;
        buffer.flush(n1.getASIdentifier());

        // update sent
        assertEquals(2, updates.size());
        assertEquals(updates.get(1).getPrefixes(), getPrefixList(5));
        assertEmpty("", updates.get(1).getWithdrawals());

    }

}

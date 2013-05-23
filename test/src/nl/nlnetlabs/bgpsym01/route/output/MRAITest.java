package nl.nlnetlabs.bgpsym01.route.output;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.OutputAddEntity;
import nl.nlnetlabs.bgpsym01.primitives.OutputRemoveEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.route.MRAITimerImpl;
import nl.nlnetlabs.bgpsym01.route.MockedPrefixStoreFactory;
import nl.nlnetlabs.bgpsym01.route.Policy;
import nl.nlnetlabs.bgpsym01.route.PolicyImpl;
import nl.nlnetlabs.bgpsym01.route.PrefixStore;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreMapImpl;

import org.apache.log4j.Logger;

/*
 * lacking tests:
 *  - MRAI self-expire
 */

public class MRAITest extends AbstractTest {

    private static Logger log = Logger.getLogger(MRAITest.class);

    private OutputBuffer outputBuffer;
    ASIdentifier as0;
    ASIdentifier as1;
    ASIdentifier as2;

    NeighborMock n1;
    NeighborMock n2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateASes(1000);
        Prefix.init(1000);
        as0 = ASFactory.getInstance(0);
        as1 = ASFactory.getInstance(1);
        as2 = ASFactory.getInstance(2);
    }

    @Override
    protected void tearDown() throws Exception {
    }

    /**
     * Allows us to flush one as at a time
     */
    public void testPerAS() {
        log.info("-------------------testPerAS");
        loadMocks(0, new PolicyImpl());

        PrefixStore store = getStore();
        // should be propagated to everybody
        store.prefixReceived(getAS(1), getPrefixList(1, 2, 3), createRoute(101, 102));
        assertEquals("prefixes not propagated", 1, n1.getUpdates().size());
        assertEquals("prefixes not propagated", 1, n2.getUpdates().size());

        // shouldn't be propagated (because of the timers)
        store.prefixReceived(getAS(2), getPrefixList(5, 6, 7), createRoute(103, 104));
        assertEquals("prefixes propagated", 1, n1.getUpdates().size());
        assertEquals("prefixes propagated", 1, n2.getUpdates().size());

        n1.getTimer().reset();
        n2.getTimer().reset();

        outputBuffer.flush(getAS(2));
        // n1 should have only 1 and n2 2 updates
        assertEquals(1, n1.getUpdates().size());
        assertEquals(2, n2.getUpdates().size());
    }

    /**
     * Tests whether MRAI behaves well when things are added through a prefix
     * store
     * 
     * This is not a real unit-test as it tests interaction between two
     * different modules
     */
    public void testWithPrefixStore() {
        log.info("-------------------testWithPrefixStore");
        // we'ar AS0, our neighbors are AS1 and AS2
        loadMocks(0, new PolicyImpl());

        PrefixStoreMapImpl store = getStore();
        // should be propagated to everybody
        store.prefixReceived(getAS(1), getPrefixList(1, 2, 3), createRoute(101, 102));
        assertEquals("prefixes not propagated", 1, n1.getUpdates().size());
        assertEquals("prefixes not propagated", 1, n2.getUpdates().size());

        // shouldn't be propagated (because of the timers)
        store.prefixReceived(getAS(2), getPrefixList(5, 6, 7), createRoute(103, 104));
        assertEquals("prefixes not propagated", 1, n1.getUpdates().size());
        assertEquals("prefixes not propagated", 1, n2.getUpdates().size());

        n1.getTimer().reset();

        store.flush(n1.getASIdentifier());
        n1.getTimer().reset();
        // n1 should receive this and previous one (because the timer is reset),
        // n2 nothing
        store.prefixReceived(getAS(1), getPrefixList(9, 10, 11), createRoute(103, 105));
        assertEquals("prefixes not propagated", 3, n1.getUpdates().size());
        assertEquals("prefixes not propagated", 1, n2.getUpdates().size());

        n2.getTimer().reset();
        n1.getTimer().reset();
        outputBuffer.flush();
        outputBuffer.flush(n2.getASIdentifier());
        // old stuff for n2 should have been sent
        assertEquals(3, n1.getUpdates().size());
        assertEquals(3, n2.getUpdates().size());

        // make the timers full
        n1.getTimer().sent();
        n2.getTimer().sent();

        // send info about 3 (changing the route)
        store.prefixReceived(getAS(1), getPrefixList(3), createRoute(101, 106));
        // nothing was sent because of MRAI
        assertEquals(3, n1.getUpdates().size());
        assertEquals(3, n2.getUpdates().size());

        // send info about 3 (changing it back)
        store.prefixReceived(getAS(1), getPrefixList(3), createRoute(101, 102));
        // nothing was sent because of MRAI
        assertEquals(3, n1.getUpdates().size());
        assertEquals(3, n2.getUpdates().size());

        // reset MRAI - nothing should be sent as nothing has changed globally
        n1.getTimer().reset();
        n2.getTimer().reset();
        outputBuffer.flush();
        assertEquals(n1.getUpdates().get(n1.getUpdates().size() - 1).toString(), 3, n1.getUpdates().size());
        assertEquals(3, n2.getUpdates().size());

        n1.getTimer().sent();
        n2.getTimer().sent();
        // change info about 1, 2, 3
        store.prefixReceived(getAS(1), getPrefixList(1, 2, 3), createRoute(101, 112));
        // change 1 and 3 back
        store.prefixReceived(getAS(1), getPrefixList(1, 3), createRoute(101, 102));

        // flush and see whether only update about 2 was sent
        n1.getTimer().reset();
        n2.getTimer().reset();
        outputBuffer.flush(n1.getASIdentifier());
        outputBuffer.flush(n2.getASIdentifier());
        assertEquals(n1.getUpdates().get(n1.getUpdates().size() - 2).toString(), 4, n1.getUpdates().size());
        assertEquals(4, n2.getUpdates().size());

        BGPUpdate update = n1.getUpdates().get(3);
        assertEquals(update.getPrefixes(), getPrefixList(2));
        assertEquals(update.getRoute(), createRoute(101, 112).copyWithMeOnPath(getAS(0)));

        // enough for now :)
    }

    private PrefixStoreMapImpl getStore() {
        PrefixStoreMapImpl store = MockedPrefixStoreFactory.getStore();
        store.setOutputBuffer(outputBuffer);
        return store;

        // PrefixStoreMapImpl store = new PrefixStoreMapImpl();
        // store.setAsIdentifier(ASFactory.getInstance(0));
        // store.setTimeController(TimeControllerFactory.getTimeController());
        //
        // // TODO test callback
        // store.setCallback(CallbackMock.getInstance());
        // // outputBuffer = new OutputBufferMock();
        // store.setOutputBuffer(outputBuffer);
        // PrefixCache cache = new PrefixCacheMock();
        // store.setCache(cache);
        // store.setPolicy(new PolicyImpl());
        // return store;
    }

    /**
     * Add a prefix A, withdraw it and add it again. After first announcement is
     * propagated, withdrawal should be blocked because MRAI did not expire and
     * then should be discarded because of the later announcement.
     */
    public void testWithdrawalRepaired() {
        log.info("-------------------testWithdrawalRepaired");
        loadMocks(0, new PolicyImpl());

        outputBuffer.add(new OutputAddEntity(getPrefix(2), createRoute(1, 2, 3, 4), null));
        outputBuffer.flush();
        assertEquals("no update got propagated", 1, n1.getUpdates().size());

        outputBuffer.add(new OutputRemoveEntity(getPrefix(2), createRoute(1, 2, 3, 4)));
        for (int i = 0; i < 20; i++) {
            outputBuffer.flush(n1.getASIdentifier());
            assertEquals("withdrawal got propagated", 1, n1.getUpdates().size());
        }

        // get the same prefix back..
        outputBuffer.add(new OutputAddEntity(getPrefix(2), createRoute(1, 2, 3, 4), null));
        outputBuffer.flush(n1.getASIdentifier());
        assertEquals("something was sent...", 1, n1.getUpdates().size());

        n1.getTimer().reset();
        outputBuffer.flush(n1.getASIdentifier());
        assertEquals("withdrawal did not get cut", 1, n1.getUpdates().size());
    }

    /**
     * Add a prefix A, withdraw it and make sure withdrawal is not sent as long
     * MRAI does not expire
     */
    public void testWithdrawal() {
        log.info("-------------------testWithdrawal");
        loadMocks(0, new PolicyImpl());

        outputBuffer.add(new OutputAddEntity(getPrefix(2), createRoute(1, 2, 3, 4), null));
        outputBuffer.flush();
        assertEquals("no update got propagated", 1, n1.getUpdates().size());

        outputBuffer.add(new OutputRemoveEntity(getPrefix(2), createRoute(1, 2, 3, 4)));

        for (int i = 0; i < 20; i++) {
            outputBuffer.flush(n1.getASIdentifier());
            assertEquals("withdrawal got propagated", 1, n1.getUpdates().size());
        }

        n1.getTimer().reset();
        outputBuffer.flush();
        assertEquals("withdrawal did not get propagated", 2, n1.getUpdates().size());
    }

    public void testMRAIPerPeer() {
        log.info("-------------------testMRAIPerPeer");
        /*
         * policy is as follows:
         *  - everything newer is better
         *  - propagate to peer X if X is on path :)
         */
        Policy policy = getPolicy();
        loadMocks(0, policy);

        // will go to n1 and won't go to n2
        outputBuffer.add(new OutputAddEntity(getPrefix(2), createRoute(1, 5), null));
        outputBuffer.flush();
        assertEquals("no updates were sent", 1, n1.getUpdates().size());
        assertEquals("update was sent ", 0, n2.getUpdates().size());

        if (log.isDebugEnabled()) {
            log.debug("second part...");
        }
        // should go to both of the guys but n1 has already got MRAI timer
        // problem
        outputBuffer.add(new OutputAddEntity(getPrefix(3), createRoute(1, 2, 3, 4), null));
        outputBuffer.flush();
        assertEquals("update was sent but MRAI was on", 1, n1.getUpdates().size());
        assertEquals("update was not sent ", 1, n2.getUpdates().size());
    }

    private Policy getPolicy() {
        Policy policy = new Policy() {

            public boolean isAdvertisable(ASIdentifier myId, Neighbor neighbor, Neighbors neighbors, Route route) {
                boolean outcome = false;
                for (ASIdentifier asId : route.getHops()) {
                    if (neighbor.getASIdentifier().equals(asId)) {
                        outcome = true;
                        break;
                    }
                }
                log.debug("outcome=" + outcome);
                return outcome;
            }

            public boolean isBetter(ASIdentifier myAsId, Prefix prefix, Route route1, Neighbor n1, Route route2, Neighbor n2) {
                return true;
            }

        };
        return policy;
    }

    /**
     * test whether {@link #testMRAIMessageShortcut()} doesn't come into play to
     * often (everything for prefix 2) comes A - propagated comes B - suspended
     * comes C - suspended MRAI expires - C get propagated
     */
    public void testMRAIMessageNoAggregation() {
        log.info("-------------------testMRAIMessageNoAggregation");
        loadMocks(0, new PolicyImpl());

        Route route1 = createRoute(1, 2, 3, 4, 5);
        // send one update and make sure that it was in fact propagated
        OutputAddEntity o1 = new OutputAddEntity(getPrefix(2), route1, null);
        outputBuffer.add(o1);
        outputBuffer.flush();
        assertEquals("no updates were sent", 1, n1.getUpdates().size());

        // send second one and ensure it was not propagated
        Route route2 = createRoute(2, 3, 4, 5);
        OutputAddEntity o2 = new OutputAddEntity(getPrefix(2), route2, route1);
        outputBuffer.add(o2);
        outputBuffer.flush();
        assertEquals("no new should have been sent (MRAI)", 1, n1.getUpdates().size());

        // send third one so that the route becomes what it was at the beginning
        Route route3 = createRoute(1, 2, 3, 4, 5, 6);
        OutputAddEntity o3 = new OutputAddEntity(getPrefix(2), route3, route2);
        outputBuffer.add(o3);
        outputBuffer.flush();
        assertEquals("no new should have been sent (MRAI)", 1, n1.getUpdates().size());

        /*
         * reset the MRAI and flush everything - nothing should be sent because the state has not changed
         */

        n1.getTimer().reset();
        outputBuffer.flush(n1.getASIdentifier());
        log.info("updates: " + n1.getUpdates().toString());
        assertEquals("new route didn't get propagated", 2, n1.getUpdates().size());
        assertEquals("not appropriate route got propagated", n1.getUpdates().get(1).getRoute(), route3.copyWithMeOnPath(getAS(0)));

    }

    /**
     * flow (everything for prefix 2). comes A - propagated comes B - suspended
     * comes A - suspended MRAI expires - nothing is sent (as it's A that was
     * sent last time)
     */
    public void testMRAIMessageShortcut() {
        log.info("-------------------testMRAIMessageShortcut");
        loadMocks(0, new PolicyImpl());

        Route route1 = createRoute(1, 2, 3, 4, 5);
        // send one update and make sure that it was in fact propagated
        OutputAddEntity o1 = new OutputAddEntity(getPrefix(2), route1, null);
        outputBuffer.add(o1);
        outputBuffer.flush();
        assertEquals("no updates were sent", 1, n1.getUpdates().size());

        // send second one and ensure it was not propagated
        OutputAddEntity o2 = new OutputAddEntity(getPrefix(2), createRoute(2, 3, 4, 5), route1);
        outputBuffer.add(o2);
        outputBuffer.flush();
        assertEquals("no new should have been sent (MRAI)", 1, n1.getUpdates().size());

        // send third one so that the route becomes what it was at the beginning
        OutputAddEntity o3 = new OutputAddEntity(getPrefix(2), createRoute(1, 2, 3, 4, 5), createRoute(2, 3, 4, 5));
        outputBuffer.add(o3);
        outputBuffer.flush();
        assertEquals("no new should have been sent (MRAI)", 1, n1.getUpdates().size());

        /*
         * reset the MRAI and flush everything - nothing should be sent because the state has not changed
         */

        n1.getTimer().reset();
        outputBuffer.flush();
        log.info("updates: " + n1.getUpdates().toString());
        assertEquals("the route has not changed - nothing should've been sent", 1, n1.getUpdates().size());

    }

    /**
     * Very dumb test - send one update, send second one (causing MRAI to step
     * in) and then force MRAI to get back into work
     */
    public void testMRAI() {
        log.info("-------------testMRAI");
        loadMocks(0, new PolicyImpl());

        Route route1 = createRoute(1, 2, 3, 4, 5);

        // send one update and make sure that it was in fact propagated
        OutputAddEntity o1 = new OutputAddEntity(getPrefix(1), route1, null);
        outputBuffer.add(o1);
        outputBuffer.flush();
        assertEquals("no updates were sent", 1, n1.getUpdates().size());

        OutputAddEntity o2 = new OutputAddEntity(getPrefix(2), route1, null);
        outputBuffer.add(o2);
        outputBuffer.flush();
        assertEquals("no new should have been sent (MRAI)", 1, n1.getUpdates().size());

        // here: change the MRAI timer so that the program things it is already
        // time
        n1.getTimer().reset();
        outputBuffer.flush(n1.getASIdentifier());

        assertEquals("MRAI did not trigger new message sent", 2, n1.getUpdates().size());
    }

    private void loadMocks(int asInternalId, Policy policy) {
        OutputBufferImpl buffer = MockedOutputBufferFactory.getInstance(asInternalId, policy, null, 1, 2);
        Neighbors neighbors = buffer.getNeighbors();
        n1 = (NeighborMock) neighbors.getNeighbor(getAS(1));
        n1.setTimer(new MRAITimerImpl());
        n2 = (NeighborMock) neighbors.getNeighbor(getAS(2));
        n2.setTimer(new MRAITimerImpl());
        outputBuffer = buffer;
        this.outputBuffer = buffer;
    }

}

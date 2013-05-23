package nl.nlnetlabs.bgpsym01.route.output;

import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.OutputAddEntity;
import nl.nlnetlabs.bgpsym01.primitives.OutputRemoveEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.mocks.MRAITimerMock;

public class OutputBufferImplTest3 extends AbstractTest {

    /*
     * to write:
     *  - flush(AS)
     */

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Add few prefixes and send them. That's it.
     */
    public void testAddAndSend() {
        OutputBufferImpl buffer = MockedOutputBufferFactory.getInstance(0, null, null, 1, 2);
        OutputBufferStore bufferStore = buffer.getBufferStore();

        NeighborMock n1 = (NeighborMock) buffer.getNeighbors().getNeighbor(getAS(1));

        Route r2 = createRoute(1, 3, 4, 5);
        Route r1 = createRoute(1, 2, 3);
        bufferStore.addAnnouncement(new OutputAddEntity(getPrefix(1), r1, r2));
        bufferStore.addAnnouncement(new OutputAddEntity(getPrefix(2), r1, r2));
        bufferStore.addAnnouncement(new OutputAddEntity(getPrefix(3), r1, r2));

        buffer.flush();
        ArrayList<BGPUpdate> updates = n1.getUpdates();
        assertEquals(1, updates.size());

        BGPUpdate update = updates.get(0);

        assertEquals(r1.copyWithMeOnPath(getAS(0)), update.getRoute());
        compareCollectionWithIterator(getPrefixList(1, 2, 3), update.getPrefixes().iterator());

        assertFalse(bufferStore.announcementsIterator().hasNext());
    }

    /**
     * Tests whether withdrawals get deleted after processing
     */
    public void testDeleteWithdrawals() {
        OutputBufferImpl buffer = MockedOutputBufferFactory.getInstance(0, null, null, 1, 2);
        OutputBufferStore bufferStore = buffer.getBufferStore();

        bufferStore.addWithdrawal(new OutputRemoveEntity(getPrefix(12), createRoute(122)));
        bufferStore.addWithdrawal(new OutputRemoveEntity(getPrefix(13), createRoute(123)));

        assertTrue(bufferStore.withdrawalsIterator().hasNext());
        buffer.flush();
        assertFalse(bufferStore.withdrawalsIterator().hasNext());
    }

    /**
     * Add few prefixes and suppress them because of the timer
     */
    public void testAddButTimer() {
        OutputBufferImpl buffer = MockedOutputBufferFactory.getInstance(0, null, null, 1, 2);
        OutputBufferStore bufferStore = buffer.getBufferStore();

        NeighborMock n1 = (NeighborMock) buffer.getNeighbors().getNeighbor(getAS(1));
        ((MRAITimerMock) n1.getTimer()).canSendNow = false;
        ((MRAITimerMock) n1.getTimer()).isTicking = true;

        Route r2 = createRoute(11, 13, 14, 15);
        Route r1 = createRoute(11, 12, 13);
        OutputAddEntity oad1 = new OutputAddEntity(getPrefix(1), r1, r2);
        bufferStore.addAnnouncement(oad1);
        OutputAddEntity oad2 = new OutputAddEntity(getPrefix(2), r1, r2);
        bufferStore.addAnnouncement(oad2);
        OutputAddEntity oad3 = new OutputAddEntity(getPrefix(3), r1, r2);
        bufferStore.addAnnouncement(oad3);

        ArrayList<OutputAddEntity> entities = new ArrayList<OutputAddEntity>();
        entities.add(oad1);
        entities.add(oad2);
        entities.add(oad3);

        // nothing has got send because of the timer...
        buffer.flush();
        ArrayList<BGPUpdate> updates = n1.getUpdates();
        assertEquals(0, updates.size());

        // check that they were moved to suppressed list
        compareCollectionWithIterator(entities, bufferStore.announcementsIterator(n1));
        assertFalse(bufferStore.announcementsIterator().hasNext());

        /* now check that as long as the timer is ticking, nothing will be sent normally, even
         * if it has expired---canSendNow==true
         */
        ((MRAITimerMock) n1.getTimer()).canSendNow = true;
        ((MRAITimerMock) n1.getTimer()).isTicking = true;
        OutputAddEntity oad4 = new OutputAddEntity(getPrefix(4), createRoute(31, 51), createRoute(31, 41, 51));
        bufferStore.addAnnouncement(oad4);

        buffer.flush();
        assertEquals(0, n1.getUpdates().size());

        entities.add(oad4);
        // check that they were moved to suppressed list
        compareCollectionWithIterator(entities, bufferStore.announcementsIterator(n1));
        assertFalse(bufferStore.announcementsIterator().hasNext());

    }

    public void testFlushSuppressed() {
        OutputBufferImpl buffer = MockedOutputBufferFactory.getInstance(0, null, null, 1, 2);
        OutputBufferStore bufferStore = buffer.getBufferStore();

        NeighborMock n1 = (NeighborMock) buffer.getNeighbors().getNeighbor(getAS(1));

        Route r2 = createRoute(1, 3, 4, 5);
        Route r1 = createRoute(1, 2, 3);
        bufferStore.addAnnouncement(n1, new OutputAddEntity(getPrefix(1), r1, r2));
        bufferStore.addAnnouncement(n1, new OutputAddEntity(getPrefix(2), r1, r2));
        bufferStore.addAnnouncement(n1, new OutputAddEntity(getPrefix(3), r2, null));

        buffer.flush(n1.getASIdentifier());
        ArrayList<BGPUpdate> updates = n1.getUpdates();
        assertEquals(2, updates.size());

        BGPUpdate update = updates.get(0);
        compareCollectionWithIterator(getPrefixList(1, 2), update.getPrefixes().iterator());
        assertEquals(update.getRoute(), r1.copyWithMeOnPath(getAS(0)));

        update = updates.get(1);
        compareCollectionWithIterator(getPrefixList(3), update.getPrefixes().iterator());
        assertEquals(update.getRoute(), r2.copyWithMeOnPath(getAS(0)));

        assertFalse(bufferStore.announcementsIterator().hasNext());
        assertFalse(bufferStore.announcementsIterator(n1).hasNext());

    }

    /**
     * Tests whether adding a withdrawal clears waiting announcement and
     * replaces the lastRoute
     * 
     */
    public void testAddWithdrawal() {
        OutputBufferImpl buffer = MockedOutputBufferFactory.getInstance(0, null, null, 1, 2);
        OutputBufferStore bufferStore = buffer.getBufferStore();

        NeighborMock n1 = (NeighborMock) buffer.getNeighbors().getNeighbor(getAS(1));
        MRAITimerMock timer = new MRAITimerMock();
        timer.canSendNow = false;
        n1.setTimer(timer);

        Route r2 = createRoute(1, 3, 4, 5);
        Route r1 = createRoute(1, 2, 3);
        buffer.add(new OutputAddEntity(getPrefix(1), r1, r2));
        buffer.add(new OutputAddEntity(getPrefix(2), r1, r2));
        buffer.add(new OutputAddEntity(getPrefix(3), r1, null));
        buffer.flush();

        ArrayList<BGPUpdate> updates = n1.getUpdates();
        assertEquals(0, updates.size());
        assertTrue(bufferStore.announcementsIterator(n1).hasNext());

        buffer.add(new OutputRemoveEntity(getPrefix(1), createRoute(1, 5, 7)));
        buffer.add(new OutputRemoveEntity(getPrefix(2), createRoute(1, 5, 7)));
        buffer.add(new OutputRemoveEntity(getPrefix(3), createRoute(1, 5, 7)));
        buffer.flush();

        assertEquals(1, updates.size());
        BGPUpdate update = updates.get(0);
        assertEmpty("", update.getPrefixes());
        assertEquals(2, update.getWithdrawals().size());

        // nothing should be sent for prefix 3 (was null and is null!!!)
        compareCollectionWithIterator(getPrefixList(1, 2), update.getWithdrawals().iterator());

        assertFalse(bufferStore.announcementsIterator(n1).hasNext());
    }

}

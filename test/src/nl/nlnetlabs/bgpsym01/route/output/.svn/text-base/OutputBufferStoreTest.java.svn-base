package nl.nlnetlabs.bgpsym01.route.output;

import java.util.ArrayList;
import java.util.Iterator;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.primitives.OutputAddEntity;
import nl.nlnetlabs.bgpsym01.primitives.OutputRemoveEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

public class OutputBufferStoreTest extends AbstractTest {

    private OutputBufferStore store;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateASes(1000);
        Prefix.init(1000);
        store = new OutputBufferStoreImpl();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAnnouncements() {
        ArrayList<OutputAddEntity> entities = new ArrayList<OutputAddEntity>();

        int count = 10;
        for (int i = 0; i < count; i++) {
            OutputAddEntity entity = new OutputAddEntity(getPrefix(i), createRoute(1, 2, i), createRoute(3, 4, i));
            entities.add(entity);
            store.addAnnouncement(entity);
        }

        // check whether the iterator gives everything
        compareCollectionWithIterator(entities, store.announcementsIterator());

        // remove one using iterator and be sure it was truly removed
        entities.remove(2);
        Iterator<OutputAddEntity> announcementsIterator = store.announcementsIterator();
        announcementsIterator.next();
        announcementsIterator.next();
        announcementsIterator.next();
        announcementsIterator.remove();

        compareCollectionWithIterator(entities, store.announcementsIterator());

        store.clearAnnouncements();
        assertFalse(store.announcementsIterator().hasNext());
    }

    /**
     * Tests {@link OutputBufferStore#addWithdrawal(OutputRemoveEntity)} and
     * {@link OutputBufferStore#withdrawalsIterator()}
     */
    public void testWithdrawals() {
        ArrayList<OutputRemoveEntity> entities = new ArrayList<OutputRemoveEntity>();

        int count = 10;
        for (int i = 0; i < count; i++) {
            OutputRemoveEntity entity = new OutputRemoveEntity(getPrefix(i), createRoute(1, 2, i));
            entities.add(entity);
            store.addWithdrawal(entity);
        }

        // check whether the iterator gives everything
        compareCollectionWithIterator(entities, store.withdrawalsIterator());

        // remove one using iterator and be sure it was truly removed
        entities.remove(2);
        Iterator<OutputRemoveEntity> withdrawalsIterator = store.withdrawalsIterator();
        withdrawalsIterator.next();
        withdrawalsIterator.next();
        withdrawalsIterator.next();
        withdrawalsIterator.remove();

        compareCollectionWithIterator(entities, store.withdrawalsIterator());
    }

    /**
     * Tests correlation between
     * {@link OutputBufferStore#addAnnouncement(nl.nlnetlabs.bgpsym01.neighbor.Neighbor, OutputAddEntity)}
     * ,
     * {@link OutputBufferStore#removeAnnouncement(nl.nlnetlabs.bgpsym01.neighbor.Neighbor, nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix)}
     * and
     * {@link OutputBufferStore#announcementsIterator(nl.nlnetlabs.bgpsym01.neighbor.Neighbor)}
     */
    public void testAnnouncementsNeighbors() {
        ArrayList<OutputAddEntity> entities = new ArrayList<OutputAddEntity>();

        Neighbor n1 = new NeighborMock(getAS(1));
        Neighbor n2 = new NeighborMock(getAS(1));

        int count = 10;
        for (int i = 0; i < count; i++) {
            OutputAddEntity entity = new OutputAddEntity(getPrefix(i), createRoute(1, 2, i), createRoute(2, 3, i));
            entities.add(entity);
            store.addAnnouncement(n1, entity);
            store.addAnnouncement(n2, entity);
        }

        // check remove for 2 and 4
        assertEquals(entities.get(2), store.removeAnnouncement(n1, getPrefix(2)));
        assertEquals(entities.get(4), store.removeAnnouncement(n1, getPrefix(4)));

        // now n1 hest only 8 announcements, and n2 has 10

        compareCollectionWithIterator(entities, store.announcementsIterator(n2));

        // remove those 2 things from entities and check whether it is equals to
        // what n1 has
        entities.remove(4);
        entities.remove(2);
        compareCollectionWithIterator(entities, store.announcementsIterator(n1));

        // remove seconds using iterator and check whether it gets removed
        Iterator<OutputAddEntity> iterator = store.announcementsIterator(n1);
        iterator.next();
        iterator.next();
        iterator.remove();
        entities.remove(1);
        compareCollectionWithIterator(entities, store.announcementsIterator(n1));
    }

}

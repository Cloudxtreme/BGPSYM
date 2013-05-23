package nl.nlnetlabs.bgpsym01.neighbor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.route.output.NeighborMock;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class NeighborsTest extends AbstractTest {

    @Override
    @Before
    protected void setUp() throws Exception {
        generateASes(1000);
    }

    /**
     * test whether for myself the answer is good
     */
    public void testOriginatorMyself() {
        int size = 5;
        ASIdentifier myId = getAS(size + 1);
        Neighbors neighbors = getNeighbors(myId, size);
        int myNum = neighbors.getNeighborNum(myId);
        assertEquals("normaly I'm at the end of the array", size, myNum);
        assertEquals("wrong neighbors size", size, neighbors.size());
    }

    /**
     * If we ask for a nonexistent neighbor we want to have an exception
     */
    public void testNonexistentNeighbor() {
        int size = 5;
        Neighbors neighbors = getNeighbors(null, 5);
        try {
            neighbors.getNeighborNum(getAS(size * 3));
            fail("we should have an exception!!!");
        } catch (NoSuchElementException e) {
            // it's OK
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * for small amount of neighbors we want to use TreeMap, for big HashMap
     */
    @Test
    public void testMapClass() {
        Neighbors neighbors = new Neighbors(getAS(2), 1);
        assertTrue(TreeMap.class.isAssignableFrom(neighbors.getMapClass()));

        neighbors = new Neighbors(getAS(299), Neighbors.MINIMAL_SIZE_FOR_HASHMAP * 3);
        String errorMsg = "the type is: " + neighbors.getMapClass().getName() + " instead of HashMap";
        assertTrue(errorMsg, HashMap.class.isAssignableFrom(neighbors.getMapClass()));
    }

    @Test
    public void testNeighborNormalRemove() {
        ASIdentifier as0 = ASFactory.getInstance(0);
        ASIdentifier as3 = ASFactory.getInstance(3);
        ASIdentifier as2 = ASFactory.getInstance(2);
        Neighbors ns = new Neighbors(getAS(100));
        Neighbor n0 = new NeighborMock(as0);
        Neighbor n1 = new NeighborMock(ASFactory.getInstance(1));
        Neighbor n2 = new NeighborMock(as2);
        Neighbor n3 = new NeighborMock(as3);

        ns.addNeighbor(n0);
        ns.addNeighbor(n1);
        ns.addNeighbor(n2);
        ns.addNeighbor(n3);

        assertEquals(4, ns.size());
        assertNotNull(ns.getNeighbor(as0));
        assertNotNull(ns.getNeighbor(as3));

        // remove n0
        ns.remove(n0.getASIdentifier());
        assertEquals(3, ns.size());
        assertNull(ns.getNeighbor(as0));
        assertNotNull(ns.getNeighbor(as3));
        assertNotNull(ns.getNeighbor(as2));

        // remove n3
        ns.remove(n3.getASIdentifier());
        assertEquals(2, ns.size());
        assertNull(ns.getNeighbor(as0));
        assertNull(ns.getNeighbor(as3));
        assertNotNull(ns.getNeighbor(as2));

    }

    /**
     * check whether iterator remove truly removes a neighbor, there was a
     * normal iterator earlier
     * 
     * NOTE: right now Neighbor is not capable of removing in iterations so this
     * test is not performed.
     */
    @Ignore
    public void testNeighborsIteratorRemove() {
        // remove this if you want this test to run
        if (1 < 2) {
            return;
        }
        ASIdentifier as0 = ASFactory.getInstance(0);
        ASIdentifier as3 = ASFactory.getInstance(3);
        Neighbors ns = new Neighbors(getAS(300));
        Neighbor n0 = new NeighborMock(as0);
        Neighbor n1 = new NeighborMock(ASFactory.getInstance(1));
        Neighbor n2 = new NeighborMock(ASFactory.getInstance(2));
        Neighbor n3 = new NeighborMock(as3);

        ns.addNeighbor(n0);
        ns.addNeighbor(n1);
        ns.addNeighbor(n2);
        ns.addNeighbor(n3);

        assertEquals(4, ns.size());
        assertNotNull(ns.getNeighbor(as0));
        assertNotNull(ns.getNeighbor(as3));

        // remove as3 through iterator and see whether he was in fact removed
        Iterator<Neighbor> iterator = ns.iterator();
        while (iterator.hasNext()) {
            Neighbor neighbor = iterator.next();
            if (neighbor.getASIdentifier().equals(as3)) {
                iterator.remove();
            }
        }

        assertEquals(3, ns.size());
        assertNotNull(ns.getNeighbor(as0));
        assertNull(ns.getNeighbor(as3));

    }

}

package nl.nlnetlabs.bgpsym01.neighbor;

import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;

/**
 * Tests whether {@link NeighborsMap} works properly
 */
public class NeighborsMapTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        generateASes(1000);
    }

    /**
     * Tests whether EntrySet behaves properly
     */
    public void testEntrySet() {
        int size = 20;
        Neighbors neighbors = getNeighbors(null, size);
        NeighborsMap map = new NeighborsMap(neighbors);

        int counter = 0;
        // there shouldn't be anything!
        assertFalse(map.keySet().iterator().hasNext());

        ArrayList<ASIdentifier> asIds = new ArrayList<ASIdentifier>();
        for (int i = 0; i < size / 2; i++) {
            PrefixTableEntry pte = new PrefixTableEntry();
            pte.setOrignator(getAS(i));
            map.put(getAS(i), pte);
            asIds.add(getAS(i));
        }

        ArrayList<ASIdentifier> asIds2 = new ArrayList<ASIdentifier>();
        for (ASIdentifier asId : map.keySet()) {
            counter++;
            asIds2.add(asId);
        }
        assertEquals("wrong map entries size", size / 2, counter);
        assertEquals(asIds, asIds2);

    }

    /**
     * Tests:<br>
     * 1. {@link NeighborsMap#put(ASIdentifier, PrefixTableEntry)}<br>
     * 2. {@link NeighborsMap#remove(Object)}<br>
     * 3. {@link NeighborsMap#get(Object)}<br>
     * 4. {@link NeighborsMap#size()}<br>
     * 5. {@link NeighborsMap#clear()}
     */
    public void test1() {
        Neighbors neighbors = getNeighbors(getAS(7), 6);

        PrefixTableEntry p1 = new PrefixTableEntry();
        PrefixTableEntry p2 = new PrefixTableEntry();
        PrefixTableEntry p3 = new PrefixTableEntry();

        NeighborsMap map = new NeighborsMap(neighbors);
        assertNull(map.get(getAS(2)));

        map.put(getAS(2), p1);
        assertSame(p1, map.get(getAS(2)));
        assertNull(map.get(getAS(1)));
        assertEquals(1, map.size());

        map.put(getAS(2), p2);
        assertSame(p2, map.get(getAS(2)));
        assertNull(map.get(getAS(1)));
        assertEquals(1, map.size());

        map.put(getAS(3), p3);
        assertSame(p2, map.get(getAS(2)));
        assertSame(p3, map.get(getAS(3)));
        assertNull(map.get(getAS(1)));
        assertEquals(2, map.size());

        map.remove(getAS(4));
        assertSame(p2, map.get(getAS(2)));
        assertSame(p3, map.get(getAS(3)));
        assertNull(map.get(getAS(1)));
        assertEquals(2, map.size());

        map.remove(getAS(2));
        assertNull(map.get(getAS(2)));
        assertSame(p3, map.get(getAS(3)));
        assertNull(map.get(getAS(1)));
        assertEquals(1, map.size());

        map.remove(getAS(3));
        assertNull(map.get(getAS(2)));
        assertNull(map.get(getAS(3)));
        assertNull(map.get(getAS(1)));
        assertEquals(0, map.size());

        // clear
        map.put(getAS(1), p1);
        map.put(getAS(2), p2);
        map.put(getAS(1), p3);
        assertEquals(2, map.size());

        map.clear();
        assertNull(map.get(getAS(1)));
        assertNull(map.get(getAS(2)));
    }

}

package nl.nlnetlabs.bgpsym01.cache;

import java.util.Map;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.neighbor.NeighborsMap;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

/**
 * Tests {@link NeighborsMapsContainerImpl} with and without caching.
 */
public class NeighborsMapsContainterTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateASes(1000);
    }

    /**
     * Tests:<br>
     * 1. {@link NeighborsMapsContainerImpl#getMap()}<br>
     * 2. {@link NeighborsMapsContainerImpl#giveBack(Map)}
     * 
     * with caching.
     */
    public void testNoCaching() {
        XProperties properties = XProperties.getInstance();
        properties.setNeighborsContainerCaching(false);
        properties.setUseNeighborsMap(true);
        XProperties.setInstance(properties);

        Neighbors neighbors = getNeighbors(getAS(6), 5);

        NeighborsMapsContainerImpl container = new NeighborsMapsContainerImpl();
        container.setNeighbors(neighbors);
        PrefixTableEntry p1 = new PrefixTableEntry();

        Map<ASIdentifier, PrefixTableEntry> map1 = container.getMap();
        Map<ASIdentifier, PrefixTableEntry> map2 = container.getMap();
        Map<ASIdentifier, PrefixTableEntry> map3 = container.getMap();
        Map<ASIdentifier, PrefixTableEntry> map4 = container.getMap();
        assertTrue(map1 instanceof NeighborsMap);
        assertTrue(map2 instanceof NeighborsMap);
        assertTrue(map3 instanceof NeighborsMap);
        assertTrue(map4 instanceof NeighborsMap);

        map1.put(getAS(3), p1);
        map2.put(getAS(3), p1);
        map3.put(getAS(3), p1);
        map4.put(getAS(3), p1);

        container.giveBack(map2);

        Map<ASIdentifier, PrefixTableEntry> map5 = container.getMap();
        assertNotSame("should be a new map", map2, map5);

        Map<ASIdentifier, PrefixTableEntry> map6 = container.getMap();
        assertNotSame(map1, map6);
        assertNotSame(map2, map6);
        assertNotSame(map3, map6);
        assertNotSame(map4, map6);
        assertNotSame(map5, map6);
    }

    /**
     * Tests:<br>
     * 1. {@link NeighborsMapsContainerImpl#getMap()}<br>
     * 2. {@link NeighborsMapsContainerImpl#giveBack(Map)}
     * 
     * with caching.
     */
    public void testCaching() {
        XProperties properties = XProperties.getInstance();
        properties.setNeighborsContainerCaching(true);
        properties.setUseNeighborsMap(true);
        XProperties.setInstance(properties);

        Neighbors neighbors = getNeighbors(getAS(6), 5);

        NeighborsMapsContainerImpl container = new NeighborsMapsContainerImpl();
        container.setNeighbors(neighbors);
        PrefixTableEntry p1 = new PrefixTableEntry();

        Map<ASIdentifier, PrefixTableEntry> map1 = container.getMap();
        Map<ASIdentifier, PrefixTableEntry> map2 = container.getMap();
        Map<ASIdentifier, PrefixTableEntry> map3 = container.getMap();
        Map<ASIdentifier, PrefixTableEntry> map4 = container.getMap();
        assertTrue(map1 instanceof NeighborsMap);
        assertTrue(map2 instanceof NeighborsMap);
        assertTrue(map3 instanceof NeighborsMap);
        assertTrue(map4 instanceof NeighborsMap);

        map1.put(getAS(3), p1);
        map2.put(getAS(3), p1);
        map3.put(getAS(3), p1);
        map4.put(getAS(3), p1);

        container.giveBack(map2);

        Map<ASIdentifier, PrefixTableEntry> map5 = container.getMap();
        assertSame(map2, map5);
        assertEquals("map was not cleared", 0, map5.size());

        Map<ASIdentifier, PrefixTableEntry> map6 = container.getMap();
        assertNotSame(map1, map6);
        assertNotSame(map2, map6);
        assertNotSame(map3, map6);
        assertNotSame(map4, map6);
        assertNotSame(map5, map6);
    }
}

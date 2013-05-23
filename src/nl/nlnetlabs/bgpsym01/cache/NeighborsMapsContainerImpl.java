package nl.nlnetlabs.bgpsym01.cache;

import java.util.HashMap;
import java.util.Map;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.neighbor.NeighborsMap;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.primitives.types.FIFOQueue;
import nl.nlnetlabs.bgpsym01.primitives.types.FIFOQueueImpl;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

/**
 * This class implements a container that maintains information neighbors map
 * (so that they can be easily reused).
 * 
 * @see NeighborsMapsContainterTest
 */
public class NeighborsMapsContainerImpl implements NeighborsMapsContainer {

    Neighbors neighbors;

    FIFOQueue<Map<ASIdentifier, PrefixTableEntry>> queue;

    private boolean useCaching;

    public NeighborsMapsContainerImpl() {
        useCaching = XProperties.getInstance().isNeighborsContainerCaching();
        if (useCaching) {
            queue = new FIFOQueueImpl<Map<ASIdentifier, PrefixTableEntry>>();
        }
    }

    public NeighborsMapsContainerImpl(Neighbors neighbors) {
        this();
        setNeighbors(neighbors);
    }

    public void setNeighbors(Neighbors neighbors) {
        this.neighbors = neighbors;
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.cache.NeighborsMapsContainer#getMap()
     */
    public Map<ASIdentifier, PrefixTableEntry> getMap() {
        if (useCaching && queue.size() > 0) {
            return queue.remove();
        } else {
            if (XProperties.getInstance().isUseNeighborsMap()) {
                return new NeighborsMap(neighbors);
            } else {
                return new HashMap<ASIdentifier, PrefixTableEntry>();
            }
        }
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.cache.NeighborsMapsContainer#giveBack(java.util.Map)
     */
    public void giveBack(Map<ASIdentifier, PrefixTableEntry> map) {
        if (useCaching) {
            map.clear();
            queue.add(map);
        }
    }
}

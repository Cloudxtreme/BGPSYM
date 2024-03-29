package nl.nlnetlabs.bgpsym01.neighbor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.primitives.types.AbstractSet;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;

/**
 * Implements {@link Map} that is based on array so that search time is constant
 * and information overhead associated with each {@link PrefixTableEntry} is
 * small. Main reason for writing and having this class is to slow down young
 * generation (as this maps are widely used in the simulation).
 * 
 * From {@link Map#keySet()}, {@link Map#entrySet()} and {@link Map#values()}
 * only {@link Map#keySet()} is supported and from its method only
 * {@link Set#iterator()}. This iterator isn't constant time and in future may
 * lead to performance breakdowns!!!
 * 
 * Many methods are unsupported (as they are not used and don't seem suitable
 * for our purposes).
 */
public class NeighborsMap implements Map<ASIdentifier, PrefixTableEntry> {

    public class KeysIterator implements Iterator<ASIdentifier> {

        int next = -1;

        public boolean hasNext() {
            next++;
            while (next < array.length && array[next] == null) {
                next++;
            }
            return next < array.length;
        }

        public ASIdentifier next() {
            return array[next].getOriginator();
        }

        public void remove() {
            throw new NotImplementedException();
        }

    }

    private PrefixTableEntry[] array;

    private Neighbors neighbors;

    private int size;

    public NeighborsMap(Neighbors neighbors) {
        this.neighbors = neighbors;
        // I need also space for originator == null (that would be me)
        array = new PrefixTableEntry[neighbors.size() + 1];
    }

    public void clear() {
        Arrays.fill(array, null);
        size = 0;
    }

    public boolean containsKey(Object key) {
        return array[neighbors.getNeighborNum((ASIdentifier) key)] != null;
    }

    public boolean containsValue(Object value) {
        throw new NotImplementedException();
    }

    // entry set is only iterable, other operations don't work!
    public Set<java.util.Map.Entry<ASIdentifier, PrefixTableEntry>> entrySet() {
        throw new NotImplementedException();
    }

    public PrefixTableEntry get(Object key) {
        return array[neighbors.getNeighborNum((ASIdentifier) key)];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Set<ASIdentifier> keySet() {
        return new AbstractSet<ASIdentifier>() {

            @Override
            public Iterator<ASIdentifier> iterator() {
                return new KeysIterator();
            }

        };
    }

    public PrefixTableEntry put(ASIdentifier key, PrefixTableEntry value) {
        int index = neighbors.getNeighborNum(key);
        PrefixTableEntry old = null;
        if (array[index] == null) {
            size++;
            old = array[index];
        }
        array[index] = value;
        return old;
    }

    public void putAll(Map<? extends ASIdentifier, ? extends PrefixTableEntry> m) {
        throw new NotImplementedException();
    }

    public PrefixTableEntry remove(Object key) {
        int index = neighbors.getNeighborNum((ASIdentifier) key);
        PrefixTableEntry pte = null;
        if (array[index] != null) {
            size--;
            pte = array[index];
            array[index] = null;
        }
        return pte;
    }

    public int size() {
        return size;
    }

    public Collection<PrefixTableEntry> values() {
        throw new NotImplementedException();
    }

}
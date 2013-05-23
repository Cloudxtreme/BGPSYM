package nl.nlnetlabs.bgpsym01.neighbor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;

import org.apache.log4j.Logger;

public class Neighbors implements Iterable<Neighbor> {

	private static Logger log = Logger.getLogger(Neighbors.class);

    private class Info implements Comparable<Info> {
        private int num;
        private Neighbor neighbor;
		private boolean deleted;

        public int compareTo(Info o) {
            return num < o.num ? -1 : num == o.num ? 0 : 1;
        }

        public Info(int num, Neighbor neighbor) {
            super();
            this.num = num;
            this.neighbor = neighbor;
        }
    }

    private static final double LOAD_FACTOR = 0.75;
    static final int DEFAULT_NEIGHBORS_SIZE = 5;
    static final int MINIMAL_SIZE_FOR_HASHMAP = 16;
    private Map<ASIdentifier, Info> map;
	private Map<ASIdentifier, Info> deletedMap;

    private ASIdentifier myId;

    public Neighbors(ASIdentifier myId, Neighbor... ns) {
        this(myId, ns.length == 0 ? DEFAULT_NEIGHBORS_SIZE : ns.length);
        for (Neighbor n : ns) {
            addNeighbor(n);
        }
    }

    public Neighbors(ASIdentifier myId, int size) {
        if (size < MINIMAL_SIZE_FOR_HASHMAP) {
            map = new TreeMap<ASIdentifier, Info>();
			deletedMap = new TreeMap<ASIdentifier, Info>();
        } else {
            map = new HashMap<ASIdentifier, Info>((int) (size / LOAD_FACTOR) + 1, (float) LOAD_FACTOR);
			deletedMap = new HashMap<ASIdentifier, Info>((int) (size / LOAD_FACTOR) + 1, (float) LOAD_FACTOR);
        }
        this.myId = myId;
    }

    public void addNeighbor(Neighbor neighbor) {
        map.put(neighbor.getASIdentifier(), new Info(map.size(), neighbor));
		deletedMap.remove(neighbor.getASIdentifier());
    }

    public Neighbor getNeighbor(ASIdentifier asIdentifier) {
        Info info = map.get(asIdentifier);
        return info == null ? null : info.deleted ? null : info.neighbor;
    }

    public Iterator<Neighbor> iterator() {
        return new Iterator<Neighbor>() {

            Iterator<Info> iterator = map.values().iterator();

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public Neighbor next() {
                return iterator.next().neighbor;
            }

            public void remove() {
                throw new NotImplementedException();
            }

        };
    }

    public int size() {
        return map.size();
    }

    public void remove(ASIdentifier asIdentifier) {
		Info info = map.get(asIdentifier);

		if (info != null) {
			deletedMap.put(asIdentifier, info);
		}

		info = map.remove(asIdentifier);
    }

    @SuppressWarnings("unchecked")
    Class<? extends Map> getMapClass() {
        return map.getClass();
    }

    public int getNeighborNum(ASIdentifier originator) {
        if (originator == myId) {
            return map.size();
        }
        Info info = map.get(originator);
        // this null will throw an exception, this is cool behavior
        if (info == null) {
            throw new NoSuchElementException("originator=" + originator);
        }
        return info.num;
    }

}

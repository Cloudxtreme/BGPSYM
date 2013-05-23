package nl.nlnetlabs.bgpsym01.main;

import java.util.HashMap;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;

public class ObjectRegister {

    public enum Type {
        CACHE, DISK;
    }

    class Pair {
        ASIdentifier asIdentifier;
        Type type;

        public Pair(ASIdentifier asIdentifier, Type type) {
            super();
            this.asIdentifier = asIdentifier;
            this.type = type;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Pair) {
                Pair tmp = (Pair) obj;
                return tmp.asIdentifier.equals(asIdentifier) && tmp.type == type;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return asIdentifier.hashCode() * 391 + type.hashCode() * 79;
        }

    }

    private HashMap<Pair, Object> map = new HashMap<Pair, Object>();

    public void store(ASIdentifier asIdentifier, Type type, Object object) {
        map.put(new Pair(asIdentifier, type), object);
    }

    public Object get(ASIdentifier asIdentifier, Type type) {
        return map.get(new Pair(asIdentifier, type));
    }

    private static ObjectRegister instance = new ObjectRegister();

    public static ObjectRegister getInstance() {
        return instance;
    }

    private ObjectRegister() {

    }

}

package nl.nlnetlabs.bgpsym01.primitives.bgp;

import java.io.Serializable;

public class Prefix implements Comparable<Prefix>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6126855142546835013L;
    private int id;

    private static Prefix[] array;

    public static void init(int count) {
        array = new Prefix[count];
        for (int i = 0; i < count; i++) {
            array[i] = new Prefix(i);
        }
    }

    public final static Prefix getInstance(int num) throws ArrayIndexOutOfBoundsException {
        return array[num];
    }

    Prefix(int prefix) {
        this.id = prefix;
    }

    public int getNum() {
        return id;
    }

    @Override
    public String toString() {
        return "msg_" + id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Prefix)) {
            return false;
        }
        Prefix tmp = (Prefix) obj;
        return id == tmp.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public int compareTo(Prefix o) {
        if (id < o.id) {
            return -1;
        }
        return id == o.id ? 0 : 1;
    }

}
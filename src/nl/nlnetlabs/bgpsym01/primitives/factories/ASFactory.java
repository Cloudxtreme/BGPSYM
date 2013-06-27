package nl.nlnetlabs.bgpsym01.primitives.factories;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;

public class ASFactory {

    private static int _internalId = 0;

    private static ASIdentifier[] asArray;

    public static void init(int size) {
        asArray = new ASIdentifier[size];
    }

    public static boolean hasInstance(int num) {
        return asArray[num] != null;
    }

    public static ASIdentifier getInstance(int num) {
        ASIdentifier out = asArray[num];
        if (out == null) {
            throw new NoSuchElementException("num=" + num);
        }
        return out;
    }

    public static ASIdentifier createInstance(String id) {
        int num = _internalId;
        ASIdentifier asIdentifier = new ASIdentifier(id, num);
        asArray[_internalId] = asIdentifier;
        _internalId++;
        return asIdentifier;
    }

    public static int getSize() {
        return asArray.length;
    }

    public static void registerAS(ASIdentifier asIdentifier, int internalNumber) {
        boolean out = asArray[internalNumber] != null;
        if (out) {
            return;
        }

        asArray[internalNumber] = asIdentifier;
    }

    public static ASIdentifier getInstance(String fromName) {
        for (ASIdentifier as : asArray) {
            if (as != null && as.getId().equals(fromName)) {
                return as;
            }
        }
        throw new NoSuchElementException("as=" + fromName);
    }

    public static Iterator<ASIdentifier> iterator() {
        List<ASIdentifier> list = Arrays.asList(asArray);
        return list.iterator();

    }

}

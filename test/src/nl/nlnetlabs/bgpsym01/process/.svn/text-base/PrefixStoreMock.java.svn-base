package nl.nlnetlabs.bgpsym01.process;

import java.io.IOException;
import java.util.Collection;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.route.Policy;
import nl.nlnetlabs.bgpsym01.route.PrefixStore;

public class PrefixStoreMock implements PrefixStore {

    private int receivedArrayCount;
    private int receivedArraySize;

    private int removedArrayCount;
    private int removedArraySize;

    public void prefixReceived(ASIdentifier asIdentifier, Collection<Prefix> prefix, Route route) {
        receivedArraySize += prefix.size();
        receivedArrayCount++;
    }

    public void prefixRemove(ASIdentifier asIdentifier, Collection<Prefix> prefixes) {
        removedArrayCount++;
        removedArraySize += prefixes.size();
    }

    public void storePrefixesPermanent() throws IOException {
    }

    public int getReceivedArrayCount() {
        return receivedArrayCount;
    }

    public void setReceivedArrayCount(int receivedArrayCount) {
        this.receivedArrayCount = receivedArrayCount;
    }

    public int getReceivedArraySize() {
        return receivedArraySize;
    }

    public void setReceivedArraySize(int receivedArraySize) {
        this.receivedArraySize = receivedArraySize;
    }

    public int getRemovedArrayCount() {
        return removedArrayCount;
    }

    public void setRemovedArrayCount(int removedArrayCount) {
        this.removedArrayCount = removedArrayCount;
    }

    public int getRemovedArraySize() {
        return removedArraySize;
    }

    public void setRemovedArraySize(int removedArraySize) {
        this.removedArraySize = removedArraySize;
    }

    public void setPolicy(Policy policy) {
    }

    public void flush(ASIdentifier asId) {
    }

    public void unflap(Prefix prefix, ASIdentifier asId) {
    }

    public PrefixStoreType getType() {
        return PrefixStoreType.MOCK;
    }

}

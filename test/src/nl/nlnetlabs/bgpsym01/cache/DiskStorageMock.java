package nl.nlnetlabs.bgpsym01.cache;

import java.io.IOException;
import java.util.Iterator;

import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;

class MockIterator<T> implements Iterator<T> {

    public boolean hasNext() {
        return false;
    }

    public T next() {
        throw new NotImplementedException();
    }

    public void remove() {
    }

}

public class DiskStorageMock implements DiskStorage {

    public Iterator<PrefixInfo> readPrefix(Prefix prefix) {
        return new MockIterator<PrefixInfo>();
    }

    public void storePrefixes(Iterator<PrefixInfo> iterator) throws IOException {
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    public void sync() {
    }

    public void writePrefixArrayPermanent() throws IOException {
    }

}

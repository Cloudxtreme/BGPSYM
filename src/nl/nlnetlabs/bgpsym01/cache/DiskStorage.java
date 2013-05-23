package nl.nlnetlabs.bgpsym01.cache;

import java.io.IOException;
import java.util.Iterator;

import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

public interface DiskStorage {

    public void writePrefixArrayPermanent() throws IOException;

    public void storePrefixes(Iterator<PrefixInfo> iterator) throws IOException;

    public Iterator<PrefixInfo> readPrefix(Prefix prefix);

    public void sync();

}
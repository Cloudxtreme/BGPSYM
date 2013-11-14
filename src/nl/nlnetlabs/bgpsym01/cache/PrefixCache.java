package nl.nlnetlabs.bgpsym01.cache;

import java.util.LinkedHashMap;

import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

public interface PrefixCache {

    public PrefixInfo getPrefixInfo(Prefix prefix);

    public void setPrefixInfo(Prefix prefix, PrefixInfo prefixInfo);

    void storePrefixesPermanent();

    void flush();

    public LinkedHashMap<Prefix, PrefixInfo> getTable();
}

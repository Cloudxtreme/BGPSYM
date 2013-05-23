package nl.nlnetlabs.bgpsym01.cache;

import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

public interface PrefixCache {

    public PrefixInfo getPrefixInfo(Prefix prefix);

    public void setPrefixInfo(Prefix prefix, PrefixInfo prefixInfo);

    void storePrefixesPermanent();

    void flush();

}

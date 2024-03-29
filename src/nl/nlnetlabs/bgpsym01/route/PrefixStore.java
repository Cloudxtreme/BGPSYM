package nl.nlnetlabs.bgpsym01.route;

import java.io.IOException;
import java.util.Collection;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;

public interface PrefixStore {

    public enum PrefixStoreType {
        MAP, MOCK, ROUTEVIEW, RIS;
    }

    public PrefixStoreType getType();

    // public void prefixReceived(ASIdentifier asIdentifier, Prefix prefix,
    // Route route);

    public void prefixReceived(ASIdentifier asIdentifier, Collection<Prefix> prefix, Route route);

    /**
     * Removes information about a prefix advertised by neighbor
     * 
     * @param asIdentifier
     * @param prefix
     */
    // public void prefixRemove(ASIdentifier asIdentifier, Prefix prefix);
    void storePrefixesPermanent() throws IOException;

    public void prefixRemove(ASIdentifier asIdentifier, Collection<Prefix> prefixes);

    public void flush(ASIdentifier asId);

    public void unflap(Prefix prefix, ASIdentifier asId);
}

package nl.nlnetlabs.bgpsym01.route;

import java.util.HashMap;
import java.util.TreeMap;

import nl.nlnetlabs.bgpsym01.cache.PrefixCache;
import nl.nlnetlabs.bgpsym01.cache.PrefixInfo;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;

public class PrefixCacheMock implements PrefixCache {

    private HashMap<Prefix, PrefixInfo> table = new HashMap<Prefix, PrefixInfo>();

    private PrefixInfo createNewPrefix(Prefix prefix) {
        PrefixInfo prefixInfo = new PrefixInfo();
        prefixInfo.setPrefix(prefix);
        prefixInfo.setNeighborsMap(new TreeMap<ASIdentifier, PrefixTableEntry>());
        return prefixInfo;
    }

    public PrefixInfo getPrefixInfo(Prefix prefix) {
        PrefixInfo prefixInfo = table.get(prefix);
        if (prefixInfo == null) {
            prefixInfo = createNewPrefix(prefix);
            table.put(prefix, prefixInfo);
        }
        return prefixInfo;
    }

    public void addRoute(ASIdentifier asIdentifier, Prefix prefix, Route route, boolean isDefault) {
        PrefixInfo prefixInfo = getPrefixInfo(prefix);
        PrefixTableEntry entry = prefixInfo.getNeighborsMap().get(asIdentifier);
        if (entry == null) {
            entry = new PrefixTableEntry(route);
            prefixInfo.getNeighborsMap().put(asIdentifier, entry);
        } else {
            entry.setRoute(route);
        }
        if (isDefault) {
            prefixInfo.setCurrentEntry(entry);
        }
    }

    public int size() {
        return table.size();
    }

    public void invalidate(Prefix prefix) {
    }

    public void storePrefixesPermanent() {
    }

    public void setPrefixInfo(Prefix prefix, PrefixInfo prefixInfo) {
        table.put(prefix, prefixInfo);
    }

    @Override
    public String toString() {
        return table.toString();
    }

    public void flush() {
        throw new NotImplementedException();
    }

}

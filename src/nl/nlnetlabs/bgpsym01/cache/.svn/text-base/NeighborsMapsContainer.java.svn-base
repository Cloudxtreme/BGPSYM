package nl.nlnetlabs.bgpsym01.cache;

import java.util.Map;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;

public interface NeighborsMapsContainer {

    public abstract Map<ASIdentifier, PrefixTableEntry> getMap();

    public abstract void giveBack(Map<ASIdentifier, PrefixTableEntry> map);

}
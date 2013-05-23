package nl.nlnetlabs.bgpsym01.process;

import java.util.List;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

public interface NeighborInvalidator {

    public void invalidate(Neighbor neighbor, List<Prefix> prefixes);

    public void validate(Neighbor neighbor, List<Prefix> prefixes);

}
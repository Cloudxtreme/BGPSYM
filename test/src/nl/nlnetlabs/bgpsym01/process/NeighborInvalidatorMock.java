package nl.nlnetlabs.bgpsym01.process;

import java.util.List;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

public class NeighborInvalidatorMock implements NeighborInvalidator {

    public List<Prefix> validated;
    public Neighbor validatedNeighbor;
    public List<Prefix> invalidated;
    public Neighbor invalidatedNeighbor;

    public void invalidate(Neighbor neighbor, List<Prefix> prefixes) {
        invalidated = prefixes;
        invalidatedNeighbor = neighbor;
    }

    public void validate(Neighbor neighbor, List<Prefix> prefixes) {
        validated = prefixes;
        validatedNeighbor = neighbor;
    }



}

package nl.nlnetlabs.bgpsym01.route.output;

import java.util.List;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.primitives.OutputEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;

public interface OutputBuffer {

    /**
     * Adds new prefix to be advertised
     * 
     * @param entity
     *            model of the prefix announcement
     */
    public void add(OutputEntity entity);

    public void flush();

    public void flush(ASIdentifier as);

    public void invalidate(Neighbor neighbor, List<Prefix> prefixList);

    public void validate(Neighbor neighbor, List<Pair<Prefix, Route>> prefixes);

}

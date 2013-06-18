package nl.nlnetlabs.bgpsym01.process;

import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.cache.PrefixCache;
import nl.nlnetlabs.bgpsym01.cache.PrefixInfo;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreMapImpl;
import nl.nlnetlabs.bgpsym01.route.output.OutputBuffer;
import nl.nlnetlabs.bgpsym01.route.output.OutputState;

public class NeighborInvalidatorImpl implements NeighborInvalidator {

    private OutputBuffer outputBuffer;

    private OutputState outputState;

    /**
     * Needed to get info about current route when the prefix is validated
     * again.
     */
    private PrefixCache prefixCache;

    private Neighbors neighbors;
    
    private PrefixStoreMapImpl storeImpl;

    public void setNeighbors(Neighbors neighbors) {
        this.neighbors = neighbors;
    }

    public PrefixCache getPrefixCache() {
        return prefixCache;
    }

    public void setPrefixCache(PrefixCache prefixCache) {
        this.prefixCache = prefixCache;
    }

    public OutputBuffer getOutputBuffer() {
        return outputBuffer;
    }

    public void setOutputBuffer(OutputBuffer outputBuffer) {
        this.outputBuffer = outputBuffer;
    }

    public OutputState getOutputState() {
        return outputState;
    }

    public void setOutputState(OutputState outputState) {
        this.outputState = outputState;
    }

    /**
     * Registers prefixes in the output state
     * @param neighbor TODO
     * @param prefixes
     */
    void registerPrefixes(Neighbor neighbor, List<Prefix> prefixes) {
        outputState.registerPrefixes(neighbor, prefixes);
    }

    void deregisterPrefixes(Neighbor neighbor, List<Prefix> prefixes) {
        outputState.deregisterPrefixes(neighbor, prefixes);
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.process.NeighborInvalidator#invalidate(nl.nlnetlabs.bgpsym01.neighbor.Neighbor, java.util.List)
     */
    public void invalidate(Neighbor neighbor, List<Prefix> prefixes) {
        if (neighbor == null) {
            invalidateAll(prefixes);
        } else {
            invalidateNeighbor(neighbor, prefixes);
        }
    }

    private void invalidateAll(List<Prefix> prefixes) {
        for (Neighbor n : neighbors) {
            invalidate(n, prefixes);
        }
    }

    private void invalidateNeighbor(Neighbor neighbor, List<Prefix> prefixes) {
    	storeImpl.removePrefixesFromSender(neighbor.getASIdentifier());
        registerPrefixes(neighbor, prefixes);
        neighbor.setValid(false); // !outputState.hasRegisteredPrefixes(neighbor)
        outputBuffer.invalidate(neighbor, prefixes);
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.process.NeighborInvalidator#validate(nl.nlnetlabs.bgpsym01.neighbor.Neighbor, java.util.List)
     */
    public void validate(Neighbor neighbor, List<Prefix> prefixes) {
        deregisterPrefixes(neighbor, prefixes);
        neighbor.setValid(!outputState.hasRegisteredPrefixes(neighbor));
        List<Pair<Prefix, Route>> prefixesWithRoutes = new ArrayList<Pair<Prefix,Route>>();
        for (Prefix prefix : prefixes) {
            prefixesWithRoutes.add(new Pair<Prefix, Route>(prefix, getRoute(prefix)));
        }
        outputBuffer.validate(neighbor, prefixesWithRoutes);
    }

    private Route getRoute(Prefix prefix) {
        PrefixInfo pi = prefixCache.getPrefixInfo(prefix);
        if (pi == null) {
            return null;
        }
        return pi.getCurrentEntry() == null ? null : pi.getCurrentEntry().getRoute();
    }
    
    public void setPrefixStore (PrefixStoreMapImpl storeImpl) {
    	this.storeImpl = storeImpl;
    }

}

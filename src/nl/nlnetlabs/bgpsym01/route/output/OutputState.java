package nl.nlnetlabs.bgpsym01.route.output;

import java.util.List;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;

public interface OutputState {

    public enum UpdateToSendType {
        NONE, ANNOUNCE, WITHDRAWAL
    }

    public OutputState.UpdateToSendType getUpdateType(Neighbor neighbor, Prefix prefix, Route route, Route lastRoute);

    public void deferred(Neighbor neighbor, Prefix prefix, Route route);

    public void sent(Neighbor neighbor, Prefix prefix);

    /**
     * Registers prefixes which should not be sent to the given neighbor.
     * 
     * @param neighbor
     * @param prefixes
     */
    public void registerPrefixes(Neighbor neighbor, List<Prefix> prefixes);

    public void deregisterPrefixes(Neighbor neighbor, List<Prefix> prefixes);

    public boolean hasRegisteredPrefixes(Neighbor neighbor);

}

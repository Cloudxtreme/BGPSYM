package nl.nlnetlabs.bgpsym01.route.output;

import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;

public class OutputStateMock implements OutputState {

    public List<Pair<Neighbor, List<Prefix>>> registeredPrefixes = new ArrayList<Pair<Neighbor, List<Prefix>>>();
    public List<Pair<Neighbor, List<Prefix>>> deregisteredPrefixes = new ArrayList<Pair<Neighbor, List<Prefix>>>();

    public boolean hasRegisteredPrefixes = false;

    public boolean hasRegisteredPrefixes(Neighbor neighbor) {
        return hasRegisteredPrefixes;
    }

    public void registerPrefixes(Neighbor neighbor, List<Prefix> prefixes) {
        registeredPrefixes.add(new Pair<Neighbor, List<Prefix>>(neighbor, prefixes));
    }

    public void deregisterPrefixes(Neighbor neighbor, List<Prefix> prefixes) {
        deregisteredPrefixes.add(new Pair<Neighbor, List<Prefix>>(neighbor, prefixes));
    }

    public void deferred(Neighbor neighbor, Prefix prefix, Route route) {
        throw new NotImplementedException();
    }

    public UpdateToSendType getUpdateType(Neighbor neighbor, Prefix prefix, Route route, Route lastRoute) {
        throw new NotImplementedException();
    }

    public void sent(Neighbor neighbor, Prefix prefix) {
        throw new NotImplementedException();
    }

}

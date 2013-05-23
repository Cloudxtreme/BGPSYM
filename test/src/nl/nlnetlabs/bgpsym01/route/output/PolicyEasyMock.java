package nl.nlnetlabs.bgpsym01.route.output;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.route.Policy;

public class PolicyEasyMock implements Policy {

    public boolean isAdvertisable(ASIdentifier myId, Neighbor neighbor, Neighbors neighbors, Route route) {
        return (!myId.equals(neighbor.getASIdentifier())) && (!neighbor.getASIdentifier().equals(route.getSender()));
    }

    public boolean isBetter(ASIdentifier myAsId, Prefix prefix, Route route1, Neighbor n1, Route route2, Neighbor n2) {
        return route1 == null || route2.getPathLength() < route1.getPathLength();
    }

}

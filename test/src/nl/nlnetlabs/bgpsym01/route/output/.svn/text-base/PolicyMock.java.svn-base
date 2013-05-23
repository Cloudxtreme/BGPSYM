package nl.nlnetlabs.bgpsym01.route.output;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.route.Policy;

public class PolicyMock implements Policy {

    public boolean useAnswer = false;
    public boolean answer = true;

    public PolicyMock() {

    }

    public PolicyMock(boolean autoTrueAnswer) {
        if (autoTrueAnswer) {
            useAnswer = true;
            answer = true;
        }
    }

    public boolean isAdvertisable(ASIdentifier myId, Neighbor neighbor, Neighbors neighbors, Route route) {
        if (useAnswer) {
            return answer;
        }

        // go only up

        int internalId = neighbor.getASIdentifier().getInternalId();
        // don't send to ourselves
        if (internalId == myId.getInternalId()) {
            return false;
        }
        return internalId == 3 || internalId == 4;
    }

    public boolean isBetter(ASIdentifier myAsId, Prefix prefix, Route route1, Neighbor n1, Route route2, Neighbor n2) {
        // always is
        return true;
    }

}

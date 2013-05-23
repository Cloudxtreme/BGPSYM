package nl.nlnetlabs.bgpsym01.route;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;

import org.apache.log4j.Logger;

public class PolicyImpl implements Policy {

    private static Logger log = Logger.getLogger(PolicyImpl.class);

    public boolean isAdvertisable(ASIdentifier myId, Neighbor neighbor, Neighbors neighbors, Route route) {
        return !route.isFrom(neighbor.getASIdentifier());
    }

    public boolean isBetter(ASIdentifier myAsId, Prefix prefix, Route route1, Neighbor n1, Route route2, Neighbor n2) {
        boolean outcome = false;
        if (route1 == null) {
            if (log.isDebugEnabled()) {
                log.debug("route1 is null");
            }
            outcome = true;
        } else {
            outcome = route2.getHops().length < route1.getHops().length;
        }

        if (log.isDebugEnabled()) {
            log.debug("outcome=" + outcome);
        }

        return outcome;
    }

}

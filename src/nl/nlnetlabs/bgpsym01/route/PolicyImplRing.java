package nl.nlnetlabs.bgpsym01.route;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;

import org.apache.log4j.Logger;

/**
 * Prefix X is advertisable if any of following is true: - we are originator of
 * that particular prefix - we are at level 0 - the recipient is our child
 * 
 */
public class PolicyImplRing implements Policy {

    private static Logger log = Logger.getLogger(PolicyImplRing.class);

    public boolean isAdvertisable(ASIdentifier myId, Neighbor neighbor, Neighbors neighbors, Route route) {

        // we advertise all our prefixes
        if (route.getPathLength() == 0) {
            return true;
        }
        int hisLevel = (Integer) neighbor.getASIdentifier().getAttachment();
        int myLevel = (Integer) myId.getAttachment();

        // if his our son he receives it
        if (myLevel < hisLevel) {
            return true;
        }

        // level 0 transit everything for each other...
        if (myLevel == 0) {
            return true;
        }

        return false;
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

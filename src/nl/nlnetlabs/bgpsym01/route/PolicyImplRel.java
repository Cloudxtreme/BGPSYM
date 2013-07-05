package nl.nlnetlabs.bgpsym01.route;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

public class PolicyImplRel implements Policy {

    private static Logger log = Logger.getLogger(PolicyImplRel.class);
    private static XProperties properties = XProperties.getInstance();

    @SuppressWarnings("unused")
	public boolean isBetter(ASIdentifier myAsId, Prefix prefix, Route route1, Neighbor n1, Route route2, Neighbor n2) {

        boolean outcome = false;
        PeerRelation pr1 = n1 == null ? null : (PeerRelation) n1.getAttachment();
        PeerRelation pr2 = n2 == null ? null : (PeerRelation) n2.getAttachment();
        if (route1 == null) {
            if (log.isDebugEnabled()) {
                log.debug("route1 is null");
            }
            outcome = true;
        } else if (false && pr1 != pr2) {
            // the lower preference the better
            outcome = pr2.getPreference() < pr1.getPreference();
        } else {
        	log.info("prefix: "+prefix+" r1: "+route1+" n1: "+n1+" r2: "+route2+" n2:"+n2);
        	
        	if (pr2 == null) {
        		log.info("n2: "+n2+" pr2 empty");
        	}
        	
            int l1 = getRelativeLenght(myAsId, n1.getASIdentifier(), route1, pr1);
            int l2 = getRelativeLenght(myAsId, n2.getASIdentifier(), route2, pr2);
            outcome = l2 < l1;
        }

        if (log.isDebugEnabled()) {
            log.debug("outcome=" + outcome);
        }

        return outcome;
    }

    private int getRelativeLenght(ASIdentifier asIdentifier, ASIdentifier neighborAsId, Route route, PeerRelation peerRelation) {
        // for 50% of neighbors add 1 to the path length...
        int v = (neighborAsId.getInternalId() * 17 + asIdentifier.getInternalId() * 19) % 20;
        // int hops = route.getHops().length + peerRelation.getPreference();
        int hops = route.getHops().length;
        return peerRelation.getPreference() * properties.policyMultiPref + hops * properties.policyMulti + v;
    }

    public boolean isAdvertisable(ASIdentifier myId, Neighbor neighbor, Neighbors neighbors, Route route) {
        PeerRelation pr = (PeerRelation) neighbor.getAttachment();

        if (route.isFrom(neighbor.getASIdentifier())) {
            return false;
        }

		switch (pr) {
			case CUSTOMER:
			case SIBLING:
			case ROUTEVIEWMONITOR:
			case RISMONITOR:
				return true;
			case PEER:
			case PROVIDER:
				// get the sender neighbor
	
				// if it is mine route - send it to everybody
				if (route.getPathLength() == 0) {
					return true;
				}

				Neighbor sender = neighbors.getNeighbor(route.getSender());

				PeerRelation relation = null;
				relation = (PeerRelation) sender.getAttachment();
				
				if (relation == PeerRelation.CUSTOMER || relation == PeerRelation.SIBLING) {
					// I want to send things from my customer and sibling to my
					// provider and my peer
					return true;
				}
				
				return false;
	
			default:
				throw new RuntimeException("WTF, pr=" + pr);
		}
    }

}

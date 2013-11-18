package nl.nlnetlabs.bgpsym01.route;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;

import org.apache.log4j.Logger;

public class PolicyImpl implements Policy {

	private static Logger log = Logger.getLogger(PolicyImpl.class);

	/*
	 * public boolean isAdvertisable(ASIdentifier myId, Neighbor neighbor,
	 * Neighbors neighbors, Route route) { return
	 * !route.isFrom(neighbor.getASIdentifier()); }
	 */

	public boolean isAdvertisable(ASIdentifier myId, Neighbor neighbor,
			Neighbors neighbors, Route route) {
		PeerRelation pr = (PeerRelation) neighbor.getAttachment();

		if (route.isFrom(neighbor.getASIdentifier())) {
			return false;
		}
		// if (route.getPathLength() > 18) {
		// log.info("size=" + route.getPathLength());
		// }

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
				PeerRelation relation = (PeerRelation) sender.getAttachment();
				if (relation == PeerRelation.CUSTOMER
						|| relation == PeerRelation.SIBLING) {
					// I want to send things from my customer and sibling to my
					// provider and my peer
					return true;
				}
				return false;
	
			default:
				throw new RuntimeException("WTF, pr=" + pr);
		}
	}

	public boolean isBetter(ASIdentifier myAsId, Prefix prefix, Route route1,
			Neighbor n1, Route route2, Neighbor n2) {
		boolean outcome = false;
		if (route1 == null) {
			if (log.isDebugEnabled()) {
				log.debug("route1 is null");
			}
			outcome = true;
		} else {
			outcome = route2.getHops().length <= route1.getHops().length;
		}

		if (log.isDebugEnabled()) {
			log.debug("outcome=" + outcome);
		}

		return outcome;
	}

}

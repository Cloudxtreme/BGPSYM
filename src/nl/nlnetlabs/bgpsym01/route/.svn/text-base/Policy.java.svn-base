package nl.nlnetlabs.bgpsym01.route;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;

// TODO - what with nonlinear-order route values?
public interface Policy {

    /**
     * @param myAsId
     *            TODO
     * @param prefix
     * @param route1
     * @param n1
     *            TODO
     * @param route2
     * @param n2
     *            TODO
     * @return true if route2 is better than route1 for prefix prefix
     */
    public boolean isBetter(ASIdentifier myAsId, Prefix prefix, Route route1, Neighbor n1, Route route2, Neighbor n2);

    /**
     * @param myId
     * @param neighbors
     *            TODO
     * @param route
     *            TODO
     * @param hisId
     * @return <b>true</b> if route for prefix is advertisable to the neighbor
     */
    public boolean isAdvertisable(ASIdentifier myId, Neighbor neighbor, Neighbors neighbors, Route route);

}

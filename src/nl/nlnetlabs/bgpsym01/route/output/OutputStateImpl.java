package nl.nlnetlabs.bgpsym01.route.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;
import nl.nlnetlabs.bgpsym01.route.Policy;

public class OutputStateImpl implements OutputState {

    private Policy policy;

    private ASIdentifier asIdentifier;

    private Neighbors neighbors;

    Map<Pair<Neighbor, Prefix>, Route> map;

    Map<Neighbor, List<Prefix>> filteredPrefixes;

    public void setAsIdentifier(ASIdentifier asIdentifier) {
        this.asIdentifier = asIdentifier;
    }

    public void setNeighbors(Neighbors neighbors) {
        this.neighbors = neighbors;
    }

	public Map<Pair<Neighbor, Prefix>, Route> getMap () {
		return this.map;
	}

	public Map<Neighbor, List<Prefix>> getPrefixes () {
		return this.filteredPrefixes;
	}

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public OutputStateImpl() {
        map = new HashMap<Pair<Neighbor, Prefix>, Route>();
        filteredPrefixes = new TreeMap<Neighbor, List<Prefix>>();
    }

    public void registerPrefixes(Neighbor neighbor, List<Prefix> prefixes) {
        List<Prefix> list = filteredPrefixes.get(neighbor);
        
        if (list == null) {
            list = new ArrayList<Prefix>();
            filteredPrefixes.put(neighbor, list);
        }
        // remove saved data about prefixes
        if (prefixes == null) {
        	map.clear();
        	list.clear();
        }
        else {
	        for (Prefix prefix : prefixes) {
	            map.remove(new Pair<Neighbor, Prefix>(neighbor, prefix));
	        }
	        
	        list.addAll(prefixes);
        }
        
    }

    public OutputState.UpdateToSendType getUpdateType(Neighbor neighbor, Prefix prefix, Route route, Route lastRoute) {
        return getUpdateTypeInternal(neighbor, prefix, route, getLastSentRoute(neighbor, prefix, lastRoute));
    }

    /**
     * Prefixes should be sent:
     * 
     * <pre>
     * lastRoute              route                     result
     *  null                  null                      NOTHING
     *  !null                 null                      WITHDRAWAL
     *  *                     good                      ANNOUNCE
     *  null                  bad                       NOTHING
     *  !null                 bad                       WITHDRAWAL
     * </pre>
     * 
     * bad and good is decided by
     * {@link #wasAdvertised(ASIdentifier, Neighbor, Route)}
     * 
     * @param neighbor
     * @param prefix
     * @param route
     * @param lastSentRoute
     * @return
     */
    OutputState.UpdateToSendType getUpdateTypeInternal(Neighbor neighbor, Prefix prefix, Route route, Route lastSentRoute) {

        // if the guy is not valid and this is one of the prefixes: always NONE
        if (!neighbor.isValid() && isFiltered(neighbor, prefix)) {
            return UpdateToSendType.NONE;
        }

        // first get situation where route is null
        if (route == null) {
            if (lastSentRoute == null) {
                // both are null - nothing to do, though this situation is a bit
                // strange...
                return UpdateToSendType.NONE;
            } else {
                // new is null and there was something - remove it please
                return UpdateToSendType.WITHDRAWAL;
            }
        }

        // now we know that route is not null

        // if it is the same as it was - nothing should be done
        if (route.equals(lastSentRoute)) {
            // it is exactly the same - lame; just ignore it
            return UpdateToSendType.NONE;
        } else if (policy.isAdvertisable(asIdentifier, neighbor, neighbors, route)) {
            // new route is cool - whatever it is (not equal to lastSentRoute)
            // is worth sending
            return UpdateToSendType.ANNOUNCE;
        } else {
            // now we're bad (new route cannot be propagated), let see what was
            // there before
            if (lastSentRoute == null) {
                // we did not send anything last time - just leave it as it is
                return UpdateToSendType.NONE;
            } else {
                // there was something, new is bad -- withdraw the old stuff
                return UpdateToSendType.WITHDRAWAL;
            }
        }
    }

    private boolean isFiltered(Neighbor neighbor, Prefix prefix) {
        List<Prefix> list = filteredPrefixes.get(neighbor);
        return list == null ? false : list.contains(prefix);
    }


    /**
     * <b>true</b> if this route would have been advertised to this neighbor
     */
    boolean wasAdvertised(ASIdentifier myId, Neighbor neighbor, Route route) {
        return route == null ? true : policy.isAdvertisable(myId, neighbor, neighbors, route);
    }

    Route getLastSentRoute(Neighbor neighbor, Prefix prefix, Route lastRoute) {
        Route lastRouteOut = null;
        Pair<Neighbor, Prefix> pair = getPair(neighbor, prefix);

        // we have to check whether map contains pair - null does not mean that
        // it is not there
        if (map.containsKey(pair)) {
            lastRouteOut = map.get(pair);
        } else {
            lastRouteOut = lastRoute;
        }
        // if the route was not advertised, then the withdrawal was sent;
        return wasAdvertised(asIdentifier, neighbor, lastRouteOut) ? lastRouteOut : null;
    }

    public void deferred(Neighbor neighbor, Prefix prefix, Route lastSentRoute) {
        Pair<Neighbor, Prefix> pair = getPair(neighbor, prefix);
        if (!map.containsKey(pair)) {
            map.put(pair, lastSentRoute);
        }
    }

    Pair<Neighbor, Prefix> getPair(Neighbor neighbor, Prefix prefix) {
        return new Pair<Neighbor, Prefix>(neighbor, prefix);
    }

    public void sent(Neighbor neighbor, Prefix prefix) {
        map.remove(getPair(neighbor, prefix));
    }

    public void deregisterPrefixes(Neighbor neighbor, List<Prefix> prefixes) {
        List<Prefix> list = filteredPrefixes.get(neighbor);
        if (list == null) {
            return;
        }
        
        if (prefixes == null) {
        	list.clear();
        }
        else {
        	list.removeAll(prefixes);
        }
    }

    public boolean hasRegisteredPrefixes(Neighbor neighbor) {
        List<Prefix> list = filteredPrefixes.get(neighbor);
        return list == null ? false : !list.isEmpty();
    }

    public Policy getPolicy() {
        return policy;
    }

    public ASIdentifier getAsIdentifier() {
        return asIdentifier;
    }

    public Neighbors getNeighbors() {
        return neighbors;
    }
}

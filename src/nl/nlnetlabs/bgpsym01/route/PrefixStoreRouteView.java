package nl.nlnetlabs.bgpsym01.route;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import nl.nlnetlabs.bgpsym01.callback.Callback;
import nl.nlnetlabs.bgpsym01.command.RouteViewDataResponse;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeController;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

/**
 * This is prefixStore as used by RouteView - prefixes never get propagated,
 * only info about them is stored
 */
public class PrefixStoreRouteView implements PrefixStore {

    private static Logger log = Logger.getLogger(PrefixStoreRouteView.class);

    private Map<Pair<ASIdentifier, Prefix>, RouteViewDataResponse> map = new HashMap<Pair<ASIdentifier, Prefix>, RouteViewDataResponse>();

    TimeController timeController;

    Callback callback;

    private XProperties properties;

    public PrefixStoreRouteView() {
        properties = XProperties.getInstance();
    }

    public void flush(ASIdentifier asId) {
    }

    // TODO - change this to return a list :)
    public RouteViewDataResponse getPrefixData(ASIdentifier originator, Prefix prefix) {
        return map.get(getPair(originator, prefix));
    }

    public void prefixReceived(ASIdentifier originator, Collection<Prefix> prefixList, Route route) {
        refreshMap(originator, prefixList, route);
        for (Prefix prefix : prefixList) {
            if (prefix.getNum() < properties.bogusPrefixMin) {
                callback.prefixReceived(originator, prefix, route);
            }
        }
    }

    private void refreshMap(ASIdentifier originator, Collection<Prefix> prefixList, Route route) {
        long now = timeController.getCurrentTime();
        for (Prefix prefix : prefixList) {
            Pair<ASIdentifier, Prefix> pair = getPair(originator, prefix);
            RouteViewDataResponse response = map.get(pair);
            if (response == null) {
                response = new RouteViewDataResponse(originator, prefix, now, now);
                response.length++;
                map.put(pair, response);
                writeLog(originator, prefix, response, route);
            } else {
                if (response.firstSeen == -1) {
                    response.firstSeen = now;
                }
                response.lastSeen = now;
                response.length++;
                writeLog(originator, prefix, response, route);
            }
        }
    }

    private void writeLog(ASIdentifier originator, Prefix prefix, RouteViewDataResponse response, Route route) {
        if (log.isInfoEnabled() && prefix.getNum() < properties.bogusPrefixMin && response.length >= 2) {
            log.info((response.length == 2 ? "second for " : "Rgot " + response.length + " for ") + prefix + ", time="
                    + (response.lastSeen - response.firstSeen) / 1000 + " ; " + originator + "; r=" + route);
        }

    }

    private Pair<ASIdentifier, Prefix> getPair(ASIdentifier originator, Prefix prefix) {
        return new Pair<ASIdentifier, Prefix>(originator, prefix);
    }

    public void prefixRemove(ASIdentifier asIdentifier, Collection<Prefix> prefixes) {
        refreshMap(asIdentifier, prefixes, null);
        for (Prefix prefix : prefixes) {
            if (prefix.getNum() < properties.bogusPrefixMin) {
                callback.withdrawalReceived(asIdentifier, prefix);
            }
        }
    }

    public void storePrefixesPermanent() throws IOException {
    }

    public void unflap(Prefix prefix, ASIdentifier asId) {
    }

    public void setTimeController(TimeController timeController) {
        this.timeController = timeController;
    }

    public Collection<RouteViewDataResponse> getPrefixDataList() {
        return map.values();
    }

    public PrefixStoreType getType() {
        return PrefixStoreType.ROUTEVIEW;
    }

    public void resetPrefixData(Prefix prefix) {
        for (RouteViewDataResponse response : map.values()) {
            if (response.prefix.equals(prefix)) {
                response.reset();
            }
        }
    }

    public TimeController getTimeController() {
        return timeController;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    
    public void dumpTables() {
    }
    
    public void dumpTables2() {
    }

}

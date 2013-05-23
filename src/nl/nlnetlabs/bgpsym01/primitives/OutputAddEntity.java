package nl.nlnetlabs.bgpsym01.primitives;

import nl.nlnetlabs.bgpsym01.cache.PrefixInfo;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;

public class OutputAddEntity extends OutputEntity {

    private Route route;

    public OutputAddEntity(Prefix prefix, Route newRoute, Route lastRoute) {
        super(prefix, lastRoute);
        this.route = newRoute;
    }

    public OutputAddEntity(PrefixInfo prefixInfo, Route newRoute, Route lastRoute) {
        super(prefixInfo, lastRoute);
        this.route = newRoute;
    }

    @Override
    public String toString() {
        return "OD " + getType() + " [" + prefix + "; " + route + " / " + getLastRoute() + "]";
    }

    public OutputAddEntity() {
    }

    @Override
    public OutputEntityType getType() {
        return OutputEntityType.ANNOUNCE;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

}

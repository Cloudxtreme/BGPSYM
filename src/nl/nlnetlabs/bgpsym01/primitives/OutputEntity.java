package nl.nlnetlabs.bgpsym01.primitives;

import nl.nlnetlabs.bgpsym01.cache.PrefixInfo;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;

abstract public class OutputEntity {

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    protected Prefix prefix;
    protected Route lastRoute;
    protected PrefixInfo prefixInfo;

    abstract public OutputEntityType getType();

    public OutputEntity(PrefixInfo prefixInfo, Route lastroute) {
        this.lastRoute = lastroute;
        this.prefixInfo = prefixInfo;
        this.prefix = prefixInfo.getPrefix();
    }

    public OutputEntity(Prefix prefix, Route route) {
        this.prefix = prefix;
        this.lastRoute = route;
    }

    public OutputEntity() {

    }

    public Prefix getPrefix() {
        return prefix;
    }

    public void setPrefix(Prefix prefix) {
        this.prefix = prefix;
    }

    public Route getLastRoute() {
        return lastRoute;
    }

    protected void setLastRoute(Route oldRoute) {
        this.lastRoute = oldRoute;
    }

}
package nl.nlnetlabs.bgpsym01.primitives;

import nl.nlnetlabs.bgpsym01.cache.PrefixInfo;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;

public class OutputRemoveEntity extends OutputEntity {

    public OutputRemoveEntity(Prefix prefix, Route route) {
        super(prefix, route);
    }

    public OutputRemoveEntity(PrefixInfo prefixInfo, Route route) {
        super(prefixInfo, route);
    }

    public OutputRemoveEntity() {

    }

    @Override
    public String toString() {
        return "OD " + getType() + " [" + prefix + "; " + lastRoute + "]";
    }

    @Override
    public OutputEntityType getType() {
        return OutputEntityType.WITHDRAWAL;
    }

}

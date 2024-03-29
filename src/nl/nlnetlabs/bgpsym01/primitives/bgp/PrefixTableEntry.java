package nl.nlnetlabs.bgpsym01.primitives.bgp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.mocks.FlapTimerAdapter;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimer;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimerImpl;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EExternalizable;

import org.apache.log4j.Logger;

public class PrefixTableEntry implements EExternalizable, Cloneable  {

    private static Logger log = Logger.getLogger(PrefixTableEntry.class);

    public static Map<ASIdentifier, PrefixTableEntry> getEmptyMap() {
        return new HashMap<ASIdentifier, PrefixTableEntry>();
    }

    private Route route;

    private ASIdentifier orignator;

    private FlapTimer flapTimer;

    private boolean containsMe = false;

    public void invalidate(boolean containsMe) {
        route = null;
        this.containsMe = containsMe;
    }

    public PrefixTableEntry() {
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public PrefixTableEntry(Route route) {
        // this.prefix = prefix;
        this.route = route;
    }

    public ASIdentifier getOriginator() {
        return orignator;
    }

    public void setOrignator(ASIdentifier originator) {
        this.orignator = originator;
    }

    public void setOrignator(Neighbor neighbor) {
        setOrignator(neighbor.getASIdentifier());
    }

    @Override
    public String toString() {
        return "PTE ;r=" + route;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PrefixTableEntry) {
            PrefixTableEntry pte = (PrefixTableEntry) obj;
            if (pte.route == null && pte.route != route || pte.route != null && !pte.route.equals(route)) {
                return false;
            }
            if (flapTimer == null && pte.flapTimer != null) {
                return false;
            }

            if (!flapTimer.equals(pte.flapTimer)) {
                return false;
            }

            if (isValid() != pte.isValid()) {
                return false;
            }

            return true;
        }
        return false;
    }

    public FlapTimer getFlapTimer() {
        return flapTimer;
    }

    public void setFlapTimer(FlapTimer flapTimer) {
        this.flapTimer = flapTimer;
    }

    public boolean isValid() {
        return route != null;
    }

    public void readExternal(EDataInputStream in) throws IOException {
        orignator = ASFactory.getInstance(in.readBits(SystemConstants.AS_SIZE_BITS));
        if (in.readBoolean()) {
            route = new Route();
            route.readExternal(in);
        }

        // TODO - magic stuff where to find who is CISCO and who's not...
        boolean isFlapTimerImpl = in.readBoolean();
        log.warn("read flap timer!!!");

        if (isFlapTimerImpl) {
            flapTimer = new FlapTimerImpl();
            flapTimer.readExternal(in);
        } else {
            flapTimer = new FlapTimerAdapter();
        }

    }

    public void writeExternal(EDataOutputStream out) throws IOException {
        out.writeBits(orignator.getInternalId(), SystemConstants.AS_SIZE_BITS);

        out.writeBoolean(route != null);
        if (route != null) {
            route.writeExternal(out);
        }

        boolean isFlapTimerImpl = flapTimer instanceof FlapTimerImpl;
        out.writeBoolean(isFlapTimerImpl);
        flapTimer.writeExternal(out);
    }

    public boolean isContainsMe() {
        return containsMe;
    }

}

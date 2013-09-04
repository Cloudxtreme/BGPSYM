package nl.nlnetlabs.bgpsym01.primitives.bgp;

import java.io.IOException;
import java.util.Arrays;

import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EExternalizable;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.annotations.XStreamAlias;

//@XStreamConverter(SerializableConverter.class)
@XStreamAlias("r")
public class Route implements EExternalizable {

    private static final int ROUTE_LENGTH_BITS = 10;

    private static Logger log = Logger.getLogger(Route.class);

    public ASIdentifier[] hops;

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Route) {
            return Arrays.equals(hops, ((Route) obj).hops);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (hops == null) ? -3 : hops.hashCode();
    }

    public Route() {
    }
    
    public Route (ASIdentifier[] hops) {
    	this.hops = hops;
    }

    public ASIdentifier[] getHops() {
        return hops;
    }
    
    public Route clone () {
    	Route clone = new Route();
    	if (hops != null && hops.length > 0) {
    		clone.hops = new ASIdentifier[hops.length];
    		System.arraycopy(hops, 0, clone.hops, 0, hops.length);
    	}
    	return clone;
    }

    public Route copyWithMeOnPath(ASIdentifier asIdentifier) {
        Route n = new Route();
        if (hops != null) {
            n.hops = new ASIdentifier[hops.length + 1];
            System.arraycopy(hops, 0, n.hops, 0, hops.length);
            n.hops[n.hops.length - 1] = asIdentifier;
        } else {
            n.hops = new ASIdentifier[1];
            n.hops[0] = asIdentifier;
        }
        //log.info("old route: "+hops+" new route: "+n);
        
        return n;
    }

    public void setHops(ASIdentifier[] hops) {
        this.hops = hops.length > 0 ? hops : null;
    }

    public void createEmptyHops() {
        this.hops = null;
    }

    public int getPathLength() {
        return (hops == null) ? 0 : hops.length;
    }

    public boolean containsMe(ASIdentifier asIdentifier) {
        if (log.isDebugEnabled()) {
            log.debug("we have: " + hops + ", looking for: " + asIdentifier);
        }
        if (hops == null) {
            return false;
        }
        for (ASIdentifier asId : hops) {
            if (asId.equals(asIdentifier)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return hops == null ? null : Arrays.toString(hops);
    }

    public void readExternal(EDataInputStream in) throws IOException {
        // route length
        int count = in.readBits(ROUTE_LENGTH_BITS);

        if (count > 0) {
            hops = new ASIdentifier[count];
            for (int i = 0; i < count; i++) {
                hops[i] = ASFactory.getInstance(in.readBits(SystemConstants.AS_SIZE_BITS));
            }
        }
    }

    public void writeExternal(EDataOutputStream out) throws IOException {
        // route length
        out.writeBits(getPathLength(), ROUTE_LENGTH_BITS);

        if (hops != null) {
            for (ASIdentifier hop : hops) {
                out.writeBits(hop.getInternalId(), SystemConstants.AS_SIZE_BITS);
            }
        }
    }
    
    public ASIdentifier getOrigin () {
    	if (hops == null) {
    		return null;
    	}
    	
    	return hops[0];
    }

    public ASIdentifier getSender() {
        if (hops == null) {
            return null;
        }
        return hops[hops.length - 1];
    }

    public boolean isFrom(ASIdentifier identifier) {
        return identifier.equals(getSender());
    }

}

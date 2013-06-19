package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EExternalizable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("response")
public class RouteViewDataResponse implements EExternalizable {

    @Override
    public String toString() {
        long diff = lastSeen == -1 ? -1 : lastSeen - firstSeen;
        return "num="+prefix.getNum() + ";diff=" + diff + ";first=" + firstSeen + ";last=" + lastSeen + ";len=" + length + ";asId="+asId+";route="+route;
    }

	@XStreamAlias("origin")
    public ASIdentifier asId;

	@XStreamAlias("prefix")
    public Prefix prefix;

    public long firstSeen;

    public long lastSeen;

    // not tested
    public int length;

	@XStreamAlias("route")
	public Route route;

    public RouteViewDataResponse(ASIdentifier asId, Prefix prefix, long firstSeen, long lastSeen) {
        super();
        this.asId = asId;
        this.prefix = prefix;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
    }
    
	public RouteViewDataResponse(ASIdentifier asId, Prefix prefix, long firstSeen, long lastSeen, Route route) {
        super();
        this.asId = asId;
        this.prefix = prefix;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
		this.route = route;
    }

    public RouteViewDataResponse() {
    }
    
    public RouteViewDataResponse clone() {
    	Route route = (this.route != null) ? this.route.clone() : null;
    	RouteViewDataResponse response = new RouteViewDataResponse(asId, prefix, firstSeen, lastSeen, route);
    	response.length = this.length;
    	return response;
    }

    public void readExternal(EDataInputStream in) throws IOException {
        asId = ASFactory.getInstance(in.readInt());
        prefix = Prefix.getInstance(in.readInt());
        firstSeen = in.readLong();
        lastSeen = in.readLong();
        length = in.readInt();
    }

    public void writeExternal(EDataOutputStream out) throws IOException {
        out.writeInt(asId.getInternalId());
        out.writeInt(prefix.getNum());
        out.writeLong(firstSeen);
        out.writeLong(lastSeen);
        out.writeInt(length);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RouteViewDataResponse) {
            RouteViewDataResponse tmp = (RouteViewDataResponse) obj;
            return asId.equals(tmp.asId) && tmp.lastSeen == lastSeen && tmp.firstSeen == firstSeen && tmp.prefix.equals(prefix);
        }
        return false;
    }

    public void reset() {
        lastSeen = -1;
        firstSeen = -1;
        length = 0;
    }

}

package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EExternalizable;

public class RouteViewDataResponse implements EExternalizable {

    @Override
    public String toString() {
        long diff = lastSeen == -1 ? -1 : lastSeen - firstSeen;
        return prefix.getNum() + " ; " + diff + " ; " + firstSeen + "; " + lastSeen + " ; " + length;
    }

    public ASIdentifier asId;

    public Prefix prefix;

    public long firstSeen;

    public long lastSeen;

    // not tested
    public int length;

    public RouteViewDataResponse(ASIdentifier asId, Prefix prefix, long firstSeen, long lastSeen) {
        super();
        this.asId = asId;
        this.prefix = prefix;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
    }

    public RouteViewDataResponse() {
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

package nl.nlnetlabs.bgpsym01.primitives;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EExternalizable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("tcpm")
public class TCPMessage implements EExternalizable {

    private ASIdentifier asId;

    private BGPUpdate update;

    public BGPUpdate getUpdate() {
        return update;
    }

    public void setUpdate(BGPUpdate update) {
        this.update = update;
    }

    @Override
    public String toString() {
        return "TCPM, to=" + asId + ", UP=" + update;
    }

    public void readExternal(EDataInputStream in) throws IOException {
        asId = ASFactory.getInstance(in.readBits(SystemConstants.AS_SIZE_BITS));
        update = new BGPUpdate();
        update.readExternal(in);
    }

    public void writeExternal(EDataOutputStream out) throws IOException {
        out.writeBits(asId.getInternalId(), SystemConstants.AS_SIZE_BITS);
        update.writeExternal(out);
    }

    public ASIdentifier getAsId() {
        return asId;
    }

    public void setAsId(ASIdentifier asId) {
        this.asId = asId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TCPMessage) {
            TCPMessage tmp = (TCPMessage) obj;
            return asId.equals(tmp.asId) && update.equals(tmp.update);
        }
        return false;
    }

}

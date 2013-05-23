package nl.nlnetlabs.bgpsym01.xstream;

import java.rmi.registry.Registry;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("neighbor")
public class XNeighbor {

    private ASIdentifier asIdentifier;

    @XStreamOmitField
    private Registry registry;

    @XStreamOmitField
    private Neighbor realNeighbor;

    private Object attachment;

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Neighbor getRealNeighbor() {
        return realNeighbor;
    }

    public void setRealNeighbor(Neighbor realNeighbor) {
        this.realNeighbor = realNeighbor;
    }

    @Override
    public int hashCode() {
        return asIdentifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof XNeighbor) {
            XNeighbor tmp = (XNeighbor) obj;
            return asIdentifier.equals(tmp.asIdentifier);
        }
        return false;
    }

    public ASIdentifier getAsIdentifier() {
        return asIdentifier;
    }

    public void setAsIdentifier(ASIdentifier asIdentifier) {
        this.asIdentifier = asIdentifier;
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

}

package nl.nlnetlabs.bgpsym01.neighbor.impl;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.route.MRAITimer;

public class NeighborImplTCP implements Neighbor {

    private ASIdentifier asIdentifier;

    private Object attachment;

    private MRAITimer timer;

    private boolean valid = true;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    private TCPConnection connection;

    public NeighborImplTCP(ASIdentifier asIdentifier, TCPConnection connection) {
        this.asIdentifier = asIdentifier;
        this.connection = connection;
    }

    @Override
    public int hashCode() {
        return asIdentifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NeighborImplTCP) {
            NeighborImplTCP tmp = (NeighborImplTCP) obj;
            return asIdentifier.equals(tmp.getASIdentifier());
        }
        return false;
    }

    public ASIdentifier getASIdentifier() {
        return asIdentifier;
    }

    public void send(BGPUpdate update) {
        connection.send(asIdentifier, update);
    }
    
    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    public MRAITimer getTimer() {
        return timer;
    }

    public void setTimer(MRAITimer timer) {
        this.timer = timer;
    }

    public int compareTo(Neighbor o) {
        return asIdentifier.compareTo(o.getASIdentifier());
    }

}

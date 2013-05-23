package nl.nlnetlabs.bgpsym01.route.output;

import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.command.Rewriter;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.route.MRAITimer;

public class NeighborMock implements Neighbor {

    private ASIdentifier asIdentifier;

    private Object attachment;

    private MRAITimer timer;

    public boolean valid = true;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    private ArrayList<BGPUpdate> updates = new ArrayList<BGPUpdate>();

    public NeighborMock(ASIdentifier as) {
        this.asIdentifier = as;
    }

    public void attach(Object object) {
        attachment = object;
    }

    public ASIdentifier getASIdentifier() {
        return asIdentifier;
    }

    public Object getAttachment() {
        return attachment;
    }

    public void send(BGPUpdate update) {
        updates.add((BGPUpdate) Rewriter.rewrite(update, update.getClass()));
    }

    public ArrayList<BGPUpdate> getUpdates() {
        return updates;
    }

    public void setUpdates(ArrayList<BGPUpdate> updates) {
        this.updates = updates;
    }

    public MRAITimer getTimer() {
        return timer;
    }

    public void setTimer(MRAITimer timer) {
        this.timer = timer;
    }

    @Override
    public String toString() {
        return asIdentifier.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NeighborMock) {
            return asIdentifier.equals(((NeighborMock) obj).asIdentifier);
        }
        return false;
    }

    public int compareTo(Neighbor o) {
        return asIdentifier.compareTo(o.getASIdentifier());
    }

}

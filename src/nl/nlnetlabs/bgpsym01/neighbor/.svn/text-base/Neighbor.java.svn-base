package nl.nlnetlabs.bgpsym01.neighbor;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.route.MRAITimer;

public interface Neighbor extends Comparable<Neighbor> {

    public void send(BGPUpdate update);

    public MRAITimer getTimer();

    public ASIdentifier getASIdentifier();

    public Object getAttachment();

    public boolean isValid();

    public void setValid(boolean valid);

}
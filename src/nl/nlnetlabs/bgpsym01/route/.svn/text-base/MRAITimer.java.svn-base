package nl.nlnetlabs.bgpsym01.route;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;

public interface MRAITimer {

    public boolean canSendNow();

    public long getStartTime();

    public void sent();

    public void reset();

    public ASIdentifier getAsIdentifier();

    public boolean isTicking();

    public void setTicking(boolean ticking);

}
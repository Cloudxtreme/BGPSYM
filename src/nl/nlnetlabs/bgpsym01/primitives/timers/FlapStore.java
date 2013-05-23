package nl.nlnetlabs.bgpsym01.primitives.timers;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.types.InputGenerator;

public interface FlapStore extends InputGenerator {

    public void register(Prefix prefix, ASIdentifier asId, FlapTimer timer);

    public boolean hasSomething();

    public long getReadyTime();

    public int size();

}

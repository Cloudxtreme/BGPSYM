package nl.nlnetlabs.bgpsym01.primitives.types;

import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;

public interface InputGenerator {

    public boolean hasSomething();

    public long getReadyTime();

    public Update getUpdate();

}

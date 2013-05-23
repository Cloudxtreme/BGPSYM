package nl.nlnetlabs.bgpsym01.mock;

import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapStore;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimer;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;

public class FlapStoreMock implements FlapStore {

    public ArrayList<Pair<Prefix, FlapTimer>> list = new ArrayList<Pair<Prefix, FlapTimer>>();
    public ArrayList<Prefix> deregisterList = new ArrayList<Prefix>();

    public long waitingTime = -1;
    public boolean ready;
    public boolean hasSomething;
    public Pair<ASIdentifier, Prefix> next;

    public long getReadyTime() {
        return waitingTime;
    }

    public boolean hasSomething() {
        return hasSomething;
    }

    public Pair<ASIdentifier, Prefix> next() {
        return next;
    }

    public void register(Prefix prefix, ASIdentifier asId, FlapTimer timer) {
        list.add(new Pair<Prefix, FlapTimer>(prefix, timer));
    }

    public int size() {
        return 0;
    }

    public Update getUpdate() {
        throw new NotImplementedException();
    }

}

package nl.nlnetlabs.bgpsym01.route.output;

import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;
import nl.nlnetlabs.bgpsym01.route.MRAIStore;
import nl.nlnetlabs.bgpsym01.route.MRAITimer;

public class MRAIStoreMock implements MRAIStore {
    public boolean hasSomething;
    public long waitingTime = -1;
    public int lastReturned;

    public List<Pair<ASIdentifier, MRAITimer>> list = new ArrayList<Pair<ASIdentifier, MRAITimer>>();

    public long getReadyTime() {
        return waitingTime;
    }

    public ASIdentifier next() {
        hasSomething = lastReturned + 1 == list.size();
        return list.get(lastReturned++).key;
    }

    public void register(ASIdentifier asId, MRAITimer timer) {
        list.add(new Pair<ASIdentifier, MRAITimer>(asId, timer));
        hasSomething = true;
    }

    public boolean hasSomething() {
        return hasSomething;
    }

    public List<Pair<ASIdentifier, MRAITimer>> getList() {
        return list;
    }

    public void setList(List<Pair<ASIdentifier, MRAITimer>> list) {
        this.list = list;
    }

    public int size() {
        return 0;
    }

    public Update getUpdate() {
        throw new NotImplementedException();
    }

}
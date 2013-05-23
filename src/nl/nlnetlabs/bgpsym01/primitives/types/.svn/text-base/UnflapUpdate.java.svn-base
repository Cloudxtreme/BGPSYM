package nl.nlnetlabs.bgpsym01.primitives.types;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.RunnableUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;

public class UnflapUpdate extends RunnableUpdate {

    private Pair<ASIdentifier, Prefix> pair;

    @Override
    public UpdateType getType() {
        return Update.UpdateType.RUNNABLE_UPDATE;
    }

    @Override
    public void run(BGPProcess process) {
        process.getStore().unflap(pair.value, pair.key);
    }

    public Pair<ASIdentifier, Prefix> getPair() {
        return pair;
    }

    public void setPair(Pair<ASIdentifier, Prefix> prefix) {
        this.pair = prefix;
    }

}

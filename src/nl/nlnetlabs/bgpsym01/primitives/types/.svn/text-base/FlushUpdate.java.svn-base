package nl.nlnetlabs.bgpsym01.primitives.types;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.RunnableUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;

public class FlushUpdate extends RunnableUpdate {

    private ASIdentifier asId;

    @Override
    public UpdateType getType() {
        return Update.UpdateType.RUNNABLE_UPDATE;
    }

    @Override
    public void run(BGPProcess process) {
        process.getStore().flush(asId);
    }

    public ASIdentifier getAsId() {
        return asId;
    }

    public void setAsId(ASIdentifier asId) {
        this.asId = asId;
    }

}

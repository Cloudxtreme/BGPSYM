package nl.nlnetlabs.bgpsym01.primitives.bgp;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;

abstract public class RunnableUpdate implements Update {

    public void readExternal(EDataInputStream in) throws IOException {
    }

    public void writeExternal(EDataOutputStream out) throws IOException {
    }

    public UpdateType getType() {
        return UpdateType.RUNNABLE_UPDATE;
    }

    abstract public void run(BGPProcess process);

}

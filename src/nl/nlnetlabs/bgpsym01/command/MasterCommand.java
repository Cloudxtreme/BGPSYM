package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.main.tcp.TCPStart;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;

import org.apache.log4j.Logger;

public abstract class MasterCommand extends CoordinationCommand {

	private static Logger log = Logger.getLogger(MasterCommand.class);

    private int processId;

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
    }

    /**
     * Redefine this method if you want to be able to react when all AS do their
     * job...
     * 
     * Will NEVER be called by two threads in parallel.
     * 
     * @return
     */
    protected int decCount() {
        throw new NotImplementedException();
    }

    public synchronized void sent() {
        int count = decCount();
        if (count == 0) {
            sendAckToCoordinator();
        }
    }

    protected TCPStart jvm;

    public MasterCommand() {
        super();
    }

    public void setJvm(TCPStart jvm) {
        this.jvm = jvm;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    protected void sendAckToCoordinator() {
        AckCommand ackCommand = new AckCommand();
        jvm.getCst().sendCommand(ackCommand);
    }

    protected void waitForEmptyQueues() {
        throw new NotImplementedException();
        // for (BGPProcess process : jvm.getProcesses().values()) {
        // process.getQueue().waitForEmpty();
        // }
    }

}

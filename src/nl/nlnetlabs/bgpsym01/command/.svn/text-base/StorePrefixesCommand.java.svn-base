package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.main.StorageCompressor;
import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.RunnableUpdate;
import nl.nlnetlabs.bgpsym01.primitives.types.ShutdownadbleThread;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;

public class StorePrefixesCommand extends MasterCommand {

    private Thread thread;

    private int received;

    public StorePrefixesCommand() {
        throw new BGPSymException("storage not supported - has to be aware of many JVMs per node");
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.STORE_PREFIXES;
    }

    @Override
    public void process() {
        thread = new ShutdownadbleThread() {

            private StorageCompressor storageCompressor = new StorageCompressor();

            @Override
            public void run() {
                setName("store_" + Tools.getInstance().getProcNum());
                waitForEmptyQueues();
                RunnableUpdate su = new RunnableUpdate() {

                    @Override
                    public void run(BGPProcess process) {
                        try {
                            // store prefixes
                            process.getStore().storePrefixesPermanent();
                        } catch (IOException e1) {
                            throw new BGPSymException(e1);
                        } finally {
                            // let us know that you did it
                            prefixesStored();
                        }
                    }

                    @Override
                    public String toString() {
                        return "STORE_PERMAMENT, procNum=" + Tools.getInstance().getProcNum();
                    }

                };

                for (BGPProcess process : jvm.getProcesses().values()) {
                    process.getQueue().addMessage(su);
                }

                synchronized (this) {
                    while (received != jvm.getProcesses().size()) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }

                storageCompressor.compressStorage();
                sendAckToCoordinator();

            }

            @Override
            public void shutdown() {
                storageCompressor.shutdown();
            }

        };
        thread.start();
    }

    public void prefixesStored() {
        synchronized (thread) {
            received++;
            thread.notify();
        }
    }

}

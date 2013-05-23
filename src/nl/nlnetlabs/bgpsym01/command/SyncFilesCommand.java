package nl.nlnetlabs.bgpsym01.command;

import nl.nlnetlabs.bgpsym01.cache.PrefixCache;
import nl.nlnetlabs.bgpsym01.main.ObjectRegister;
import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.primitives.bgp.RunnableUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;

public class SyncFilesCommand extends MasterCommand {

    private static final String SYNC_ = "sync_";
    private int waitingFor;

    @Override
    public CommandType getCommandType() {
        return CommandType.SYNC_FILES;
    }

    private synchronized void done() {
        waitingFor--;
        if (waitingFor == 0) {
            notify();
        }
    }

    @Override
    public void process() {
        final SyncFilesCommand parent = this;
        Thread thread = new Thread() {

            @Override
            public void run() {
                setName(SYNC_ + Tools.getInstance().getProcNum());
                waitingFor = jvm.getProcesses().size();
                Update update = new RunnableUpdate() {

                    @Override
                    public void run(BGPProcess process) {
                        PrefixCache cache = (PrefixCache) ObjectRegister.getInstance().get(process.getAsIdentifier(), ObjectRegister.Type.CACHE);
                        if (cache != null) {
                            cache.flush();
                        }
                        done();
                    }
                };
                for (BGPProcess process : jvm.getProcesses().values()) {
                    process.getQueue().addMessage(update);
                }

                synchronized (parent) {
                    while (waitingFor != 0) {
                        try {
                            parent.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    sendAckToCoordinator();
                }
            }
        };
        thread.start();
    }
}

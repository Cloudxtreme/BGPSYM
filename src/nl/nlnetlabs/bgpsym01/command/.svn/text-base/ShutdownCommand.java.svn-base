package nl.nlnetlabs.bgpsym01.command;

import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.primitives.types.ShutdownadbleThread;

import org.apache.log4j.Logger;

public class ShutdownCommand extends MasterCommand {
    private static final int MAX_JOIN_WAITING_TIME = 000;
    private static final String KILLER_ = "killer_";
    private static Logger log = Logger.getLogger(ShutdownCommand.class);

    private Object monitor = new Object();
    private boolean finished;
    private Thread thread;

    @Override
    public CommandType getCommandType() {
        return CommandType.SHUTDOWN;
    }

    @Override
    public void process() {
        thread = new Thread() {

            @Override
            public void run() {
                ArrayList<Thread> threads = new ArrayList<Thread>();
                setName(KILLER_ + Tools.getInstance().getProcNum());

                int active = Thread.activeCount();
                Thread[] thArray = new Thread[active];
                Thread.enumerate(thArray);

                for (Thread thread : thArray) {
                    if (thread != null && thread instanceof ShutdownadbleThread) {
                        ((ShutdownadbleThread) thread).shutdown();
                        threads.add(thread);
                    }
                }

                for (Thread thread : threads) {
                    try {
                        thread.join(MAX_JOIN_WAITING_TIME);
                        if (thread.isAlive()) {
                            log.warn("thread " + thread.getName() + " did not join");
                        }
                    } catch (InterruptedException e) {
                    }
                }
                synchronized (monitor) {
                    finished = true;
                    monitor.notify();
                }
            }
        };
        thread.start();
    }

    public void waitForFinished() {
        synchronized (monitor) {
            while (!finished) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        // make this guy disappear
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
    }

}

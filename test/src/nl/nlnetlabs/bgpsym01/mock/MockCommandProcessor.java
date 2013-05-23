package nl.nlnetlabs.bgpsym01.mock;

import java.util.LinkedList;

import nl.nlnetlabs.bgpsym01.primitives.types.ShutdownadbleThread;

import org.apache.log4j.Logger;

public class MockCommandProcessor extends ShutdownadbleThread {

    private static Logger log = Logger.getLogger(MockCommandProcessor.class);

    boolean shutdown;

    LinkedList<MockCommand> commands = new LinkedList<MockCommand>();

    public synchronized void addCommand(MockCommand command) {
        commands.add(command);
        notify();
    }

    @Override
    public void run() {
        synchronized (this) {
            while (!shutdown && commands.size() == 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            if (shutdown) {
                return;
            }
            if (commands.size() > 0) {
                MockCommand command = commands.removeFirst();
                command.process();
            } else {
                log.warn("commands.size()==0");
            }
        }
    }

    @Override
    public synchronized void shutdown() {
        shutdown = true;
        notifyAll();
    }

}

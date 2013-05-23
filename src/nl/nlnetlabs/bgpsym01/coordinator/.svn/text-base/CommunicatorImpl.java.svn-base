package nl.nlnetlabs.bgpsym01.coordinator;

import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.command.MasterCommand;
import nl.nlnetlabs.bgpsym01.xstream.XRegistry;

public class CommunicatorImpl implements Communicator {

    SelectorThread selectorThread;

    public CommunicatorImpl(Coordinator coordinator, ArrayList<XRegistry> registries, int port) {
        selectorThread = new SelectorThread(coordinator, registries, port);
        selectorThread.start();
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.coordinator.Communicator#sendCommand(nl.nlnetlabs.bgpsym01.command.MasterCommand)
     */
    public void sendCommand(MasterCommand command) {
        selectorThread.send(command);
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.coordinator.Communicator#shutdown()
     */
    public void shutdown() {
        selectorThread.shutdown();
        try {
            selectorThread.join();
        } catch (InterruptedException e) {
        }
    }

}

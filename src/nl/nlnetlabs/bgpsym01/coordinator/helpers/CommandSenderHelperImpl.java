package nl.nlnetlabs.bgpsym01.coordinator.helpers;

import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.command.AckCommand;
import nl.nlnetlabs.bgpsym01.command.AnnounceCommand;
import nl.nlnetlabs.bgpsym01.command.MasterCommand;
import nl.nlnetlabs.bgpsym01.command.SyncFilesCommand;
import nl.nlnetlabs.bgpsym01.command.WaitForEmptyCommand;
import nl.nlnetlabs.bgpsym01.coordinator.Communicator;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.xstream.XRegistry;

import org.apache.log4j.Logger;

/**
 * Takes responsibility for sending commands, updates and so on.
 * 
 * Besides {@link DisconnectHelper} is the only entity that have direct access
 * to {@link Communicator}
 */
public class CommandSenderHelperImpl implements CommandSenderHelper {

    private static Logger log = Logger.getLogger(CommandSenderHelperImpl.class);

    private Object attachment;

    private Communicator communicator;

    private ArrayList<ASIdentifier> ases;

    private ArrayList<XRegistry> registries;

    private boolean allHostsHere;

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.coordinator.helpers.CommandSenderHelper#sendUpdate(int)
     */
    public void sendUpdate(int count) {
        sendUpdate(count, count % ases.size());
    }

    private void sendUpdate(int count, int asNum) {
        BGPUpdate u = new BGPUpdate(count);
        Route route = new Route();
        ASIdentifier asId = ASFactory.getInstance(asNum);
        route.createEmptyHops();
        u.setRoute(route);
        u.setSender(asId);
        AnnounceCommand command = new AnnounceCommand();
        command.setUpdate(u);
        command.setRecipient(asId);
        communicator.sendCommand(command);
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.coordinator.helpers.CommandSenderHelper#sendUpdate(java.util.List, nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier)
     */
    public int sendUpdate(List<Prefix> prefixList, ASIdentifier asId) {
        return sendUpdate(prefixList, null, asId);
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.coordinator.helpers.CommandSenderHelper#sendUpdate(java.util.List, nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier)
     */
    public int sendUpdate(List<Prefix> prefixList, List<Prefix> withdrawals, ASIdentifier asId) {
        BGPUpdate u = new BGPUpdate();
        u.setPrefixes(prefixList);
        u.setWithdrawals(withdrawals);
        Route route = new Route();
        route.createEmptyHops();
        u.setRoute(route);
        u.setSender(asId);
        AnnounceCommand command = new AnnounceCommand();
        command.setUpdate(u);
        command.setRecipient(asId);
        communicator.sendCommand(command);
        return 1;
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.coordinator.helpers.CommandSenderHelper#waitForEmptyQueues()
     */
    public void waitForEmptyQueues() {
        sendToAllHosts(new WaitForEmptyCommand());
        waitForAllHosts();
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.coordinator.helpers.CommandSenderHelper#waitForAllHosts()
     */
    public void waitForAllHosts() {
        synchronized (this) {
            while (!allHostsHere) {
                try {
                    wait();
                } catch (InterruptedException e) {
                	log.info(e);
                }
            }
            allHostsHere = false;
            AckCommand.resetCounter();
        }
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.coordinator.helpers.CommandSenderHelper#sendToAllHosts(nl.nlnetlabs.bgpsym01.command.MasterCommand)
     */
    public void sendToAllHosts(MasterCommand command) {
        for (int i = 0; i < registries.size(); i++) {
            sendToAHost(command, i);
        }
    }

    public void sendToAHost(MasterCommand command, int i) {
        command.setProcessId(i);
        communicator.sendCommand(command);
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.coordinator.helpers.CommandSenderHelper#sendSyncCommand()
     */
    public void sendSyncCommand() {
        if (log.isInfoEnabled()) {
            log.info("sending sync");
        }
        long start = System.currentTimeMillis();
        sendToAllHosts(new SyncFilesCommand());
        waitForAllHosts();
        
        if (log.isInfoEnabled()) {
            log.info("synced, time=" + (System.currentTimeMillis() - start));
        }
    }

    public Communicator getCommunicator() {
        return communicator;
    }

    public void setCommunicator(Communicator communicator) {
        this.communicator = communicator;
    }

    public ArrayList<ASIdentifier> getAses() {
        return ases;
    }

    public void setAses(ArrayList<ASIdentifier> ases) {
        this.ases = ases;
    }

    public ArrayList<XRegistry> getRegistries() {
        return registries;
    }

    public void setRegistries(ArrayList<XRegistry> registries) {
        this.registries = registries;
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.coordinator.helpers.CommandSenderHelper#ackReceived()
     */
    public void ackReceived() {
        synchronized (this) {
            allHostsHere = true;
            notify();
        }
    }

    public void attach(Object attachment) {
        this.attachment = attachment;
    }

    public Object attachment() {
        return attachment;
    }

}

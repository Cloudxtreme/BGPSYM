package nl.nlnetlabs.bgpsym01.coordinator.helpers;

import java.util.List;

import nl.nlnetlabs.bgpsym01.command.MasterCommand;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;

public interface CommandSenderHelper {

    /**
     * Sends prefix to a node (counts the node number itself).
     * 
     * @param count
     *            - prefix number
     */
    public void sendUpdate(int count);

    /**
     * Send a queue of prefixes to a particular node
     * 
     * @param prefixList
     *            prefixes queue
     * @param asId
     *            node
     * @return
     */
    public int sendUpdate(List<Prefix> prefixList, ASIdentifier asId);

    /**
     * Send a queue of prefixes to a particular node
     * 
     * @param prefixList
     *            prefixes queue
     * @param withdrawals
     *            TODO
     * @param asId
     *            node
     * @return
     */
    public int sendUpdate(List<Prefix> prefixList, List<Prefix> withdrawals, ASIdentifier asId);

    /**
     * Waits till all queues are empty
     * 
     * @see BGPProcess#getQueue()
     */
    public void waitForEmptyQueues();

    /**
     * Waits till all hosts send ack
     */
    public void waitForAllHosts();

    /**
     * Send command to all hosts
     * 
     * @param command
     *            command to be sent
     */
    public void sendToAllHosts(MasterCommand command);

    public void sendToAHost(MasterCommand command, int hostNum);

    public void sendSyncCommand();

    /**
     * Called from outside when ack has been received
     */
    public void ackReceived();

    public void attach(Object attachment);

    public Object attachment();

}
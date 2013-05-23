package nl.nlnetlabs.bgpsym01.coordinator.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.command.ConnectCommand;
import nl.nlnetlabs.bgpsym01.coordinator.Communicator;
import nl.nlnetlabs.bgpsym01.coordinator.Coordinator;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.xstream.XNeighbor;
import nl.nlnetlabs.bgpsym01.xstream.XNode;

/**
 * Performs user connection (sends appropriate commands). Takes away
 * responsibility for connecting from {@link Coordinator}
 */
public class ConnectHelper {

    private ArrayList<XNode> nodes;

    private Communicator communicator;

    /**
     * Performs user connection with his new neighbor(s). Connection works in
     * both directions, so what is in fact performed are link insertions.
     * 
     * package access for testing purposes
     * 
     * @param user
     *            user to be connected
     * @param neighbors
     *            neighbors to connect with, if <i>null</i> then
     *            connecting with all his new neighbors
     */
    public void connect(ASIdentifier user, List<ASIdentifier> neighbors) {
        /*
         * 1. send connect to this user with neighbors queue
         * 2. send info to his neighbors
         */

        ConnectCommand cc = new ConnectCommand();
        // send to the user
        cc.setAsIdentifier(user);
        cc.setAsIds(neighbors);
        cc.setProcessId(user.getProcessId());
        communicator.sendCommand(cc);

        List<ASIdentifier> tmpList = new LinkedList<ASIdentifier>();
        tmpList.add(user);
		List<XNeighbor> xNeighbors = new ArrayList<XNeighbor>();
        for (ASIdentifier asId : neighbors) {
            cc.setAsIdentifier(asId);
            cc.setProcessId(asId.getProcessId());
            cc.setAsIds(tmpList);
            communicator.sendCommand(cc);

			XNeighbor xNeighbor = new XNeighbor();
			xNeighbor.setAsIdentifier(asId);
			xNeighbors.add(xNeighbor);
        }

        nodes.get(user.getInternalId()).getNeighbors().addAll(xNeighbors);
        
		for (ASIdentifier asId : neighbors) {
			XNeighbor xNeighbor = new XNeighbor();
			xNeighbor.setAsIdentifier(user);
            nodes.get(asId.getInternalId()).getNeighbors().add(xNeighbor);
        }
    }

    public void setNodes(ArrayList<XNode> nodes) {
        this.nodes = nodes;
    }

    public void setCommunicator(Communicator communicator) {
        this.communicator = communicator;
    }

}

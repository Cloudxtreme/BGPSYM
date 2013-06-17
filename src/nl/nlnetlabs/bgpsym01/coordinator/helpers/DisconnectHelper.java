package nl.nlnetlabs.bgpsym01.coordinator.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.command.DisconnectCommand;
import nl.nlnetlabs.bgpsym01.command.InvalidateCommand;
import nl.nlnetlabs.bgpsym01.coordinator.Communicator;
import nl.nlnetlabs.bgpsym01.coordinator.Coordinator;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.xstream.XNeighbor;
import nl.nlnetlabs.bgpsym01.xstream.XNode;

/**
 * Performs user disconnection (sends appropriate commands). Takes away
 * responsibility for disconnecting from {@link Coordinator}
 * 
 * @see DisconnectHelperTest
 */
public class DisconnectHelper {

    private ArrayList<XNode> nodes;

    private Communicator communicator;

    /**
     * Performs user disconnection from his neighbor(s)
     * 
     * @param user
     * 
     * @see DisconnectHelperTest#testSendDisconnectAll()
     */
    public void disconnect(ASIdentifier user) {
        ArrayList<XNeighbor> neighbors = nodes.get(user.getInternalId()).getNeighbors();
        ArrayList<ASIdentifier> list = new ArrayList<ASIdentifier>(neighbors.size());
        for (XNeighbor neighbor : neighbors) {
            list.add(neighbor.getAsIdentifier());
        }
        // clear nodes queue - we don't want n^2 operations
        nodes.get(user.getInternalId()).getNeighbors().clear();
        disconnect(user, list);
    }

    /**
     * Performs user disconnection from his neighbor(s). Disconnection works in
     * both directions, so what is in fact performed are link failures.
     * 
     * package access for testing purposes
     * 
     * @param user
     *            user to be disconnected
     * @param neighbors
     *            neighbors to disconnect the user from, if <i>null</i> then
     *            disconnecting from all his neighbors
     * 
     * @see DisconnectHelperTest#testSendDisconnect()
     */
    public void disconnect(ASIdentifier user, List<ASIdentifier> neighbors) {
        /*
         * 1. send disconnect to this user with neighbors queue
         * 2. send info to his neighbors
         */
    	 
    	InvalidateCommand ic = new InvalidateCommand();
    	ic.setAsIdentifier(user);
    	ic.setValidate(false);
    	communicator.sendCommand(ic);
    	
    	for (ASIdentifier asId : neighbors) {
	    	InvalidateCommand icNeighbor = new InvalidateCommand();
	    	icNeighbor.setAsIdentifier(asId);
	    	icNeighbor.setValidate(false);
	    	icNeighbor.setNeighborsIdentifier(user);
	    	communicator.sendCommand(icNeighbor);
    	}

        /*DisconnectCommand dc = new DisconnectCommand();
        // send to the user
        dc.setAsIdentifier(user);
        dc.setAsIds(neighbors);
        dc.setProcessId(user.getProcessId());
        communicator.sendCommand(dc);

        List<ASIdentifier> tmpList = new LinkedList<ASIdentifier>();
        tmpList.add(user);
        for (ASIdentifier asId : neighbors) {
            dc.setAsIdentifier(asId);
            dc.setProcessId(asId.getProcessId());
            dc.setAsIds(tmpList);
            communicator.sendCommand(dc);
        }*/

        // delete from nodes
        /*nodes.get(user.getInternalId()).getNeighbors().removeAll(neighbors);
        for (ASIdentifier asId : neighbors) {
            Iterator<XNeighbor> iterator = nodes.get(asId.getInternalId()).getNeighbors().iterator();
            while (iterator.hasNext()) {
                XNeighbor neighbor = iterator.next();
                if (neighbor.getAsIdentifier().equals(user)) {
                    iterator.remove();
                    break;
                }
            }
        }*/
    }

    public void setNodes(ArrayList<XNode> nodes) {
        this.nodes = nodes;
    }

    public void setCommunicator(Communicator communicator) {
        this.communicator = communicator;
    }

}

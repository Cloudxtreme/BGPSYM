package nl.nlnetlabs.bgpsym01.coordinator.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nl.nlnetlabs.bgpsym01.command.DisconnectCommand;
import nl.nlnetlabs.bgpsym01.command.MasterCommand;
import nl.nlnetlabs.bgpsym01.coordinator.CommunicatorMock;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.xstream.XNeighbor;
import nl.nlnetlabs.bgpsym01.xstream.XNode;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;
import nl.nlnetlabs.bgpsym01.xstream.XRegistry;

/**
 * Tests whether coordinator sends good disconnects commands when called
 */
public class DisconnectHelperTest extends AbstractTest {

    private DisconnectHelper disconnectHelper;
    private CommunicatorMock communicator;

    /**
     * disconnect the node from all it's neighbors
     */
    public void testSendDisconnectAll() {

        List<MasterCommand> commands = communicator.getCommands();
        DisconnectCommand command;

        /*
         * I'm AS0
         * my neighbors are: AS1, AS7, AS8, AS10, AS18-23
         * I disconnect from all of them but do not write them explicitly
         */

        ArrayList<XNode> nodes = new ArrayList<XNode>();
        XNode node;
        node = new XNode();
        node.setAsIdentifier(getAS(0));
        nodes.add(node);
        List<ASIdentifier> asIds = createASList(1, 7, 8, 10, 18, 19, 20, 21, 23);
        ArrayList<XNeighbor> neighbors = new ArrayList<XNeighbor>();
        node.setNeighbors(neighbors);

        for (ASIdentifier n : asIds) {
            XNeighbor neighbor = new XNeighbor();
            neighbor.setAsIdentifier(n);
            neighbors.add(neighbor);

            while (nodes.size() <= n.getInternalId()) {
                nodes.add(null);
            }
            node = new XNode();
            node.setAsIdentifier(n);
            ArrayList<XNeighbor> tmpNeighbors = new ArrayList<XNeighbor>(2);
            for (ASIdentifier asX : createASList(0, 101, 203)) {
                XNeighbor tmpNeighbor = new XNeighbor();
                // add 0 and another one - he has to survive
                tmpNeighbor.setAsIdentifier(asX);
                tmpNeighbors.add(tmpNeighbor);
            }
            node.setNeighbors(tmpNeighbors);

            nodes.set(n.getInternalId(), node);
        }

        disconnectHelper.setNodes(nodes);

        // disconnect him fully
        disconnectHelper.disconnect(getAS(0));
        assertEquals(1 + asIds.size(), commands.size());

        command = getCommand(commands);
        assertEquals(getAS(0), command.getAsIdentifier());
        assertEquals(asIds, command.getAsIds());
        // no more neighbors
        assertEquals(0, nodes.get(0).getNeighbors().size());

        // check if all disconnect commands have been sent
        for (ASIdentifier n : asIds) {
            command = getCommand(commands);
            assertEquals(n, command.getAsIdentifier());
            assertEquals(createASList(0), command.getAsIds());

            assertEquals(2, nodes.get(n.getInternalId()).getNeighbors().size());
            assertEquals(getAS(101), nodes.get(n.getInternalId()).getNeighbors().get(0).getAsIdentifier());
            assertEquals(getAS(203), nodes.get(n.getInternalId()).getNeighbors().get(1).getAsIdentifier());
        }

        // nodes should have been deleted!
        assertEquals(0, nodes.get(0).getNeighbors().size());

    }

    /**
     * disconnect one node from few others
     */
    public void testSendDisconnect() {
        // we don't care about XNode in this test
        ArrayList<XNode> nodes = new ArrayList<XNode>();
        for (int i = 0; i < 100; i++) {
            XNode node = new XNode();
            node.setNeighbors(new ArrayList<XNeighbor>());
            nodes.add(node);
        }
        disconnectHelper.setNodes(nodes);

        List<ASIdentifier> asList = createASList(10, 11, 12, 15);
        ASIdentifier myId = getAS(3);
        disconnectHelper.disconnect(myId, asList);

        List<MasterCommand> commands = communicator.getCommands();
        // there should be message to us and to each of the neighbors
        assertEquals(5, commands.size());
        DisconnectCommand command;

        List<ASIdentifier> tmpList = createASList(3);

        command = getCommand(commands);
        assertEquals(command.getAsIdentifier(), myId);
        assertEquals(command.getAsIds(), asList);

        command = getCommand(commands);
        assertEquals(command.getAsIdentifier(), getAS(10));
        assertEquals(command.getAsIds(), tmpList);

        // skip 11
        command = getCommand(commands);

        // get 12
        command = getCommand(commands);
        assertEquals(command.getAsIdentifier(), getAS(12));
        assertEquals(command.getAsIds(), tmpList);

        // get 15
        command = getCommand(commands);
        assertEquals(command.getAsIdentifier(), getAS(15));
        assertEquals(command.getAsIds(), tmpList);

        commands.clear();
    }

    private DisconnectCommand getCommand(List<MasterCommand> list) {
        Iterator<MasterCommand> iterator = list.iterator();
        MasterCommand masterCommand = iterator.next();
        assertTrue(masterCommand instanceof DisconnectCommand);
        iterator.remove();
        return (DisconnectCommand) masterCommand;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        XProperties properties = XProperties.getInstance();
        properties.setPrefixCacheSize(1000);
        properties.setSleepingTime(1);
        XProperties.setInstance(properties);
        generateASes(1000);

        communicator = new CommunicatorMock();
        disconnectHelper = new DisconnectHelper();
        disconnectHelper.setCommunicator(communicator);
        ArrayList<XRegistry> registries = new ArrayList<XRegistry>();
        registries.add(null);
    }

}

package nl.nlnetlabs.bgpsym01.coordinator.helpers;

import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.command.AnnounceCommand;
import nl.nlnetlabs.bgpsym01.command.MasterCommand;
import nl.nlnetlabs.bgpsym01.command.ShutdownCommand;
import nl.nlnetlabs.bgpsym01.coordinator.Communicator;
import nl.nlnetlabs.bgpsym01.coordinator.CommunicatorMock;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.StaticThread;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;
import nl.nlnetlabs.bgpsym01.xstream.XRegistry;

/**
 * Tests whether {@link CommandSenderHelper} works correctly
 * 
 * {@link CommandSenderHelperImpl} -> {@link Communicator}
 */
public class CommandSenderHelperTest extends AbstractTest {

    private CommunicatorMock communicator;
    private XProperties properties;

    private CommandSenderHelperImpl commandSenderHelper;

    private ArrayList<XRegistry> registries;

    private ArrayList<ASIdentifier> ases;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        properties = XProperties.getInstance();
        properties.setPrefixCacheSize(1000);
        XProperties.setInstance(properties);
        generateASes(10000);
        generatePrefixes(10000);

        ases = new ArrayList<ASIdentifier>();
        for (int i = 0; i < 100; i++) {
            ases.add(ASFactory.getInstance(i));
        }

        communicator = new CommunicatorMock();
        registries = new ArrayList<XRegistry>();
        for (int i = 0; i < 10; i++) {
            registries.add(null);
        }

        commandSenderHelper = new CommandSenderHelperImpl();
        commandSenderHelper.setRegistries(registries);
        commandSenderHelper.setCommunicator(communicator);
        commandSenderHelper.setAses(ases);
    }

    /**
     * Tests whether waiting for all hosts works
     * 
     * @see CommandSenderHelper#waitForAllHosts()
     * 
     * @throws InterruptedException
     */
    public void testWaitForAllHosts() throws InterruptedException {
        Thread thread = new Thread() {

            @Override
            public void run() {
                commandSenderHelper.waitForAllHosts();
            }
        };
        thread.start();
        Thread.yield();
        while (!thread.isAlive()) {
            StaticThread.sleep(10);
        }
        thread.join(100);
        assertTrue(thread.isAlive());
        commandSenderHelper.ackReceived();
        thread.join(5000);
        assertFalse(thread.isAlive());
    }

    /**
     * @see CommandSenderHelper#sendToAllHosts(MasterCommand)
     */
    public void testSendToAllHosts() {
        communicator.clear();
        MasterCommand command = new ShutdownCommand();
        commandSenderHelper.sendToAllHosts(command);
        List<MasterCommand> commands = communicator.getCommands();
        assertEquals(registries.size(), commands.size());
        for (int i = 0; i < registries.size(); i++) {
            assertEquals(commands.get(i).getClass(), ShutdownCommand.class);
            // process id is lost during rewriting
        }
    }

    /**
     * Send updates with simple API
     * 
     * @see CommandSenderHelper#sendUpdate(int)
     */
    public void testUpdatesSimple() {
        communicator.clear();
        // first send updates just with numbers

        int end = 1200;
        int start = 1000;
        for (int i = start; i < end; i++) {
            commandSenderHelper.sendUpdate(i);
        }
        List<MasterCommand> commands = communicator.getCommands();
        assertEquals(end - start, commands.size());
        int num = start;
        for (MasterCommand command : commands) {
            assertEquals(AnnounceCommand.class, command.getClass());
            AnnounceCommand ann = (AnnounceCommand) command;
            assertEquals(ann.getRecipient(), ases.get(num % ases.size()));
            BGPUpdate update = (BGPUpdate) ann.getUpdate();
            assertEquals(update.getRoute(), createRoute());
            assertEquals(update.getPrefixes(), getPrefixList(num));
            num++;
        }
    }

    /**
     * Tests sending updates using given prefix arrays
     * 
     * @see CommandSenderHelper#sendUpdate(List, List, ASIdentifier)
     */
    public void testUpdatesComplex() {
        communicator.clear();
        int updatesCount = 150;
        int listSize = 50;

        // we cannot send more than 256 prefixes in one message!!!
        for (int i = 0; i < updatesCount; i++) {
            List<Prefix> prefixList = getPrefixListRange(i * 2, i * 2 + listSize + i);
            commandSenderHelper.sendUpdate(prefixList, null, getAS(i));
        }

        List<MasterCommand> commands = communicator.getCommands();
        assertEquals(updatesCount, commands.size());
        int num = 0;
        for (MasterCommand command : commands) {
            assertEquals(AnnounceCommand.class, command.getClass());
            AnnounceCommand ann = (AnnounceCommand) command;
            assertEquals(ann.getRecipient(), ASFactory.getInstance(num));
            BGPUpdate update = (BGPUpdate) ann.getUpdate();
            assertEquals(update.getRoute(), createRoute());
            assertEquals(update.getPrefixes(), getPrefixListRange(num * 2, num * 2 + listSize + num));
            num++;
        }
    }

    public void testWithdrawals() {
        communicator.clear();

        int updatesCount = 1200;
        for (int i = 0; i < updatesCount; i++) {
            commandSenderHelper.sendUpdate(i % 2 == 0 ? getPrefixListRange(i, i + 7) : null, getPrefixListRange(2 * i, 2 * i + 19), getAS(i));
        }

        List<MasterCommand> commands = communicator.getCommands();
        assertEquals(updatesCount, commands.size());
        int i = 0;
        for (MasterCommand command : commands) {
            assertEquals(AnnounceCommand.class, command.getClass());
            AnnounceCommand ann = (AnnounceCommand) command;
            assertEquals(ann.getRecipient(), ASFactory.getInstance(i));
            BGPUpdate update = (BGPUpdate) ann.getUpdate();
            assertEquals(update.getRoute(), createRoute());
            assertEquals(update.getPrefixes(), i % 2 == 0 ? getPrefixListRange(i, i + 7) : null);
            assertEquals(update.getWithdrawals(), getPrefixListRange(2 * i, 2 * i + 19));
            i++;
        }
    }
}

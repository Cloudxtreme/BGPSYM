package nl.nlnetlabs.bgpsym01.coordinator.helpers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.coordinator.Communicator;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;
import nl.nlnetlabs.bgpsym01.xstream.XPrefix;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;
import nl.nlnetlabs.bgpsym01.xstream.XRegistry;

import org.apache.log4j.Logger;

/**
 * Tests whether prefixes are sent correctly. Tests whether interaction:
 * {@link PropagationHelperImpl} -> {@link CommandSenderHelperImpl} ->
 * {@link Communicator}
 */
public class PropagationHelperTest extends AbstractTest {

    private static Logger log = Logger.getLogger(PropagationHelperTest.class);

    private static final int AGGREGATION_SIZE = 40;

    PropagationHelperImpl propagationHelper;

    private CommandSenderHelperMock commandSenderHelper;

    private XProperties properties;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Prefix.init(1000);
        properties = XProperties.getInstance();
        properties.setPrefixCacheSize(1000);
        properties.setSleepingTime(1);
        XProperties.setInstance(properties);
        generateASes(1000);

        ArrayList<XRegistry> registries = new ArrayList<XRegistry>();
        registries.add(null);

        commandSenderHelper = getCommandSenderHelper(registries);

        propagationHelper = new PropagationHelperImpl();
        propagationHelper.setCommandSenderHelper(commandSenderHelper);
        propagationHelper.setPrefixAggregationSize(1);
        propagationHelper.setPrefixAggreagationSleeperMultiplier(1000);

    }

    private CommandSenderHelperMock getCommandSenderHelper(ArrayList<XRegistry> registries) {
        CommandSenderHelperMock commandSenderHelper = new CommandSenderHelperMock();
        return commandSenderHelper;
    }

    public void testOneASWithAggregation() {
        commandSenderHelper.getReceived().clear();
        log.info("1");
        properties.setPrefixStartingPoint(0);
        properties.setPrefixCount(30);
        int prefixesSize = 20;
        List<XPrefix> prefixes = new LinkedList<XPrefix>();
        for (int i = 0; i < prefixesSize; i++) {
            prefixes.add(new XPrefix("pr_" + i, i, "AS" + 0, 0));
        }
        // this one should not be added (WARN in log4j)
        prefixes.add(new XPrefix("pr_" + 1000, 1000, "AS" + 0, 0));

        propagationHelper.setPrefixes(prefixes);
        propagationHelper.setPrefixAggregationSize(AGGREGATION_SIZE);
        propagationHelper.propagatePrefixes();

        ArrayList<Pair<List<Prefix>, ASIdentifier>> received = commandSenderHelper.getReceived();

        assertEquals(1, received.size());

        for (int i = 0; i < 1; i++) {
            XPrefix prefix = prefixes.get(i);
            assertEquals(prefix.getPrefixNum(), received.get(i).key.get(0).getNum());
            assertEquals(prefix.getAsInternalId(), received.get(i).value.getInternalId());
        }
    }

    public void testThreeASs() {
        log.info("2");
        properties.setPrefixStartingPoint(0);
        properties.setPrefixCount(30);
        int prefixesSize = 30;
        List<XPrefix> prefixes = new LinkedList<XPrefix>();
        for (int i = 0; i < prefixesSize; i++) {
            prefixes.add(new XPrefix("pr_" + i, i, "AS" + (i % 3), i % 3));
        }
        propagationHelper.setPrefixes(prefixes);
        propagationHelper.setPrefixAggregationSize(AGGREGATION_SIZE);
        propagationHelper.propagatePrefixes();

        ArrayList<Pair<List<Prefix>, ASIdentifier>> received = commandSenderHelper.getReceived();

        assertEquals(3, received.size());

        for (int i = 0; i < 3; i++) {
            assertEquals(10, received.get(i).key.size());
            for (int j = 0; j < 10; j++) {
                XPrefix prefix = prefixes.get(i + j * 3);
                assertEquals(prefix.getPrefixNum(), received.get(i).key.get(j).getNum());
                assertEquals(prefix.getAsInternalId(), received.get(i).value.getInternalId());
            }
        }

    }

    /**
     * Adds prefixes for 3 different ASes but so many that they have to be sent
     * separately
     */
    public void testThreeASsWithSplit() {
        log.info("3");
        properties.setPrefixStartingPoint(0);
        properties.setPrefixCount(210);
        int prefixesSize = 210;
        List<XPrefix> prefixes = new LinkedList<XPrefix>();
        for (int i = 0; i < prefixesSize; i++) {
            prefixes.add(new XPrefix("pr_" + i, i, "AS" + (i % 3), i % 3));
        }
        propagationHelper.setPrefixes(prefixes);
        propagationHelper.setPrefixAggregationSize(AGGREGATION_SIZE);
        propagationHelper.propagatePrefixes();

        ArrayList<Pair<List<Prefix>, ASIdentifier>> received = commandSenderHelper.getReceived();

        // List<MasterCommand> commands = communicator.getCommands();
        assertEquals(6, received.size());

        for (int i = 0; i < 6; i += 2) {
            ArrayList<Prefix> list = new ArrayList<Prefix>();
            assertEquals(40, received.get(i).key.size());
            list.addAll(received.get(i).key);
            assertEquals(30, received.get(i + 1).key.size());
            list.addAll(received.get(i + 1).key);

            for (int j = 0; j < 70; j++) {
                XPrefix prefix = prefixes.get(i / 2 + j * 3);
                assertEquals(prefix.getPrefixNum(), list.get(j).getNum());
                assertEquals(prefix.getAsInternalId(), received.get(i).value.getInternalId());
            }
        }

    }

    public void testPropagatePrefixesListNoAggregation() {
        log.info("1");
        propagateAndCheckNoAggregation(2, 30, 40);
        log.info("2");
        propagateAndCheckNoAggregation(2, 50, 40);
        log.info("3");
        propagateAndCheckNoAggregation(200, 500, 470);
        log.info("4");
        propagateAndCheckNoAggregation(1, 2, 470);
    }

    private void propagateAndCheckNoAggregation(int start, int count, int prefixesSize) {
        commandSenderHelper.getReceived().clear();

        XProperties properties = XProperties.getInstance();
        properties.setPrefixStartingPoint(start);
        properties.setPrefixCount(count);
        List<XPrefix> prefixes = new LinkedList<XPrefix>();
        for (int i = 0; i < prefixesSize; i++) {
            prefixes.add(new XPrefix("pr_" + i, i, "AS" + i, i));
        }

        propagationHelper.setPrefixes(prefixes);
        propagationHelper.propagatePrefixes();

        ArrayList<Pair<List<Prefix>, ASIdentifier>> received = commandSenderHelper.getReceived();

        // List<MasterCommand> commands = communicator.getCommands();
        assertEquals(Math.min(count - start, prefixesSize - start), received.size());

        for (int i = 0; i < Math.min(count - start, prefixesSize - start); i++) {
            XPrefix prefix = prefixes.get(i + start);
            assertEquals(prefix.getPrefixNum(), received.get(i).key.get(0).getNum());
            assertEquals(prefix.getAsInternalId(), received.get(i).value.getInternalId());
        }

        // for (int i = 0; i < Math.min(count - start, prefixesSize - start);
        // i++) {
        // assertTrue(commands.get(i) instanceof AnnounceCommand);
        // AnnounceCommand announceCommand = (AnnounceCommand) commands.get(i);
        // BGPUpdate update = (BGPUpdate) announceCommand.getUpdate();
        // XPrefix prefix = prefixes.get(i + start);
        // assertEquals(prefix.getPrefixNum(),
        // update.getPrefixes().get(0).getNum());
        // assertEquals(prefix.getAsInternalId(),
        // announceCommand.getRecipient().getInternalId());
        // }
    }
}

package nl.nlnetlabs.bgpsym01.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.nlnetlabs.bgpsym01.coordinator.events.PrefixData;
import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.route.NabsirUpdate;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.junit.Ignore;

public class RISGetDataResponseCommandTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateASes(20000);
        XProperties properties = XProperties.getInstance();
        properties.setResultDirectory(getTmpDir());
    }

    public void testType() {
        RISGetDataResponseCommand c = getCommand();
        assertEquals(c.getCommandType(), CommandType.RIS_RESPONSE);
    }

    public void testSerialize() throws IOException {
        ASIdentifier asFrom = getAS(3);
        boolean isWithdrawal = true;
        Prefix prefix = getPrefix(34);
        ASIdentifier asTo = getAS(4);
        Route route = createRoute(4, 6, 1);
        NabsirUpdate n1 = getNabsirUpdate(asFrom, isWithdrawal, prefix, asTo, route);

        NabsirUpdate n2 = new NabsirUpdate();
        n2.setPrefix(getPrefix(39));
        n2.setFrom(asFrom);
        n2.setTo(asTo);
        n2.setRoute(route);
        n2.setWithdrawal(isWithdrawal);

        ArrayList<NabsirUpdate> list = new ArrayList<NabsirUpdate>();
        list.add(n1);
        list.add(n2);

        RISGetDataResponseCommand response = getCommand();

        try {
            fail(Rewriter.rewrite(response, RISGetDataResponseCommand.class) + "");
        } catch (BGPSymException e) {
            // it's ok
        }

        response.setList(list);
        RISGetDataResponseCommand object = (RISGetDataResponseCommand) CoordinationCommand.readCommand(Rewriter.getStream(response));
        assertEquals(list, object.getList());

    }

    private NabsirUpdate getNabsirUpdate(ASIdentifier asFrom, boolean isWithdrawal, Prefix prefix, ASIdentifier asTo, Route route) {
        NabsirUpdate n1 = new NabsirUpdate();
        n1.setFrom(asFrom);
        n1.setPrefix(prefix);
        n1.setTo(asTo);
        n1.setRoute(route);
        n1.setWithdrawal(isWithdrawal);
        return n1;
    }

    private RISGetDataResponseCommand getCommand() {
        return new RISGetDataResponseCommand();
    }

    @Ignore
    public void testProcess() {
        if (1 < 2) {
            return;
        }
        RISGetDataResponseCommand command = getCommand();

        HashMap<String, PrefixData> map = new HashMap<String, PrefixData>();
        for (int i = 0; i < 13; i++) {
            map.put("msg_" + i, new PrefixData(getPrefix(i), "trans_" + i, 19));
        }
        int asNum = 33;

        RISGetDataResponseCommand.setMap(map);
        List<NabsirUpdate> list = new ArrayList<NabsirUpdate>();
        NabsirUpdate n1 = getNabsirUpdate(getAS(101), false, getPrefix(12), getAS(asNum), createRoute(1, 2, 3));
        NabsirUpdate n3 = getNabsirUpdate(getAS(101), false, getPrefix(2), getAS(asNum), createRoute(1, 2));
        list.add(n1);
        list.add(n3);

        File directory = null;
        for (File file : directory.listFiles()) {
            assertTrue(file.delete());
        }
        assertTrue(directory.delete());

        assertFalse(directory.exists());

        command.setList(list);
        command.process();

        assertTrue(directory.exists());

        assertEquals(1, directory.list().length);
        assertEquals(directory.list()[0], "output_" + asNum);

    }
}

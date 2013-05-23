package nl.nlnetlabs.bgpsym01.command;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import nl.nlnetlabs.bgpsym01.coordinator.events.PrefixData;
import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.route.NabsirUpdate;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class ResultWriterRISTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateASes(20000);
        XProperties properties = XProperties.getInstance();
        properties.setResultDirectory(getTmpDir());
    }

    public void testGetDir() {
        ResultWriterRIS writer = getCommand();
        File directory = writer.getDirectory();
        assertEquals(getDirName(), directory.getAbsolutePath());
        assertTrue(directory.exists());
    }

    private NabsirUpdate getUpdate(int t, Prefix pr) {
        NabsirUpdate n1 = new NabsirUpdate();
        n1.setPrefix(pr);
        n1.setTime(t);
        return n1;
    }


    public void testSort() {
        ResultWriterRIS command = getCommand();
        HashMap<String, PrefixData> map = new HashMap<String, PrefixData>();
        // day one
        for (int i = 0; i < 13; i++) {
            map.put("msg_" + i, new PrefixData(getPrefix(i), "trans_" + i, 19));
        }

        // day two
        int day = 3600 * 1000 * 24;
        for (int i = 100; i < 113; i++) {
            map.put("msg_" + i, new PrefixData(getPrefix(i), "trans_" + i, day));
        }
        command.setMap(map);

        // prefixes > 100 have one day of additional time, so they should be
        // after prefixes < 100
        ArrayList<NabsirUpdate> sortedList = new ArrayList<NabsirUpdate>();
        ArrayList<NabsirUpdate> list = new ArrayList<NabsirUpdate>();
        NabsirUpdate n1 = getUpdate(45000, getPrefix(1));
        NabsirUpdate n2 = getUpdate(49000, getPrefix(4));
        NabsirUpdate n3 = getUpdate(51000, getPrefix(101));
        NabsirUpdate n4 = getUpdate(58000, getPrefix(103));
        NabsirUpdate n5 = getUpdate(65000, getPrefix(9));
        NabsirUpdate n6 = getUpdate(72000, getPrefix(112));

        list.add(n1);
        list.add(n2);
        list.add(n3);
        list.add(n4);
        list.add(n5);
        list.add(n6);

        sortedList.add(n1);
        sortedList.add(n2);
        sortedList.add(n5);
        sortedList.add(n3);
        sortedList.add(n4);
        sortedList.add(n6);

        assertEquals(command.getSortedList(list), sortedList);

    }


    /**
     * Tests
     * {@link RISGetDataResponseCommand#getFilename(nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier)}
     */
    public void testGetFilename() {
        ResultWriterRIS writer = getCommand();

        assertEquals(getDirName() + File.separator + "output_12024", writer.getFilename(getAS(12024)));
        assertEquals(getDirName() + File.separator + "output_12023", writer.getFilename(getAS(12023)));
        assertEquals(getDirName() + File.separator + "output_11", writer.getFilename(getAS(11)));
    }

    private String getDirName() {
        return getTmpDir() + File.separator + Tools.getInstance().getStartAsString();
    }

    private ResultWriterRIS getCommand() {
        return new ResultWriterRIS();
    }

}

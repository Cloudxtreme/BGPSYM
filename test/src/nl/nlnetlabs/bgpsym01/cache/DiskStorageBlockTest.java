package nl.nlnetlabs.bgpsym01.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimerImpl;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimer.FlapTimerType;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

/**
 * Tests {@link DiskStorageBlock}.
 */
public class DiskStorageBlockTest extends AbstractTest {

    private DiskStorageBlock storage;
    ByteArrayOutputStream baos;

    private static Logger log = Logger.getLogger(DiskStorageBlockTest.class);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        XProperties properties = XProperties.getInstance();
        generatePrefixes(1000);
        properties.setPrefixArraySize(1000);
        XProperties.setInstance(properties);
        // storage = DiskStorage.createDiskStorage(4096, "/dev/shm/test",
        // "/dev/null2");
        storage = new DiskStorageBlock(4096, "/dev/shm/test", "/dev/null2");
        baos = new ByteArrayOutputStream();
    }

    public void testBlocks() {
        assertEquals(storage.getBlockNumber(), 0);
        assertEquals(storage.getBlockNumber(), 1);
        assertEquals(storage.getBlockNumber(), 2);
        assertEquals(storage.getBlockNumber(), 3);
        assertEquals(storage.getBlockNumber(), 4);
        storage.giveBlockBack(2);
        storage.giveBlockBack(1);
        assertEquals(storage.getBlockNumber(), 2);
        assertEquals(storage.getBlockNumber(), 1);
        assertEquals(storage.getBlockNumber(), 5);

    }

    /**
     * Tests writing and reading to streams (no saving stuff on disk).
     */
    public void testWritePrefixInfo() {
        if (null instanceof DiskStorageBlockTest) {
            if (log.isInfoEnabled()) {
                log.info("instance!");
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("not instance");
            }
        }
        ASFactory.init(3);
        ASIdentifier as0 = ASFactory.createInstance("as0");
        ASIdentifier as1 = ASFactory.createInstance("as1");
        ASIdentifier as2 = ASFactory.createInstance("as2");
        Map<ASIdentifier, PrefixTableEntry> map = new TreeMap<ASIdentifier, PrefixTableEntry>();

        ArrayList<ASIdentifier> hops = new ArrayList<ASIdentifier>();
        hops.add(as0);
        hops.add(as1);
        hops.add(as2);
        Route r1 = new Route();
        r1.setHops(hops.toArray(new ASIdentifier[0]));

        hops = new ArrayList<ASIdentifier>();
        hops.add(as0);
        hops.add(as2);
        Route r2 = new Route();
        r2.setHops(hops.toArray(new ASIdentifier[0]));

        Prefix prefix = Prefix.getInstance(12);

        PrefixTableEntry entry1 = new PrefixTableEntry(r1);
        entry1.setOrignator(as0);
        entry1.setFlapTimer(getFlapTimer());
        map.put(as0, entry1);

        PrefixTableEntry entry2 = new PrefixTableEntry(r2);
        entry2.setOrignator(as1);
        entry2.setFlapTimer(getFlapTimer());
        map.put(as1, entry2);

        PrefixInfo info1 = new PrefixInfo(prefix, entry1, map);

        try {
            storage.writeEntry(baos, info1);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            PrefixInfo prefixInfo = storage.readPrefixRecord(bais);
            log.info("prefixInfo=" + prefixInfo);

            assertEquals(prefixInfo, info1);
            assertNotSame(prefixInfo, info1);
            assertEquals(prefixInfo.getCurrentEntry(), entry1);
            assertEquals(prefixInfo.getNeighborsMap(), map);

        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

    }

    private FlapTimerImpl getFlapTimer() {
        return new FlapTimerImpl(FlapTimerType.CISCO);
    }

}

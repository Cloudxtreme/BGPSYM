package nl.nlnetlabs.bgpsym01.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimerImpl;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimer.FlapTimerType;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;
import org.junit.Ignore;

/**
 * Bug exploiter: after storing things on disk and recovering them if we store
 * new prefixes the old one get overwritten!
 * 
 * <b>Ignored since we don't use this feature right now, and it is broken!</b>
 */
@Ignore
public class DiskStorageBlockTest2 extends AbstractTest {

    private static Logger log = Logger.getLogger(DiskStorageBlockTest2.class);

    private static final String DATA_ARRAY_FILE_NAME = TEST_DIRECTORY + "/bgpsymArray";

    private static final String DATA_FILE_NAME = TEST_DIRECTORY + "/bgpsym";

    private DiskStorageBlock storage;

    private PrefixCache cache;

    private int size = 3000;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Prefix.init(3000);
        generateASes(10000);
        generatePrefixes(10000);
        XProperties properties = XProperties.getInstance();
        properties.setPrefixArraySize(size);
        properties.setPrefixCacheSize(size / 5);
        XProperties.setInstance(properties);
        initTestDir();
    }

    public void testStoreAndRecovery() throws IOException {
        String fileName = DATA_FILE_NAME;
        String arrayFileName = DATA_ARRAY_FILE_NAME;
        storage = new DiskStorageBlock(3, fileName, arrayFileName);
        PrefixCacheImplBlock cache2 = new PrefixCacheImplBlock();
        cache2.setDoLog(true);
        cache2.setStorage(storage);
        cache2.setContainer(new NeighborsMapsContainerMock());

        cache = cache2;

        // first store 3000k prefixes

        ArrayList<PrefixInfo> list = new ArrayList<PrefixInfo>();

        for (int i = 0; i < size; i++) {
            PrefixInfo prefixInfo = cache.getPrefixInfo(getPrefix(i));
            prefixInfo = enrichPrefixInfo(prefixInfo, i);
            list.add(prefixInfo);
        }

        /*
         * just check that we have got the same things
         * and that all things have been written to the disk
         */
        for (int i = 0; i < size; i++) {
            assertEquals(list.get(i), cache.getPrefixInfo(getPrefix(i)));
            assertNotSame(list.get(i), cache.getPrefixInfo(getPrefix(i)));
        }

        // store everything on disk
        cache.storePrefixesPermanent();

        // and load it
        XProperties properties = XProperties.getInstance();
        properties.setPrefixArraySize(size * 2);
        properties.setPrefixCount(size * 2);
        properties.bogusPrefixMin = 10000;

        storage = new DiskStorageBlock(3, fileName, arrayFileName);
        cache2 = new PrefixCacheImplBlock();
        cache2.setDoLog(true);
        cache2.setStorage(storage);
        cache2.setContainer(new NeighborsMapsContainerMock());
        cache = cache2;

        // add next 3000 prefixes
        for (int i = 0; i < size; i++) {
            PrefixInfo prefixInfo = cache.getPrefixInfo(getPrefix(i + size));
            prefixInfo = enrichPrefixInfo(prefixInfo, i + size);
            list.add(prefixInfo);
        }

        /*
         * just check that we have got the same things
         * and that all things have been written to the disk
         */
        for (int i = 0; i < size * 2; i++) {
            assertEquals(list.get(i), cache.getPrefixInfo(getPrefix(i)));
            assertNotSame(list.get(i), cache.getPrefixInfo(getPrefix(i)));
        }
    }

    @Override
    protected void tearDown() throws Exception {
        initTestDir();
        if (log.isInfoEnabled()) {
            log.info("file successfully deleted");
        }
    }

    private PrefixInfo enrichPrefixInfo(PrefixInfo pi, int i) {
        pi.setPrefix(getPrefix(i));
        PrefixTableEntry entry1 = new PrefixTableEntry();
        entry1.setFlapTimer(getFlapTimer());
        entry1.setRoute(createRoute(i, i + 1, i + 2));
        entry1.setOrignator(ASFactory.getInstance(i));

        PrefixTableEntry entry2 = new PrefixTableEntry();
        entry2.setFlapTimer(getFlapTimer());
        entry2.setRoute(createRoute(i, i + 1, i + 2, i + 3));
        entry2.setOrignator(ASFactory.getInstance(i + 1));

        Map<ASIdentifier, PrefixTableEntry> map = new TreeMap<ASIdentifier, PrefixTableEntry>();
        map.put(ASFactory.getInstance(i), entry1);
        map.put(ASFactory.getInstance(i + 1), entry2);

        pi.setCurrentEntry(entry1);
        pi.setNeighborsMap(map);

        return pi;

    }

    private FlapTimerImpl getFlapTimer() {
        return new FlapTimerImpl(FlapTimerType.CISCO);
    }

}

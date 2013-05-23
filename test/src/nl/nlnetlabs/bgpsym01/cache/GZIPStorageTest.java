package nl.nlnetlabs.bgpsym01.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;
import org.junit.Ignore;

/**
 * @see DiskStorageGZIP
 */
@Ignore
public class GZIPStorageTest extends AbstractTest {

    private static Logger log = Logger.getLogger(GZIPStorageTest.class);

    private DiskStorageGZIP dsg;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        XProperties properties = XProperties.getInstance();
        generatePrefixes(10000);
        properties.setPrefixArraySize(10000);
        XProperties.setInstance(properties);

        dsg = new DiskStorageGZIP(10);
        int asSize = 13000;
        ASFactory.init(asSize);
        for (int i = 0; i < asSize; i++) {
            ASFactory.registerAS(new ASIdentifier("a" + i, i), i);
        }

    }

    public PrefixInfo getPrefixInfo(int num) {
        PrefixInfo pi = new PrefixInfo();
        Prefix prefix = Prefix.getInstance(num);
        pi.setPrefix(prefix);

        Route r1 = new Route();
        ArrayList<ASIdentifier> hops = new ArrayList<ASIdentifier>();
        hops.add(ASFactory.getInstance(num));
        hops.add(ASFactory.getInstance(num + 1));
        hops.add(ASFactory.getInstance(num + 2));
        hops.add(ASFactory.getInstance(num + 3));
        r1.setHops(hops.toArray(new ASIdentifier[0]));

        PrefixTableEntry currentEntry = new PrefixTableEntry();

        currentEntry.setOrignator(ASFactory.getInstance(num + 17));
        currentEntry.setRoute(r1);
        pi.setCurrentEntry(currentEntry);

        TreeMap<ASIdentifier, PrefixTableEntry> map = new TreeMap<ASIdentifier, PrefixTableEntry>();
        map.put(ASFactory.getInstance(num + 17), currentEntry);

        pi.setNeighborsMap(map);

        return pi;
    }

    // 100 small prefixes have to fit into one block
    public void testGzip() throws IOException {
        if (log.isInfoEnabled()) {
            log.info("test1");
        }
        ArrayList<PrefixInfo> infos = new ArrayList<PrefixInfo>();
        int size = 100;
        for (int i = 0; i < size; i++) {
            infos.add(getPrefixInfo(i));
        }

        ArrayList<PrefixInfo> copyList = new ArrayList<PrefixInfo>(infos);

        dsg.storePrefixes(infos.iterator());

        assertEquals(0, infos.size());

        Iterator<PrefixInfo> iterator = dsg.readPrefix(Prefix.getInstance(0));

        ArrayList<PrefixInfo> prefixes = getPrefixesList(iterator);
        assertEquals(size, prefixes.size());
        assertEquals(copyList, prefixes);
    }

    public void testGzip2() throws IOException {
        if (log.isInfoEnabled()) {
            log.info("test2");
        }
        ArrayList<PrefixInfo> infos = new ArrayList<PrefixInfo>();
        int size = 10000;
        for (int i = 0; i < size; i++) {
            infos.add(getPrefixInfo(i));
        }

        ArrayList<PrefixInfo> copyList = new ArrayList<PrefixInfo>(infos);

        dsg.storePrefixes(infos.iterator());
        dsg.storePrefixes(infos.iterator());

        assertFalse(infos.size() == 0);

        ArrayList<PrefixInfo> p1 = getPrefixesList(dsg.readPrefix(Prefix.getInstance(0)));
        assertNull(dsg.readPrefix(Prefix.getInstance(0)));
        int prefixStart = p1.size() + 1;
        ArrayList<PrefixInfo> p2 = getPrefixesList(dsg.readPrefix(Prefix.getInstance(prefixStart)));
        assertEquals(size, infos.size() + p1.size() + p2.size());
        p1.addAll(p2);
        p1.addAll(infos);
        assertEquals(copyList, p1);
    }

    private ArrayList<PrefixInfo> getPrefixesList(Iterator<PrefixInfo> iterator) {
        ArrayList<PrefixInfo> prefixes = new ArrayList<PrefixInfo>();
        while (iterator.hasNext()) {
            prefixes.add(iterator.next());
        }
        return prefixes;
    }

}

package nl.nlnetlabs.bgpsym01.mock;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.route.output.NeighborMock;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public abstract class AbstractTest extends TestCase {

    private static final String TMP_DIR_NAME = "tmpDir";

    @Override
    protected void setUp() throws Exception {
        XProperties.dummyInit();
        XProperties.getInstance().setDiskCacheDir("/tmp/");
        generateASes(1000);
        Prefix.init(1000);
    }

    public String getTmpDir() {
        String tmpDir = System.getProperty("user.dir") + File.separator + TMP_DIR_NAME;
        File dir = new File(tmpDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                fail();
            }
        }
        if (!dir.isDirectory()) {
            fail();
        }
        return tmpDir;
    }

    private volatile boolean threadFailed;

    public static String TEST_DIRECTORY = "/home/wojciech/test/";

    public String getTestDirectory() {
        return TEST_DIRECTORY + getClass().getName();
    }

    public void clearDir(String name) {
        File f = new File(name);
        assertFalse("directory " + name + " is a file", f.exists() && f.isFile());
        if (!f.exists()) {
            assertTrue("unable to create dir", f.mkdirs());
        } else {
            for (File tmp : f.listFiles()) {
                assertTrue("unable to remove " + tmp.getName(), f.delete());
            }
        }
    }

    public void removeDir(String dir) {
        assertTrue("unable to remove " + dir, new File(dir).delete());
    }

    public AbstractTest() {
        Thread.setDefaultUncaughtExceptionHandler(new JUnitExceptionHandler(this));
    }

    public void setFailed() {
        threadFailed = true;
    }

    protected void failIfNecessary() {
        if (threadFailed) {
            fail("see log4j error");
        }
    }

    protected ASIdentifier getAS(int num) {
        return ASFactory.getInstance(num);
    }

    protected void generateASes(int asSize) {
        ASFactory.init(asSize);
        for (int i = 0; i < asSize; i++) {
            ASIdentifier as = new ASIdentifier("AS" + i, i);
            as.setProcessId(i * 12);
            ASFactory.registerAS(as, i);
        }
    }

    protected void generatePrefixes(int size) {
        Prefix.init(size);
    }

    protected Route createRoute(int... ids) {
        Route route = new Route();
        ArrayList<ASIdentifier> hops = new ArrayList<ASIdentifier>(ids.length);
        for (int id : ids) {
            hops.add(ASFactory.getInstance(id));
        }
        route.setHops(hops.toArray(new ASIdentifier[0]));
        return route;
    }

    protected List<ASIdentifier> createASList(int... ases) {
        List<ASIdentifier> list = new ArrayList<ASIdentifier>();
        for (int as : ases) {
            list.add(getAS(as));
        }
        return list;
    }

    protected List<Prefix> getPrefixList(int... prefixes) {
        ArrayList<Prefix> list = new ArrayList<Prefix>(prefixes.length);
        for (int prefix : prefixes) {
            list.add(Prefix.getInstance(prefix));
        }
        return list;
    }

    protected List<Prefix> getPrefixListRange(int start, int end) {
        assertTrue(end >= start);
        ArrayList<Prefix> list = new ArrayList<Prefix>(end - start);
        for (int i = start; i < end; i++) {
            list.add(getPrefix(i));
        }
        return list;
    }

    protected Prefix getPrefix(int num) {
        return Prefix.getInstance(num);
    }

    @Override
    protected void tearDown() throws Exception {
        failIfNecessary();
    }

    protected void initTestDir() {
        File dir = new File(TEST_DIRECTORY);
        if (dir.exists()) {
            assertTrue(dir.isDirectory());
        } else {
            assertTrue(dir.mkdirs());
        }
        for (File file : dir.listFiles()) {
            assertTrue(file.delete());
        }
    }

    protected Neighbors getNeighbors(ASIdentifier asId, int size) {
        Neighbors neighbors = new Neighbors(asId);
        fillNeighbors(neighbors, size);
        return neighbors;
    }

    protected void fillNeighbors(Neighbors neighbors, int size) {
        for (int i = 0; i < size; i++) {
            neighbors.addNeighbor(getNeighbor(getAS(i)));
        }
    }

    protected Neighbor getNeighbor(ASIdentifier as) {
        Neighbor n1 = new NeighborMock(as);
        return n1;
    }

    protected void checkValidPrefixesCount(Map<ASIdentifier, PrefixTableEntry> map, int valid, int invalid) {
        int validCount = 0;
        int invalidCount = 0;
        for (ASIdentifier asId : map.keySet()) {
            PrefixTableEntry pte = map.get(asId);
            if (pte.isValid()) {
                validCount++;
            } else {
                invalidCount++;
            }
        }
        assertEquals(valid, validCount);
        assertEquals(invalid, invalidCount);
    }

    protected void compareCollectionWithIterator(Collection<?> collection, Iterator<?> iterator) {
        Collection<Object> tmp = new HashSet<Object>();
        while (iterator.hasNext()) {
            tmp.add(iterator.next());
        }
        assertEquals(tmp, new HashSet<Object>(collection));
    }

    public static void assertEmpty(String msg, Collection<?> collection) {
        if (collection != null && collection.size() > 0) {
            fail(msg + ", size=" + collection.size());
        }
    }

}

package nl.nlnetlabs.bgpsym01.cache;

import java.util.Map;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.neighbor.NeighborsMap;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

/**
 * Tests some aspects of PrefixCacheImplBlock
 */
public class PrefixCacheImplBlockTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateASes(1000);
        Prefix.init(1000);
        XProperties.getInstance().setPrefixCacheSize(10000);
    }

    /**
     * Tests whether {@link PrefixCacheImplBlock} uses
     * {@link NeighborsMapsContainerImpl} properly (acquiring maps and releasing
     * them).
     * 
     * TODO: use mock here!!!
     */
    public void testNeighborsMap() {
        XProperties.getInstance().setNeighborsContainerCaching(true);
        XProperties.getInstance().setUseNeighborsMap(true);

        Neighbors neighbors = getNeighbors(getAS(7), 6);
        PrefixCacheImplBlock cache = new PrefixCacheImplBlock();
        cache.setStorage(new DiskStorageMock());
        NeighborsMapsContainerImpl container = new NeighborsMapsContainerImpl(neighbors);
        Map<ASIdentifier, PrefixTableEntry> map1 = container.getMap();
        container.giveBack(map1);
        cache.setContainer(container);

        PrefixInfo prefixInfo = cache.getPrefixInfo(getPrefix(12));

        String errorMsg = "class is " + prefixInfo.getNeighborsMap().getClass().getName();
        assertTrue(errorMsg, prefixInfo.getNeighborsMap() instanceof NeighborsMap);

        // commented out because this feature is turned off
        /* cache.storePrefixesPermanent();

        // prefix is stored so the map should be freed
        Map<ASIdentifier, PrefixTableEntry> map2 = container.getMap();
        assertSame("map shouldn't have changed", map1, map2);*/
    }
}

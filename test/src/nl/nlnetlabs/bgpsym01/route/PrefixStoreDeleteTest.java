package nl.nlnetlabs.bgpsym01.route;

import java.util.Map;

import nl.nlnetlabs.bgpsym01.cache.PrefixInfo;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.primitives.mocks.FlapTimerMock;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class PrefixStoreDeleteTest extends AbstractTest {

    private PrefixStore store;
    private PrefixCacheMock cache;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Prefix.init(1000);

        XProperties properties = XProperties.getInstance();
        properties.setPrefixArraySize(10000);

        generateASes(1000);
        this.store = getStore();
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testDontDelete() {
        store = getStore();

        store.prefixReceived(getAS(1), getPrefixList(1), createRoute(12, 23));

        PrefixInfo prefixInfo = cache.getPrefixInfo(getPrefix(1));
        Map<ASIdentifier, PrefixTableEntry> neighborsMap = prefixInfo.getNeighborsMap();
        assertEquals(neighborsMap.size(), 1);
        PrefixTableEntry pte = neighborsMap.get(getAS(1));
        FlapTimerMock timer = new FlapTimerMock(true, 1001);
        pte.setFlapTimer(timer);

        store.prefixRemove(getAS(1), getPrefixList(1));
        assertSame(pte, neighborsMap.get(getAS(1)));
    }

    private PrefixStore getStore() {
        PrefixStoreMapImpl store2 = MockedPrefixStoreFactory.getStore();
        cache = (PrefixCacheMock) store2.getCache();
        return store2;
    }

}

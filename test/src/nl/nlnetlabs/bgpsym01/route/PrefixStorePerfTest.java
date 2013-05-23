package nl.nlnetlabs.bgpsym01.route;

import java.util.Map;
import java.util.Random;

import nl.nlnetlabs.bgpsym01.cache.NeighborsMapsContainerImpl;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.neighbor.impl.NeighborImplTCP;
import nl.nlnetlabs.bgpsym01.primitives.OutputAddEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.primitives.types.StaticThread;
import nl.nlnetlabs.bgpsym01.route.output.OutputBufferImpl;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.junit.Ignore;

// this test's purpose was to see how much time things take. No use to run it every time...

@Ignore
public class PrefixStorePerfTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        Prefix.init(75000);
        generateASes(25000);
        XProperties.getInstance();
    }

    public void testPerf() {
        // NeighborsMap map = NeighborsMapsContainerImpl.
        Neighbors neighbors = new Neighbors(getAS(0));
        int count = 200;
        for (int i = 0; i < count; i++) {
            NeighborImplTCP neighbor = new NeighborImplTCP(getAS(i + 1), null);
            neighbors.addNeighbor(neighbor);
        }

        OutputBufferImpl outputBuffer = new OutputBufferImpl(getAS(0));
        outputBuffer.setNeighbors(neighbors);
        int prefixCount = 200;
        for (int i = 0; i < prefixCount; i++) {
            outputBuffer.add(new OutputAddEntity(getPrefix(i), createRoute(1), createRoute(1, i % 20000)));
        }
        Random r = new Random(12312);

        NeighborsMapsContainerImpl container = new NeighborsMapsContainerImpl(neighbors);
        Map<ASIdentifier, PrefixTableEntry> map = container.getMap();
        System.out.println("map.size()=" + map.size());
        // map = new HashMap<ASIdentifier, PrefixTableEntry>();
        StaticThread.sleep(100);
        Prefix prefixX = getPrefix(r.nextInt(prefixCount));
        long start = System.currentTimeMillis();
        for (int i = 0; i < 5000; i++) {
            // for (ASIdentifier asId : map.keySet()) {
            // map.get(asId);
            // }
            outputBuffer.add(new OutputAddEntity(prefixX, createRoute(1), createRoute(1, i % 20000)));
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);

    }

}

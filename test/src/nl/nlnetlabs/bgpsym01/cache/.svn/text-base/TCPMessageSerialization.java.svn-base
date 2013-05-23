package nl.nlnetlabs.bgpsym01.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimerImpl;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimer.FlapTimerType;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class TCPMessageSerialization extends AbstractTest {

    private static final int NUM = 89;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Prefix.init(3000);
        XProperties properties = XProperties.getInstance();
        properties.setPrefixArraySize(3000);
        XProperties.setInstance(properties);
    }

    public void testSerialize() throws IOException {

        ASFactory.init(35);
        ASIdentifier as0 = new ASIdentifier("dupa0", 0);
        ASIdentifier as1 = new ASIdentifier("dupa1", 1);
        ASIdentifier as2 = new ASIdentifier("dupa2", 2);

        ASFactory.registerAS(as0, as0.getInternalId());
        ASFactory.registerAS(as1, as1.getInternalId());
        ASFactory.registerAS(as2, as2.getInternalId());

        ArrayList<ASIdentifier> hops = new ArrayList<ASIdentifier>();
        hops.add(as0);
        hops.add(as1);
        hops.add(as2);
        Route r1 = new Route();
        r1.setHops(new ASIdentifier[0]);

        BGPUpdate u = new BGPUpdate(12);
        u.setRoute(r1);
        u.setSender(as0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EDataOutputStream eos = new EDataOutputStream(baos);

        u.writeExternal(eos);
        eos.close();

        byte ar[] = baos.toByteArray();
        EDataInputStream eis = new EDataInputStream(new ByteArrayInputStream(ar));

        BGPUpdate u2 = new BGPUpdate();
        u2.readExternal(eis);

        assertNotSame(u, u2);
        assertEquals(u, u2);

        PrefixInfo pi = new PrefixInfo();

        PrefixTableEntry pte = new PrefixTableEntry(r1);
        pte.setFlapTimer(getFlapTimer());
        pte.setOrignator(as0);
        TreeMap<ASIdentifier, PrefixTableEntry> map = new TreeMap<ASIdentifier, PrefixTableEntry>();
        map.put(as0, pte);
        pi.setNeighborsMap(map);
        pi.setCurrentEntry(pte);
        pi.setPrefix(Prefix.getInstance(NUM));

        baos = new ByteArrayOutputStream();
        eos = new EDataOutputStream(baos);

        pi.writeExternal(eos);
        eos.close();

        ar = baos.toByteArray();
        eis = new EDataInputStream(new ByteArrayInputStream(ar));

        PrefixInfo pi2 = new PrefixInfo();
        pi2.readExternal(eis);

        assertNotSame(pi, pi2);
        assertEquals(pi, pi2);

        LinkedList<PrefixInfo> prefixes = new LinkedList<PrefixInfo>();
        prefixes.add(pi);

        DiskStorage storage = new DiskStorageBlock(4096, "/dev/shm/tmp", "/dev/null2");

        storage.storePrefixes(prefixes.iterator());

        Iterator<PrefixInfo> readPrefix = storage.readPrefix(Prefix.getInstance(NUM));

        PrefixInfo pi3 = readPrefix.next();

        assertNotSame(pi, pi3);
        assertEquals(pi, pi3);

    }

    private FlapTimerImpl getFlapTimer() {
        return new FlapTimerImpl(FlapTimerType.CISCO);
    }

}

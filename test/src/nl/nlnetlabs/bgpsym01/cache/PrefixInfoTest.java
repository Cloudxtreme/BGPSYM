package nl.nlnetlabs.bgpsym01.cache;

import java.io.IOException;
import java.util.HashMap;

import nl.nlnetlabs.bgpsym01.command.Rewriter;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class PrefixInfoTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateASes(1000);
        Prefix.init(1000);
        XProperties properties = XProperties.getInstance();
        XProperties.setInstance(properties);
        properties.setPrefixCacheSize(10000);
    }

    public void testSerializeLastSeen() throws IOException {
        PrefixInfo pi = new PrefixInfo();
        pi.setPrefix(getPrefix(12));
        pi.setNeighborsMap(new HashMap<ASIdentifier, PrefixTableEntry>());

        assertEquals(pi, (Rewriter.rewrite(pi, PrefixInfo.class)));
    }

}

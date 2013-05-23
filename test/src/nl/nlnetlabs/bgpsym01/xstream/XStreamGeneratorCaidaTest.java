package nl.nlnetlabs.bgpsym01.xstream;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASType;

public class XStreamGeneratorCaidaTest extends AbstractTest {

    public void testASType() {
        /* < 70k -> normal
         * 70k - 80k -> ROUTEVIEW
         * > -> RIS
         */

        XStreamGeneratorCaida gen = new XStreamGeneratorCaida();
        gen.init(30);
        ASIdentifier as1 = gen.getASId(20000, 10);
        ASIdentifier as2 = gen.getASId(70000, 11);
        ASIdentifier as3 = gen.getASId(80000, 12);

        assertNotNull(as1);
        assertNotNull(as2);
        assertNotNull(as3);

        assertEquals(as1.getType(), ASType.NORMAL);
        assertEquals(as2.getType(), ASType.ROUTEVIEW);
        assertEquals(as3.getType(), ASType.RIS);

    }

}

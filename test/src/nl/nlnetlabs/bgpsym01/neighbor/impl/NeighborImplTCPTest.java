package nl.nlnetlabs.bgpsym01.neighbor.impl;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;

public class NeighborImplTCPTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * 
     */
    public void testValid() {
        /*
         * valid after begining to changes status by setters
         */
        NeighborImplTCP ntcp = new NeighborImplTCP(getAS(0), null);
        assertTrue(ntcp.isValid());
        ntcp.setValid(false);
        assertFalse(ntcp.isValid());
        ntcp.setValid(true);
        assertTrue(ntcp.isValid());
    }

    public void testEquals() {
        NeighborImplTCP ntcp1 = new NeighborImplTCP(getAS(0), null);
        NeighborImplTCP ntcp2 = new NeighborImplTCP(getAS(1), null);
        NeighborImplTCP ntcp3 = new NeighborImplTCP(getAS(2), null);

        assertEquals(ntcp1, ntcp1);
        assertEquals(ntcp2, ntcp2);
        assertEquals(ntcp3, ntcp3);

        assertFalse(ntcp1.equals(ntcp2));
        assertFalse(ntcp2.equals(ntcp3));
        assertFalse(ntcp3.equals(ntcp1));
        assertFalse(ntcp3.equals(ntcp2));
    }

}

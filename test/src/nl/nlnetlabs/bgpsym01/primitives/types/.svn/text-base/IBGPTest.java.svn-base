package nl.nlnetlabs.bgpsym01.primitives.types;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class IBGPTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Tests whether {@link IBGPModelImpl#getConvergenceTime()} returns good
     * answers :)
     */
    public void testIBGPExp() {
        XProperties.getInstance().iBGPLog = false;

        int max = 1000;
        int convergence = 20;
        // IBGPModelImpl model = new IBGPModelImpl(convergence, max, 10);
        // assertEquals(1, model.getConvergenceTime());

        // assertEquals((int) Math.pow(convergence, 300.0 / max), getValue(max,
        // convergence, 300));

        // check that the max is honored
        assertEquals(convergence, getValue(max, convergence, max));
        assertEquals(convergence, getValue(max, convergence, max + 1));
        assertEquals(convergence, getValue(max, convergence, max * 3));
    }

    public void testIBGPLog() {
        XProperties.getInstance().iBGPLog = true;

        int max = 1000;
        int convergence = 2000;
        // IBGPModelImpl model = new IBGPModelImpl(convergence, max, 10);
        // assertEquals(1, model.getConvergenceTime());

        // check that the max is honored
        assertEquals(convergence, getValue(max, convergence, max), 5.0);
        assertEquals(getRealValueLog(convergence, max, max + 1), getValue(max, convergence, max + 1), 2.0);
        assertEquals(0, getValue(max, convergence, 1), 2.0);
    }

    private int getValue(int max, int convergence, int asSize) {
        return new IBGPModelImpl(convergence, max, asSize).getConvergenceTime();
    }

    private double getRealValueLog(int maxConvergenceTime, int maxAsSize, int asSize) {
        return Math.log(asSize) / Math.log(Math.pow(maxAsSize, 1.0 / maxConvergenceTime));
    }

}

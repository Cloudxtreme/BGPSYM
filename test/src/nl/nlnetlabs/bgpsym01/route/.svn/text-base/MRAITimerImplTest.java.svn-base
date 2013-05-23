package nl.nlnetlabs.bgpsym01.route;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;

public class MRAITimerImplTest extends AbstractTest {

    /**
     * Timer has to be able to have a threshold zero
     */
    public void testZeroIsOK() {
        MRAITimerImpl timer = new MRAITimerImpl();
        for (int i = 0; i < 100; i++) {
            assertEquals(0, timer.getThreshold(0));
        }
    }

    public void testCalculateValue() {

        /* in 10000 calculations we expect:
         * 
         * 1. the maximum at least 29.5
         * 2. the minimum value at most 22.6
         */

        MRAITimerImpl timer = new MRAITimerImpl();

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double avg = 0;

        int count = 10000;
        for (int i = 0; i < count; i++) {

            long value = timer.getThreshold(30000);
            min = Math.min(min, value);
            max = Math.max(max, value);
            avg += value;
        }
        avg /= count;
        assertEquals(max, 30000.0, 5.0);
        assertEquals(min, 22500.0, 5.0);
        assertEquals(avg, 26250.0, 50.0);


    }

}

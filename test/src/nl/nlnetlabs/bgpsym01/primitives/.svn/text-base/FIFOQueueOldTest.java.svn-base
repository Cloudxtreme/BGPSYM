package nl.nlnetlabs.bgpsym01.primitives;

import junit.framework.TestCase;
import nl.nlnetlabs.bgpsym01.primitives.types.FIFOQueue;
import nl.nlnetlabs.bgpsym01.primitives.types.FIFOQueueImplOld;

public class FIFOQueueOldTest extends TestCase {

    public void testEasy() {
        FIFOQueue<Integer> queue = new FIFOQueueImplOld<Integer>();

        for (int i = 0; i < 10; i++) {
            queue.add(i);
        }
        int value = 0;

        while (queue.size() > 0) {
            assertEquals(value++, (int) queue.remove());
        }
        assertEquals(value, 10);

        value = 0;
        for (int i = 0; i < 15; i++) {
            queue.add(i);
        }

        while (queue.size() > 0) {
            assertEquals(value++, (int) queue.remove());
        }
        assertEquals(value, 15);

    }

}

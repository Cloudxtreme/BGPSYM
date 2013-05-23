package nl.nlnetlabs.bgpsym01.primitives.types;

import java.util.Random;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;

public class FIFOQueueImplTest extends AbstractTest {

    Random random = new Random();

    public void testAvailalble() {
        FIFOQueueImpl<Integer> queue = getQueue();
        queue.available = 12;
        assertEquals(12, queue.size());
        assertEquals(12, queue.size());

        queue.available = 15;
        assertEquals(15, queue.size());
    }

    public void testGetNewPointerPos() {
        FIFOQueueImpl<Integer> queue = getQueue();
        assertEquals(11, queue.getNewPointerPos(10, 1, 13));
        assertEquals(12, queue.getNewPointerPos(10, 2, 13));
        assertEquals(7, queue.getNewPointerPos(10, 20, 23));

    }

    public void testAdd() {
        FIFOQueueImpl<Integer> queue = getQueue();
        queue.tab = new Integer[10];
        queue.endPos = 7;
        queue.available = 5;
        queue.add(i(1090));
        assertEquals(queue.tab[7], i(1090));
        assertEquals(queue.endPos, 8);
        assertEquals(queue.available, 6);
    }

    public void testRemove() {
        FIFOQueueImpl<Integer> queue = getQueue();
        queue.tab = new Integer[10];
        queue.startPos = 6;
        queue.tab[6] = i(193);
        queue.available = 6;
        assertEquals(queue.remove(), i(193));
        assertEquals(queue.startPos, 7);
        assertEquals(queue.available, 5);
    }

    public void testDoResize() {
        FIFOQueueImpl<Integer> queue = getQueue();
        queue.tab = new Integer[10];
        queue.tab[8] = i(101);
        queue.tab[9] = i(102);
        queue.tab[0] = i(103);
        queue.tab[1] = i(104);
        Object[] objects = queue.resize(queue.tab, 8, 1, 4, 20);
        assertEquals(objects[0], i(101));
        assertEquals(objects[3], i(104));
    }

    public void testNeedsResize() {
        FIFOQueueImpl<Integer> queue = getQueue();
        assertTrue(queue.needsResize(10, 1, 10));
        assertFalse(queue.needsResize(10, 1, 12));
        assertTrue(queue.needsResize(3, -1, 12));
        assertTrue(queue.needsResize(0, 0, 12));
    }

    public void testBig() {
        FIFOQueueImpl<Integer> queue = getQueue(5);
        for (int i = 0; i < 1042; i++) {
            queue.add(i(i*3));
        }
        assertEquals(1790, queue.tab.length); /* this is (((5+1) * 2) + 1) * 2 etc. */
        for (int i = 0; i < 1042; i++) {
            assertEquals(i(i * 3), queue.peek());
            assertEquals(i(i * 3), queue.peek());
            assertEquals(i(i*3), queue.remove());
            if (i != 1041) {
                assertEquals(i((i + 1) * 3), queue.peek());
            }
        }
        assertEquals(0, queue.available);
        assertEquals(5, queue.tab.length);
    }

    private FIFOQueueImpl<Integer> getQueue(int i) {
        return new FIFOQueueImpl<Integer>(i);
    }

    private Integer i(int num) {
        return new Integer(num);
    }

    private FIFOQueueImpl<Integer> getQueue() {
        return new FIFOQueueImpl<Integer>();
    }

}

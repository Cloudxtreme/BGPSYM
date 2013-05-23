package nl.nlnetlabs.bgpsym01.primitives.types;

import java.util.ArrayList;
import java.util.Arrays;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.mock.TimeControllerMock;
import nl.nlnetlabs.bgpsym01.mock.UpdateMock;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;

public class MessageQueueImplTest extends AbstractTest {

    private TimeControllerMock timeController = new TimeControllerMock();

    private static final class InputGeneratorMock implements InputGenerator {
        long readyTime;
        boolean hasSomething;
        int value;

        public long getReadyTime() {
            return readyTime;
        }

        public Update getUpdate() {
            return new UpdateMock(value);
        }

        public boolean hasSomething() {
            return hasSomething;
        }
    }

    /**
     * Tests {@link MessageQueue#addInputGenerator(InputGenerator)}
     */
    public void testAddInputGenerator() {
        MessageQueueImpl queue = getQueue();
        ArrayList<InputGenerator> list = new ArrayList<InputGenerator>();

        InputGenerator input1 = getInputGenerator();
        queue.addInputGenerator(input1);
        InputGenerator input2 = getInputGenerator();
        queue.addInputGenerator(input2);
        list.add(input1);
        list.add(input2);

        assertEquals(2, queue.size());
        assertTrue(Arrays.deepEquals(list.toArray(new InputGenerator[0]), queue.list));
    }

    private MessageQueueImpl getQueue() {
        MessageQueueImpl queue = new MessageQueueImpl();
        queue.setTimeController(timeController);
        return queue;
    }

    /**
     * Tests {@link MessageQueue#shutdown()}
     */
    public void testShutdown() {
        MessageQueue queue = getQueue();
        queue.shutdown();

        assertNull(queue.getNext());
    }

    /**
     * Tests whether queue.getWaitingTime() works fine...
     */
    public void testGetNext1() {
        InputGeneratorMock generator1 = getInputGenerator();
        InputGeneratorMock generator2 = getInputGenerator();
        MessageQueueImpl queue = getQueue();

        queue.addInputGenerator(generator1);
        queue.addInputGenerator(generator2);

        int currentTime = 9;
        timeController.currentTime = currentTime;

        assertFalse(queue.hasSomething());
        generator1.hasSomething = true;
        int ready1 = 23;
        generator1.readyTime = ready1;
        assertTrue(queue.hasSomething());
        assertEquals(ready1, queue.getWaitingTime());

        generator2.hasSomething = true;
        int ready2 = 21;
        generator2.readyTime = ready2;
        assertTrue(queue.hasSomething());
        assertEquals(ready2, queue.getWaitingTime());

        // test -1
        generator1.readyTime = -1;
        assertTrue(queue.hasSomething());
        assertEquals(-1, queue.getWaitingTime());
    }

    public void testGetNext2() {
        InputGeneratorMock generator1 = getInputGenerator();
        MessageQueue queue = getQueue();

        queue.addInputGenerator(generator1);

        int currentTime = 9;
        timeController.currentTime = currentTime;

        generator1.hasSomething = true;
        generator1.readyTime = -1;
        generator1.value = 291;

        Update update = queue.getNext();
        assertEquals(291, ((UpdateMock) update).num);

    }

    private InputGeneratorMock getInputGenerator() {
        return new InputGeneratorMock();
    }

}

package nl.nlnetlabs.bgpsym01.primitives.types;

import nl.nlnetlabs.bgpsym01.main.tcp.OverloadMonitor;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.mock.IBGPModelMock;
import nl.nlnetlabs.bgpsym01.mock.MessageQueueMock;
import nl.nlnetlabs.bgpsym01.mock.TimeControllerMock;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

public class MessageInputGeneratorTest extends AbstractTest {

    private TimeControllerMock timeController;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateASes(1000);
        Prefix.init(1000);
    }

    public void testTestHasSomething() {
        MessageInputGenerator generator = getMessageInputGenerator();

        BGPUpdate update = getBGPUpdate();
        assertFalse(generator.hasSomething());
        generator.addMessage(update);
        assertTrue(generator.hasSomething());
    }

    /**
     * After adding new update MessageInputGenerator should ping MessageQueue
     */
    public void testPing() {
        MessageInputGenerator generator = getMessageInputGenerator();
        MessageQueueMock queue = (MessageQueueMock) generator.getMessageQueue();

        BGPUpdate update = getBGPUpdate();
        assertEquals(0, queue.pinged);
        generator.addMessage(update);
        assertEquals(1, queue.pinged);
        generator.addMessage(update);
        assertEquals(2, queue.pinged);
    }

    /**
     * Tests whether updates are deferred by good amount of time
     */
    public void testGetReadyTime() {
        MessageInputGenerator generator = getMessageInputGenerator();
        IBGPModelMock pmodel = new IBGPModelMock();
        int convTime = 1234;
        pmodel.answer = convTime;
        generator.setIBGPmodel(pmodel);

        BGPUpdate update = getBGPUpdate();

        timeController.currentTime = 13;
        assertFalse(generator.hasSomething());
        generator.addMessage(update);
        assertTrue(generator.hasSomething());

        long time = timeController.getRealMS(timeController.getCurrentTime() + convTime);
        assertEquals(time, generator.getReadyTime());
    }

    private BGPUpdate getBGPUpdate() {
        return new BGPUpdate(getAS(1));
    }

    private MessageInputGenerator getMessageInputGenerator() {
        MessageInputGenerator generator = new MessageInputGenerator();
        generator.setMessageQueue(new MessageQueueMock());
        generator.setIBGPmodel(new IBGPModelMock());
        // generator.setIBGPConvergenceTime(CONVERGENCE_TIME);
        timeController = new TimeControllerMock();
        generator.setTimeController(timeController);
        generator.setMonitor(new OverloadMonitor(null));
        return generator;
    }

}

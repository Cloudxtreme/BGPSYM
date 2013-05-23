package nl.nlnetlabs.bgpsym01.process;

import nl.nlnetlabs.bgpsym01.callback.CallbackMock;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.mock.MessageQueueMock;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;

import org.apache.log4j.Logger;

/**
 * @see BGPProcess
 */
public class BGPProcessTest extends AbstractTest {

    private static Logger log = Logger.getLogger(BGPProcessTest.class);

    private PrefixStoreMock store;
    private BGPProcess process;

    @Override
    protected void setUp() throws Exception {
        generateASes(1000);
        Prefix.init(1000);
        store = new PrefixStoreMock();
        process = new BGPProcess(CallbackMock.getInstance());
        process.setMessageQueue(new MessageQueueMock());
        process.setStore(store);
        process.setAsIdentifier(ASFactory.getInstance(0));

    }

    @Override
    protected void tearDown() throws Exception {
        process.shutdown();
        if (log.isInfoEnabled()) {
            log.info("joining...");
        }
        process.join();
        if (log.isInfoEnabled()) {
            log.info("joined...");
        }
        failIfNecessary();
    }

    /**
     * Just check whether announced messages reach the store
     * 
     * @see BGPProcess#processUpdate(nl.nlnetlabs.bgpsym01.primitives.bgp.Update)
     */
    public void testAnnounce() {
        BGPUpdate update = new BGPUpdate();
        update.setSender(ASFactory.getInstance(1));
        update.setPrefixes(getPrefixList(1));
        update.setRoute(createRoute(1, 2, 3));

        // just an update with one message
        process.processUpdate(update);
        assertEquals(1, store.getReceivedArrayCount());
        assertEquals(1, store.getReceivedArraySize());

        update.setPrefixes(getPrefixList(1, 2, 3, 4));
        process.processUpdate(update);
        assertEquals(2, store.getReceivedArrayCount());
        assertEquals(5, store.getReceivedArraySize());

        update.setPrefixes(getPrefixList(6));
        process.processUpdate(update);
        assertEquals(3, store.getReceivedArrayCount());
        assertEquals(6, store.getReceivedArraySize());
    }

    /**
     * Tests whether messages with withdrawals are processed correctly
     * 
     * @see BGPProcess#processUpdate(nl.nlnetlabs.bgpsym01.primitives.bgp.Update)
     */
    public void testWithdrawals() {
        BGPUpdate update = new BGPUpdate();
        update.setSender(ASFactory.getInstance(1));
        update.setWithdrawals(getPrefixList(1));

        process.processUpdate(update);
        assertEquals(1, store.getRemovedArrayCount());
        assertEquals(1, store.getRemovedArraySize());

        update.setWithdrawals(getPrefixList(1, 2, 3));
        process.processUpdate(update);
        assertEquals(2, store.getRemovedArrayCount());
        assertEquals(4, store.getRemovedArraySize());

    }

}

package nl.nlnetlabs.bgpsym01.route.output;

import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.OutputAddEntity;
import nl.nlnetlabs.bgpsym01.primitives.OutputRemoveEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.mocks.MRAITimerMock;

import org.apache.log4j.Logger;

public class OutputBufferImplTest2 extends AbstractTest {

    private static Logger log = Logger.getLogger(OutputBufferImplTest2.class);

    private OutputBufferImpl outputBuffer;
    ASIdentifier as0;
    ASIdentifier as1;
    ASIdentifier as2;
    NeighborMock n1;
    Neighbor n2;
    Neighbor n3;
    Neighbor n4;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateASes(1000);
        Prefix.init(1000);
    }

    /**
     * Tests two things: 1. withdrawals should be sent automatically 2.
     * announcements waiting to be sent should be discarded
     */
    public void testWithdrawalGoImmediately() {
        if (log.isInfoEnabled()) {
            log.info("---------testWithdrawalGoImmediately---------");
        }
        outputBuffer = getTwoGuysNeighbors();

        MRAITimerMock timerMock1 = new MRAITimerMock();
        n1.setTimer(timerMock1);

        Prefix prefix = getPrefix(12);

        Route r1 = createRoute(1, 2, 3);
        outputBuffer.add(new OutputAddEntity(prefix, r1, null));
        outputBuffer.flush();
        // should be sent
        int updatesSize = 1;
        checkSizes(updatesSize, 1, 0);

        timerMock1.canSendNow = false;
        Route r2 = createRoute(1, 2, 3, 4);
        outputBuffer.add(new OutputAddEntity(prefix, r2, r1));
        outputBuffer.flush();
        // should be blocked
        checkSizes(updatesSize, 0, 0);

        outputBuffer.add(new OutputRemoveEntity(prefix, r2));
        outputBuffer.flush();
        // withdrawal should be sent
        checkSizes(++updatesSize, 0, 1);

    }

    private void checkSizes(int updatesSize, int annSize, int withSize) {
        ArrayList<BGPUpdate> updates = n1.getUpdates();
        assertEquals(updatesSize, updates.size());
        if (annSize > 0) {
            BGPUpdate update = updates.get(updatesSize - 1);
            assertEquals(annSize, update.getPrefixes().size());
        }

        if (withSize > 0) {
            BGPUpdate update = updates.get(updatesSize - 1);
            assertEquals(withSize, update.getWithdrawals().size());
        }
    }

    private OutputBufferImpl getTwoGuysNeighbors() {
        OutputBufferImpl bufferImpl = MockedOutputBufferFactory.getInstance(100, null, null, 90, 101);
        Neighbors neighbors = bufferImpl.getNeighbors();
        n1 = (NeighborMock) neighbors.getNeighbor(getAS(90));
        n2 = (Neighbor) neighbors.getNeighbor(getAS(101));
        return bufferImpl;
    }

}

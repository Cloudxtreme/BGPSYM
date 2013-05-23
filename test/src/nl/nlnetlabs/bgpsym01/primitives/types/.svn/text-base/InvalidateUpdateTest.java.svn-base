package nl.nlnetlabs.bgpsym01.primitives.types;

import java.util.List;

import nl.nlnetlabs.bgpsym01.cache.PrefixCacheImplBlock;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.process.NeighborInvalidatorImpl;
import nl.nlnetlabs.bgpsym01.process.NeighborInvalidatorMock;
import nl.nlnetlabs.bgpsym01.route.PrefixCacheMock;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreMapImpl;
import nl.nlnetlabs.bgpsym01.route.output.NeighborMock;
import nl.nlnetlabs.bgpsym01.route.output.OutputBufferImpl;
import nl.nlnetlabs.bgpsym01.route.output.OutputBufferMock;
import nl.nlnetlabs.bgpsym01.route.output.OutputState;
import nl.nlnetlabs.bgpsym01.route.output.OutputStateImpl;
import nl.nlnetlabs.bgpsym01.route.output.OutputStateMock;

public class InvalidateUpdateTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCall() {
        NeighborInvalidatorMock mock = new NeighborInvalidatorMock();
        InvalidateUpdate update = getUpdate();
        List<Prefix> prefixList = getPrefixList(1, 2, 3);
        update.setValidate(true);
        update.setPrefixes(prefixList);
        ASIdentifier nm1Id = getAS(1);
        update.setNeighborId(nm1Id);

        Neighbors neighbors = new Neighbors(getAS(0));
        NeighborMock nm1 = new NeighborMock(nm1Id);
        neighbors.addNeighbor(nm1);
        ASIdentifier nm2Id = getAS(2);
        NeighborMock nm2 = new NeighborMock(nm2Id);
        neighbors.addNeighbor(nm2);


        update.call(neighbors, mock);
        assertSame(prefixList, mock.validated);
        assertSame(nm1, mock.validatedNeighbor);

        update.setNeighborId(nm2Id);
        update.setValidate(false);
        update.call(neighbors, mock);
        assertSame(prefixList, mock.invalidated);
        assertSame(nm2, mock.invalidatedNeighbor);


        mock.invalidated = null;
        update.setNeighborId(null);
        update.call(neighbors, mock);
        assertSame(prefixList, mock.invalidated);
        assertSame(null, mock.invalidatedNeighbor);


    }

    public void testCreateInvalidator() {
        InvalidateUpdate update = getUpdate();
        PrefixCacheMock cache = new PrefixCacheMock();
        OutputStateMock state = new OutputStateMock();
        OutputBufferMock buffer = new OutputBufferMock();
        NeighborInvalidatorImpl invalidator = update.createInvalidator(cache, state, buffer);

        assertSame(cache, invalidator.getPrefixCache());
        assertSame(state, invalidator.getOutputState());
        assertSame(buffer, invalidator.getOutputBuffer());
    }

    private InvalidateUpdate getUpdate() {
        return new InvalidateUpdate();
    }

    public void testInferrStuff() {
        /*
         * This method has to use real implementations because there is casting involved during inferring
         */
        Neighbors neighbors = new Neighbors(getAS(0));
        neighbors.addNeighbor(new NeighborMock(getAS(1)));
        PrefixStoreMapImpl store = new PrefixStoreMapImpl();
        store.setNeighbors(neighbors);
        PrefixCacheImplBlock cache = new PrefixCacheImplBlock();
        OutputBufferImpl outputBuffer = new OutputBufferImpl(getAS(3));
        OutputState outputState = new OutputStateImpl();
        outputBuffer.setOutputState(outputState);
        store.setOutputBuffer(outputBuffer);
        store.setCache(cache);

        InvalidateUpdate update = getUpdate();
        update.inferStuff(store);

        assertSame(update.outputBuffer, outputBuffer);
        assertSame(update.outputState, outputState);
        assertSame(update.cache, cache);
        assertSame(update.neighbors, neighbors);
    }


}

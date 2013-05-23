package nl.nlnetlabs.bgpsym01.process;

import java.util.List;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;
import nl.nlnetlabs.bgpsym01.route.PrefixCacheMock;
import nl.nlnetlabs.bgpsym01.route.output.NeighborMock;
import nl.nlnetlabs.bgpsym01.route.output.OutputBufferMock;
import nl.nlnetlabs.bgpsym01.route.output.OutputStateMock;

public class NeighborInvalidatorImplTest extends AbstractTest {

    private OutputStateMock outputState;
    private OutputBufferMock outputBuffer;
    private PrefixCacheMock cache;

    /**
     * Tests that prefixes get correctly registerd in OutputState
     */
    public void testRegisterPrefixes() {
        NeighborInvalidatorImpl invalidator = getInvalidator();
        assertEquals(0, outputState.registeredPrefixes.size());

        Neighbor nm = new NeighborMock(getAS(1));
        List<Prefix> prefixList = getPrefixList(3,4,5,9);

        invalidator.registerPrefixes(nm, prefixList);
        assertEquals(1, outputState.registeredPrefixes.size());
        assertEquals(new Pair<Neighbor, List<Prefix>>(nm, prefixList), outputState.registeredPrefixes.get(0));
        assertEquals(0, outputState.deregisteredPrefixes.size());
    }

    /**
     * Covers the special case when neighbor is null
     */
    public void testInvalidateAll() {
        NeighborInvalidatorImpl invalidator = getInvalidator();
        Neighbor n1 = new NeighborMock(getAS(1));
        Neighbor n2 = new NeighborMock(getAS(2));
        invalidator.setNeighbors(new Neighbors(getAS(0), n1, n2));
        outputState.hasRegisteredPrefixes = true;

        invalidator.invalidate(null, getPrefixList(1, 2, 3));
        List<Prefix> list2 = getPrefixList(4, 5);
        invalidator.invalidate(null, list2);
        assertEquals(4, outputState.registeredPrefixes.size());
        assertEquals(new Pair<Neighbor, List<Prefix>>(n2, list2), outputState.registeredPrefixes.get(3));

        assertFalse(n1.isValid());
        assertFalse(n2.isValid());

    }

    public void testDeregisterPrefixes() {
        NeighborInvalidatorImpl invalidator = getInvalidator();
        assertEquals(0, outputState.deregisteredPrefixes.size());
        Neighbor nm = new NeighborMock(getAS(1));

        List<Prefix> prefixList = getPrefixList(3, 4, 5, 9);

        invalidator.deregisterPrefixes(nm, prefixList);
        assertEquals(1, outputState.deregisteredPrefixes.size());
        assertEquals(new Pair<Neighbor, List<Prefix>>(nm, prefixList), outputState.deregisteredPrefixes.get(0));
        assertEquals(0, outputState.registeredPrefixes.size());
    }

    /**
     * Tests that validation causes OutputBuffer to be called
     */
    public void testOutputBufferInvalidateCalled() {
        NeighborInvalidator invalidator = getInvalidator();
        Neighbor nm = new NeighborMock(getAS(1));

        // invalidation
        assertEquals(0, outputBuffer.invalidated);
        invalidator.invalidate(nm, getPrefixList(1, 2, 3));
        assertEquals(1, outputBuffer.invalidated); // one for the whole list
    }

    /**
     * Tests whether outputBuffer is called with appropriate route for
     * appropriate prefixes
     */
    public void testOutputBufferValidateCalled() {
        NeighborInvalidator invalidator = getInvalidator();
        Neighbor nm1 = new NeighborMock(getAS(1));
        Neighbor nm2 = new NeighborMock(getAS(2));

        // 1,2,3 for prefix 1
        cache.addRoute(nm1.getASIdentifier(), getPrefix(1), createRoute(1, 2, 3), true);
        cache.addRoute(nm2.getASIdentifier(), getPrefix(1), createRoute(2, 3, 4), false);

        // null for prefix 2

        // 10,11,15 for prefix 3
        cache.addRoute(nm1.getASIdentifier(), getPrefix(3), createRoute(10, 11, 15), true);

        invalidator.validate(nm1, getPrefixList(1, 2));
        assertEquals(1, outputBuffer.validatedCount);

        assertEquals(1, outputBuffer.validateList.size());
        Pair<Neighbor, List<Pair<Prefix, Route>>> pair = outputBuffer.validateList.get(0);
        assertEquals(nm1, pair.key);
        assertEquals(2, pair.value.size());
        assertEquals(pair.value.get(0), new Pair<Prefix, Route>(getPrefix(1), createRoute(1, 2, 3)));
        assertEquals(pair.value.get(1), new Pair<Prefix, Route>(getPrefix(2), null));

        outputBuffer.validateList.clear();

        invalidator.validate(nm2, getPrefixList(3));
        assertEquals(1, outputBuffer.validateList.size());
        pair = outputBuffer.validateList.get(0);
        assertEquals(nm2, pair.key);
        assertEquals(1, pair.value.size());
        assertEquals(pair.value.get(0), new Pair<Prefix, Route>(getPrefix(3), createRoute(10, 11, 15)));


    }

    public void testNeighborInvalidate() {
        // test invalidate
        NeighborInvalidator invalidator = getInvalidator();
        Neighbor nm = new NeighborMock(getAS(1));
        assertTrue(nm.isValid());
        outputState.hasRegisteredPrefixes = true;
        invalidator.invalidate(nm, getPrefixList(1, 2, 3));
        assertFalse(nm.isValid());

        // and validate (only if his list is empty)
        invalidator.validate(nm, getPrefixList(2, 3));
        outputState.hasRegisteredPrefixes = true;
        assertFalse(nm.isValid());
        // all removed - neighbor should be valid now
        outputState.hasRegisteredPrefixes = false;
        invalidator.validate(nm, getPrefixList(1));
        assertTrue(nm.isValid());
    }

    private NeighborInvalidatorImpl getInvalidator() {
        NeighborInvalidatorImpl invalidator = new NeighborInvalidatorImpl();
        outputState = new OutputStateMock();
        outputBuffer = new OutputBufferMock();
        invalidator.setOutputState(outputState);
        invalidator.setOutputBuffer(outputBuffer);
        cache = new PrefixCacheMock();
        invalidator.setPrefixCache(cache);
        return invalidator;
    }

}

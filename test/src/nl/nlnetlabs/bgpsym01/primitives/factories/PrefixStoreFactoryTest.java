package nl.nlnetlabs.bgpsym01.primitives.factories;

import java.io.File;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASType;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimerFactory;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimerFactoryMock;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimerFactoryReal;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimerImpl;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimer.FlapTimerType;
import nl.nlnetlabs.bgpsym01.route.MRAIStore;
import nl.nlnetlabs.bgpsym01.route.Policy;
import nl.nlnetlabs.bgpsym01.route.PrefixStore;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreMapImpl;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreRIS;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreRouteView;
import nl.nlnetlabs.bgpsym01.route.output.MRAIStoreMock;
import nl.nlnetlabs.bgpsym01.route.output.OutputBufferImpl;
import nl.nlnetlabs.bgpsym01.route.output.OutputStateImpl;
import nl.nlnetlabs.bgpsym01.route.output.PolicyMock;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

/**
 * Tests whether {@link PrefixStoreFactory} works just as it should
 */
public class PrefixStoreFactoryTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generateASes(100);
        XProperties.getInstance().setWorkingDir("/tmp/");
        File tmp = new File("/tmp/log");
        if (!tmp.exists()) {
            assertTrue(tmp.mkdirs());
        }
    }

    /**
     * This test is not extensive!!!
     */
    public void testNormalStore() {
        ASIdentifier as = getAS(1);
        MRAIStore mraiStore = new MRAIStoreMock();
        Policy policy = new PolicyMock();
        Neighbors neighbors = new Neighbors(as);
        PrefixStore storeX = PrefixStoreFactory.createStore(as, neighbors, null, policy, mraiStore, null);
        assertTrue(storeX instanceof PrefixStoreMapImpl);
        PrefixStoreMapImpl store = (PrefixStoreMapImpl) storeX;
        assertNotNull(store.getOutputBuffer());
        assertTrue(store.getOutputBuffer() instanceof OutputBufferImpl);

        OutputBufferImpl outputBuffer = (OutputBufferImpl) store.getOutputBuffer();
        assertNotNull(outputBuffer.getNeighbors());
        assertNotNull(outputBuffer.getMraiStore());
        assertNotNull(outputBuffer.getOutputState());
        assertTrue(outputBuffer.getOutputState() instanceof OutputStateImpl);

        OutputStateImpl state = (OutputStateImpl) outputBuffer.getOutputState();
        assertNotNull(state.getAsIdentifier());
        assertNotNull(state.getNeighbors());
        assertNotNull(state.getPolicy());

        // check consistency
        assertSame(outputBuffer.getNeighbors(), state.getNeighbors());
        assertSame(outputBuffer.getNeighbors(), neighbors);

        assertSame(outputBuffer.getPolicy(), policy);
        assertSame(outputBuffer.getPolicy(), state.getPolicy());

    }

    @Override
    protected void tearDown() throws Exception {
    }

    /**
     * If the ASIdentifier is Route View then {@link PrefixStoreRouteView}
     * should be returned, and {@link PrefixStoreMapImpl} otherwise
     */
    public void testRouteView() {
        ASIdentifier as = getAS(1);
        assertTrue(PrefixStoreFactory.createStore(as, new Neighbors(as), null, null, null, null) instanceof PrefixStoreMapImpl);
        as.setType(ASType.ROUTEVIEW);
        PrefixStore newStore = PrefixStoreFactory.createStore(as, new Neighbors(as), null, null, null, null);
        assertTrue(newStore instanceof PrefixStoreRouteView);
        PrefixStoreRouteView routeView = (PrefixStoreRouteView) newStore;
        assertNotNull(routeView.getTimeController());
        assertNotNull(routeView.getCallback());
    }

    public void testRIS() {
        ASIdentifier as = getAS(1);
        Neighbors neighbors = new Neighbors(as);
        assertTrue(PrefixStoreFactory.createStore(as, neighbors, null, null, null, null) instanceof PrefixStoreMapImpl);
        as.setType(ASType.RIS);
        PrefixStore newStore = PrefixStoreFactory.createStore(as, neighbors, null, null, null, null);
        assertTrue(newStore instanceof PrefixStoreRIS);
        PrefixStoreRIS store = (PrefixStoreRIS) newStore;
        assertSame(neighbors, store.getNeighbors());
    }

    /**
     * Tests whether flap timer factory is set correctly
     */
    public void testFlapTimerFactoryTest() {
        // if it's 500 OK in a row, than it is true :)
        int percentage = 100;
        int distribution = 100;
        FlapTimerType type = FlapTimerType.CISCO;
        doFactoryTest(percentage, distribution, type, FlapTimerFactoryReal.class);

        percentage = 100;
        distribution = 0;
        type = FlapTimerType.JUNIPER;
        doFactoryTest(percentage, distribution, type, FlapTimerFactoryReal.class);

        percentage = 0;
        distribution = 100;
        type = FlapTimerType.CISCO;
        doFactoryTest(percentage, distribution, type, FlapTimerFactoryMock.class);
    }

    private void doFactoryTest(int percentage, int distribution, FlapTimerType type, Class<? extends FlapTimerFactory> clazz) {
        int count = 1000;
        XProperties properties = XProperties.getInstance();
        properties.flapPercentage = percentage; // everybody has flap damping
        properties.flapDistribution = distribution; // only cisco

        for (int i = 0; i < count; i++) {

            FlapTimerFactory factory = PrefixStoreFactory.getFlapTimerFactory();
            assertTrue(clazz.isInstance(factory));
            if (clazz == FlapTimerFactoryReal.class) {
                assertEquals(type, ((FlapTimerImpl) factory.getFlapTimer()).getTimerType());
            }
        }
    }

}

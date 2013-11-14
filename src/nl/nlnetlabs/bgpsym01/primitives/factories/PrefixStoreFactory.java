package nl.nlnetlabs.bgpsym01.primitives.factories;

import java.util.Random;

import nl.nlnetlabs.bgpsym01.cache.DiskStorageBlock;
import nl.nlnetlabs.bgpsym01.cache.NeighborsMapsContainerImpl;
import nl.nlnetlabs.bgpsym01.cache.PrefixCacheImplBlock;
import nl.nlnetlabs.bgpsym01.callback.Callback;
import nl.nlnetlabs.bgpsym01.main.ObjectRegister;
import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapStore;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimerFactory;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimerFactoryMock;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimerFactoryReal;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.route.MRAIStore;
import nl.nlnetlabs.bgpsym01.route.Policy;
import nl.nlnetlabs.bgpsym01.route.PolicyImpl;
import nl.nlnetlabs.bgpsym01.route.PrefixStore;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreMapImpl;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreRIS;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreRouteView;
import nl.nlnetlabs.bgpsym01.route.output.OutputBufferImpl;
import nl.nlnetlabs.bgpsym01.route.output.OutputBufferStoreImpl;
import nl.nlnetlabs.bgpsym01.route.output.OutputState;
import nl.nlnetlabs.bgpsym01.route.output.OutputStateImpl;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class PrefixStoreFactory {

    private static Random random = new Random(System.currentTimeMillis() * 17);

    private static final String ARRAY_SUFFIX = "_array";

    private static final Class<? extends Policy> DEFAULT_POLICY_CLASS = PolicyImpl.class;

    public static PrefixStore createStore(ASIdentifier asIdentifier, Neighbors neighbors, Callback callback, Policy policy, MRAIStore mraiStore,
            FlapStore flapStore) {
        switch (asIdentifier.getType()) {
        case RIS:
            PrefixStoreRIS store = new PrefixStoreRIS();
            store.setAsId(asIdentifier);
            store.setNeighbors(neighbors);
            return store;
        case ROUTEVIEW:
            PrefixStoreRouteView prefixStoreRouteView = new PrefixStoreRouteView();
            prefixStoreRouteView.setTimeController(TimeControllerFactory.getTimeController());
            prefixStoreRouteView.setCallback(CallbackFactory.getCallback(asIdentifier));
            return prefixStoreRouteView;
        case NORMAL:
            return createStoreMapImpl(asIdentifier, neighbors, callback, policy, mraiStore, flapStore);
        default:
            throw new BGPSymException("unknown type: " + asIdentifier.getType());
        }
    }

    private static PrefixStore createStoreMapImpl(ASIdentifier asIdentifier, Neighbors neighbors, Callback callback, Policy policy, MRAIStore mraiStore,
            FlapStore flapStore) {

        OutputBufferImpl tOutputBuffer = new OutputBufferImpl(asIdentifier);
        tOutputBuffer.setMraiStore(mraiStore);
        tOutputBuffer.setBufferStore(new OutputBufferStoreImpl());
        tOutputBuffer.setCallback(callback);
        tOutputBuffer.setNeighbors(neighbors);

        OutputState state = createOutpuState(asIdentifier, neighbors, policy);
        tOutputBuffer.setOutputState(state);

        PrefixStoreMapImpl store = new PrefixStoreMapImpl();
        store.setOutputBuffer(tOutputBuffer);
        store.setAsIdentifier(asIdentifier);
        store.setFlapStore(flapStore);
        store.setNeighbors(neighbors);

        store.setFlapTimerFactory(getFlapTimerFactory());

        store.setCallback(callback);

        PrefixCacheImplBlock cache = new PrefixCacheImplBlock();
        cache.setContainer(new NeighborsMapsContainerImpl(neighbors));
        store.setCache(cache);
        cache.setDoLog(Tools.getInstance().isLogOn(asIdentifier.toString()));
        ObjectRegister.getInstance().store(asIdentifier, ObjectRegister.Type.CACHE, cache);

        DiskStorageBlock diskStorageBlock = new DiskStorageBlock(neighbors.size(), getDiskCacheFile(asIdentifier), getDiskCacheArrayFile(asIdentifier));
        cache.setStorage(diskStorageBlock);
        ObjectRegister.getInstance().store(asIdentifier, ObjectRegister.Type.DISK, diskStorageBlock);

        if (policy == null) {
            try {
                policy = DEFAULT_POLICY_CLASS.newInstance();
            } catch (Exception e) {
                throw new BGPSymException(e);
            }
        }
        store.setPolicy(policy);

        tOutputBuffer.setPolicy(policy);
        return store;
    }

    private static OutputState createOutpuState(ASIdentifier asIdentifier, Neighbors neighbors, Policy policy) {
        OutputStateImpl state = new OutputStateImpl();
        state.setPolicy(policy);
        state.setNeighbors(neighbors);
        state.setAsIdentifier(asIdentifier);
        return state;
    }

    static FlapTimerFactory getFlapTimerFactory() {
        FlapTimerFactory factory = null;
        if (random.nextInt(100) < XProperties.getInstance().flapPercentage) {
            /*
             * it's real flap, we still have to decide whether this is cisco or juniper flap...
             */
            factory = new FlapTimerFactoryReal(random.nextInt(100) < XProperties.getInstance().flapDistribution);
        } else {
            factory = new FlapTimerFactoryMock();
        }
               
        factory = new FlapTimerFactoryMock();
        return factory;
    }

    private static String getDiskCacheFile(ASIdentifier asIdentifier) {
        return XProperties.getInstance().getDiskCacheDir() + asIdentifier;
    }

    private static String getDiskCacheArrayFile(ASIdentifier asIdentifier) {
        return XProperties.getInstance().getDiskCacheDir() + asIdentifier + ARRAY_SUFFIX;
    }

}

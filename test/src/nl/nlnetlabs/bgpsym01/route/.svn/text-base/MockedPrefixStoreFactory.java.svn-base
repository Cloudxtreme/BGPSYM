package nl.nlnetlabs.bgpsym01.route;

import nl.nlnetlabs.bgpsym01.callback.CallbackMock;
import nl.nlnetlabs.bgpsym01.mock.FlapStoreMock;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.route.output.OutputBuffer;
import nl.nlnetlabs.bgpsym01.route.output.OutputBufferMock;

public class MockedPrefixStoreFactory {

    public static PrefixStoreMapImpl getStore() {
        PrefixStoreMapImpl store = new PrefixStoreMapImpl();
        ASIdentifier myAsId = ASFactory.getInstance(0);
        store.setAsIdentifier(myAsId);
        store.setCallback(CallbackMock.getInstance());
        store.setFlapStore(new FlapStoreMock());
        store.setNeighbors(new Neighbors(myAsId));
        store.setFlapTimerFactory(new FlapTimerFactoryMockForTests());

        // TODO test callback
        store.setCallback(CallbackMock.getInstance());
        OutputBuffer outputBuffer = new OutputBufferMock();
        store.setOutputBuffer(outputBuffer);
        PrefixCacheMock cache = new PrefixCacheMock();
        store.setCache(cache);
        store.setPolicy(new PolicyImpl());
        return store;
    }
}

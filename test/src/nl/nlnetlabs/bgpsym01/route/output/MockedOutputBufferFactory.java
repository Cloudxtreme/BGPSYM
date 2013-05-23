package nl.nlnetlabs.bgpsym01.route.output;

import nl.nlnetlabs.bgpsym01.callback.CallbackMock;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.mocks.MRAITimerMock;
import nl.nlnetlabs.bgpsym01.route.Policy;
import nl.nlnetlabs.bgpsym01.route.PolicyImpl;

public class MockedOutputBufferFactory {

    public static OutputBufferImpl getInstance(int myNum, Policy policy, Neighbors neighbors, int... neighborsNum) {
        ASIdentifier myAsId = ASFactory.getInstance(myNum);
        OutputBufferImpl buffer = new OutputBufferImpl(myAsId);
        OutputBufferStore bufferStore = new OutputBufferStoreImpl();
        buffer.setBufferStore(bufferStore);
        if (policy == null) {
            policy = new PolicyImpl();
        }
        buffer.setPolicy(policy);
        buffer.setCallback(new CallbackMock());
        buffer.setMraiStore(new MRAIStoreMock());

        if (neighbors == null) {
            neighbors = new Neighbors(myAsId);
            for (int n : neighborsNum) {
                NeighborMock n1 = new NeighborMock(ASFactory.getInstance(n));
                n1.setTimer(new MRAITimerMock());
                neighbors.addNeighbor(n1);
            }
        }
        buffer.setNeighbors(neighbors);

        OutputStateImpl outputState = new OutputStateImpl();
        outputState.setPolicy(policy);
        outputState.setNeighbors(neighbors);
        outputState.setAsIdentifier(myAsId);

        buffer.setOutputState(outputState);

        return buffer;
    }

}

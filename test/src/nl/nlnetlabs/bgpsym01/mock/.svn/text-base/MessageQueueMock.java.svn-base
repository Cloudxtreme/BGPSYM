package nl.nlnetlabs.bgpsym01.mock;

import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.primitives.types.InputGenerator;
import nl.nlnetlabs.bgpsym01.primitives.types.MessageQueue;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;

public class MessageQueueMock implements MessageQueue {

    public int pinged;

    public void addInputGenerator(InputGenerator inputGenerator) {
    }

    public Update getNext() {
        throw new NotImplementedException();
    }

    public void ping() {
        pinged++;
    }

    public void shutdown() {
    }

}
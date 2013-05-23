package nl.nlnetlabs.bgpsym01.main.tcp;

import java.io.IOException;
import java.io.InputStream;

import nl.nlnetlabs.bgpsym01.coordinator.AbstractChannelAttachment;
import nl.nlnetlabs.bgpsym01.primitives.InputHandler;
import nl.nlnetlabs.bgpsym01.primitives.TCPMessage;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;

public class TCPMessageReceivingBuffer extends AbstractChannelAttachment {

    private InputHandler inputHandler = new InputHandler() {

        public void handleInput(InputStream inputStream) throws IOException {
            EDataInputStream eis = new EDataInputStream(inputStream);
            TCPMessage message = new TCPMessage();
            message.readExternal(eis);
            message.getAsId().getProcess().getQueue().addMessage(message.getUpdate());
        }

    };

    public TCPMessageReceivingBuffer() {
        super();
    }

    @Override
    protected InputHandler getInputHandler() throws IOException {
        return inputHandler;
    }

}

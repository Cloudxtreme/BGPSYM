package nl.nlnetlabs.bgpsym01.main.tcp;

import java.io.IOException;
import java.io.InputStream;

import nl.nlnetlabs.bgpsym01.command.CoordinationCommand;
import nl.nlnetlabs.bgpsym01.command.MasterCommand;
import nl.nlnetlabs.bgpsym01.coordinator.AbstractChannelAttachment;
import nl.nlnetlabs.bgpsym01.primitives.InputHandler;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;

public class MasterCommandReceivingAttachment extends AbstractChannelAttachment {

    private TCPStart jvm;

    private InputHandler inputHandler = new InputHandler() {

        public void handleInput(InputStream inputStream) throws IOException {
            EDataInputStream eis = new EDataInputStream(inputStream);
            CoordinationCommand command = CoordinationCommand.readCommand(eis);
            processCommand(command);
        }

    };

    public MasterCommandReceivingAttachment(TCPStart jvm) {
        super();
        this.jvm = jvm;
    }

    protected void processCommand(CoordinationCommand tmp) {
        MasterCommand command = (MasterCommand) tmp;
        command.setJvm(jvm);
        command.process();
    }

    @Override
    protected InputHandler getInputHandler() throws IOException {
        return inputHandler;
    }
}

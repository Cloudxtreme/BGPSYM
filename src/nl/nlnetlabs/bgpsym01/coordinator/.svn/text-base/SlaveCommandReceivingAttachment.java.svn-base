package nl.nlnetlabs.bgpsym01.coordinator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SocketChannel;

import nl.nlnetlabs.bgpsym01.command.CoordinationCommand;
import nl.nlnetlabs.bgpsym01.command.SetRegistryCommand;
import nl.nlnetlabs.bgpsym01.command.SlaveCommand;
import nl.nlnetlabs.bgpsym01.primitives.InputHandler;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;

import org.apache.log4j.Logger;

public class SlaveCommandReceivingAttachment extends AbstractChannelAttachment {

    static Logger log = Logger.getLogger(SlaveCommandReceivingAttachment.class);

    Coordinator main;

    private SocketChannel channel;

    private InputHandler inputHandler = new InputHandler() {

        public void handleInput(InputStream inputStream) throws IOException {
            EDataInputStream eis = new EDataInputStream(inputStream);
            CoordinationCommand command = CoordinationCommand.readCommand(eis);
            processCommand(command);
        }

    };

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public SlaveCommandReceivingAttachment(Coordinator main) {
        super();
        this.main = main;
    }

    protected void processCommand(CoordinationCommand command) {
        if (command instanceof SetRegistryCommand) {
            // for this command we have to set the channel
            ((SetRegistryCommand) command).setChannel(channel);
        }

        SlaveCommand mc = (SlaveCommand) command;
        mc.setCoordinator(main);
        mc.process();
    }

    @Override
    protected InputHandler getInputHandler() throws IOException {
        return inputHandler;
    }

}

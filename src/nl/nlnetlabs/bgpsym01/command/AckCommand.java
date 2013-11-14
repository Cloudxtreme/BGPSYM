package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class AckCommand extends SlaveCommand {

    private static CommandType type = CommandType.ACK;

    private static int count;

    @Override
    public CommandType getCommandType() {
        return type;
    }

    @Override
    public void process() {
        count++;
        if (count == XProperties.getInstance().hostCount) {
            coordinator.getCommandSenderHelper().ackReceived();
        }
    }

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
    }

    public static void resetCounter() {
        count = 0;
    }

}

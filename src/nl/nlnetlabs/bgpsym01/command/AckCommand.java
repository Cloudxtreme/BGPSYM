package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

public class AckCommand extends SlaveCommand {

	private static Logger log = Logger.getLogger(AckCommand.class);

    private static CommandType type = CommandType.ACK;

    private static int count;

    @Override
    public CommandType getCommandType() {
        return type;
    }

    @Override
    public void process() {
    	//log.info("received ACK. Count: "+count+" hostCount:"+XProperties.getInstance().hostCount);
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

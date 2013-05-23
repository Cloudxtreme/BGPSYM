package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;

public class RTTCommand extends MasterCommand {

    long startTime;

    public RTTCommand() {
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.RTT;
    }

    @Override
    public void process() {
        RTTCommandResponse response = new RTTCommandResponse(startTime);
        jvm.getCst().sendCommand(response);
    }

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
        startTime = in.readLong();
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
        out.writeLong(startTime);
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

}

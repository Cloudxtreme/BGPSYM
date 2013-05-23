package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;

import org.apache.log4j.Logger;

public class RTTCommandResponse extends SlaveCommand {

    private static Logger log = Logger.getLogger(RTTCommandResponse.class);

    long sendTime;

    public RTTCommandResponse(long sendTime) {
        super();
        this.sendTime = sendTime;
    }

    public RTTCommandResponse() {
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.RTT_RESPONSE;
    }

    @Override
    public void process() {
        long receiveTime = System.currentTimeMillis();
        if (log.isInfoEnabled()) {
            log.info("rtt=" + (receiveTime - sendTime));
        }
    }

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
        sendTime = in.readLong();
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
        out.writeLong(sendTime);
    }

}

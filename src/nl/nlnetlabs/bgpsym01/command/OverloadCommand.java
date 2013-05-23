package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;

import org.apache.log4j.Logger;

public class OverloadCommand extends SlaveCommand {

    private static final int SENDER_ID_BITS_LENGTH = 7;

    private static Logger log = Logger.getLogger(OverloadCommand.class);

    CommandType type = CommandType.OVERLOAD;

    boolean isOverload;

    private int senderId;

    @Override
    public CommandType getCommandType() {
        return type;
    }

    @Override
    public void process() {
        if (log.isInfoEnabled()) {
            log.info("processing, isOverload=" + isOverload);
        }
        coordinator.getPropagationHelper().changeLoad(isOverload ? 1 : -1);
    }

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
        isOverload = in.readBoolean();
        senderId = in.readBits(SENDER_ID_BITS_LENGTH);
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
        out.writeBoolean(isOverload);
        out.writeBits(senderId, SENDER_ID_BITS_LENGTH);
    }

    public boolean isOverload() {
        return isOverload;
    }

    public void setOverload(boolean isOverload) {
        this.isOverload = isOverload;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

}

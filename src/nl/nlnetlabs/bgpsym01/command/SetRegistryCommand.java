package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.StaticThread;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

public class SetRegistryCommand extends SlaveCommand {

    private static Logger log = Logger.getLogger(SetRegistryCommand.class);
    private int num;

    private SocketChannel channel;

    private static int count;

    public SetRegistryCommand() {

    }

    public SetRegistryCommand(int num) {
        this.num = num;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.SET_REGISTRY;
    }

    @Override
    public void process() {
        count++;
        log.info("count=" + count + ", num=" + num);
        coordinator.getRegistries().get(num).setAttachment(channel);
        if (count == XProperties.getInstance().hostCount) {
            while (coordinator.getCommandSenderHelper() == null) {
                if (log.isInfoEnabled()) {
                    log.info("is null");
                    StaticThread.sleep(10);
                }
            }
            coordinator.getCommandSenderHelper().ackReceived();
        }
    }

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
        num = in.readInt();
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
        out.writeInt(num);
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void setNum(int num) {
        this.num = num;
    }

}

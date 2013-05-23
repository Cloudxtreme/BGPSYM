package nl.nlnetlabs.bgpsym01.main.tcp;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import nl.nlnetlabs.bgpsym01.command.SlaveCommand;

import org.apache.log4j.Logger;

public class CommunicationSocketThread extends AbstractCommunicationThread {

    static Logger log = Logger.getLogger(CommunicationSocketThread.class);
    private SocketChannel channel;
    private TCPStart jvm;
    private MasterCommandReceivingAttachment commandReceivingAttachment;

    public CommunicationSocketThread(SocketChannel channel, TCPStart jvm) {
        /*
         * it is possible that this channel is not connected right now, 
         * although the connect method has already been called
         */

        this.jvm = jvm;
        this.channel = channel;
        setName("command_recv_" + jvm.getMyNum());
        init();
    }

    @Override
    protected void localInit() throws IOException {
        int ops = channel.isConnected() ? SelectionKey.OP_READ : SelectionKey.OP_CONNECT;
        channel.register(selector, ops);
        commandReceivingAttachment = new MasterCommandReceivingAttachment(jvm);
        channel.keyFor(selector).attach(commandReceivingAttachment);
    }

    @Override
    protected boolean reportIdle() {
        return false;
    }

    public void sendCommand(SlaveCommand command) {
        send(channel, command);
    }
}

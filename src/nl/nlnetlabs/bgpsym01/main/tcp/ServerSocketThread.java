package nl.nlnetlabs.bgpsym01.main.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;

import org.apache.log4j.Logger;

/**
 * This thread is responsible for handing {@see TCPMessage}'s received from
 * other nodes
 */
public class ServerSocketThread extends AbstractCommunicationThread {

    private static Logger log = Logger.getLogger(ServerSocketThread.class);

    private int port;

    public ServerSocketThread(int port) {
        this.port = port;
        init();
    }

    @Override
    protected void localInit() {
        try {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress(port));
            ssc.configureBlocking(false);
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            if (log.isDebugEnabled()) {
                log.debug("created selector...");
            }
        } catch (IOException e) {
            log.error("IOException, msg=" + e.getMessage());
            throw new BGPSymException(e);
        }
    }

    @Override
    protected SocketChannel processAccept(SelectionKey key) throws IOException {
        SocketChannel socketChannel = super.processAccept(key);
        socketChannel.keyFor(selector).attach(new TCPMessageReceivingBuffer());
        return socketChannel;
    }

    public SocketChannel registerConnection(String host, int port, OverloadMonitor monitor) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(host, port));
        int ops = channel.isConnected() ? 0 : SelectionKey.OP_CONNECT;
        assert (selector != null);
        channel.register(selector, ops);

        TCPMessageSendAttachedBuffer attachment = new TCPMessageSendAttachedBuffer();
        attachment.setMonitor(monitor);
        channel.keyFor(selector).attach(attachment);

        selector.wakeup();
        return channel;
    }

}

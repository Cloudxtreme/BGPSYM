package nl.nlnetlabs.bgpsym01.neighbor.impl;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import nl.nlnetlabs.bgpsym01.main.tcp.OverloadMonitor;
import nl.nlnetlabs.bgpsym01.main.tcp.ServerSocketThread;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.TCPMessage;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;

import org.apache.log4j.Logger;

public class TCPConnection {

    private static Logger log = Logger.getLogger(TCPConnection.class);
    private ServerSocketThread serverSocketThread;
    private SocketChannel socketChannel;

    public TCPConnection(String host, int port, ServerSocketThread serverSocketThread, OverloadMonitor monitor) {
        this.serverSocketThread = serverSocketThread;
        try {
            socketChannel = serverSocketThread.registerConnection(host, port, monitor);
        } catch (IOException e) {
            log.error(e.getMessage() + ", host=" + host + ":" + port);
            throw new BGPSymException(e);
        }
    }

    /*
     * only writing to the socket is synchronized
     */
    public void send(ASIdentifier receiverId, BGPUpdate update) {
        TCPMessage msg = new TCPMessage();
        msg.setAsId(receiverId);
        msg.setUpdate(update);

        serverSocketThread.send(socketChannel, msg);

    }

}

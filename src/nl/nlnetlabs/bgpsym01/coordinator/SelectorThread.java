package nl.nlnetlabs.bgpsym01.coordinator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.command.MasterCommand;
import nl.nlnetlabs.bgpsym01.main.tcp.AbstractCommunicationThread;
import nl.nlnetlabs.bgpsym01.xstream.XRegistry;

public class SelectorThread extends AbstractCommunicationThread {

    @Override
    protected boolean reportIdle() {
        return false;
    }

    private static final String SELECTOR = "selector";

    private int port = -1;

    private ArrayList<XRegistry> registries;

    private Coordinator main;

    public SelectorThread(Coordinator coordinator, ArrayList<XRegistry> registries, int port) {
        super();
        this.registries = registries;
        this.main = coordinator;
        this.port = port;
        setName(SELECTOR);
        init();
    }

    @Override
    protected SocketChannel processAccept(SelectionKey key) throws IOException {
        SocketChannel sc = super.processAccept(key);

        // this is TCP connection - means that we have to finish our job
        if (key.attachment().equals(false)) {
            main.end();
            sc.close();
            return sc;
        } else if (key.attachment().equals(true)) {
            SlaveCommandReceivingAttachment attachment = new SlaveCommandReceivingAttachment(main);
            attachment.setChannel(sc);
            sc.keyFor(selector).attach(attachment);
            return sc;
        } else {
            throw new RuntimeException("impossible");
        }
    }

    @Override
    protected void localInit() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(port));
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        ssc.keyFor(selector).attach(true);

        // control socket
        ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(port + 1));
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        ssc.keyFor(selector).attach(false);

    }

    public void send(MasterCommand command) {
        // get the socket for this update
        SocketChannel sc = (SocketChannel) registries.get(command.getProcessId()).getAttachment();
        send(sc, command);
    }

}

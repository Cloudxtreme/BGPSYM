package nl.nlnetlabs.bgpsym01.main.tcp;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import nl.nlnetlabs.bgpsym01.coordinator.AbstractChannelAttachment;
import nl.nlnetlabs.bgpsym01.coordinator.InputHandlerThread;
import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.types.EExternalizable;
import nl.nlnetlabs.bgpsym01.primitives.types.ShutdownadbleThread;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

public abstract class AbstractCommunicationThread extends ShutdownadbleThread {

    // TODO we want it to be injected
    private InputHandlerThread inputHandlerThread;

    private static Logger log = Logger.getLogger(AbstractCommunicationThread.class);
    protected Selector selector;
    protected ByteBuffer buffer = ByteBuffer.wrap(new byte[SystemConstants.BYTE_ARRAY_SIZE]);

    // private FIFOQueue<SelectionKey> queue = new
    // FIFOQueue<SelectionKey>(QUEUE_CAPACITY);

    protected volatile boolean shutdown;

    /**
     * override this method in subclass if you want some local initilization
     */
    protected void localInit() throws IOException {

    }

    /**
     * Should we report that we have been idle? It's not desirable behavior
     * (they should override this method).
     * 
     * @return
     */
    protected boolean reportIdle() {
        return true;
    }

    public void init() {
        // TODO let him tweak this behavior
        setPriority(SystemConstants.COMMUNICATION_THREAD_PRIORITY);
        try {
            selector = Selector.open();
            localInit();

        } catch (IOException e) {
            log.error("", e);
            throw new BGPSymException(e);
        }

        if (XProperties.getInstance().isUseInputHandlerThread()) {
            inputHandlerThread = new InputHandlerThread();
            inputHandlerThread.setDaemon(true);
            inputHandlerThread.setName("iht_" + Tools.getInstance().getProcNum());
            inputHandlerThread.start();
        }
    }

    @Override
    public void run() {
        int selectCounter = 0;

        int zeros = 0;
        while (true) {
            try {
                int selected = selector.select();

                if (shutdown) {
                    return;
                }
                selectCounter++;

                if (selectCounter % 5000000 == 0) {
                    if (log.isInfoEnabled()) {
                        log.info("is zero (" + zeros + "/" + selectCounter + ")");
                    }
                }

                if (selected == 0) {
                    zeros++;
                    continue;
                }

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    processKey(key);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new BGPSymException(e);
            }
        }
    }

    private void processKey(SelectionKey key) throws IOException {
        if (shutdown) {
            return;
        }
        if (!key.isValid()) {
            key.cancel();
        }
        if (key.isValid() && key.isAcceptable()) {
            processAccept(key);
        } else if (key.isValid() && key.isConnectable()) {
            processConnect(key);
        } else if (key.isValid() && key.isReadable()) {
            processRead(key);
        } else if (key.isValid() && key.isWritable()) {
            processWrite(key);
        }
    }

    private void processWrite(SelectionKey key) throws IOException {
        AbstractChannelAttachment attachment = (AbstractChannelAttachment) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();

        boolean wantsToWrite = false;
        if (shutdown) {
            key.cancel();
            return;
        }
        synchronized (attachment) {
            if (attachment.wantsToWrite()) {
                attachment.fillBuffer(buffer);
                wantsToWrite = true;
            }
        }

        if (wantsToWrite) {
            int written;
            buffer.flip();
            try {
                written = channel.write(buffer);
            } catch (IOException exc) {
                key.cancel();
                return;
            } catch (BufferOverflowException e) {
                key.cancel();
                return;
            }
            synchronized (attachment) {
                attachment.cutOutputBuffer(written);
                setOps(key, attachment);
            }
            buffer.clear();
        }
    }

    // we are synchronized on the attachment
    private boolean setOps(SelectionKey key, AbstractChannelAttachment attachment) {
        // we don't like unvalid keys
        if (!key.isValid()) {
            return false;
        }
        if (!((SocketChannel) key.channel()).isConnected()) {
            return false;
        }

        int ops = attachment.wantsToWrite() ? SelectionKey.OP_WRITE : 0;

        ops |= attachment.wantsToRead() ? SelectionKey.OP_READ : 0;
        int oldOps = key.interestOps();
        if (oldOps != ops) {
            key.interestOps(ops);
            // if we want less than we wanted then we don't have to wake the guy
            // up
            return !(ops < oldOps);
        }
        return false;
    }

    private void processRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        int count = channel.read(buffer);
        if (count == -1) {
            key.cancel();
            return;
        }

        // that's all - give him the stuff and let him play
        AbstractChannelAttachment attachment = ((AbstractChannelAttachment) key.attachment());
        buffer.flip();
        synchronized (attachment) {
            attachment.read(buffer, inputHandlerThread);
            if (!channel.isConnected()) {
                throw new BGPSymException();
            }
            setOps(key, attachment);
        }
        buffer.clear();
    }

    private void processConnect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.finishConnect();
        AbstractChannelAttachment attachment = ((AbstractChannelAttachment) key.attachment());
        synchronized (attachment) {
            setOps(key, attachment);
        }
    }

    protected SocketChannel processAccept(SelectionKey key) throws IOException {
        ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssChannel.accept();
        sc.configureBlocking(false);

        // if a socket does not want read it have to deregister it in the
        // overriden method!
        sc.register(selector, SelectionKey.OP_READ);
        return sc;
    }

    @Override
    public void shutdown() {
        shutdown = true;
        selector.wakeup();
        if (inputHandlerThread != null) {
            inputHandlerThread.shutdown();
            try {
                inputHandlerThread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    public void send(SocketChannel channel, EExternalizable message) {
        SelectionKey key = channel.keyFor(selector);
        if (key == null || !key.isValid()) {
            // log.info("key is not valid, not sending");
            return;
        }
        AbstractChannelAttachment attachment = (AbstractChannelAttachment) key.attachment();
        attachment.sendObject(message);
        boolean out;
        synchronized (attachment) {
            out = setOps(key, attachment);
        }
        if (out) {
            selector.wakeup();
        }
    }

}
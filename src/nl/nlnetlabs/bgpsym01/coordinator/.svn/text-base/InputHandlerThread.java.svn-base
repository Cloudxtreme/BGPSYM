package nl.nlnetlabs.bgpsym01.coordinator;

import java.io.IOException;
import java.io.InputStream;

import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.InputHandler;
import nl.nlnetlabs.bgpsym01.primitives.types.FIFOQueue;
import nl.nlnetlabs.bgpsym01.primitives.types.FIFOQueueImpl;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;
import nl.nlnetlabs.bgpsym01.primitives.types.ShutdownadbleThread;

import org.apache.log4j.Logger;

public class InputHandlerThread extends ShutdownadbleThread {

    private boolean finished;

    private static Logger log = Logger.getLogger(InputHandlerThread.class);
    FIFOQueue<Pair<InputStream, InputHandler>> queue = new FIFOQueueImpl<Pair<InputStream, InputHandler>>(4096);

    public synchronized void add(InputStream stream, InputHandler handler) {
        queue.add(new Pair<InputStream, InputHandler>(stream, handler));
        notify();
    }

    @Override
    public void run() {
        while (true) {
            Pair<InputStream, InputHandler> pair;
            synchronized (this) {
                while (queue.size() == 0) {
                    if (finished) {
                        return;
                    }
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
                if (finished) {
                    return;
                }
                pair = queue.remove();
            }
            try {
                pair.value.handleInput(pair.key);
            } catch (IOException e) {
                log.error(e);
                throw new BGPSymException(e);
            }
        }

    }

    public synchronized int size() {
        return queue.size();
    }

    @Override
    public synchronized void shutdown() {
        finished = true;
        notify();
    }

}

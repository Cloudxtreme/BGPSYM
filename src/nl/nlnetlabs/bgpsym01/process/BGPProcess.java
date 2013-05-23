package nl.nlnetlabs.bgpsym01.process;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.callback.Callback;
import nl.nlnetlabs.bgpsym01.cache.ResultWriterLog;
import nl.nlnetlabs.bgpsym01.main.EL;
import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASType;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.bgp.RunnableUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.primitives.types.MessageInputGenerator;
import nl.nlnetlabs.bgpsym01.primitives.types.MessageQueue;
import nl.nlnetlabs.bgpsym01.primitives.types.ShutdownadbleThread;
import nl.nlnetlabs.bgpsym01.route.PrefixStore;
import nl.nlnetlabs.bgpsym01.xstream.XRegistry;

import org.apache.log4j.Logger;

public class BGPProcess extends ShutdownadbleThread {

    private static final int MAX_ALLOWED_PROCESSING_TIME = 100;

    private PrefixStore store;

    private Neighbors neighbors;

    private ASIdentifier asIdentifier;

    private Callback callback;

    private MessageQueue messageQueue;

    private MessageInputGenerator queue;

    private transient boolean started;

    private Logger log = Logger.getLogger(BGPProcess.class);

	private ResultWriterLog resultWriterLog;

	private ArrayList<XRegistry> registries;

    public BGPProcess(Callback callback) {
        this.callback = callback;
    }

    public void init() {

        setName(asIdentifier.toString());
        if (asIdentifier.getType() != ASType.NORMAL) {
            setPriority(SystemConstants.ROUTE_VIEW_THREAD_PRIORITY);
        }

        synchronized (this) {
            started = true;
            notify();
        }
        // queue.setLogOn(Tools.getInstance().isLogOn(asIdentifier.toString()));
    }

    public synchronized void waitForStart() {
        while (!started) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
                log.error("interrupted", e);
            }
        }
    }

    public void setNeighbors(Neighbors neighbors) {
        this.neighbors = neighbors;
    }

    @SuppressWarnings("unused")
	public void uponReceive(BGPUpdate update) {

        callback.updateReceived(update.getSender(), update);
        // add to prefix store
        ASIdentifier sender = update.getSender();
        if (sender == null) {
            sender = asIdentifier;
        }

        List<Prefix> prefixes = update.getPrefixes();
        if (prefixes != null) {
            Route route = update.getRoute();
            store.prefixReceived(sender, prefixes, route);
        }

        Collection<Prefix> withdrawals = update.getWithdrawals();
        if (withdrawals != null) {
            store.prefixRemove(sender, withdrawals);
        }
        if (EL.queueDebug && log.isInfoEnabled()) {
            log.info("X7, processed...");
        }
    }

    @SuppressWarnings("unused")
	@Override
    public void run() {
        init();
        
		while (true) {
            Update update = messageQueue.getNext();
            if (EL.queueDebug && log.isInfoEnabled()) {
                log.info("X7, update=" + update);
            }
            if (update == null) {
                if (log.isDebugEnabled()) {
                    log.debug("got shutdown..., cleaning up");
                }
                shutdown();
                break;
            }

            long start = System.currentTimeMillis();
            processUpdate(update);
            if (EL.checkWarnings) {
                long processingTime = System.currentTimeMillis() - start;
                if (update instanceof BGPUpdate && processingTime > MAX_ALLOWED_PROCESSING_TIME) {
                    //log.warn("processingTime=" + processingTime + "; size=" + neighbors.size());
                }
            }
            // Thread.yield();
            // callback.flush();
        }
        callback.close();
		resultWriterLog.close();
        
		if (log.isDebugEnabled()) {
            log.debug("thread finished");
        }
    }

    void processUpdate(Update update) {
        switch (update.getType()) {
        case BGPUPDATE:
            uponReceive((BGPUpdate) update);
            break;
        case RUNNABLE_UPDATE:
            ((RunnableUpdate) update).run(this);
            break;
        }
    }

    @Override
    public void shutdown() {
        messageQueue.shutdown();
    }

    public MessageInputGenerator getQueue() {
        return queue;
    }

    public Neighbors getNeighbors() {
        return neighbors;
    }

    public ASIdentifier getASIdentifier() {
        return asIdentifier;
    }

    public PrefixStore getStore() {
        return store;
    }

    public void setQueue(MessageInputGenerator queue) {
        this.queue = queue;
    }

    public ASIdentifier getAsIdentifier() {
        return asIdentifier;
    }

    public void setAsIdentifier(ASIdentifier asIdentifier) {
        this.asIdentifier = asIdentifier;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setStore(PrefixStore store) {
        this.store = store;
    }

    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

	public void setResultWriterLog (ResultWriterLog resultWriter) {
		this.resultWriterLog = resultWriter;
	}

	public ResultWriterLog getResultWriterLog () {
		return resultWriterLog;
	}

	public void setRegistries (ArrayList<XRegistry> registries) {
		this.registries = registries;
	}

	public ArrayList<XRegistry> getRegistries () {
		return registries;
	}
}

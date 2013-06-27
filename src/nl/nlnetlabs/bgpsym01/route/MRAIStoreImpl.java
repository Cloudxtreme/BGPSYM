package nl.nlnetlabs.bgpsym01.route;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

import nl.nlnetlabs.bgpsym01.callback.Callback;
import nl.nlnetlabs.bgpsym01.main.EL;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeController;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.FlushUpdate;

import org.apache.log4j.Logger;

/**
 * {@link MRAIStore} implementation using {@link PriorityQueue} for timer
 * ordering and {@link HashSet} for as storing (to know for which as we already
 * have MRAI timer stored (as the first timer registered is always the best
 * one)).
 */
public class MRAIStoreImpl implements MRAIStore {

    PriorityQueue<MRAITimer> queue = new PriorityQueue<MRAITimer>(10, new MRAIComparator());

    // Set<ASIdentifier> ases = new HashSet<ASIdentifier>();

    private static Logger log = Logger.getLogger(MRAIStoreImpl.class);

    private Callback callback;

    static TimeController timeController = TimeControllerFactory.getTimeController();

    private static class MRAIComparator implements Comparator<MRAITimer> {
        public int compare(MRAITimer o1, MRAITimer o2) {
            if (o1.getStartTime() < o2.getStartTime()) {
                return -1;
            }
            return o1.getStartTime() == o2.getStartTime() ? 0 : 1;
        }
    }

    long startTime = -1;

    public boolean hasSomething() {
        return startTime != -1;
    }

    // it's like peek at the top of the queue
    public long getReadyTime() {
        return startTime;
    }

    public void register(ASIdentifier asId, MRAITimer timer) {
        if (timer.isTicking()) {
            return;
        }
        timer.setTicking(true);
        queue.add(timer);
        startTime = timeController.getRealMS(queue.peek().getStartTime());

        if (EL.timersLogging) {
            callback.mraiRegister(asId, timeController.getRealMS(timer.getStartTime()), startTime);
        }
    }

    public ASIdentifier next() {
        MRAITimer timer = queue.poll();
        timer.setTicking(false);
        if (EL.queueDebug && log.isInfoEnabled()) {
            log.info("unticking, id=" + System.identityHashCode(this));
        }
        // ases.remove(timer.getAsIdentifier());
        startTime = queue.size() > 0 ? timeController.getRealMS(queue.peek().getStartTime()) : -1;
        ASIdentifier asId = timer.getAsIdentifier();
        if (EL.timersLogging) {
            callback.mraiTrigger(asId);
        }
        return asId;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public int size() {
        return queue.size();
    }

    public Update getUpdate() {
    	// EL.queueDebug &&
        if ( log.isInfoEnabled()) {
            log.info("X7, mrai flushing...,id=" + System.identityHashCode(this));
        }
        ASIdentifier identifier = next();
        FlushUpdate update = new FlushUpdate();
        update.setAsId(identifier);
        // if (log.isInfoEnabled()) {
        // log.info("unflushing... " + identifier);
        // }
        return update;
    }

}

package nl.nlnetlabs.bgpsym01.route;

import java.util.Random;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeController;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class MRAITimerImpl implements MRAITimer {

    @Override
    public String toString() {
        return "lastSent=" + lastSent + ", startTime=" + getStartTime() + ", current=" + timeController.getCurrentTime() + ", canSend=" + canSendNow();
    }

    private Random random = new Random(System.currentTimeMillis());

    private ASIdentifier asIdentifier;

    private static TimeController timeController = TimeControllerFactory.getTimeController();

    private static int timeScaler = XProperties.getInstance().timeScaler;

    private long lastSent = -1;

    private boolean ticking;

    private long threshold = 30000; // value in logical ms

    private long currentThreshhold = threshold;

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.route.MRAITimer#canSendNow()
     */
    public boolean canSendNow() {
        return lastSent == -1 || timeController.getCurrentTime() >= getStartTime();
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.route.MRAITimer#getStartTime()
     */
    public long getStartTime() {
        return lastSent + currentThreshhold;
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.route.MRAITimer#sent()
     */
    public void sent() {
        lastSent = timeController.getCurrentTime();
        if (threshold > 0) {
            currentThreshhold = getThreshold(threshold);
        }
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.route.MRAITimer#reset()
     */
    public void reset() {
        lastSent = -1;
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
        this.currentThreshhold = threshold;
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.route.MRAITimer#getAsIdentifier()
     */
    public ASIdentifier getAsIdentifier() {
        return asIdentifier;
    }

    public void setAsIdentifier(ASIdentifier asIdentifier) {
        this.asIdentifier = asIdentifier;
        random = new Random(System.currentTimeMillis() + asIdentifier.getInternalId() * 17);
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.route.MRAITimer#isTicking()
     */
    public boolean isTicking() {
        return ticking;
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.route.MRAITimer#setTicking(boolean)
     */
    public void setTicking(boolean ticking) {
        this.ticking = ticking;
    }

    public TimeController getTimeController() {
        return timeController;
    }

    long getThreshold(long threshold) {
        return (long) (threshold * (random.nextDouble() / 4 + 0.75));
    }

}

package nl.nlnetlabs.bgpsym01.primitives.timers;

/**
 * Interface for controlling the time.
 * 
 * {@link TimeControllerImpl} is the default implementation with static time
 * scaling by a given factor.
 */
public interface TimeController {

    /**
     * Allows to translate between real time and logical time
     * 
     * @param time
     *            the logical time
     * @return how many real-world milliseconds has to elapse before the logical
     *         time will have the desired value
     */
    public long getWaitingTime(long time);

    /**
     * @return the current logical time
     */
    public long getCurrentTime();

    public long getRealMS(long logical);

    public long realWaitingTime(long time, boolean beNice);

    public long getRealWorldDiscrepancy(long logicalDiscrepancy);
}

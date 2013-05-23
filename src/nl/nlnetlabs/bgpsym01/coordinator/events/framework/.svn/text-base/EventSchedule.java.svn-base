package nl.nlnetlabs.bgpsym01.coordinator.events.framework;

/**
 * Models when the event should be scheduled
 */

public abstract class EventSchedule {

    /**
     * @return info if he knows when he wants to be scheduled
     */
    abstract public boolean timeIsKnown();

    /**
     * 
     * @return time in millis (from the simulation start) when the event wants
     *         to be fired
     * @throws IllegalStateException
     *             thrown if {@link #timeIsKnown()} is not true
     */
    abstract public long getLaunchTime() throws IllegalStateException;

}

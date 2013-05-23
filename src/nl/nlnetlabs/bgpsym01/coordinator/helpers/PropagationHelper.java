package nl.nlnetlabs.bgpsym01.coordinator.helpers;

import nl.nlnetlabs.bgpsym01.coordinator.Coordinator;

/**
 * Takes the responsibility of propagating prefixes from the coordinator.
 * 
 * @see Coordinator
 */
public interface PropagationHelper {

    /**
     * Propagate prefixes according to settings
     */
    public void propagatePrefixes();

    /**
     * Ends the party
     */
    public void end();

    /**
     * Change load - load behavior is implementation dependent
     * 
     * @param value
     *            - number showing how to change the load
     */
    public void changeLoad(int value);

}
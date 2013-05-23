package nl.nlnetlabs.bgpsym01.route;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.types.InputGenerator;
import nl.nlnetlabs.bgpsym01.route.output.OutputBufferImpl;

public interface MRAIStore extends InputGenerator {

    /**
     * Registers a timer for particular neighboring AS. As soon as the timer
     * expires the {@link OutputBufferImpl#flush(ASIdentifier)} should be called
     * for this neighbor.
     * 
     * @param asId
     * @param timer
     */
    public void register(ASIdentifier asId, MRAITimer timer);

    /**
     * It's an error to call this function if there {@link #hasSomething()}
     * returns false.
     * 
     * @return how much time we have to wait for the next timer to expire.
     */
    public long getReadyTime();

    /**
     * @return <b>true</b> iff there is any timer awaiting expiry
     */
    public boolean hasSomething();

    public int size();

}

package nl.nlnetlabs.bgpsym01.primitives.types;

import java.util.List;

import nl.nlnetlabs.bgpsym01.cache.PrefixCache;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.RunnableUpdate;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;
import nl.nlnetlabs.bgpsym01.process.NeighborInvalidator;
import nl.nlnetlabs.bgpsym01.process.NeighborInvalidatorImpl;
import nl.nlnetlabs.bgpsym01.route.PrefixStore;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreMapImpl;
import nl.nlnetlabs.bgpsym01.route.output.OutputBuffer;
import nl.nlnetlabs.bgpsym01.route.output.OutputBufferImpl;
import nl.nlnetlabs.bgpsym01.route.output.OutputState;

import org.apache.log4j.Logger;

public class InvalidateUpdate extends RunnableUpdate {

    private static Logger log = Logger.getLogger(InvalidateUpdate.class);

    /**
     * If true validate, invalidate otherwise
     */
    private boolean validate;

    private List<Prefix> prefixes;

    private ASIdentifier neighborId;


    public void setNeighborId(ASIdentifier neighborId) {
        this.neighborId = neighborId;
    }

    // /////////////////////
    // private stuff
    PrefixCache cache;
    OutputState outputState;
    OutputBuffer outputBuffer;
    Neighbors neighbors;



    /**
     * Sets {@link #cache}, {@link #outputBuffer} and {@link #outputState}
     * 
     * @param store
     */
    void inferStuff(PrefixStore store) {
        if (!(store instanceof PrefixStoreMapImpl)) {
            log.error("only PrefixStoreMapImpl supported, unknown type: " + store.getClass().getName());
            return;
        }
        PrefixStoreMapImpl storeImpl = (PrefixStoreMapImpl) store;
        if (!(storeImpl.getOutputBuffer() instanceof OutputBufferImpl)) {
            log.error("only OutputBufferImpl supported, unknown type: " + store.getClass().getName());
            return;
        }
        OutputBufferImpl buffer = (OutputBufferImpl) storeImpl.getOutputBuffer();
        cache = storeImpl.getCache();
        outputState = buffer.getOutputState();
        outputBuffer = buffer;
        neighbors = storeImpl.getNeighbors();
    }

    NeighborInvalidatorImpl createInvalidator(PrefixCache cache, OutputState outputState, OutputBuffer outputBuffer) {
        // we use only parameters here, we do not acces fields
        NeighborInvalidatorImpl invalidator = new NeighborInvalidatorImpl();
        invalidator.setOutputBuffer(outputBuffer);
        invalidator.setPrefixCache(cache);
        invalidator.setOutputState(outputState);
        invalidator.setNeighbors(neighbors);
        return invalidator;
    }

    /**
     * Calls
     * {@link NeighborInvalidatorImpl#validate(nl.nlnetlabs.bgpsym01.neighbor.Neighbor, java.util.List)}
     * or
     * {@link NeighborInvalidatorImpl#invalidate(nl.nlnetlabs.bgpsym01.neighbor.Neighbor, java.util.List)}
     * 
     */
    void call(Neighbors neighbors, NeighborInvalidator invalidator) {
        Neighbor neighbor = neighborId == null ? null : neighbors.getNeighbor(neighborId);
        if (validate) {
            invalidator.validate(neighbor, prefixes);
        } else {
            invalidator.invalidate(neighbor, prefixes);
        }
    }

    @Override
    public void run(BGPProcess process) {
        inferStuff(process.getStore());
        // call with attributes as parameters
        NeighborInvalidator invalidator = createInvalidator(cache, outputState, outputBuffer);
        call(neighbors, invalidator);
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public List<Prefix> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<Prefix> prefixes) {
        this.prefixes = prefixes;
    }

    public ASIdentifier getNeighborId() {
        return neighborId;
    }

}

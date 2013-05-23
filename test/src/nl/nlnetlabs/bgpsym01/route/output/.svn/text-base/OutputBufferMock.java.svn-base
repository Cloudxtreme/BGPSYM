package nl.nlnetlabs.bgpsym01.route.output;

import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.primitives.OutputAddEntity;
import nl.nlnetlabs.bgpsym01.primitives.OutputEntity;
import nl.nlnetlabs.bgpsym01.primitives.OutputRemoveEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;

import org.apache.log4j.Logger;

public class OutputBufferMock implements OutputBuffer {

    private static Logger log = Logger.getLogger(OutputBufferMock.class);

    private List<OutputAddEntity> addedEntities = new ArrayList<OutputAddEntity>();

    int flushed = 0;
    int added = 0;
    int removed = 0;

    public int invalidated = 0;
    public int validatedCount = 0;

    public List<Pair<Neighbor, List<Pair<Prefix, Route>>>> validateList = new ArrayList<Pair<Neighbor, List<Pair<Prefix, Route>>>>();

    public void invalidate(Neighbor neighbor, List<Prefix> prefixList) {
        invalidated++;
    }

    public void validate(Neighbor neighbor, List<Pair<Prefix, Route>> prefixes) {
        validatedCount++;
        validateList.add(new Pair<Neighbor, List<Pair<Prefix, Route>>>(neighbor, prefixes));
    }

    public void add(OutputEntity entity) {
        if (entity instanceof OutputAddEntity) {
            added++;
            addedEntities.add((OutputAddEntity) entity);
            if (log.isDebugEnabled()) {
                log.debug("add:" + entity);
            }
        } else if (entity instanceof OutputRemoveEntity) {
            removed++;
        }
    }

    public void flush() {
        flushed++;
    }

    public int getFlushed() {
        return flushed;
    }

    public void setFlushed(int flushed) {
        this.flushed = flushed;
    }

    public int getAdded() {
        return added;
    }

    public void setAdded(int added) {
        this.added = added;
    }

    public int getRemoved() {
        return removed;
    }

    public void setRemoved(int removed) {
        this.removed = removed;
    }

    public List<OutputAddEntity> getAddedEntities() {
        return addedEntities;
    }

    public void setAddedEntities(ArrayList<OutputAddEntity> addedEntities) {
        this.addedEntities = addedEntities;
    }

    public void flush(ASIdentifier as) {
        flush();
    }

}

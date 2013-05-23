package nl.nlnetlabs.bgpsym01.cache;

import java.io.IOException;
import java.util.Map;

import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EExternalizable;

import org.apache.log4j.Logger;

public class PrefixInfo implements EExternalizable {

    private static Logger log = Logger.getLogger(PrefixInfo.class);

    private static final int NEIGHBORS_MAP_MAX_SIZE_BITS = 16;

    private Prefix prefix;

    private Map<ASIdentifier, PrefixTableEntry> neighborsMap;

    private PrefixTableEntry currentEntry;

    public PrefixInfo(Prefix prefix, PrefixTableEntry currentEntry, Map<ASIdentifier, PrefixTableEntry> neighborsMap) {
        this.prefix = prefix;
        this.currentEntry = currentEntry;
        this.neighborsMap = neighborsMap;
    }

    public PrefixInfo() {
    }

    public Prefix getPrefix() {
        return prefix;
    }

    public void setPrefix(Prefix prefix) {
        this.prefix = prefix;
    }

    public Map<ASIdentifier, PrefixTableEntry> getNeighborsMap() {
        return neighborsMap;
    }

    public void setNeighborsMap(Map<ASIdentifier, PrefixTableEntry> neighborsMap) {
        this.neighborsMap = neighborsMap;
    }

    public PrefixTableEntry getCurrentEntry() {
        return currentEntry;
    }

    public void setCurrentEntry(PrefixTableEntry currentEntry) {
        this.currentEntry = currentEntry;
    }

    @Override
    public String toString() {
        return "p: " + prefix + ", current=" + (currentEntry == null ? null : currentEntry.getRoute()) + ", map=" + neighborsMap;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PrefixInfo) {
            PrefixInfo tmp = (PrefixInfo) obj;
            if (!(prefix.equals(tmp.prefix))) {
                return false;
            }

            if (!neighborsMap.equals(tmp.neighborsMap)) {
                return false;
            }

            if (currentEntry == null && tmp.currentEntry != null) {
                return false;
            }

            if (currentEntry != null && !currentEntry.equals(tmp.currentEntry)) {
                return false;

            }

            return true;
        }
        return false;
    }

    public void readExternal(EDataInputStream in) throws IOException {
        prefix = Prefix.getInstance(in.readBits(SystemConstants.PREFIX_SIZE_BITS));

        int size = in.readBits(NEIGHBORS_MAP_MAX_SIZE_BITS);

        neighborsMap = PrefixTableEntry.getEmptyMap();

        for (int i = 0; i < size; i++) {
            PrefixTableEntry pte = new PrefixTableEntry();
            pte.readExternal(in);

            if (in.readBoolean()) {
                this.setCurrentEntry(pte);
            }

            // add the prefixEntry to the map
            if (pte.getFlapTimer().isPositive() || pte.isValid()) {
                neighborsMap.put(pte.getOriginator(), pte);
            }

        }

        if (getCurrentEntry() == null && neighborsMap.size() != 0) {
            log.warn("no current entry found! prefix=" + prefix + ", map.size()=" + neighborsMap.size());
        }
    }

    public void writeExternal(EDataOutputStream out) throws IOException {
        out.writeBits(prefix.getNum(), SystemConstants.PREFIX_SIZE_BITS);

        int size = neighborsMap == null ? 0 : neighborsMap.size();
        out.writeBits(size, NEIGHBORS_MAP_MAX_SIZE_BITS);

        if (size > 0) {
            for (ASIdentifier asId : neighborsMap.keySet()) {
                PrefixTableEntry pte = neighborsMap.get(asId);

                pte.writeExternal(out);
                out.writeBoolean(pte == getCurrentEntry());
            }
        }
    }

    public PrefixInfo getSimpleCopy() {
        PrefixInfo copy = new PrefixInfo();
        copy.prefix = prefix;
        copy.currentEntry = currentEntry;
        return copy;
    }

}

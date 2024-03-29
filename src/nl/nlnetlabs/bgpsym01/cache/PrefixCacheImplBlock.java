package nl.nlnetlabs.bgpsym01.cache;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

// TODO it might be good idea to change PrefixStore to take advantage of PrefixInfo directly
public class PrefixCacheImplBlock implements PrefixCache {

    private static final int CREATED_LOGGING_INTERVAL = XProperties.getInstance().getCreatedLoggingInterval();

    private static Logger log = Logger.getLogger(PrefixCacheImplBlock.class);

    DiskStorage storage;

    private int prefixesCreated;

    private NeighborsMapsContainer container;

    // private static final int PREFIX_CACHE_SIZE = 100;
    // TODO [values here]
    LinkedHashMap<Prefix, PrefixInfo> table = new LinkedHashMap<Prefix, PrefixInfo>(XProperties.getInstance().prefixCacheSize * 2, (float) 0.80, true);

    private boolean doLog;

    private class TableIterator implements Iterator<PrefixInfo> {

        private Iterator<Entry<Prefix, PrefixInfo>> iterator;
        Entry<Prefix, PrefixInfo> current;

        public TableIterator() {
            iterator = table.entrySet().iterator();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public PrefixInfo next() {
            current = iterator.next();
            return current.getValue();
        }

        public void remove() {
            iterator.remove();
            // prefix is stored - we have to give his map back to the pool
            container.giveBack(current.getValue().getNeighborsMap());
        }
    }

    private void storeBlock() {
        try {
            int count = table.size();

            storage.storePrefixes(new TableIterator());
            if (log.isDebugEnabled()) {
                log.debug("stored prefix, before=" + count + "; after=" + table.size());
            }
        } catch (IOException e) {
            log.error("io error", e);
            e.printStackTrace(System.err);
        }
    }

    private void loadPrefix(Prefix prefix) {
    	
    	synchronized (table) {
    		if (table.get(prefix) == null) {
                if (log.isDebugEnabled()) {
                    log.debug("trying to load prefix " + prefix);
                }
                while (table.size() > XProperties.getInstance().prefixCacheSize) {
                    // we have to store a block to load a new one
                    storeBlock();
                }

                Iterator<PrefixInfo> iterator = storage.readPrefix(prefix);
                if (iterator == null) {
                    createNewPrefix(prefix);
                    return;
                }
                boolean found = false;
                while (iterator.hasNext()) {
                    PrefixInfo prefixInfo = iterator.next();
                    /*                if (log.isInfoEnabled()) {
                                        if (table.get(prefixInfo.getPrefix()) != null) {
                                            // this is a BAD thing!
                                            log.warn("prefix " + prefixInfo.getPrefix() + " is already present in the table");
                                        }
                                    }*/
                    table.put(prefixInfo.getPrefix(), prefixInfo);
                    found = true;
                }
                if (!found) {
                    log.info("1");
                    log.warn("this is bad for prefix=" + prefix.getNum());
                    createNewPrefix(prefix);
                }
            }
		}
    }

    private void createNewPrefix(Prefix prefix) {
        if (log.isDebugEnabled()) {
            log.debug("creating new prefix for " + prefix);
        }
        PrefixInfo prefixInfo = new PrefixInfo();
        prefixInfo.setPrefix(prefix);
        prefixInfo.setNeighborsMap(container.getMap());
        
        synchronized (table) {
        	table.put(prefix, prefixInfo);
		}        

        if (doLog && log.isInfoEnabled()) {
            if (prefixesCreated++ % CREATED_LOGGING_INTERVAL == 0) {
                log.info("last= " + prefix + ", created " + prefixesCreated);
            }
        }
    }

    public void storePrefixesPermanent() {
        if (1 < 2) {
            throw new NotImplementedException("do not use it!!!");
        }
        while (table.size() > 0) {
            storeBlock();
        }
        try {
            storage.writePrefixArrayPermanent();
        } catch (IOException e) {
            log.error(e);
            throw new BGPSymException(e);
        }
    }

    public PrefixInfo getPrefixInfo(Prefix prefix) {
        loadPrefix(prefix);
        return table.get(prefix);
    }

    public void setPrefixInfo(Prefix prefix, PrefixInfo prefixInfo) {
        // TODO count distinct prefixes
    	synchronized (table) {
    		table.put(prefix, prefixInfo);
		}
    }

    public void setDoLog(boolean doLog) {
        this.doLog = doLog;
    }

    public DiskStorage getStorage() {
        return storage;
    }

    public void setStorage(DiskStorage storage) {
        this.storage = storage;
    }

    public void flush() {
        while (table.size() > XProperties.getInstance().prefixCacheSize / 10) {
            storeBlock();
        }
        // storage.sync();
    }

    public void setContainer(NeighborsMapsContainer container) {
        this.container = container;
    }
    
    public LinkedHashMap<Prefix, PrefixInfo> getTable() {
        return table;
    }
}

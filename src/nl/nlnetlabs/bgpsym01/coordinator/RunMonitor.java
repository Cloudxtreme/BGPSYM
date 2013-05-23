package nl.nlnetlabs.bgpsym01.coordinator;

import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

/**
 * This class monitors the run of the whole simulation
 */
public class RunMonitor {
    private static Logger log = Logger.getLogger(RunMonitor.class);

    private long start;

    private long storeStart;

    public void start() {
        log.info("START, props=" + XStreamFactory.getXStream().toXML(XProperties.getInstance()));
        start = System.currentTimeMillis();
    }

    public void storeStart() {
        storeStart = System.currentTimeMillis();
        log.info("STORE START, (time=" + (storeStart - start) + ")");
    }

    public void stored() {
        long storeTime = System.currentTimeMillis() - storeStart;
        long time = System.currentTimeMillis() - start;
        log.info("STORED (time=" + time + ", storeTime=" + storeTime + ")");
    }

    public void finish() {
        long time = System.currentTimeMillis() - start;
        log.info("DONE(time=" + time + ")");

    }

}

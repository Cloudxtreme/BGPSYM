package nl.nlnetlabs.bgpsym01.primitives.types;

import org.apache.log4j.Logger;

public class StaticThread {

    private static Logger log = Logger.getLogger(StaticThread.class);

    public final static void sleep(long millis) {
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                log.error("", e);
            }
        }
    }

}

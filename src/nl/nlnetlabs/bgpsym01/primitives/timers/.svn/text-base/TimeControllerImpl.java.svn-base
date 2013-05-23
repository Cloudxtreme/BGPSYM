package nl.nlnetlabs.bgpsym01.primitives.timers;

import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

public class TimeControllerImpl implements TimeController {

    private static Logger log = Logger.getLogger(TimeControllerImpl.class);

    public static final int scaler = XProperties.getInstance().timeScaler;

    // it will be set :)
    static long start;

    TimeControllerImpl() {
    }

    // argument is in real-world time - says just for how long the wait(long)
    // should be called
    public long realWaitingTime(long time, boolean beNice) {
        if (time < 0) {
            return -1;
        } else {
            long q = time - System.currentTimeMillis();
            if (q <= 0 || (beNice && q <= scaler)) {
                q = -1;
            }
            return q;
        }
    }

    public long getCurrentTime() {
        return (System.currentTimeMillis() - start) * scaler;
    }

    // translates logical time into real world time
    public long getRealMS(long logical) {
        return logical / scaler + start;
    }

    public long getWaitingTime(long time) {
        long answer = (time - getCurrentTime()) / scaler;
        if (log.isInfoEnabled()) {
            // log.info("question: " + time + ", ans=" + answer + ", multi=" +
            // multiplier);
        }
        return answer;
    }

    public static void setStartTime(long startTime) {
        start = startTime;
    }

    public static long getStartTime() {
        return start;
    }

    public long getRealWorldDiscrepancy(long logicalDiscrepancy) {
        return logicalDiscrepancy / scaler;
    }

}

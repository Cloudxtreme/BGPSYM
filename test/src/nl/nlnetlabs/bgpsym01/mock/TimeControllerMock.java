package nl.nlnetlabs.bgpsym01.mock;

import nl.nlnetlabs.bgpsym01.primitives.timers.TimeController;

import org.apache.log4j.Logger;

public class TimeControllerMock implements TimeController {

    private static Logger log = Logger.getLogger(TimeControllerMock.class);

    /**
     * if this is true we try to compute the outcome. If false we return the
     * {@link #answer}.
     */
    public boolean oneToOne = true;

    public long answer;

    public long currentTime;

    public long waitingTimeAnswer = -7;

    public long getWaitingTime(long time) {
        if (oneToOne) {
            long ret = time - currentTime;
            if (log.isDebugEnabled()) {
                log.debug("returning " + ret + " for time " + time);
            }
            return ret;
        }
        return answer;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public long getRealMS(long logical) {
        return logical;
    }

    public long realWaitingTime(long time, boolean beNice) {
        if (waitingTimeAnswer != -7) {
            return waitingTimeAnswer;
        }
        if (time == -1) {
            return -1;
        }
        if (log.isDebugEnabled()) {
            log.debug("t=" + time + ", c=" + currentTime);
        }
        return time - currentTime;
    }

    public long getRealWorldDiscrepancy(long logicalDiscrepancy) {
        return logicalDiscrepancy;
    }

}

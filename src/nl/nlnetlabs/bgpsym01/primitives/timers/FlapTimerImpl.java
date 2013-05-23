package nl.nlnetlabs.bgpsym01.primitives.timers;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.main.EL;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;

import org.apache.log4j.Logger;

// in this class times are in seconds (not ms because we don't like millions)
public class FlapTimerImpl implements FlapTimer {

    public FlapTimerImpl() {
        // synchronized (random) {
        // if (random.nextInt(100) < properties.flapDistribution) {
        // timerType = FlapTimerType.JUNIPER;
        // } else {
        // timerType = FlapTimerType.CISCO;
        // }
        // }
    }

    public FlapTimerImpl(FlapTimerType timerType) {
        this.timerType = timerType;
    }

    // how many logical ms can go before we call an unflapped route an error
    private static final int ACCEPTABLE_UNFLAP_DELAY = 1000;

    private static final int EPSILON = 5;

    private static final int MS_IN_MINUTE = 1000;

    private static final double LOG20 = Math.log(2.0);

    // don't make too much fuss if we get awaken a bit too early
    private static final long TIME_EPSILON = 2;

    private static TimeController timeController = TimeControllerFactory.getTimeController();

    private static Logger log = Logger.getLogger(FlapTimerImpl.class);

    private FlapTimerType timerType;

    private long unSupressTime;

    long suppressTime;

    double value = 0;

    long valueTime;

    boolean suspended = false;

    /* unfortunately too many objects are equal for us, but we don't want any
     * computations involved... This method is used only by testing so it actually doesn't matter 
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof FlapTimerImpl) {
            FlapTimerImpl timer = (FlapTimerImpl) o;

            // suspended state should be the same
            if (timer.suspended ^ suspended) {
                return false;
            }

            if (suspended && timer.getUnflapTime() != getUnflapTime()) {
                return false;
            }
            return true;
        }
        return false;
    }

    public long getUnflapTime() {
        if (!suspended) {
            return 0;
        }

        return unSupressTime * MS_IN_MINUTE;
    }

    public double getValue() {
        return value;
    }

    void updateValue() {
        long time = timeController.getCurrentTime() / MS_IN_MINUTE;
        value = value * Math.pow(2.0, -((time - valueTime)) / timerType.halfTime);
        double waitingTime = -timerType.halfTime * Math.log(timerType.reuseThreshold / value) / LOG20;
        if (!suspended) {
            suppressTime = time;
        }
        unSupressTime = Math.min(suppressTime + timerType.maxSuppress, time + (long) waitingTime);

        valueTime = time;
    }

    void updateState() {
        if (!suspended && value > timerType.threshold) {
            suspended = true;
            // if (log.isDebugEnabled()) {
            // log.debug("suppress, time=" + supressTime + ", unsuppress=" +
            // unSupressTime + ", curr=" + timeController.getCurrentTime() + ",
            // value=" + value);
            // }
        }
        if (suspended) {
            updateValue();
        }
    }

    public boolean isFlapped() {
        return suspended;
    }

    public int compareTo(FlapTimer o) {
        /*
         * TODO: be able to compare also with different timers
         */

        if (o instanceof FlapTimerImpl) {
            FlapTimerImpl fsi = (FlapTimerImpl) o;
            if (!suspended || !fsi.suspended) {
                // it is actually an error
                log.error("suspended=" + suspended + ", o.suspended=" + fsi.suspended);
                return -1;
            }
            long myUnflpaTime = getUnflapTime();
            long hisUnflapTime = fsi.getUnflapTime();
            return myUnflpaTime < hisUnflapTime ? -1 : myUnflpaTime == hisUnflapTime ? 0 : 1;
        } else {
            log.error("got flapTimer with bad class, class=" + o.getClass().getName());
            return -1;
        }

    }

    public void unflap(Prefix prefix) {
        if (!suspended) {
            if (EL.checkWarnings) {
                log.warn("not suspended, value=" + value + ", prefix=" + prefix);
            }
            return;
        }

        if (EL.flapLogging && log.isInfoEnabled()) {
            log.info("unflapping, id=" + System.identityHashCode(this));
        }

        long u1 = unSupressTime;
        double v1 = value;
        updateValue();
        // we love seconds not ms
        long currentTime = timeController.getCurrentTime() / MS_IN_MINUTE;

        if (log.isDebugEnabled()) {
            log.debug("TIME: " + unSupressTime + "/" + u1 + ", currentTime=" + currentTime + ", " + timeController.getWaitingTime(getUnflapTime()) + ", v="
                    + value + "/" + v1 + ", id=" + System.identityHashCode(this));
        }

        // we were sleeping for the whole hour - time to get some rest :)
        if (currentTime + TIME_EPSILON >= suppressTime + timerType.maxSuppress) {
            // TODO: this is a very interesting piece of code...
            value = 0;
        }
        if (value - EPSILON > timerType.reuseThreshold) {
            String msg = "value=" + value + ", valueTime=" + valueTime + ", reuse=" + timerType.reuseThreshold + ", unsuppress=" + unSupressTime + ", current="
                    + currentTime + ", suppress=" + suppressTime;
            log.error(msg);

            // we cannot leave it like that!
        }
        suspended = false;
        suppressTime = -1;

    }

    public void announce() {
        addValue(timerType.annPenalty);
    }

    public void withdraw() {
        addValue(timerType.withPenalty);
    }

    private void addValue(int pen) {
        if (pen != 0) {
            updateValue();
            unflapIfNeeded();
            value += pen;
            updateState();
        }
    }

    public void reannounce() {
        addValue(timerType.reAnnPenalty);
    }

    /**
     * Unflaps the route if previous flap is not needed anymore.
     * 
     * TODO: when we are done with flap testing remove this function
     * 
     * For more info look at {@link http
     * ://iii.nlnetlabs.nl/twiki/bin/view/Main/BGPSimulatorProject
     * #Automatic_unflapping}
     */
    public void unflapIfNeeded() {
        long currentTime = timeController.getCurrentTime();
        // two simulation seconds seems to be acceptable
        if (suspended && (currentTime - unSupressTime * MS_IN_MINUTE > ACCEPTABLE_UNFLAP_DELAY)) {
            long diff = timeController.getRealMS(currentTime) - timeController.getRealMS(unSupressTime * 1000);
            if (diff > ACCEPTABLE_UNFLAP_DELAY) {
                log.warn("would have unflapped..., curr=" + currentTime + ", unsuppress=" + unSupressTime + ", real diff=" + diff + ", value=" + value + ",id="
                        + System.identityHashCode(this));
            }

        }
    }

    public boolean isPositive() {
        return value > 0;
    }

    public void readExternal(EDataInputStream in) throws IOException {
        timerType = in.readBoolean() ? FlapTimerType.CISCO : FlapTimerType.JUNIPER;
        suspended = in.readBoolean();
        value = in.readDouble();
        valueTime = in.readLong();
        if (suspended) {
            suppressTime = in.readLong();
        }
        updateValue();
    }

    public void writeExternal(EDataOutputStream out) throws IOException {
        updateValue();
        out.writeBoolean(timerType == FlapTimerType.CISCO);
        out.writeBoolean(suspended);
        out.writeDouble(value);
        out.writeLong(valueTime);
        if (suspended) {
            out.writeLong(suppressTime);
        }
    }

    public static TimeController getTimeController() {
        return timeController;
    }

    public static void setTimeController(TimeController timeController) {
        FlapTimerImpl.timeController = timeController;
    }

    public FlapTimerType getTimerType() {
        return timerType;
    }

    public void setTimerType(FlapTimerType timerType) {
        this.timerType = timerType;
    }

}

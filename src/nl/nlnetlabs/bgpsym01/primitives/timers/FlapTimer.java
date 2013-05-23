package nl.nlnetlabs.bgpsym01.primitives.timers;

import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.types.EExternalizable;

public interface FlapTimer extends Comparable<FlapTimer>, EExternalizable {

    public enum FlapTimerType {
        CISCO(0, 500, 1000, 2000), JUNIPER(1000, 500, 1000, 3000);

        public final int annPenalty;

        public final int reAnnPenalty;

        public final int withPenalty;

        public final int threshold;

        public final double halfTime = 900;

        public final int reuseThreshold = 750;

        public final long maxSuppress = 3600;

        private FlapTimerType(int annPenalty, int reAnnPenalty, int withPenalty, int threshold) {
            this.annPenalty = annPenalty;
            this.reAnnPenalty = reAnnPenalty;
            this.withPenalty = withPenalty;
            this.threshold = threshold;
        }
    }

    public boolean isFlapped();

    public long getUnflapTime();

    public void announce();

    public void reannounce();

    public void withdraw();

    public boolean isPositive();

    public void unflap(Prefix prefix);

    public double getValue();

}

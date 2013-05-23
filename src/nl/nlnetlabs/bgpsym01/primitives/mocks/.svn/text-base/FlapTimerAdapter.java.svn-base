package nl.nlnetlabs.bgpsym01.primitives.mocks;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimer;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;

public class FlapTimerAdapter implements FlapTimer {

    public int announced;
    public int reannounced;
    public int withdrawn;

    public boolean positive = true;

    public void announce() {
        announced++;
    }

    public void reannounce() {
        reannounced++;
    }

    public void withdraw() {
        withdrawn++;
    }

    public long getUnflapTime() {
        throw new NotImplementedException();
    }

    public boolean isFlapped() {
        return false;
    }

    public int compareTo(FlapTimer o) {
        long myTime = getUnflapTime();
        long hisTime = o.getUnflapTime();
        return myTime < hisTime ? -1 : myTime == hisTime ? 0 : 1;
    }

    public boolean isPositive() {
        return positive;
    }

    public void readExternal(EDataInputStream in) throws IOException {
    }

    public void writeExternal(EDataOutputStream out) throws IOException {
    }

    @Override
    public boolean equals(Object obj) {
        return true;
    }

    public void unflap(Prefix prefix) {
    }

    public double getValue() {
        return 0;
    }

}

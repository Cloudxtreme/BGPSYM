package nl.nlnetlabs.bgpsym01.primitives.mocks;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;

public class FlapTimerMock extends FlapTimerAdapter {

    @Override
    public void readExternal(EDataInputStream in) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public void writeExternal(EDataOutputStream out) throws IOException {
        throw new NotImplementedException();
    }

    public boolean flapped;
    public long unflapTime;

    public boolean reactWithFlap;

    public boolean reactWithUnflap;

    public FlapTimerMock() {

    }

    @Override
    public void unflap(Prefix prefix) {
        flapped = false;
    }

    void react() {
        if (reactWithFlap) {
            flapped = true;
        } else if (reactWithUnflap) {
            flapped = false;
        }
        reactWithFlap = false;
        reactWithUnflap = false;
    }

    @Override
    public void announce() {
        announced++;
        react();
    }

    @Override
    public void reannounce() {
        reannounced++;
        react();
    }

    @Override
    public void withdraw() {
        withdrawn++;
        react();
    }

    public FlapTimerMock(boolean flapped, long unflapTime) {
        super();
        this.flapped = flapped;
        this.unflapTime = unflapTime;
    }

    @Override
    public long getUnflapTime() {
        return unflapTime;
    }

    @Override
    public boolean isFlapped() {
        return flapped;
    }

}

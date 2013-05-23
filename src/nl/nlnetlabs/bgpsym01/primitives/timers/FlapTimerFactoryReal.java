package nl.nlnetlabs.bgpsym01.primitives.timers;

import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimer.FlapTimerType;

public class FlapTimerFactoryReal implements FlapTimerFactory {

    private boolean isCiscoLike;

    public FlapTimerFactoryReal(boolean isCiscoLike) {
        this.isCiscoLike = isCiscoLike;
    }

    public FlapTimer getFlapTimer() {
        return new FlapTimerImpl(isCiscoLike ? FlapTimerType.CISCO : FlapTimerType.JUNIPER);
    }

}

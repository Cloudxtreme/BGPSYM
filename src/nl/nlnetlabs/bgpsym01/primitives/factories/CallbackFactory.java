package nl.nlnetlabs.bgpsym01.primitives.factories;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.callback.Callback;
import nl.nlnetlabs.bgpsym01.callback.CallbackLog4j;
import nl.nlnetlabs.bgpsym01.callback.CallbackMock;
import nl.nlnetlabs.bgpsym01.callback.FileCallbackFull;
import nl.nlnetlabs.bgpsym01.callback.TimersCallback;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASType;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class CallbackFactory {

    public static Callback getCallbackMock() {
        return CallbackMock.getInstance();
    }

    public static Callback getCallbackTimer(ASIdentifier id) {
        try {
            TimersCallback timersCallback = new TimersCallback(XProperties.getInstance().workingDir + "/log/timers_log" + "_" + id);
            timersCallback.setTimeController(TimeControllerFactory.getTimeController());
            return timersCallback;
        } catch (IOException e) {
            throw new BGPSymException(e);
        }
    }

    public static Callback getCallbackRegister(ASIdentifier id) {
        try {
            // TODO make it better
            return new FileCallbackFull(XProperties.getInstance().workingDir + "/log/callback.log" + "_" + id);
        } catch (IOException e) {
            throw new BGPSymException(e);
        }
    }

    public static CallbackLog4j getCallbackLog4j() {
        return new CallbackLog4j();
    }

    public static Callback getCallback(ASIdentifier id) {
		if (false && id.getType() == ASType.NORMAL) {
            return getCallbackRegister(id);
        } else if (false && id.getInternalId() % 1000 == 1) {
            // return getCallbackTimer(id);
            return getCallbackRegister(id);

            /*        } else if (id.getInternalId() % 1000 == 0 || id.getInternalId() == 5745) {
                        return getCallbackRegister(id);*/
        } else {
			//return getCallbackRegister(id);
			return getCallbackMock();
        }
    }

}

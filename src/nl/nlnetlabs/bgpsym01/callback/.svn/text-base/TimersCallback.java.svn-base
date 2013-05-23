package nl.nlnetlabs.bgpsym01.callback;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeController;

public class TimersCallback extends FileCallbackAbstract {

    private TimeController timeController;

    @Override
    public void flapRegister(ASIdentifier asId, Prefix prefix, long unflap, long readyTime) {
        write("FR ; " + asId + " ; " + prefix + " ; " + timeController.realWaitingTime(unflap, false) + " ; "
                + timeController.realWaitingTime(readyTime, false) + " ; ");
    }

    @Override
    public void flapTrigger(ASIdentifier asId, Prefix prefix, long readyTime) {
        write("FT ; " + asId + " ; " + prefix + " ; " + timeController.realWaitingTime(readyTime, false));
    }

    @Override
    public void mraiRegister(ASIdentifier asId, long timerStart, long storeStart) {
        write("MR ; " + asId + " ; " + timeController.realWaitingTime(timerStart, false) + " ; " + timeController.realWaitingTime(storeStart, false));
    }

    @Override
    public void mraiTrigger(ASIdentifier asId) {
        write("MT ; " + asId);
    }

    public TimersCallback(String fileName) throws IOException {
        super(fileName);
    }

    public void setTimeController(TimeController timeController) {
        this.timeController = timeController;
    }

    /*    @Override
        public void withdrawalReceived(ASIdentifier asIdentifier, Prefix prefix) {
             write("WITH AS=" + asIdentifier + " ; PREFIX=" + prefix);
        }*/

    @Override
    public void arbitrary(String msg) {
        write(msg);
    }

    /*    @Override
        public void prefixReceived(ASIdentifier asIdentifier, Prefix prefix, Route route) {
             write("RCV AS=" + asIdentifier + " ; PREFIX=" + prefix + " ; ROUTE=" + route);
        }*/

}

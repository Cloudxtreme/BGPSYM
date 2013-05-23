package nl.nlnetlabs.bgpsym01.callback;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.OutputEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeController;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;

public class FileCallbackFull extends FileCallbackAbstract {

    @Override
    public void addEntity(OutputEntity entity) {
        write(entity.toString());
    }

    TimeController timeController = TimeControllerFactory.getTimeController();

    public FileCallbackFull(String fileName) throws IOException {
        super(fileName);
    }

    @Override
    public void prefixRegistered(ASIdentifier asIdentifier, Prefix prefix, Route oldRoute, Route newRoute) {
        write("register AS=" + asIdentifier + " ; PREFIX=" + prefix + " ; OLDROUTE=" + oldRoute + " ; NEWROUTE=" + newRoute);
    }

    @Override
    public void prefixReceived(ASIdentifier asIdentifier, Prefix prefix, Route route) {
        // write("prefRcv AS=" + asIdentifier + " ; PREFIX=" + prefix + " ;
        // ROUTE=" + route);
    }

    @Override
    public void prefixUnregistered(ASIdentifier asIdentifier, Prefix prefix, Route oldRoute, Route newRoute) {
        write("throw AS=" + asIdentifier + " ; PREFIX=" + prefix + " ; OLDROUTE=" + oldRoute + " ; NEWROUTE=" + newRoute);
    }

    @Override
    public void arbitrary(String msg) {
        write(msg);
    }

    @Override
    public void withdrawalReceived(ASIdentifier asIdentifier, Prefix prefix) {
        write("withdMsg AS=" + asIdentifier + " ; PREFIX=" + prefix);
    }

    @Override
    public void withdrawalSent(ASIdentifier asIdentifier, Prefix prefix, Route newRoute) {
        // write("sentwith AS=" + asIdentifier + " ; PREFIX=" + prefix + ";
        // NEWROUTE=" + newRoute);
    }

    @Override
    public void updateSend(ASIdentifier asIdentifier, BGPUpdate update) {
        write("upSend  AS=" + asIdentifier + "; update=" + update);
    }

    @Override
    public void prefixAdvertised(ASIdentifier asIdentifier, Prefix prefix, Route route) {
        // write("prefAdv AS=" + asIdentifier + "; prefix=" + prefix + ",
        // route=" + route);
    }

    @Override
    public void updateReceived(ASIdentifier asIdentifier, BGPUpdate update) {
        write("upRecv  AS=" + asIdentifier + "; update=" + update);
    }

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
        write("MR ; " + asId + " ; " + timeController.realWaitingTime(timeController.getRealMS(timerStart), false) + " ; "
                + timeController.realWaitingTime(timeController.getRealMS(storeStart), false));
    }

    @Override
    public void mraiTrigger(ASIdentifier asId) {
        write("MT ; " + asId);
    }

}

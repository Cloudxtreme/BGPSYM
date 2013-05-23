package nl.nlnetlabs.bgpsym01.callback;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;

import org.apache.log4j.Logger;

public class CallbackLog4j extends CallbackMock {

    private static Logger log = Logger.getLogger(CallbackLog4j.class);

    @Override
    public void prefixRegistered(ASIdentifier asIdentifier, Prefix prefix, Route oldRoute, Route newRoute) {
        if (log.isInfoEnabled()) {
            log.info("REGISTER AS=" + asIdentifier + " ; PREFIX=" + prefix + " ; OLDROUTE=" + oldRoute + " ; NEWROUTE=" + newRoute);
        }
    }

}

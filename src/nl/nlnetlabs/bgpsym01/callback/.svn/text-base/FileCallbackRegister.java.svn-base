package nl.nlnetlabs.bgpsym01.callback;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;

public class FileCallbackRegister extends FileCallbackAbstract {

    public FileCallbackRegister(String fileName) throws IOException {
        super(fileName);
    }

    @Override
    public void prefixRegistered(ASIdentifier asIdentifier, Prefix prefix, Route oldRoute, Route newRoute) {
        write("REGISTER AS=" + asIdentifier + " ; PREFIX=" + prefix + " ; OLDROUTE=" + oldRoute + " ; NEWROUTE=" + newRoute);
    }

}

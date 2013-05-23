package nl.nlnetlabs.bgpsym01.primitives.types;

import java.io.IOException;

public interface EExternalizable {

    public void readExternal(EDataInputStream in) throws IOException;

    public void writeExternal(EDataOutputStream out) throws IOException;

}

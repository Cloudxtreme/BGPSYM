package nl.nlnetlabs.bgpsym01.mock;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;

public class UpdateMock implements Update {

    public int num;

    public UpdateMock(int num) {
        this.num = num;
    }

    public UpdateType getType() {
        return null;
    }

    public void readExternal(EDataInputStream in) throws IOException {
    }

    public void writeExternal(EDataOutputStream out) throws IOException {
    }

}

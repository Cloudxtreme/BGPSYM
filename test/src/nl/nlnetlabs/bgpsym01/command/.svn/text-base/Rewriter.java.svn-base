package nl.nlnetlabs.bgpsym01.command;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EExternalizable;

public class Rewriter {

    public static EExternalizable rewrite(EExternalizable object, Class<? extends EExternalizable> clazz) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            EDataOutputStream eos = new EDataOutputStream(baos);

            object.writeExternal(eos);
            eos.close();

            EDataInputStream eis = new EDataInputStream(new ByteArrayInputStream(baos.toByteArray()));

            EExternalizable out = clazz.newInstance();
            out.readExternal(eis);

            return out;
        } catch (Exception e) {
            throw new BGPSymException(e);
        }
    }

    public static EDataInputStream getStream(EExternalizable object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EDataOutputStream eos = new EDataOutputStream(baos);

        try {
            object.writeExternal(eos);
            eos.close();
        } catch (IOException e) {
            throw new BGPSymException(e);
        }

        EDataInputStream eis = new EDataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        return eis;

    }

}

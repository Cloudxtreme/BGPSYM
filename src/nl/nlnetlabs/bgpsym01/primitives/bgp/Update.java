package nl.nlnetlabs.bgpsym01.primitives.bgp;

import java.io.IOException;
import java.util.NoSuchElementException;

import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EExternalizable;

public interface Update extends EExternalizable {
    public enum UpdateType {
        BGPUPDATE(BGPUpdate.class, 0),
        RUNNABLE_UPDATE(RunnableUpdate.class, 1);

        private Class<? extends Update> clazz;

        private int num;

        private static int BITS = 1;

        private UpdateType(Class<? extends Update> clazz, int num) {
            this.clazz = clazz;
            this.num = num;
        }

        public static Update getInstance(EDataInputStream in) throws IOException {
            int num = in.readBits(BITS);
            for (UpdateType type : UpdateType.values()) {
                if (type.num == num) {
                    try {
                        return type.clazz.newInstance();
                    } catch (Exception e) {
                        throw new BGPSymException(e);
                    }
                }
            }
            throw new NoSuchElementException();
        }

        public void writeExternal(EDataOutputStream eos) throws IOException {
            eos.writeBits(num, BITS);
        }

    }

    public UpdateType getType();

}

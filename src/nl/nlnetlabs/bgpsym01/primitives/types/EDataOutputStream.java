package nl.nlnetlabs.bgpsym01.primitives.types;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

import org.apache.log4j.Logger;

/**
 * @see EDataOutputStreamTest
 */
public class EDataOutputStream {

    private static Logger log = Logger.getLogger(EDataOutputStream.class);

    int size;

    int currentByte;
    int bitsWritten;

    OutputStream out;

    public EDataOutputStream(OutputStream out) {
        this.out = out;
    }

    public EDataOutputStream() {
    }

    public void init(OutputStream out) {
        this.out = out;
        currentByte = 0;
        bitsWritten = 0;
        size = 0;
    }

    private void storeCurrentByte() throws IOException {
        if (bitsWritten > 0) {
            out.write(currentByte);
            bitsWritten = 0;
            currentByte = 0;
            size++;
        }
    }

    public final void writeBits(int v, int size) throws IOException {
        if (v >= 1 << size) {
            String msg = "value is too big, v=" + v + ", size=" + size + " bits";
            log.error(msg);
            throw new BGPSymException(msg);
        }

        // TODO check...
        int BYTE_LENGTH = 8;
        if (size <= BYTE_LENGTH - bitsWritten) {
            // we can put the whole thing into currentByte
            currentByte |= v << BYTE_LENGTH - bitsWritten - size;
            bitsWritten += size;
            if (bitsWritten == BYTE_LENGTH) {
                storeCurrentByte();
            }
        } else {
            // the oldest still not written bit
            int pos = size;

            while (pos > 0) {
                if (pos >= BYTE_LENGTH - bitsWritten) {
                    // we can fill current byte up to the end
                    int toWrite = BYTE_LENGTH - bitsWritten;
                    int tmp = (1 << toWrite) - 1;

                    currentByte |= v >> pos - toWrite & tmp;

                    bitsWritten = BYTE_LENGTH;
                    pos -= toWrite;
                    storeCurrentByte();
                } else {
                    // we want fill the byte up to the end
                    int tmp = 0xFF;

                    currentByte |= v << BYTE_LENGTH - bitsWritten - pos & tmp;
                    bitsWritten += pos;
                    pos = 0;
                }
            }
        }
    }

    public final void writeInt(int v) throws IOException {
        writeInt(v, 4);
    }

    public final void writeInt(int v, int size) throws IOException {
        storeCurrentByte();
        if (size == 4) {
            out.write(v >>> 24 & 0xFF);
            out.write(v >>> 16 & 0xFF);
            out.write(v >>> 8 & 0xFF);
            out.write(v >>> 0 & 0xFF);
            size += 4;
        } else if (size == 3) {
            if (v > 0xFFFFFF) {
                throw new IllegalArgumentException("v > 0xFFFFFF: v=" + v);
            }
            out.write(v >>> 16 & 0xFF);
            out.write(v >>> 8 & 0xFF);
            out.write(v >>> 0 & 0xFF);
            size += 3;
        } else if (size == 2) {
            if (v > 0xFFFF) {
                throw new IllegalArgumentException("v > 0xFFFF: v=" + v);
            }
            out.write(v >>> 8 & 0xFF);
            out.write(v >>> 0 & 0xFF);
            size += 2;
        } else {
            throw new IllegalArgumentException("size=" + size);
        }
    }

    public final void writeList(List<? extends EExternalizable> list) throws IOException {
        int size = list == null ? 0 : list.size();
        writeInt(size);
        if (size > 0) {
            for (EExternalizable e : list) {
                e.writeExternal(this);
            }
        }
    }

    public final void writePrefixList(List<Prefix> list) throws IOException {
        writeInt(list.size());
        for (Prefix prefix : list) {
            writeBits(prefix.getNum(), SystemConstants.PREFIX_SIZE_BITS);
        }
    }

    public final void writeDouble(double d) throws IOException {
        writeLong(Double.doubleToLongBits(d));
    }

    public final void writeLong(long v) throws IOException {
        storeCurrentByte();
        out.write((byte) (v >>> 56));
        out.write((byte) (v >>> 48));
        out.write((byte) (v >>> 40));
        out.write((byte) (v >>> 32));
        out.write((byte) (v >>> 24));
        out.write((byte) (v >>> 16));
        out.write((byte) (v >>> 8));
        out.write((byte) (v >>> 0));
        size += 8;
    }

    public final void writeBoolean(boolean v) throws IOException {
        writeBits(v ? 1 : 0, 1);
    }

    public final void close() throws IOException {
        storeCurrentByte();
        out.flush();
        out.close();
    }

    public int getSize() {
        return size;
    }

}

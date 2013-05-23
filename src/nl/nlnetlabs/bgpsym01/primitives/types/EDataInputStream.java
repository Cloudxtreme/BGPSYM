package nl.nlnetlabs.bgpsym01.primitives.types;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

public class EDataInputStream {

    private static final int BYTE_LENGTH = 8;
    private InputStream in;
    private int bitsInMemory;
    private int currentByte;

    public EDataInputStream(InputStream in) {
        this.in = in;
    }

    public EDataInputStream() {
    }

    public EDataInputStream(byte[] byteArray) {
        this(new ByteArrayInputStream(byteArray));
    }

    public void init(InputStream inputStream) {
        bitsInMemory = 0;
        currentByte = 0;
        in = inputStream;
    }

    private void cleanBits() {
        bitsInMemory = 0;
    }

    private void reloadBits() throws IOException {
        if (bitsInMemory == 0) {
            currentByte = in.read();
            if (currentByte < 0) {
                throw new EOFException("currentByte=" + currentByte);
            }
            bitsInMemory = BYTE_LENGTH;
        }
    }

    public final int readInt() throws IOException {
        return readInt(4);
    }

    public List<Prefix> readPrefixList() throws IOException {
        int size = readInt();
        List<Prefix> list = new ArrayList<Prefix>(size);
        for (int i = 0; i < size; i++) {
            list.add(Prefix.getInstance(readBits(SystemConstants.PREFIX_SIZE_BITS)));
        }
        return list;
    }

    public int readBits(int size) throws IOException {
        int out = 0;
        while (size > 0) {
            reloadBits();
            if (size <= bitsInMemory) {
                // we take just the part
                out <<= size;
                int tmp = (1 << size) - 1 << bitsInMemory - size;
                out |= (currentByte & tmp) >> bitsInMemory - size;
                bitsInMemory -= size;
                size = 0;
            } else {
                // we take the rest
                int toTake = bitsInMemory;
                out <<= toTake;
                out |= currentByte & (1 << toTake) - 1;
                bitsInMemory -= toTake;
                size -= toTake;
            }
        }
        return out;
    }

    public final int readInt(int size) throws IOException {
        cleanBits();
        if (size == 4) {
            int ch1 = in.read();
            int ch2 = in.read();
            int ch3 = in.read();
            int ch4 = in.read();
            if ((ch1 | ch2 | ch3 | ch4) < 0) {
                throw new EOFException();
            }
            return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
        } else if (size == 3) {
            int ch2 = in.read();
            int ch3 = in.read();
            int ch4 = in.read();
            if ((ch2 | ch3 | ch4) < 0) {
                throw new EOFException();
            }
            return (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
        } else if (size == 2) {
            int ch3 = in.read();
            int ch4 = in.read();
            if ((ch3 | ch4) < 0) {
                throw new EOFException();
            }
            return (ch3 << 8) + (ch4 << 0);
        } else {
            throw new IllegalArgumentException("size=" + size);
        }
    }

    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public final long readLong() throws IOException {
        cleanBits();
        return ((long) in.read() << 56) + ((long) (in.read() & 255) << 48) + ((long) (in.read() & 255) << 40) + ((long) (in.read() & 255) << 32)
        + ((long) (in.read() & 255) << 24) + ((in.read() & 255) << 16) + ((in.read() & 255) << 8) + ((in.read() & 255) << 0);
    }

    public final boolean readBoolean() throws IOException {
        return readBits(1) == 1;
    }

    public final void close() throws IOException {
        in.close();
    }

}

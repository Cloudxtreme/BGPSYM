package nl.nlnetlabs.bgpsym01.primitives.types;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ByteFIFOImpl implements ByteFIFO {

    private static final int DEFAULT_EMPTY_SIZE = 64;
    int available;
    int start;
    int end;
    int minSize;
    final boolean cut;

    byte[] tab;

    public ByteFIFOImpl() {
        this(DEFAULT_EMPTY_SIZE, true);
    }

    public ByteFIFOImpl(int size, boolean cut) {
        tab = new byte[size];
        minSize = size;
        this.cut = cut;
    }

    /**
     * @param startPos
     *            starting position
     * @param needed
     *            how many bytes are in fact needed
     * @param size
     *            the size of the array
     * @return how many bytes can be read from starting position till the
     *         physical end of the array (before folding)
     */
    final int getAvailableTillEnd(int startPos, int needed, int size) {
        return Math.min(needed, size - startPos);
    }

    /**
     * @param pointer
     *            actual value of the pointer (points to some element in the
     *            array)
     * @param amount
     *            how much should be added to the pointer
     * @param size
     *            the size of the array
     * @return the new position of the pointer (takes folding into account)
     */
    final int getNewPointerPos(int pointer, int amount, int size) {
        return (pointer + amount) % size;
    }

    @Override
    public int available() {
        return available;
    }

    /**
     * @param available
     *            how many bytes are used in the array
     * @param toAdd
     *            how many are to be added
     * @return new size of the array (takes initial size into account)
     */
    final int getNewSize(int available, int toAdd) {
        return Math.max(available + toAdd << 1, minSize);
    }

    /**
     * Resize array to provide right capacity for the given elements count
     * 
     * @param toAdd
     *            how many elements are to be added
     */
    void resize(int toAdd) {
        if (!needsResize(available, tab.length, toAdd)) {
            return;
        }
        int newSize = getNewSize(available, toAdd);
        if (newSize != tab.length) {
            tab = resize(tab, start, end, available, newSize);
            start = 0;
            end = available;
        }
    }

    void assertSize(int amount, int available) throws EOFException {
        if (amount > available) {
            throw new EOFException("amount=" + amount + ", available=" + available);
        }
    }

    @Override
    public InputStream getInputStream() {

        return new InputStream() {
            int ourStart = start;
            int ourAvailable = available;
            boolean closed = false;

            @Override
            public int read() throws IOException {
                assertSize(1, ourAvailable);
                int out = tab[ourStart];
                ourAvailable--;
                ourStart = getNewPointerPos(ourStart, 1, tab.length);

                return out < 0 ? out + (1 << Byte.SIZE) : out;
            }

            @Override
            public int available() throws IOException {
                return ourAvailable;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                assertSize(len, ourAvailable);
                int amount = getAvailableTillEnd(ourStart, len, tab.length);
                System.arraycopy(tab, ourStart, b, 0, amount);
                // in case we are folding...
                if (len != amount) {
                    System.arraycopy(tab, 0, b, amount, len - amount);
                }

                // move the pointer right were it should be :)
                ourStart = getNewPointerPos(ourStart, len, tab.length);
                ourAvailable -= len;

                return len;
            }

            @Override
            public int read(byte[] b) throws IOException {
                return read(b, 0, b.length);
            }

            @Override
            public void close() throws IOException {
                // remove all what was read
                if (cut && !closed) {
                    removeBytes(available - ourAvailable);
                    closed = true;
                }
            }

        };
    }

    @Override
    public OutputStream getOutputStream() {
        return new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                resize(1);
                tab[end] = (byte) b;
                end = getNewPointerPos(end, 1, tab.length);
                available++;
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                resize(len);
                int amount = getAvailableTillEnd(end, len, tab.length);
                System.arraycopy(b, off, tab, end, amount);
                // in case we are folding
                if (len != amount) {
                    System.arraycopy(b, off + amount, tab, 0, len - amount);
                }

                // move the pointers
                available += len;
                end = getNewPointerPos(end, len, tab.length);
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }

        };
    }

    @Override
    public boolean isEmpty() {
        return available == 0;
    }

    @Override
    public int removeBytes(int n) {
        start = getNewPointerPos(start, n, tab.length);
        available -= n;
        resize(0);
        return n;
    }

    /**
     * 
     * 
     * @param tab
     *            src array
     * @param start
     *            starting pointer in <b>tab</b>
     * @param end
     *            end pointer in <b>tab</b>
     * @param available
     *            bytes in <b>tab</b>
     * @param newSize
     * @return new array of size <b>newSize<b> which contains all elements from
     *         <b>tab</b>
     */
    byte[] resize(byte[] tab, int start, int end, int available, int newSize) {
        byte[] outputTab = new byte[newSize];
        int amount = getAvailableTillEnd(start, available, tab.length);
        System.arraycopy(tab, start, outputTab, 0, amount);
        System.arraycopy(tab, 0, outputTab, amount, available - amount);
        return outputTab;
    }

    public boolean needsResize(int available, int tabSize, int toAdd) {
        final boolean bigger = available + toAdd > tabSize;
        final boolean smaller = available + toAdd << 2 < tabSize;
        return bigger || smaller;
    }

}

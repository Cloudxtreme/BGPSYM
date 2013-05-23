package nl.nlnetlabs.bgpsym01.primitives.types;

import java.util.NoSuchElementException;

public class FIFOQueueImpl<T> implements FIFOQueue<T> {

    private static final int DEFAULT_MIN_SIZE = 64;

    Object tab[];

    int startPos = 0;

    int endPos = 0;

    int available;

    int minSize = 12;

    public FIFOQueueImpl(int minSize) {
        tab = new Object[minSize];
        this.minSize = minSize;
    }

    public FIFOQueueImpl() {
        this(DEFAULT_MIN_SIZE);
    }

    final int getNewPointerPos(int pointer, int add, int size) {
        return (pointer + add) % size;
    }

    final int getNewSize(int available, int toAdd) {
        return Math.max(available + toAdd << 1, minSize);
    }

    void resize(int toAdd) {
        if (!needsResize(available, toAdd, tab.length)) {
            return;
        }
        int newSize = getNewSize(available, toAdd);
        if (newSize != tab.length) {
            tab = resize(tab, startPos, endPos, available, newSize);
            startPos = 0;
            endPos = available;
        }
    }

    @Override
    public int add(T e) {
        resize(1);
        tab[endPos] = e;
        endPos = getNewPointerPos(endPos, 1, tab.length);
        available++;
        return size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T peek() {
        return (T) tab[startPos];
    }

    @SuppressWarnings("unchecked")
    @Override
    public T remove() throws NoSuchElementException {
        T out = (T) tab[startPos];
        startPos = getNewPointerPos(startPos, 1, tab.length);
        available--;
        resize(0);
        return out;
    }

    @Override
    public int size() {
        return available;
    }

    public boolean needsResize(int used, int toAdd, int size) {
        final boolean bigger = used + toAdd > size;
        final boolean smaller = used + toAdd << 2 < size;
        return bigger || smaller;
    }

    final int getAvailableTillEnd(int startPos, int needed, int size) {
        return Math.min(needed, size - startPos);
    }

    public Object[] resize(Object[] tab, int start, int end, int available, int newSize) {
        Object[] out = new Object[newSize];
        int amount = getAvailableTillEnd(start, available, tab.length);
        System.arraycopy(tab, start, out, 0, amount);
        System.arraycopy(tab, 0, out, amount, available - amount);
        return out;
    }

}

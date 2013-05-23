package nl.nlnetlabs.bgpsym01.primitives.types;

import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

public class FIFOQueueImplOld<T> implements FIFOQueue<T> {

    private static int INITIAL_CAPACITY = 4096;
    private static Logger log = Logger.getLogger(FIFOQueueImplOld.class);

    private int capacity;
    private int minCapacity;

    int start = 0;
    int end = 0;
    volatile int size = 0;

    private Object tab[];

    public FIFOQueueImplOld(int capacity) {

        this.capacity = capacity;
        this.minCapacity = capacity;
        tab = new Object[capacity];
    }

    public FIFOQueueImplOld() {
        this(INITIAL_CAPACITY);
    }

    private void resize() {
        if (capacity == size) {
            if (log.isDebugEnabled()) {
                log.debug("doubling to " + capacity * 2);
            }
            Object[] nTab = new Object[capacity * 2];
            if (end > start) {
                System.arraycopy(tab, start, nTab, 0, size);
            } else if (size != 0) {
                System.arraycopy(tab, start, nTab, 0, capacity - start);
                System.arraycopy(tab, 0, nTab, capacity - start, end);
            }
            tab = nTab;
            capacity *= 2;
            start = 0;
            end = size;
            if (log.isDebugEnabled()) {
                log.debug("doubled to " + capacity);
            }
        } else if (size * 4 < capacity) {
            int nCapacity = Math.max(size * 2, minCapacity);
            if (nCapacity >= capacity) {
                return;
            }

            Object[] nTab = new Object[nCapacity];
            if (end > start) {
                /*if (log.isDebugEnabled()) {
                    log.debug("end > start, start=" + start + ", end=" + end);
                }*/
                System.arraycopy(tab, start, nTab, 0, size);
            } else if (size != 0) {
                System.arraycopy(tab, start, nTab, 0, capacity - start);
                System.arraycopy(tab, 0, nTab, capacity - start, end);
            }
            tab = nTab;
            capacity = nCapacity;
            start = 0;
            end = size;
            if (log.isDebugEnabled()) {
                log.debug("halved to " + capacity);
            }
        }
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.primitives.types.FIFOQueue#add(T)
     */
    public int add(T e) {
        resize();
        tab[end++] = e;
        end %= capacity;
        size++;
        return size;
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.primitives.types.FIFOQueue#peek()
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        return (T) tab[start];
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.primitives.types.FIFOQueue#remove()
     */
    @SuppressWarnings("unchecked")
    public T remove() throws NoSuchElementException {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        T ret = (T) tab[start];
        tab[start] = null;
        start++;
        start %= capacity;
        size--;
        resize();
        return ret;
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.primitives.types.FIFOQueue#size()
     */
    public int size() {
        return size;
    }

}

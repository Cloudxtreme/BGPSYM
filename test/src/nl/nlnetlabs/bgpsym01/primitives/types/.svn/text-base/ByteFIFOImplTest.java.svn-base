package nl.nlnetlabs.bgpsym01.primitives.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;

public class ByteFIFOImplTest extends AbstractTest {

    public void testGetAvailableTillEnd() {
        ByteFIFOImpl bf = getQueue();
        assertEquals(2, bf.getAvailableTillEnd(3, 2, 10));
        assertEquals(1, bf.getAvailableTillEnd(9, 2, 10));
        assertEquals(7, bf.getAvailableTillEnd(3, 20, 10));
    }

    public void testGetNewPointerPos() {
        ByteFIFOImpl bf = getQueue();
        assertEquals(5, bf.getNewPointerPos(2, 3, 10));
        assertEquals(0, bf.getNewPointerPos(2, 8, 10));
        assertEquals(3, bf.getNewPointerPos(2, 14, 13));
    }

    public void testAvailable() throws IOException {
        ByteFIFOImpl bf = getQueue();
        bf.available = 3;
        assertEquals(3, bf.available());
        bf.available = 3102;
        assertEquals(3102, bf.available());
    }

    public void testWrite1() throws IOException {
        ByteFIFOImpl bf = getQueue();
        bf.tab = new byte[20];
        OutputStream stream = bf.getOutputStream();


        byte[] b = new String("dupa23").getBytes();
        stream.write(b);
        assertTrue(Arrays.equals(Arrays.copyOfRange(bf.tab, 0, 6), b));
        stream.write(b);
        assertEquals(12, bf.end);
        assertTrue(Arrays.equals(Arrays.copyOfRange(bf.tab, 6, 12), b));
        assertFalse(Arrays.equals(Arrays.copyOfRange(bf.tab, 12, 18), b));

        stream.write(b);
        assertTrue(Arrays.equals(Arrays.copyOfRange(bf.tab, 12, 18), b));

        assertEquals(0, bf.start);
        assertEquals(18, bf.end);
        stream.write("12".getBytes());
        assertEquals(0, bf.end);
    }

    public void testWrite2() throws IOException {
        ByteFIFOImpl bf = getQueue();
        bf.tab = new byte[20];
        bf.end = 18;
        bf.start = 15;
        OutputStream stream = bf.getOutputStream();
        byte[] b = new String("dupa23").getBytes();
        stream.write(b);
        assertTrue(Arrays.equals(Arrays.copyOfRange(bf.tab, 18, 20), "du".getBytes()));
        assertTrue(Arrays.equals(Arrays.copyOfRange(bf.tab, 0, 4), "pa23".getBytes()));
        assertEquals(15, bf.start);
        assertEquals(4, bf.end);
    }

    public void testWriteNegative() throws IOException {
        ByteFIFOImpl bf = getQueue(20);
        bf.getOutputStream().write(223);
        assertEquals(223, bf.getInputStream().read());
    }

    public void testRead2() throws IOException {
        ByteFIFOImpl bf = getQueue(20);
        bf.getOutputStream().write("12345678901234567890".getBytes());
        bf.removeBytes(5);
        bf.getOutputStream().write("dupa".getBytes());
        assertEquals(bf.end, 4);

        byte[] ar = new byte[18];
        InputStream inputStream = bf.getInputStream();
        inputStream.read(ar);
        assertEquals("678901234567890dup", new String(ar));
        assertEquals('a', inputStream.read());
    }

    public void testRead1() throws IOException {
        ByteFIFOImpl bf = getQueue();

        bf.tab = new byte[20];
        System.arraycopy("dupa93".getBytes(), 0, bf.tab, 0, 6);
        bf.available = 6;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                InputStream stream = bf.getInputStream();
                byte ar[] = new byte[6];
                assertEquals(6, stream.read(ar));
                assertEquals("dupa93", new String(ar));
            }
            for (int j = 0; j < 3; j++) {
                byte ar1[] = new byte[3];
                byte ar2[] = new byte[2];
                InputStream stream = bf.getInputStream();
                assertEquals(3, stream.read(ar1));
                assertEquals(3, stream.available());
                assertEquals("dup", new String(ar1));
                assertEquals(2, stream.read(ar2));
                assertEquals(1, stream.available());
                assertEquals("a9", new String(ar2));


                assertEquals(6, bf.available());
            }
        }

        bf.tab = new byte[20];
        bf.start = 17;
        bf.end = 3;
        System.arraycopy("dup".getBytes(), 0, bf.tab, 17, 3);
        System.arraycopy("a93".getBytes(), 0, bf.tab, 0, 3);

        InputStream stream = bf.getInputStream();
        byte[] ar = new byte[5];
        stream.read(ar);
        assertEquals("dupa9", new String(ar));

        assertEquals(1, stream.available());
        assertEquals(6, bf.available());
    }

    public void testCut() {
        ByteFIFOImpl bf = getQueue();

        bf.tab = new byte[20];
        bf.start = 1;
        bf.end = 18;
        bf.available = 17;
        bf.removeBytes(3);
        assertEquals(bf.start, 4);
        assertEquals(bf.end, 18);
        assertEquals(bf.available, 14);

        bf.start = 17;
        bf.available = 19;
        bf.removeBytes(5);
        assertEquals(14, bf.available);
        assertEquals(2, bf.start);
        assertEquals(18, bf.end);
    }

    public void testIsEmpty() {
        ByteFIFOImpl bf = getQueue();
        bf.available = 1;
        assertFalse(bf.isEmpty());
        bf.available = 23;
        assertFalse(bf.isEmpty());
        bf.available = 0;
        assertTrue(bf.isEmpty());

    }

    public void testResize() {
        ByteFIFOImpl bf = getQueue();
        int _count = 20;
        bf.tab = new byte[_count];
        for (int i = 0; i < _count; i++) {
            bf.tab[i] = (byte) (i + 3);
        }
        byte[] xTab = bf.resize(bf.tab, 0, 20, 20, 40);
        assertTrue(Arrays.equals(Arrays.copyOfRange(bf.tab, 0, 20), Arrays.copyOfRange(xTab, 0, 20)));

        xTab = bf.resize(bf.tab, 15, 3, 8, 40);
        assertFalse(Arrays.equals(Arrays.copyOfRange(bf.tab, 0, 20), Arrays.copyOfRange(xTab, 0, 20)));
        assertTrue(Arrays.equals(Arrays.copyOfRange(bf.tab, 15, 20), Arrays.copyOfRange(xTab, 0, 5)));
        assertTrue(Arrays.equals(Arrays.copyOfRange(bf.tab, 0, 3), Arrays.copyOfRange(xTab, 5, 8)));
    }

    public void testNoResizeIfFull() throws IOException {
        ByteFIFOImpl bf = getQueue();
        bf.tab = new byte[20];
        bf.getOutputStream().write("dupa_12345".getBytes());
        bf.getOutputStream().write("dupa_12345".getBytes());
        // the tab size should not have been doubled
        assertEquals(bf.tab.length, 20);
        bf.getOutputStream().write("a".getBytes());
        // nu wel
        assertEquals(bf.tab.length, 42);

    }

    public void testGetNewSize() {
        ByteFIFOImpl bf = getQueue();
        assertEquals(20, bf.getNewSize(9, 1));
        assertEquals(20, bf.getNewSize(4, 6));
        assertEquals(200, bf.getNewSize(44, 56));

        assertEquals(10, bf.getNewSize(5, 0));
        assertEquals(10, bf.getNewSize(5, 0));
        assertEquals(6, bf.getNewSize(3, 0));
    }

    public void testNeedResize() {
        ByteFIFOImpl bf = getQueue();
        assertTrue(bf.needsResize(20, 30, 11)); // get bigger
        assertFalse(bf.needsResize(20, 30, 9));
        assertTrue(bf.needsResize(1, 30, 3)); // get smaller
    }

    public void testBig() throws IOException {
        ByteFIFOImpl bf = getQueue();
        int count = 1232;
        for (int i = 0 ;i < count ; i++) {
            bf.getOutputStream().write(("dupa_" + (10000 + count)).getBytes());
        }
        InputStream stream = bf.getInputStream();
        for (int i = 0 ;i < count ; i++) {
            byte[] ar = new byte[10];
            stream.read(ar);
            assertEquals("dupa_" + (10000 + count), new String(ar));
            assertEquals(bf.available, 10 * count);
            assertEquals(stream.available(), 10 * (count - (i + 1)));
        }

        for (int i = 0 ;i < count ; i++) {
            byte[] ar = new byte[10];
            stream = bf.getInputStream();
            stream.read(ar);
            assertEquals(stream.available(), 10 * (count - (i + 1)));
            assertEquals("dupa_" + (10000 + count), new String(ar));

            assertEquals(bf.available, 10 * (count - i));
            bf.removeBytes(10);
            assertEquals(bf.available, 10 * (count - (i + 1)));
        }
    }

    public void testWriteReadOneByte1() throws IOException {
        ByteFIFOImpl bf = getQueue(20);
        OutputStream outputStream = bf.getOutputStream();
        outputStream.write(0);
        outputStream.write(1);
        assertEquals(2, bf.end);
        assertEquals(2, bf.available);
        assertEquals(0, bf.start);
        for (int i = 2; i < 12000; i++) {
            outputStream.write(i % Byte.SIZE);
        }
        assertEquals(bf.tab[0], 0);
        assertEquals(bf.tab[1], 1);
        InputStream inputStream = bf.getInputStream();
        for (int i = 0; i < 12000; i++) {
            assertEquals("i=" + i, i % Byte.SIZE, inputStream.read());
        }
        assertFalse(bf.tab.length == 20);
        for (int i = 0; i < 12000; i++) {
            assertEquals("i=" + i, i % Byte.SIZE, bf.getInputStream().read());
            bf.removeBytes(1);
        }
        assertEquals(bf.tab.length, 20);
    }


    public void testWriteReadOneByte2() throws IOException {
        ByteFIFOImpl bf = getQueue(20);
        OutputStream outputStream = bf.getOutputStream();
        for (int i = 0; i < 10000; i++) {
            outputStream.write('d');
            outputStream.write('u');
            outputStream.write('p');
            outputStream.write('a');
            outputStream.write('2');
            outputStream.write('3');
        }
        InputStream inputStream = bf.getInputStream();
        for (int i = 0; i < 10000 - 1; i++) {
            byte[] b = new byte[6];
            inputStream.read(b);
            assertEquals(new String(b), "dupa23");
        }
        assertEquals(6, inputStream.available());
    }


    public void testDoesNotCutInitialSize() {
        ByteFIFOImpl bf = getQueue(20);
        assertEquals(200, bf.getNewSize(100, 0));
        assertEquals(22, bf.getNewSize(10, 1));
        assertEquals(20, bf.getNewSize(1, 1));
    }

    private ByteFIFOImpl getQueue() {
        return getQueue(2);
    }

    private ByteFIFOImpl getQueue(int size) {
        return new ByteFIFOImpl(size, true);
    }

}

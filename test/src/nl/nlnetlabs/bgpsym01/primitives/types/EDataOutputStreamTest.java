package nl.nlnetlabs.bgpsym01.primitives.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;

import org.apache.log4j.Logger;

public class EDataOutputStreamTest extends AbstractTest {

    private static Logger log = Logger.getLogger(EDataOutputStreamTest.class);

    /**
     * tests whether generic queue writing works as it should
     * 
     * @throws IOException
     */
    public void testListWrite() throws IOException {
        generateASes(1000);
        List<ASIdentifier> list = createASList(1, 2, 3, 4, 5, 6, 7, 102, 452, 12);
        List<ASIdentifier> list2 = createASList(1, 2, 3, 102, 452, 12, 943);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EDataOutputStream eos = new EDataOutputStream(baos);

        eos.writeList(list);
        eos.writeList(list2);
        eos.writeList(null);

        eos.close();

        byte[] ar = baos.toByteArray();
        EDataInputStream eis = new EDataInputStream(new ByteArrayInputStream(ar));
        assertEquals(list, readASList(eis));
        assertEquals(list2, readASList(eis));
        assertEquals(new ArrayList<ASIdentifier>(), readASList(eis));

        // there shouldn't be even one bit left!
        try {
            eis.readBits(1);
            fail();
        } catch (IOException e) {
            // it cool
        }
    }

    /**
     * Tests whether EDataOuputStream can be properly reused (reinitialized)
     * 
     * @throws IOException
     */
    public void testReuse() throws IOException {
        EDataOutputStream eos = new EDataOutputStream();
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        eos.init(baos1);
        for (int i = 0; i < 1000; i++) {
            eos.writeBits(i, 21);
        }
        eos.close();

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        eos.init(baos2);
        for (int i = 190; i < 3000; i++) {
            eos.writeBits(i, 21);
        }
        eos.close();

        // check whether both streams have been filled correctly

        EDataInputStream eis = new EDataInputStream();
        eis.init(new ByteArrayInputStream(baos1.toByteArray()));
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, eis.readBits(21));
        }

        eis.init(new ByteArrayInputStream(baos2.toByteArray()));
        for (int i = 190; i < 3000; i++) {
            assertEquals(i, eis.readBits(21));
        }
    }

    private List<ASIdentifier> readASList(EDataInputStream eis) throws IOException {
        int size = eis.readInt();
        ArrayList<ASIdentifier> list = new ArrayList<ASIdentifier>(size);
        for (int i = 0; i < size; i++) {
            list.add(ASIdentifier.staticReadExternal(eis));
        }
        return list;
    }

    public void testWrite() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EDataOutputStream eos = new EDataOutputStream(baos);

        int b = 10;

        eos.writeBits(0, 23);
        eos.writeBits(1, 23);
        eos.writeBits(2, 23);
        eos.writeBoolean(true);
        eos.writeBoolean(false);
        eos.writeBoolean(true);
        eos.writeBoolean(true);
        eos.writeBoolean(false);

        for (int i = 0; i < 1000; i++) {
            eos.writeBits(i, b);
        }
        eos.writeBoolean(false);

        eos.close();

        byte[] ar = baos.toByteArray();
        if (log.isDebugEnabled()) {
            log.debug(Arrays.toString(ar));
        }

        EDataInputStream eis = new EDataInputStream(new ByteArrayInputStream(ar));

        assertEquals(0, eis.readBits(23));
        assertEquals(1, eis.readBits(23));
        assertEquals(2, eis.readBits(23));
        assertTrue(eis.readBoolean());
        assertFalse(eis.readBoolean());
        assertTrue(eis.readBoolean());
        assertTrue(eis.readBoolean());
        assertFalse(eis.readBoolean());

        for (int i = 0; i < 1000; i++) {
            assertEquals(i, eis.readBits(b));
        }
        assertFalse(eis.readBoolean());

    }

}

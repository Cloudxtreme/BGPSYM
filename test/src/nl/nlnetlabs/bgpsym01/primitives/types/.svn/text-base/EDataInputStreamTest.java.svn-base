package nl.nlnetlabs.bgpsym01.primitives.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;

public class EDataInputStreamTest extends AbstractTest {

    public void testSpecialCase() throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EDataOutputStream eos = new EDataOutputStream(baos);
        for (int i = 0; i < 16; i++) {
            eos.writeBits(1, 1);
        }

        EDataInputStream eis = new EDataInputStream(baos.toByteArray());
        for (int i = 0; i < 16; i++) {
            assertEquals(eis.readBits(1), 1);
        }

    }

}

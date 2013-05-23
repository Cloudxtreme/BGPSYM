package nl.nlnetlabs.bgpsym01.coordinator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.InputHandler;
import nl.nlnetlabs.bgpsym01.primitives.types.StaticThread;

import org.apache.log4j.Logger;

public class InputHandlerTest extends AbstractTest {

    private static Logger log = Logger.getLogger(InputHandlerTest.class);

    int size = 2000;
    int counter = 0;

    @Override
    protected void setUp() throws Exception {
        counter = 0;
    }

    public void test1() {
        InputHandlerThread iht = new InputHandlerThread();
        iht.start();

        InputStream is = new ByteArrayInputStream(new byte[0]);
        ArrayList<Integer> list = new ArrayList<Integer>();
        final ArrayList<Integer> list2 = new ArrayList<Integer>();

        for (int i = 0; i < size; i++) {
            list.add(i);
            final int value = i;
            iht.add(is, new InputHandler() {

                public void handleInput(InputStream inputStream) throws IOException {
                    size++;
                    list2.add(value);
                }

            });
        }
        log.info("waiting");
        int counter = 0;
        // we wait max 1500 second
        while (iht.size() != 0 && counter < 150) {
            StaticThread.sleep(10);
            counter++;
        }
        assertTrue("waited too long...", counter < 150);
        assertEquals(list, list2);
    }

}

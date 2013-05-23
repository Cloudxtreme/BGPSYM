package nl.nlnetlabs.bgpsym01.coordinator.events;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventBackendXStreamImpl;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

public class EventBackendXStreamTest extends AbstractTest {

    private static Logger log = Logger.getLogger(EventBackendXStreamTest.class);

    public void test1() throws IOException, ClassNotFoundException {

        XStream xStream = XStreamFactory.getXStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = xStream.createObjectOutputStream(new OutputStreamWriter(baos));
        int size = 50;
        for (int i = 0; i < size; i++) {
            oos.writeObject(getEvent(i));
        }
        oos.close();

        byte[] byteArray = baos.toByteArray();
        log.info("array(l=" + byteArray.length + ") : " + new String(byteArray));

        ObjectInputStream ois = xStream.createObjectInputStream(getReader(byteArray));
        // just test xStream
        for (int i = 0; i < size; i++) {
            assertEquals(getEvent(i), ois.readObject());
        }

        // and now test the backend
        EventBackendXStreamImpl backend = new EventBackendXStreamImpl(getReader(byteArray));
        for (int i = 0; i < size; i++) {
            assertEquals(getEvent(i), backend.getNext());
        }
        assertNull(backend.getNext());
        assertNull(backend.getNext());

    }

    private InputStreamReader getReader(byte[] byteArray) {
        return new InputStreamReader(new ByteArrayInputStream(byteArray));
    }

    private Event getEvent(final int num) {
        class EventTmp extends EventAdaptor {

            private int myNum = num;

            @Override
            public boolean equals(Object obj) {
                EventTmp tmp = (EventTmp) obj;
                return myNum == tmp.myNum;
            }

        }
        return new EventTmp();
    }

}

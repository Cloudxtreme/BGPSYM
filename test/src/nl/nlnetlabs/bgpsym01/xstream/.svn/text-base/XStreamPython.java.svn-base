package nl.nlnetlabs.bgpsym01.xstream;

import java.io.FileReader;
import java.io.Reader;

import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventBackendXStreamImpl;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;

import org.junit.Ignore;

@Ignore
public class XStreamPython extends AbstractTest {

    public static void main(String[] args) throws Exception {
        new XStreamPython().xrun();
    }

    private void xrun() throws Exception {
        super.setUp();
        generateASes(20000);

        String fileName = "/home/wojciech/das3/runEnv/small_64/events_validate2.xml";

        Reader reader = new FileReader(fileName);
        EventBackendXStreamImpl backend = new EventBackendXStreamImpl(reader);

        while (backend.getNext() != null) {

        }
    }

}

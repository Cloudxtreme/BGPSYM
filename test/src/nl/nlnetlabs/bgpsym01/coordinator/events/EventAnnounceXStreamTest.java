package nl.nlnetlabs.bgpsym01.coordinator.events;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventScheduleImpl;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

public class EventAnnounceXStreamTest extends AbstractTest {

    private static Logger log = Logger.getLogger(EventAnnounceXStreamTest.class);

    private int size = 3;

    @Override
    protected void setUp() throws Exception {
        Prefix.init(10000);
        generateASes(10000);
    }

    public void testSerialize() throws IOException, ClassNotFoundException {
        XStream xStream = XStreamFactory.getXStream();
        // xStream.registerConverter(new ASIdentifierConverter());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = xStream.createObjectOutputStream(new OutputStreamWriter(baos));
        generateAndWriteEvents(oos);
        oos.close();

        log.info("unmarshalling..., " + new String(baos.toByteArray()));
        ObjectInputStream ois = xStream.createObjectInputStream(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray())));
        for (int i = 0; i < size; i++) {
            assertEquals("error for i=" + i, getEvent(i, true), ois.readObject());
        }
    }

    private void generateAndWriteEvents(ObjectOutputStream oos) throws IOException {
        for (int i = 0; i < size; i++) {
            EventAnnounce event = getEvent(i, true);
            oos.writeObject(event);
        }
        for (int i = 0; i < size; i++) {
            EventAnnounce event = getEvent(i, false);
            oos.writeObject(event);
        }
    }

    private EventAnnounce getEvent(int i, boolean prefixes) {
        EventAnnounce event = new EventAnnounce();
        event.setAsId(getAS(i));
        if (prefixes) {
            List<Prefix> prefixList = getPrefixList(i, size * 2 + i);
            event.setPrefixList(prefixList);
        } else {
            List<Prefix> withdrawals = getPrefixList(size * 3 + i, size * 3 + i * 3 + 1);
            event.setWithdrawals(withdrawals);
        }

        EventSchedule eventSchedule = new EventScheduleImpl(30 * i);
        event.setEventSchedule(eventSchedule);
        return event;
    }

}

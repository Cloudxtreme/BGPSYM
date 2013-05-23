package nl.nlnetlabs.bgpsym01.xstream;

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import nl.nlnetlabs.bgpsym01.coordinator.events.EventAnnounce;
import nl.nlnetlabs.bgpsym01.coordinator.events.EventSleep;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventScheduleImpl;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;

import org.junit.Ignore;

import com.thoughtworks.xstream.XStream;

// this is not a test!
@Ignore
public class XStreamGeneratorEvents extends AbstractTest {

    private static final int WAITING_TIME = 30;

    private final String fileName = "/dev/shm/events.xml";

    private final int prefixesSize = 100;

    private final int withdrawalSize = 30;

    public static void main(String[] args) throws IOException {
        new XStreamGeneratorEvents().go();
    }

    private void go() throws IOException {
        Prefix.init(1000);
        generateASes(1000);
        XStream xStream = XStreamFactory.getXStream();
        ObjectOutputStream oos = xStream.createObjectOutputStream(new FileWriter(fileName));
        generateAndWriteEvents(oos);
        generateAndWriteWithdrawals(oos);
        EventSleep event = new EventSleep();
        event.setEventSchedule(new EventScheduleImpl(10000));
        oos.writeObject(event);
        oos.close();
    }

    private void generateAndWriteWithdrawals(ObjectOutputStream oos) throws IOException {
        for (int i = 0; i < withdrawalSize; i++) {
            EventAnnounce event = new EventAnnounce();
            event.setAsId(getAS(i));
            List<Prefix> withdrawals = getPrefixList(i, prefixesSize * 2 + i);
            event.setWithdrawals(withdrawals);
            event.setEventSchedule(new EventScheduleImpl(prefixesSize * WAITING_TIME + WAITING_TIME + WAITING_TIME * 2 * i));
            oos.writeObject(event);
        }
    }

    private void generateAndWriteEvents(ObjectOutputStream oos) throws IOException {
        for (int i = 0; i < prefixesSize; i++) {
            EventAnnounce event = new EventAnnounce();
            event.setAsId(getAS(i));
            List<Prefix> prefixList = getPrefixList(i, prefixesSize * 2 + i);
            event.setPrefixList(prefixList);
            event.setEventSchedule(new EventScheduleImpl(WAITING_TIME * i));
            oos.writeObject(event);
        }
    }

}

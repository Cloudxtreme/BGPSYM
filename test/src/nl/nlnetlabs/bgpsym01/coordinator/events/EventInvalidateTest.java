package nl.nlnetlabs.bgpsym01.coordinator.events;

import java.util.List;

import nl.nlnetlabs.bgpsym01.command.InvalidateCommand;
import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;

import com.thoughtworks.xstream.XStream;

public class EventInvalidateTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * if neighbor as id is left null all neighbors get invalidated
     * {@link EventInvalidate}
     */
    public void testEmptyNeighborId() {
        // first the event - it should be doable :)
        EventInvalidate event = getEvent();
        event.neighborAsId = null;
        event.prefixList = getPrefixList(1, 2, 3, 4);
        event.validate = false;

        XStream stream = XStreamFactory.getXStream();
        EventInvalidate event2 = (EventInvalidate) stream.fromXML(stream.toXML(event));
        assertNull(event2.neighborAsId);

        // now test the command generation
        InvalidateCommand command = event.generateCommand();
        assertNull(command.getNeighborsIdentifier());

    }

    public void testCommandGenerate() {
        ASIdentifier as = getAS(0);
        boolean validate = false;
        ASIdentifier nAs = getAS(1);
        List<Prefix> prefixList = getPrefixList(1, 2, 3, 4);

        EventInvalidate event = getEvent();
        event.asId = as;
        event.neighborAsId = nAs;
        event.prefixList = prefixList;
        event.validate = validate;
        InvalidateCommand command = new InvalidateCommand();
        command.setNeighborsIdentifier(nAs);
        command.setAsIdentifier(as);
        command.setPrefixes(prefixList);
        command.setValidate(validate);
        assertEquals(command, event.generateCommand());
    }

    public void testXML() {
        EventInvalidate event = getEvent();
        event.asId = getAS(0);
        event.neighborAsId = getAS(1);
        event.prefixList = getPrefixList(1, 2, 3, 4);
        event.validate = false;

        XStream stream = XStreamFactory.getXStream();
        System.out.println(stream.toXML(event));
    }

    private EventInvalidate getEvent() {
        return new EventInvalidate();
    }

}

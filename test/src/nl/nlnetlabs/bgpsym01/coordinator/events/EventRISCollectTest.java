package nl.nlnetlabs.bgpsym01.coordinator.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;

public class EventRISCollectTest extends AbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testConvertListToMap() {
        EventRISCollect event = new EventRISCollect();

        List<PrefixData> list = new ArrayList<PrefixData>();

        for (int i = 0; i < 29; i++) {
            // list.add(new Pair<Prefix, String>(getPrefix(i * 17 + 3),
            // "translate_" + i));
            list.add(new PrefixData(getPrefix(i * 17 + 3), "translate_" + i, 19));

        }

        Map<String, PrefixData> map = event.convertListToMap(list);

        assertEquals(list.size(), map.size());

        for (int i = 0; i < 29; i++) {
            assertEquals(map.get("msg_" + (i * 17 + 3)).name, "translate_" + i);
        }
    }

    public void testTextRepresentation() {
        EventRISCollect event = new EventRISCollect();
        List<PrefixData> prefixes = new ArrayList<PrefixData>();

        for (int i = 0; i < 2; i++) {
            prefixes.add(new PrefixData(getPrefix(i + 3), "trans_/24" + i, 19));
        }
        event.setPrefixes(prefixes);
        String xml = XStreamFactory.getXStream().toXML(event);
        System.out.println(xml);
        assertEquals(XStreamFactory.getXStream().fromXML(xml), event);
    }

}

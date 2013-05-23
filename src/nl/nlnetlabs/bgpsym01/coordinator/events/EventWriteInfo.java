package nl.nlnetlabs.bgpsym01.coordinator.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.command.ResultWriterRouteView;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("info")
public class EventWriteInfo extends Event {

    private List<String> ases;

    @XStreamAlias("schedule")
    private EventSchedule eventSchedule;

    @Override
    public EventSchedule getEventSchedule() {
        return eventSchedule;
    }

    @Override
    public void process() {
        try {
            new ResultWriterRouteView().writeInfoFile(ases);
        } catch (IOException e) {
            throw new BGPSymException(e);
        }
    }

    public static void main(String[] args) {
        EventWriteInfo ew = new EventWriteInfo();
        ArrayList<String> list = new ArrayList<String>();
        list.add("x1");
        list.add("x2");
        list.add("x3");

        ew.ases = list;
        System.out.println(XStreamFactory.getXStream().toXML(ew));
    }

    @Override
    public String toString() {
        return "writeInfo";
    }

}

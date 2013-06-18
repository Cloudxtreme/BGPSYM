package nl.nlnetlabs.bgpsym01.coordinator.events;

import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.coordinator.helpers.DisconnectHelper;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.converters.ASIdentifierConverter;
import nl.nlnetlabs.bgpsym01.primitives.converters.ASIdentifierListConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import org.apache.log4j.Logger;

@XStreamAlias("disconnect")
public class EventDisconnect extends Event {

	@XStreamConverter(ASIdentifierConverter.class)
	private ASIdentifier as;

	@XStreamConverter(ASIdentifierListConverter.class)
	private ArrayList<ASIdentifier> neighbors;

	private static Logger log = Logger.getLogger(EventDisconnect.class);

    @XStreamAlias("schedule")
    private EventSchedule eventSchedule;

    @Override
    public EventSchedule getEventSchedule() {
        return eventSchedule;
    }

    @Override
    public void process() {
		DisconnectHelper disconnectHelper = getDisconnectHelper();
		if (neighbors == null || neighbors.size() == 0) {
			disconnectHelper.disconnect(as);
		}
		else {
			disconnectHelper.disconnect(as, neighbors);
		}
    }

    @Override
    public String toString() {
        return "DISCONNECT;" + eventSchedule + "; "+as+" with neighbors"+ neighbors+".";
    }
}

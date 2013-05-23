package nl.nlnetlabs.bgpsym01.coordinator.events;

import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.coordinator.helpers.ConnectHelper;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.converters.ASIdentifierConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import org.apache.log4j.Logger;

@XStreamAlias("connect")
public class EventConnect extends Event {

	private ASIdentifier as;

	private ArrayList<ASIdentifier> neighbors;

	@XStreamOmitField
	private static Logger log = Logger.getLogger(EventDisconnect.class);

    @XStreamAlias("schedule")
    private EventSchedule eventSchedule;

    @Override
    public EventSchedule getEventSchedule() {
        return eventSchedule;
    }

    @Override
    public void process() {
		ConnectHelper connectHelper = getConnectHelper();
		connectHelper.connect(as, neighbors);
    }

    @Override
    public String toString() {
        return "CONNECT;" + eventSchedule + "; "+as+" with neighbors"+ neighbors+".";
    }
}

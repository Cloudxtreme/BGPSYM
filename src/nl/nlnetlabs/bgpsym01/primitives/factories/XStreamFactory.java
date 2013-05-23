package nl.nlnetlabs.bgpsym01.primitives.factories;

import nl.nlnetlabs.bgpsym01.command.RouteViewDataResponse;
import nl.nlnetlabs.bgpsym01.coordinator.events.Event;
import nl.nlnetlabs.bgpsym01.coordinator.events.EventAnnounce;
import nl.nlnetlabs.bgpsym01.coordinator.events.EventInvalidate;
import nl.nlnetlabs.bgpsym01.coordinator.events.EventLastSeen;
import nl.nlnetlabs.bgpsym01.coordinator.events.EventNoise;
import nl.nlnetlabs.bgpsym01.coordinator.events.EventRISCollect;
import nl.nlnetlabs.bgpsym01.coordinator.events.EventResetData;
import nl.nlnetlabs.bgpsym01.coordinator.events.EventSleep;
import nl.nlnetlabs.bgpsym01.coordinator.events.EventWriteInfo;
import nl.nlnetlabs.bgpsym01.coordinator.events.EventLog;
import nl.nlnetlabs.bgpsym01.coordinator.events.EventDisconnect;
import nl.nlnetlabs.bgpsym01.coordinator.events.EventConnect;
import nl.nlnetlabs.bgpsym01.coordinator.events.PrefixData;
import nl.nlnetlabs.bgpsym01.coordinator.events.RTTEvent;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventScheduleImpl;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.route.PeerRelation;
import nl.nlnetlabs.bgpsym01.xstream.XComputeNodes;
import nl.nlnetlabs.bgpsym01.xstream.XNeighbor;
import nl.nlnetlabs.bgpsym01.xstream.XNode;
import nl.nlnetlabs.bgpsym01.xstream.XPrefix;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;
import nl.nlnetlabs.bgpsym01.xstream.XRegistry;
import nl.nlnetlabs.bgpsym01.xstream.XSystem;

import com.thoughtworks.xstream.XStream;

public class XStreamFactory {

    public static XStream getXStream() {
        XStream xStream = new XStream();
		xStream.processAnnotations(XNode.class);
		xStream.processAnnotations(XNeighbor.class);
		xStream.processAnnotations(XRegistry.class);
		xStream.processAnnotations(ASIdentifier.class);
		xStream.processAnnotations(XSystem.class);
		xStream.processAnnotations(XComputeNodes.class);
		xStream.processAnnotations(XProperties.class);
		xStream.processAnnotations(XPrefix.class);
		xStream.processAnnotations(PeerRelation.class);
		xStream.processAnnotations(Event.class);
		xStream.processAnnotations(EventAnnounce.class);
		xStream.processAnnotations(EventScheduleImpl.class);
		xStream.processAnnotations(EventSleep.class);
		xStream.processAnnotations(EventLastSeen.class);
		xStream.processAnnotations(RTTEvent.class);
		xStream.processAnnotations(EventNoise.class);
		xStream.processAnnotations(EventWriteInfo.class);
		xStream.processAnnotations(EventResetData.class);
		xStream.processAnnotations(EventRISCollect.class);
		xStream.processAnnotations(EventInvalidate.class);
		xStream.processAnnotations(EventLog.class);
		xStream.processAnnotations(EventDisconnect.class);
		xStream.processAnnotations(EventConnect.class);
		xStream.processAnnotations(PrefixData.class);
		xStream.processAnnotations(RouteViewDataResponse.class);

        return xStream;
    }

}

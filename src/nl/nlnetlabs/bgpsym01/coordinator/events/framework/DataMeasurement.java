package nl.nlnetlabs.bgpsym01.coordinator.events.framework;

import nl.nlnetlabs.bgpsym01.coordinator.events.Event;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

public interface DataMeasurement {

    public void eventSent(Event event);

    public long getLastSeen(Prefix prefix);

}

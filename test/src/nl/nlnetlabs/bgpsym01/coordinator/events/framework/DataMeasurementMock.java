package nl.nlnetlabs.bgpsym01.coordinator.events.framework;

import nl.nlnetlabs.bgpsym01.coordinator.events.Event;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

public class DataMeasurementMock implements DataMeasurement {
    int sent = 0;

    public void eventSent(Event event) {
        sent++;
    }

    public long getLastSeen(Prefix prefix) {
        return 0;
    }
}

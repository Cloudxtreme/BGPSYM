package nl.nlnetlabs.bgpsym01.coordinator.events.framework;

import nl.nlnetlabs.bgpsym01.coordinator.events.Event;

public interface EventBackend {

    public Event getNext();

}

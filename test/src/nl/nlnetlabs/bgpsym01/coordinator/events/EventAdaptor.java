package nl.nlnetlabs.bgpsym01.coordinator.events;

import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;

public class EventAdaptor extends Event {

    @Override
    public EventSchedule getEventSchedule() {
        return null;
    }

    @Override
    public void process() {
    }

}

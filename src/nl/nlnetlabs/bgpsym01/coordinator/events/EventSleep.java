package nl.nlnetlabs.bgpsym01.coordinator.events;

import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This event doesn't do anything. It's just a placeholder for timing purposes
 * :)
 */
@XStreamAlias("sleep")
public class EventSleep extends Event {

    @XStreamAlias("schedule")
    private EventSchedule eventSchedule;

    @Override
    public void process() {
    }

    @Override
    public EventSchedule getEventSchedule() {
        return eventSchedule;
    }

    public void setEventSchedule(EventSchedule eventSchedule) {
        this.eventSchedule = eventSchedule;
    }

    @Override
    public String toString() {
        return "SLEEP;" + eventSchedule.toString();
    }

}

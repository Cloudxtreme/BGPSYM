package nl.nlnetlabs.bgpsym01.coordinator.events.framework;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("explicit")
public class EventScheduleImpl extends EventSchedule {

    @XStreamAsAttribute
    private long launchTime;

    public EventScheduleImpl(long launchTime) {
        this.launchTime = launchTime;
    }

    @Override
    public long getLaunchTime() throws IllegalStateException {
        return launchTime;
    }

    @Override
    public boolean timeIsKnown() {
        return true;
    }

    @Override
    public String toString() {
        return "E-" + launchTime;
    }

}

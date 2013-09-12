package nl.nlnetlabs.bgpsym01.coordinator.events;

import nl.nlnetlabs.bgpsym01.command.LogRequestCommand;
import nl.nlnetlabs.bgpsym01.command.RIBRequestCommand;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.primitives.types.ShutdownadbleThread;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("rib")
public class EventRIB extends Event {

	private static Logger log = Logger.getLogger(EventLog.class);

    @XStreamAlias("schedule")
    private EventSchedule eventSchedule;

    @Override
    public EventSchedule getEventSchedule() {
        return eventSchedule;
    }

    @Override
    public void process() {
        // we want to kill NoiseThread - we don't need him any more
        Object attachment = getCommandSenderHelper().attachment();
        if (attachment instanceof ShutdownadbleThread) {
            ((ShutdownadbleThread) attachment).shutdown();
        }

		RIBRequestCommand command = new RIBRequestCommand(eventSchedule);
		getCommandSenderHelper().sendToAllHosts(command);
		getCommandSenderHelper().waitForAllHosts();
    }

    @Override
    public String toString() {
        return "LOG;" + eventSchedule + ".";
    }

}

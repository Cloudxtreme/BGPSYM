package nl.nlnetlabs.bgpsym01.coordinator.events;

import nl.nlnetlabs.bgpsym01.command.LogRequestCommand;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.primitives.types.ShutdownadbleThread;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.log4j.Logger;

@XStreamAlias("log")
public class EventLog extends Event {

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

		LogRequestCommand command = new LogRequestCommand(eventSchedule);
		getCommandSenderHelper().sendToAllHosts(command);
		getCommandSenderHelper().waitForAllHosts();

        // yeah, I know that this is bad!
        //StaticThread.sleep(20 * 1000);
    }

    @Override
    public String toString() {
        return "LOG;" + eventSchedule + ".";
    }

}

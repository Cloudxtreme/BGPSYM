package nl.nlnetlabs.bgpsym01.coordinator.events;

import java.util.List;

import nl.nlnetlabs.bgpsym01.command.LogRequestCommand;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.converters.PrefixListConverter;
import nl.nlnetlabs.bgpsym01.primitives.types.ShutdownadbleThread;
import nl.nlnetlabs.bgpsym01.primitives.types.StaticThread;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import org.apache.log4j.Logger;

@XStreamAlias("log")
public class EventLog extends Event {

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

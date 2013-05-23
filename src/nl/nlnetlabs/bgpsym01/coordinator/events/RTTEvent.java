package nl.nlnetlabs.bgpsym01.coordinator.events;

import nl.nlnetlabs.bgpsym01.command.RTTCommand;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.primitives.types.StaticThread;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("rtt")
public class RTTEvent extends Event {

    private static Logger log = Logger.getLogger(RTTEvent.class);

    @XStreamAsAttribute
    private int times;

    @XStreamAlias("schedule")
    private EventSchedule eventSchedule;

    @Override
    public EventSchedule getEventSchedule() {
        return eventSchedule;
    }

    @Override
    public void process() {
        for (int j = 0; j < times; j++) {
            RTTCommand command = new RTTCommand();
            for (int i = 0; i < XProperties.getInstance().hostCount; i++) {
                StaticThread.sleep(10);
                command.setStartTime(System.currentTimeMillis());
                getCommandSenderHelper().sendToAHost(command, i);
            }
            StaticThread.sleep(2000);
        }
        StaticThread.sleep(2000);
        if (log.isInfoEnabled()) {
            log.info("DONE");
        }
    }

}

package nl.nlnetlabs.bgpsym01.coordinator.events;

import java.util.List;

import nl.nlnetlabs.bgpsym01.command.MasterCommand;
import nl.nlnetlabs.bgpsym01.command.PrefixDataResetCommand;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.converters.PrefixListConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("resetStats")
public class EventResetData extends Event {

    @XStreamConverter(PrefixListConverter.class)
    private List<Prefix> prefixList;

    @XStreamAlias("schedule")
    private EventSchedule schedule;

    @Override
    public EventSchedule getEventSchedule() {
        return schedule;
    }

    @Override
    public void process() {
        MasterCommand command = new PrefixDataResetCommand(prefixList);
        getCommandSenderHelper().sendToAllHosts(command);
        getCommandSenderHelper().waitForAllHosts();
    }

}

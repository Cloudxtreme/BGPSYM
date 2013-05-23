package nl.nlnetlabs.bgpsym01.coordinator.events;

import java.util.List;

import nl.nlnetlabs.bgpsym01.command.InvalidateCommand;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.converters.ASIdentifierConverter;
import nl.nlnetlabs.bgpsym01.primitives.converters.PrefixListConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("invalidate")
public class EventInvalidate extends Event {

    @XStreamConverter(ASIdentifierConverter.class)
    ASIdentifier asId;

    @XStreamConverter(ASIdentifierConverter.class)
    ASIdentifier neighborAsId;

    @XStreamAlias("schedule")
    private EventSchedule schedule;

    @XStreamConverter(PrefixListConverter.class)
    List<Prefix> prefixList;

    boolean validate;

    @Override
    public EventSchedule getEventSchedule() {
        return schedule;
    }

    @Override
    public void process() {
        InvalidateCommand command = generateCommand();
        getCommandSenderHelper().sendToAHost(command, asId.getProcessId());
    }

    InvalidateCommand generateCommand() {
        InvalidateCommand command = new InvalidateCommand();
        command.setAsIdentifier(asId);
        command.setNeighborsIdentifier(neighborAsId);
        command.setPrefixes(prefixList);
        command.setValidate(validate);
        return command;
    }

    @Override
    public String toString() {
        return "validate=" + validate + ", asId=" + asId + ", nAsId=" + neighborAsId + ", prefix=" + prefixList.toString();
    }

}

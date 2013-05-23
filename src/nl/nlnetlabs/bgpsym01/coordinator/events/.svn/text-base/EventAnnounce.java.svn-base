package nl.nlnetlabs.bgpsym01.coordinator.events;

import java.util.Collection;
import java.util.List;

import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.converters.ASIdentifierConverter;
import nl.nlnetlabs.bgpsym01.primitives.converters.PrefixListConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("ann")
public class EventAnnounce extends Event {

    @XStreamConverter(ASIdentifierConverter.class)
    private ASIdentifier asId;

    @XStreamConverter(PrefixListConverter.class)
    private List<Prefix> prefixList;

    @XStreamConverter(PrefixListConverter.class)
    private List<Prefix> withdrawals;

    @XStreamAlias("schedule")
    private EventSchedule eventSchedule;

    public EventAnnounce() {
    }

    @Override
    public EventSchedule getEventSchedule() {
        return eventSchedule;
    }

    @Override
    public void process() {
        getCommandSenderHelper().sendUpdate(prefixList, withdrawals, asId);
    }

    public void setEventSchedule(EventSchedule eventSchedule) {
        this.eventSchedule = eventSchedule;
    }

    public List<Prefix> getPrefixList() {
        return prefixList;
    }

    public void setPrefixList(List<Prefix> prefixList) {
        this.prefixList = prefixList;
    }

    public ASIdentifier getAsId() {
        return asId;
    }

    public void setAsId(ASIdentifier asId) {
        this.asId = asId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventAnnounce) {
            EventAnnounce ann = (EventAnnounce) obj;
            if (asId.equals(ann.getAsId()) && prefixList.equals(ann.prefixList)) {
                return withdrawals == null ? ann.withdrawals == null : withdrawals.equals(ann.withdrawals);
            }
        }
        return false;
    }

    public List<Prefix> getWithdrawals() {
        return withdrawals;
    }

    public void setWithdrawals(List<Prefix> withdrawals) {
        this.withdrawals = withdrawals;
    }

    /*
     * behavior of this function is tested in EventAnnounceTest#testToString - every change in the output
     * has to be adjusted there
     */
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        out.append("ANN;").append(eventSchedule).append(";");
        out.append("as=").append(asId.toString()).append("; prefs=");
        addList(out, prefixList);
        out.append("; with=");
        addList(out, withdrawals);

        return out.toString();
    }

    private void addList(StringBuilder out, Collection<Prefix> list) {
        if (list == null) {
            out.append("NULL");
        } else {
            for (Prefix prefix : list) {
                out.append(prefix.toString()).append(", ");
            }
        }
    }

}

package nl.nlnetlabs.bgpsym01.coordinator.events.framework;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import nl.nlnetlabs.bgpsym01.command.LastSeenResponseCommand;
import nl.nlnetlabs.bgpsym01.command.ResultWriterRouteView;
import nl.nlnetlabs.bgpsym01.coordinator.events.Event;
import nl.nlnetlabs.bgpsym01.coordinator.events.EventAnnounce;
import nl.nlnetlabs.bgpsym01.coordinator.events.EventLastSeen;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASType;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;

import org.apache.log4j.Logger;

public class DataMeasurementImpl implements DataMeasurement {

    private static Logger log = Logger.getLogger(DataMeasurementImpl.class);

    private HashMap<Prefix, Long> lastSeenMap = new HashMap<Prefix, Long>();

    public void eventSent(Event event) {
        if (event instanceof EventAnnounce) {
            EventAnnounce ea = (EventAnnounce) event;
            if (ea.getPrefixList() != null) {
                put(ea.getPrefixList(), ea.getEventSchedule());
            }
            if (ea.getWithdrawals() != null) {
                put(ea.getWithdrawals(), ea.getEventSchedule());
            }
        } else if (event instanceof EventLastSeen) {
            ResultWriterRouteView resultWriter = new ResultWriterRouteView();
            resultWriter.setDataMeasurement(this);
            setRouteViewsSize(resultWriter);
            LastSeenResponseCommand.setResultWriter(resultWriter);
        }
    }

    private void setRouteViewsSize(ResultWriterRouteView resultWriter) {
        // how many routeView's do we have?
        int size = 0;
        Iterator<ASIdentifier> iterator = ASFactory.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getType() == ASType.ROUTEVIEW) {
                size++;
            }
        }
        resultWriter.setSize(size);
        if (log.isInfoEnabled()) {
            log.info("set size to " + size);
        }
    }

    private void put(List<Prefix> list, EventSchedule eventSchedule) {
        for (Prefix prefix : list) {
            // and here pops the question: are we interested in official
            // launchTime or rather in the factual?
            lastSeenMap.put(prefix, eventSchedule.getLaunchTime());
        }
    }

    public long getLastSeen(Prefix prefix) {
        Long lastSeen = lastSeenMap.get(prefix);
        if (lastSeen == null) {
            log.warn("lastSeen=null, prefix=" + prefix);
        }
        return lastSeen == null ? -1 : lastSeen;
    }

}

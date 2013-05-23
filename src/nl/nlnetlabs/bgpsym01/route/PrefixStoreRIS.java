package nl.nlnetlabs.bgpsym01.route;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeController;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class PrefixStoreRIS implements PrefixStore {

    private ASIdentifier asId;

    public ASIdentifier getAsId() {
        return asId;
    }

    // TODO check setter
    private Neighbors neighbors;

    public Neighbors getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(Neighbors neighbors) {
        this.neighbors = neighbors;
    }

    private TimeController timeController;

    public void setAsId(ASIdentifier asId) {
        this.asId = asId;
    }

    private ArrayList<NabsirUpdate> updates = new ArrayList<NabsirUpdate>();

    public void flush(ASIdentifier asId) {
    }

    public PrefixStoreType getType() {
        return PrefixStoreType.RIS;
    }

    private void registerUpdate(ASIdentifier originator, Prefix prefix, Route route) {
        if (prefix.getNum() >= XProperties.getInstance().bogusPrefixMin) {
            return;
        }
        NabsirUpdate update = new NabsirUpdate();
        update.setFrom(originator);
        update.setPrefix(prefix);
        update.setTo(asId);
        update.setTime(timeController.getCurrentTime());
        update.setRoute(route);
        update.setWithdrawal(route == null);
        updates.add(update);
        // log.info("got update, pr=" + prefix + "as=" + originator + ", r=" +
        // route + ", size=" + updates.size());
    }

    private void sendUpdate(BGPUpdate update) {
        for (Neighbor n : neighbors) {
            n.send(update);
        }
    }

    private BGPUpdate createUpate(Collection<Prefix> prefixes, Collection<Prefix> withdrawals, Route route) {
        BGPUpdate update = new BGPUpdate();
        update.setSender(asId);

        if (prefixes != null) {
            /* we have to make list here to satisfy BGPUpdate --- this is called
             * very rarely so we can afford this
             */
            update.setPrefixes(new ArrayList<Prefix>(prefixes));
            if (prefixes.size() > 0) {
                route = route.copyWithMeOnPath(asId);
            }
        }
        if (route == null) {
            route = new Route();
            route.createEmptyHops();
        }
        update.setRoute(route);
        update.setWithdrawals(withdrawals);
        return update;
    }

    public void prefixReceived(ASIdentifier originator, Collection<Prefix> prefixes, Route route) {

        if (originator.equals(asId)) {
            sendUpdate(createUpate(prefixes, null, route));
        } else {
            for (Prefix prefix : prefixes) {
                registerUpdate(originator, prefix, route);
            }
        }
    }

    public void prefixRemove(ASIdentifier originator, Collection<Prefix> prefixes) {

        if (originator.equals(asId)) {
            sendUpdate(createUpate(null, prefixes, null));
        } else {
            for (Prefix prefix : prefixes) {
                registerUpdate(originator, prefix, null);
            }
        }
    }

    public void storePrefixesPermanent() throws IOException {
    }

    public void unflap(Prefix prefix, ASIdentifier asId) {
    }

    public List<NabsirUpdate> getList() {
        return updates;
    }

	public void setTimeController (TimeController timeController) {
		this.timeController = timeController;
	}

	public TimeController getTimeCOntroller () {
		return timeController;
	}
}

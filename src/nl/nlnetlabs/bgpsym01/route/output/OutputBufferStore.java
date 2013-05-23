package nl.nlnetlabs.bgpsym01.route.output;

import java.util.Iterator;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.primitives.OutputAddEntity;
import nl.nlnetlabs.bgpsym01.primitives.OutputRemoveEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

public interface OutputBufferStore {

    /**
     * Adds announcement that should be sent to all neighbors
     * 
     * @param entity
     */
    public void addAnnouncement(OutputAddEntity entity);

    /**
     * Adds announcement to be sent to particular neighbor (most probably later
     * on)
     * 
     * @param neighbor
     * @param entity
     */
    public void addAnnouncement(Neighbor neighbor, OutputAddEntity entity);

    /**
     * Removes announcement that should be sent to a particular neighbor
     * 
     * @param neighbor
     * @param prefix
     * @return
     */
    public OutputAddEntity removeAnnouncement(Neighbor neighbor, Prefix prefix);

    public Iterator<OutputAddEntity> announcementsIterator(Neighbor neighbor);

    public Iterator<OutputAddEntity> announcementsIterator();

    public void clearAnnouncements();

    public void clearAnnouncements(Neighbor neighbor);

    public void addWithdrawal(OutputRemoveEntity withdrawal);

    public Iterator<OutputRemoveEntity> withdrawalsIterator();

    public void clearWithdrawals();

}

package nl.nlnetlabs.bgpsym01.route.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.primitives.OutputAddEntity;
import nl.nlnetlabs.bgpsym01.primitives.OutputRemoveEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

public class OutputBufferStoreImpl implements OutputBufferStore {

    private HashMap<Neighbor, ArrayList<OutputAddEntity>> neighborAnnouncements = new HashMap<Neighbor, ArrayList<OutputAddEntity>>();

    private ArrayList<OutputAddEntity> announcements = new ArrayList<OutputAddEntity>();

    private LinkedList<OutputRemoveEntity> withdrawals = new LinkedList<OutputRemoveEntity>();

    public void addAnnouncement(Neighbor neighbor, OutputAddEntity entity) {
        ArrayList<OutputAddEntity> list = neighborAnnouncements.get(neighbor);
        if (list == null) {
            list = new ArrayList<OutputAddEntity>();
            neighborAnnouncements.put(neighbor, list);
        }
        list.add(entity);
    }

    public void addWithdrawal(OutputRemoveEntity withdrawal) {
        withdrawals.add(withdrawal);
    }

    public Iterator<OutputAddEntity> announcementsIterator(Neighbor neighbor) {
        return neighborAnnouncements.get(neighbor).iterator();
    }
    
    public void removeAllAnnouncements(Neighbor neighbor) {
    	ArrayList<OutputAddEntity> list = neighborAnnouncements.get(neighbor);
        if (list == null) {
            return;
        }
        
        Iterator<OutputAddEntity> iterator = list.iterator();
        while (iterator.hasNext()) {
            iterator.remove();
        }
    }
    

    // TODO: this is very linear - probably Map would do better...
    public OutputAddEntity removeAnnouncement(Neighbor neighbor, Prefix prefix) {
        ArrayList<OutputAddEntity> list = neighborAnnouncements.get(neighbor);
        if (list == null) {
            return null;
        }

        Iterator<OutputAddEntity> iterator = list.iterator();
        while (iterator.hasNext()) {
            OutputAddEntity next = iterator.next();
            if (next.getPrefix().equals(prefix)) {
                iterator.remove();
                return next;
            }
        }

        return null;
    }

    public Iterator<OutputRemoveEntity> withdrawalsIterator() {
        return withdrawals.iterator();
    }

    public Iterator<Neighbor> neighborIterator() {
        return neighborAnnouncements.keySet().iterator();
    }

    public void addAnnouncement(OutputAddEntity entity) {
        announcements.add(entity);
    }

    public Iterator<OutputAddEntity> announcementsIterator() {
        return announcements.iterator();
    }

    public void clearAnnouncements() {
        announcements.clear();
    }

    public void clearAnnouncements(Neighbor neighbor) {
        neighborAnnouncements.get(neighbor).clear();
    }

    public void clearWithdrawals() {
        withdrawals.clear();
    }

}

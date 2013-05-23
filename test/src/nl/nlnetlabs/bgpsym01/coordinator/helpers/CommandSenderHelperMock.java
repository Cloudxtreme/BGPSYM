package nl.nlnetlabs.bgpsym01.coordinator.helpers;

import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.command.MasterCommand;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.types.Pair;

import org.apache.log4j.Logger;

public class CommandSenderHelperMock implements CommandSenderHelper {

    private static Logger log = Logger.getLogger(CommandSenderHelperMock.class);

    public ArrayList<Pair<List<Prefix>, ASIdentifier>> received = new ArrayList<Pair<List<Prefix>, ASIdentifier>>();
    public ArrayList<Pair<List<Prefix>, ASIdentifier>> withdrawn = new ArrayList<Pair<List<Prefix>, ASIdentifier>>();

    public ArrayList<Pair<List<Prefix>, ASIdentifier>> getWithdrawn() {
        return withdrawn;
    }

    public void setWithdrawn(ArrayList<Pair<List<Prefix>, ASIdentifier>> withdrawn) {
        this.withdrawn = withdrawn;
    }

    public void ackReceived() {
    }

    public void sendSyncCommand() {
    }

    public void sendToAllHosts(MasterCommand command) {
    }

    public void sendUpdate(int count) {
        if (log.isDebugEnabled()) {
            log.debug("count=" + count);
        }
    }

    public int sendUpdate(List<Prefix> prefixList, ASIdentifier asId) {
        return sendUpdate(prefixList, null, asId);
    }

    public int sendUpdate(List<Prefix> prefixList, List<Prefix> withdrawals, ASIdentifier asId) {
        if (prefixList != null) {
            received.add(new Pair<List<Prefix>, ASIdentifier>(new ArrayList<Prefix>(prefixList), asId));
        }
        if (withdrawals != null) {
            withdrawn.add(new Pair<List<Prefix>, ASIdentifier>(new ArrayList<Prefix>(withdrawals), asId));
        }
        return 0;
    }

    public void waitForAllHosts() {
    }

    public void waitForEmptyQueues() {
    }

    public ArrayList<Pair<List<Prefix>, ASIdentifier>> getReceived() {
        return received;
    }

    public void setReceived(ArrayList<Pair<List<Prefix>, ASIdentifier>> received) {
        this.received = received;
    }

    public void sendToAHost(MasterCommand command, int hostNum) {
    }

    public void attach(Object attachment) {
    }

    public Object attachment() {
        return null;
    }

}

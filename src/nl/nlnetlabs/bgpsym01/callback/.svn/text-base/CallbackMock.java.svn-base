package nl.nlnetlabs.bgpsym01.callback;

import nl.nlnetlabs.bgpsym01.primitives.OutputEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;

public class CallbackMock implements Callback {

    private static CallbackMock instance = new CallbackMock();

    public static CallbackMock getInstance() {
        return instance;
    }

    public void prefixReceived(ASIdentifier asIdentifier, Prefix prefix, Route route) {
    }

    public void prefixRegistered(ASIdentifier asIdentifier, Prefix prefix, Route oldRoute, Route newRoute) {
    }

    public void prefixUnregistered(ASIdentifier asIdentifier, Prefix prefix, Route oldRoute, Route newRoute) {
    }

    public void withdrawalReceived(ASIdentifier asIdentifier, Prefix prefix) {
    }

    public void prefixAdvertised(ASIdentifier asIdentifier, Prefix prefix, Route route) {
    }

    public void withdrawalSent(ASIdentifier asIdentifier, Prefix prefix, Route newRoute) {
    }

    public void updateSend(ASIdentifier asIdentifier, BGPUpdate update) {

    }

    public void close() {

    }

    public void updateReceived(ASIdentifier asIdentifier, BGPUpdate update) {
    }

    public void flapRegister(ASIdentifier asId, Prefix prefix, long unflap, long readyTime) {
    }

    public void flapTrigger(ASIdentifier asId, Prefix prefix, long readyTime) {
    }

    public void mraiRegister(ASIdentifier asId, long timerStart, long storeStart) {
    }

    public void mraiTrigger(ASIdentifier asId) {
    }

    public void addEntity(OutputEntity entity) {
    }

    public void arbitrary(String msg) {
    }

    public void flush() {
    }

}

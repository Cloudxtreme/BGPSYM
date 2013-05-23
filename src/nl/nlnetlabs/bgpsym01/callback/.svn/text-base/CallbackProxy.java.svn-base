package nl.nlnetlabs.bgpsym01.callback;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.factories.CallbackFactory;

public class CallbackProxy extends CallbackMock {

    @Override
    public void prefixAdvertised(ASIdentifier asIdentifier, Prefix prefix, Route route) {
        insideCallback.prefixAdvertised(asIdentifier, prefix, route);
    }

    @Override
    public void prefixReceived(ASIdentifier asIdentifier, Prefix prefix, Route route) {
        insideCallback.prefixReceived(asIdentifier, prefix, route);
    }

    @Override
    public void prefixRegistered(ASIdentifier asIdentifier, Prefix prefix, Route oldRoute, Route newRoute) {
        insideCallback.prefixRegistered(asIdentifier, prefix, oldRoute, newRoute);
    }

    @Override
    public void prefixUnregistered(ASIdentifier asIdentifier, Prefix prefix, Route oldRoute, Route newRoute) {
        insideCallback.prefixUnregistered(asIdentifier, prefix, oldRoute, newRoute);
    }

    @Override
    public void updateSend(ASIdentifier asIdentifier, BGPUpdate update) {
        insideCallback.updateSend(asIdentifier, update);
    }

    @Override
    public void withdrawalReceived(ASIdentifier asIdentifier, Prefix prefix) {
        insideCallback.withdrawalReceived(asIdentifier, prefix);
    }

    @Override
    public void withdrawalSent(ASIdentifier asIdentifier, Prefix prefix, Route newRoute) {
        insideCallback.withdrawalSent(asIdentifier, prefix, newRoute);
    }

    private Callback insideCallback;

    private ASIdentifier asIdentifier;

    public CallbackProxy(ASIdentifier asIdentifier, Callback.CallbackType type) {
        this.asIdentifier = asIdentifier;
        setCallback(type);
    }

    public void setCallback(Callback.CallbackType type) {
        if (type == null) {
            type = CallbackType.MOCK;
        }
        switch (type) {
        case LOG4J:
            insideCallback = CallbackFactory.getCallbackLog4j();
            break;
        case FILE:
            insideCallback = CallbackFactory.getCallbackRegister(asIdentifier);
            break;
        default:
            insideCallback = CallbackFactory.getCallbackMock();
            break;
        }
    }

}

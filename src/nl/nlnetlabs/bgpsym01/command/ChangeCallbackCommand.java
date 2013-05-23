package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.callback.Callback;
import nl.nlnetlabs.bgpsym01.callback.CallbackProxy;
import nl.nlnetlabs.bgpsym01.callback.Callback.CallbackType;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;

public class ChangeCallbackCommand extends MasterCommand {

    private CallbackType callbackType;

    public ChangeCallbackCommand(Callback.CallbackType type) {
        this.callbackType = type;
    }

    public ChangeCallbackCommand() {
    }

    @Override
    public CommandType getCommandType() {

        throw new NotImplementedException();
    }

    @Override
    public void process() {

        for (BGPProcess process : jvm.getProcesses().values()) {
            Callback callback = process.getCallback();
            if (callback instanceof CallbackProxy) {
                ((CallbackProxy) callback).setCallback(callbackType);
            }
        }

        throw new NotImplementedException();
    }

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
        callbackType = Callback.CallbackType.readExternal(in);
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
        callbackType.writeExternal(out);
    }

}

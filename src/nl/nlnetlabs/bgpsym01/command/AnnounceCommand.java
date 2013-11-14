package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;

public class AnnounceCommand extends MasterCommand {

    private CommandType type = CommandType.ANNOUNCE;

    private Update update;

    protected ASIdentifier recipient;

    @Override
    public CommandType getCommandType() {
        return type;
    }

    @Override
    public void process() {
        recipient.getProcess().getQueue().addMessage(update);
    }

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
        super.readInternalData(in);
        recipient = ASIdentifier.staticReadExternal(in);
        update = Update.UpdateType.getInstance(in);
        update.readExternal(in);
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
        super.writeInternalData(out);
        assert (recipient != null);
        recipient.writeExternal(out);
        update.getType().writeExternal(out);
        update.writeExternal(out);
    }

    public void setUpdate(Update update) {
        this.update = update;
    }

    @Override
    public String toString() {
        return "type=" + type.toString() + ", asId=" + recipient + ", update=" + update;
    }

    @Override
    public int getProcessId() {
        return recipient.getProcessId();
    }

    public void setRecipient(ASIdentifier recipient) {
        this.recipient = recipient;
    }

    public Update getUpdate() {
        return update;
    }

    public ASIdentifier getRecipient() {
        return recipient;
    }

}

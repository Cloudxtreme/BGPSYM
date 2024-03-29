package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;
import java.util.List;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.InvalidateUpdate;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;

public class InvalidateCommand extends MasterCommand {

    private ASIdentifier asIdentifier;
    private List<Prefix> prefixes;
    private boolean validate;
    private ASIdentifier neighborsIdentifier;

    public ASIdentifier getNeighborsIdentifier() {
        return neighborsIdentifier;
    }

    InvalidateUpdate getUpdate() {
        InvalidateUpdate update = new InvalidateUpdate();
        update.setNeighborId(neighborsIdentifier);
        update.setPrefixes(prefixes);
        update.setValidate(validate);
        return update;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.INVALIDATE_COMMAND;
    }

    @Override
    public void process() {
        BGPProcess process = jvm.getProcesses().get(asIdentifier);
        InvalidateUpdate update = getUpdate();
        process.getQueue().addMessage(update);
    }

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
        prefixes = in.readPrefixList();
        if (in.readBoolean()) {
            neighborsIdentifier = ASIdentifier.staticReadExternal(in);
        }
        asIdentifier = ASIdentifier.staticReadExternal(in);
        validate = in.readBoolean();
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
        out.writePrefixList(prefixes);
        out.writeBoolean(neighborsIdentifier != null);
        if (neighborsIdentifier != null) {
            neighborsIdentifier.writeExternal(out);
        }
        asIdentifier.writeExternal(out);
        out.writeBoolean(validate);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof InvalidateCommand)) {
            return false;
        }

        InvalidateCommand cmd = (InvalidateCommand) obj;
        return (neighborsIdentifier == cmd.neighborsIdentifier || neighborsIdentifier.equals(cmd.neighborsIdentifier)) && prefixes.equals(cmd.prefixes)
                && validate == cmd.validate
        && asIdentifier.equals(cmd.asIdentifier);
    }

    public void setPrefixes(List<Prefix> prefixes) {
        this.prefixes = prefixes;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public void setNeighborsIdentifier(ASIdentifier neighborsIdentifier) {
        this.neighborsIdentifier = neighborsIdentifier;
    }

    public ASIdentifier getAsIdentifier() {
        return asIdentifier;
    }

    public void setAsIdentifier(ASIdentifier asIdentifier) {
        this.asIdentifier = asIdentifier;
    }

}

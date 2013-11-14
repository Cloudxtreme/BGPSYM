package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.RunnableUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;

/**
 * This command removes neighbors from queue of given asIdentifier
 */
public class DisconnectCommand extends MasterCommand {

    private ASIdentifier asIdentifier;

    private List<ASIdentifier> asIds;

    private Prefix[] prefixes;

    @Override
    public CommandType getCommandType() {
        return CommandType.DISCONNECT_COMMAND;
    }

    @Override
    public void process() {
        /* 
         * 1. get the right process
         * 2. remove given neighbors
         * 3. remove prefixes
         */
        BGPProcess process = jvm.getProcesses().get(asIdentifier);
        Update update = new RunnableUpdate() {

            @Override
            public void run(BGPProcess process) {
                for (ASIdentifier asId : asIds) {
                    process.getNeighbors().remove(asId);
                    if (prefixes != null && prefixes.length > 0) {
                        // TODO change this prefixes to list!
                        process.getStore().prefixRemove(asId, Arrays.asList(prefixes));
                    }
                }
            }

        };
        process.getQueue().addMessage(update);
    }

    public ASIdentifier getAsIdentifier() {
        return asIdentifier;
    }

    public void setAsIdentifier(ASIdentifier asIdentifier) {
        this.asIdentifier = asIdentifier;
    }

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
        asIdentifier = ASIdentifier.staticReadExternal(in);
        int size = in.readInt();
        prefixes = new Prefix[size];
        for (int i = 0; i < size; i++) {
            prefixes[i] = Prefix.getInstance(in.readBits(SystemConstants.PREFIX_SIZE_BITS));
        }
        size = in.readInt();
        asIds = new ArrayList<ASIdentifier>(size);
        for (int i = 0; i < size; i++) {
            asIds.add(ASIdentifier.staticReadExternal(in));
        }
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
        asIdentifier.writeExternal(out);
        int size = prefixes == null ? 0 : prefixes.length;
        out.writeInt(size);
        if (size > 0) {
            for (Prefix prefix : prefixes) {
                out.writeBits(prefix.getNum(), SystemConstants.PREFIX_SIZE_BITS);
            }
        }
        out.writeList(asIds);
    }

    public Prefix[] getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(Prefix[] prefixes) {
        this.prefixes = prefixes;
    }

    public List<ASIdentifier> getAsIds() {
        return asIds;
    }

    public void setAsIds(List<ASIdentifier> ids) {
        this.asIds = ids;
    }

}

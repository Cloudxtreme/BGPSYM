package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASType;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.RunnableUpdate;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreRouteView;
import nl.nlnetlabs.bgpsym01.route.PrefixStore.PrefixStoreType;

public class PrefixDataResetCommand extends MasterCommand {

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
        int size = in.readInt();
        prefixes = new ArrayList<Prefix>();
        for (int i = 0; i < size; i++) {
            prefixes.add(Prefix.getInstance(in.readInt()));
        }
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
        out.writeInt(prefixes.size());
        for (Prefix prefix : prefixes) {
            out.writeInt(prefix.getNum());
        }
    }

    private List<Prefix> prefixes;

    /**
     * number of the ASes on the compute node. Set when the command arrives and
     * decreased everytime someone is ready with its update.
     */
    private int count;

    public PrefixDataResetCommand() {
    }

    public PrefixDataResetCommand(List<Prefix> prefixes) {
        this.prefixes = prefixes;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.PREFIX_RESET;
    }

    @Override
    protected int decCount() {
        return --count;
    }

    @Override
    public void process() {
        count = jvm.getProcesses().size();
        for (BGPProcess process : jvm.getProcesses().values()) {
            addCommand(process);
        }
    }

    private void addCommand(BGPProcess process) {
        RunnableUpdate update = new RunnableUpdate() {

            @Override
            public void run(BGPProcess process) {
                if (process.getAsIdentifier().getType() != ASType.ROUTEVIEW) {
                    assert process.getStore().getType() == PrefixStoreType.ROUTEVIEW;
                    for (Prefix prefix : prefixes) {
                        ((PrefixStoreRouteView) process.getStore()).resetPrefixData(prefix);
                    }
                }
                sent();
            }
        };
        process.getQueue().addMessage(update);
    }

}

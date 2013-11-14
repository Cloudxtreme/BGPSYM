package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASType;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.RunnableUpdate;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreRouteView;
import nl.nlnetlabs.bgpsym01.route.PrefixStore.PrefixStoreType;

import org.apache.log4j.Logger;

public class LastSeenRequestCommand extends MasterCommand {

    private static Logger log = Logger.getLogger(LastSeenRequestCommand.class);

    private List<Prefix> prefixes;

    // not serialized
    int count;

    @Override
    protected int decCount() {
        return --count;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.LAST_SEEN;
    }

    @Override
    public void process() {

        count = jvm.getProcesses().size();
        for (BGPProcess process : jvm.getProcesses().values()) {
            addCommand(process);
        }

    }

    private void addCommand(BGPProcess process) {

        if (!(process.getAsIdentifier().getType() != ASType.ROUTEVIEW)) {
            // nothing to do here...
            sent();
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("getting from " + process.getAsIdentifier());
        }

        RunnableUpdate update = new RunnableUpdate() {

            @Override
            public void run(BGPProcess process) {
                if (log.isInfoEnabled()) {
                    log.info("gettling prefixDataList");
                }
                assert process.getStore().getType() == PrefixStoreType.ROUTEVIEW;
                PrefixStoreRouteView store = (PrefixStoreRouteView) process.getStore();

                final List<RouteViewDataResponse> list = getPrefixDataList(store.getPrefixDataList());
                if (log.isInfoEnabled()) {
                    log.info("list==" + list);
                }

                LastSeenResponseCommand response = new LastSeenResponseCommand();
                response.setList(list);
                response.setAsId(process.getAsIdentifier());
                jvm.getCst().sendCommand(response);
                sent();
            }

        };
        process.getQueue().addMessage(update);
    }

    /**
     * Iterates over PrefixStoreRouteView and gets info about prefixes
     * 
     * @return
     */
    List<RouteViewDataResponse> getPrefixDataList(Collection<RouteViewDataResponse> responses) {
        final List<RouteViewDataResponse> list = new ArrayList<RouteViewDataResponse>();
        // process.assertisidle();
        for (RouteViewDataResponse response : responses) {
            if (prefixes.contains(response.prefix)) {
                list.add(response);
            }
        }
        return list;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LastSeenRequestCommand)) {
            return false;
        }
        LastSeenRequestCommand cmd = (LastSeenRequestCommand) obj;
        if (prefixes == null || cmd.prefixes == null) {
            return false;
        }
        if (!prefixes.equals(cmd.prefixes)) {
            return false;
        }
        return true;
    }

    public List<Prefix> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<Prefix> prefixes) {
        this.prefixes = prefixes;
    }

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
        prefixes = new ArrayList<Prefix>();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            prefixes.add(Prefix.getInstance(in.readBits(SystemConstants.PREFIX_SIZE_BITS)));
        }
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
        out.writeInt(prefixes.size());
        for (Prefix prefix : prefixes) {
            out.writeBits(prefix.getNum(), SystemConstants.PREFIX_SIZE_BITS);
        }
    }
}

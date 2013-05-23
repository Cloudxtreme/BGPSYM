package nl.nlnetlabs.bgpsym01.command;

import java.util.List;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASType;
import nl.nlnetlabs.bgpsym01.primitives.bgp.RunnableUpdate;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;
import nl.nlnetlabs.bgpsym01.route.NabsirUpdate;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreRIS;

import org.apache.log4j.Logger;

/*
 * not tested:
 *  - no testbed for things like this
 * 
 */

public class RISGetDataRequestCommand extends MasterCommand {

    private static Logger log = Logger.getLogger(RISGetDataRequestCommand.class);

    // not serialized
    // and THIS IS BAD!
    int count;

    @Override
    protected int decCount() {
        return --count;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.RIS_REQUEST;
    }

    @Override
    public void process() {

        count = jvm.getProcesses().size();
        for (BGPProcess process : jvm.getProcesses().values()) {
            addCommand(process);
        }

    }

    private void addCommand(BGPProcess process) {
        if (process.getAsIdentifier().getType() != ASType.RIS) {
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
                PrefixStoreRIS store = (PrefixStoreRIS) process.getStore();

                RISGetDataResponseCommand response = new RISGetDataResponseCommand();
                List<NabsirUpdate> list = store.getList();
                response.setList(list);
                if (log.isInfoEnabled()) {
                    log.info("gettling prefixDataList, asId=" + store.getAsId() + ", list.size()=" + list.size());
                }
                jvm.getCst().sendCommand(response);
                sent();
            }

        };
        process.getQueue().addMessage(update);

    }

}

package nl.nlnetlabs.bgpsym01.coordinator.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.nlnetlabs.bgpsym01.command.RISGetDataRequestCommand;
import nl.nlnetlabs.bgpsym01.command.RISGetDataResponseCommand;
import nl.nlnetlabs.bgpsym01.command.ResultWriter;
import nl.nlnetlabs.bgpsym01.command.ResultWriterRIS;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.types.ShutdownadbleThread;
import nl.nlnetlabs.bgpsym01.primitives.types.StaticThread;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/*
 * not tested:
 *  - straightforward
 *  - hard to test
 *  - real-life is enough
 */

@XStreamAlias("risCollect")
public class EventRISCollect extends Event {

    private static Logger log = Logger.getLogger(EventRISCollect.class);

    @XStreamAlias("schedule")
    private EventSchedule schedule;

    private List<PrefixData> prefixes;

    public List<PrefixData> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<PrefixData> prefixes) {
        this.prefixes = prefixes;
    }

    @Override
    public EventSchedule getEventSchedule() {
        return schedule;
    }

    Map<String, PrefixData> convertListToMap(List<PrefixData> list) {
        HashMap<String, PrefixData> map = new HashMap<String, PrefixData>();
        for (PrefixData elem : list) {
            map.put(elem.prefix.toString(), elem);
        }
        return map;
    }

    private void writeInfo() {
        ResultWriter writer = new ResultWriterRIS();
        try {
            writer.writeInfoFile(new ArrayList<String>());
        } catch (IOException e) {
            log.error(e);
            throw new BGPSymException(e);
        }
    }

    @Override
    public void process() {
        // TODO: this is nasty (those f...ing statics !)
        assert prefixes != null;

        // save properties to a file
        writeInfo();
        RISGetDataResponseCommand.setMap(convertListToMap(prefixes));

        // we want to kill NoiseThread - we don't need him any more
        Object attachment = getCommandSenderHelper().attachment();
        if (attachment instanceof ShutdownadbleThread) {
            ((ShutdownadbleThread) attachment).shutdown();
        }

        RISGetDataRequestCommand command = new RISGetDataRequestCommand();
        // TODO: store the prefixes somewhere...
        // command.setPrefixes(prefixes);

        getCommandSenderHelper().sendToAllHosts(command);
        getCommandSenderHelper().waitForAllHosts();

        // yeah, I know that this is bad!
        StaticThread.sleep(20 * 1000);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventRISCollect) {
            EventRISCollect ev = (EventRISCollect) obj;
            return prefixes.equals(ev.prefixes);
        }
        return false;
    }


}

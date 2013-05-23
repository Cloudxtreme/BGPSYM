package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.nlnetlabs.bgpsym01.coordinator.events.PrefixData;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.route.NabsirUpdate;

public class RISGetDataResponseCommand extends SlaveCommand {

    // TODO: this is temporary - has to be changed in the future
    private static Map<String, PrefixData> map;

    private ResultWriterRIS writer;

    public static void setMap(Map<String, PrefixData> map) {
        RISGetDataResponseCommand.map = map;
    }

    private List<NabsirUpdate> list;

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
        int size = in.readInt();
        list = new ArrayList<NabsirUpdate>(size);
        for (int i = 0; i < size; i++) {
            NabsirUpdate update = new NabsirUpdate();
            update.readExternal(in);
            list.add(update);
        }
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
        out.writeInt(list.size());
        for (NabsirUpdate update : list) {
            update.writeExternal(out);
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.RIS_RESPONSE;
    }

    @Override
    public void process() {

        // make it fast :) - don't block
        new Thread() {
            @Override
            public void run() {
                assert map != null;
                writer = new ResultWriterRIS();
                writer.setMap(map);
                writer.writeList(list);
            }
        }.start();
    }

    public void setList(List<NabsirUpdate> list) {
        this.list = list;
    }

    public List<NabsirUpdate> getList() {
        return list;
    }

}

package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.coordinator.events.framework.DataMeasurement;
import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;

import org.apache.log4j.Logger;

public class LastSeenResponseCommand extends SlaveCommand {

    private static Logger log = Logger.getLogger(LastSeenResponseCommand.class);

    /**
     * set in
     * {@link DataMeasurement#eventSent(nl.nlnetlabs.bgpsym01.coordinator.events.Event)}
     */
    private static ResultWriterRouteView resultWriter;

    private ASIdentifier asId;

    private List<RouteViewDataResponse> list;

    @Override
    public CommandType getCommandType() {
        return CommandType.LAST_SEEN_RESP;
    }

    @Override
    public void process() {
        // implementation for now and test for it
        try {
            for (RouteViewDataResponse response : list) {
                resultWriter.dataReceived(asId, response);
            }
            resultWriter.done();
        } catch (IOException e) {
            log.error(e);
        }
    }

    public List<RouteViewDataResponse> getList() {
        return list;
    }

    public void setList(List<RouteViewDataResponse> list) {
        this.list = list;
    }

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
        asId = ASFactory.getInstance(in.readBits(SystemConstants.AS_SIZE_BITS));
        list = new ArrayList<RouteViewDataResponse>();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            RouteViewDataResponse tmp = new RouteViewDataResponse();
            tmp.readExternal(in);
            list.add(tmp);
        }
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
        out.writeBits(asId.getInternalId(), SystemConstants.AS_SIZE_BITS);
        out.writeInt(list.size());
        for (RouteViewDataResponse tmp : list) {
            tmp.writeExternal(out);
        }
    }

    public void setAsId(ASIdentifier asId) {
        this.asId = asId;
    }

    public static void setResultWriter(ResultWriterRouteView resultWriter) {
        LastSeenResponseCommand.resultWriter = resultWriter;
    }

}

package nl.nlnetlabs.bgpsym01.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import nl.nlnetlabs.bgpsym01.coordinator.events.framework.DataMeasurement;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;

import org.apache.log4j.Logger;

public class ResultWriterRouteView extends ResultWriter {

    private static Logger log = Logger.getLogger(ResultWriterRouteView.class);

    private int size;

    private DataMeasurement dataMeasurement;

    private HashMap<Prefix, OutputStreamWriter> map = new HashMap<Prefix, OutputStreamWriter>();

    private boolean finished;

    public ResultWriterRouteView() {
        super();
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isFinished() {
        return finished;
    }

    public void dataReceived(ASIdentifier asId, RouteViewDataResponse response) throws IOException {
        OutputStreamWriter outputStream = map.get(response.prefix);
        if (outputStream == null) {
            outputStream = getOutputStream(response, outputStream);
        }
		log.info("writing: "+asId.toString() + " ; " + response.toString());
        outputStream.write(asId.toString() + " ; " + response.toString() + "\n");

    }

    private OutputStreamWriter getOutputStream(RouteViewDataResponse response, OutputStreamWriter outputStream) throws IOException {
        try {
            FileWriter fos = new FileWriter(new File(getFileName(response.prefix)));
            map.put(response.prefix, fos);
            outputStream = fos;
        } catch (FileNotFoundException e) {
            log.error("map.size()=" + map.size(), e);
        }
        return outputStream;
    }

    public void done() throws IOException {
        size--;
        if (size == 0) {
            close();
        }
    }

    private void close() throws IOException {
        finished = true;
        for (OutputStreamWriter osw : map.values()) {
            osw.close();
        }
        if (log.isInfoEnabled()) {
            log.info("closed files");
        }
    }

    private String getFileName(Prefix prefix) {
        return getResultDirectory() + File.separator + prefix.toString();
    }

    public DataMeasurement getDataMeasurement() {
        return dataMeasurement;
    }

    public void setDataMeasurement(DataMeasurement dataMeasurement) {
        this.dataMeasurement = dataMeasurement;
    }

}

package nl.nlnetlabs.bgpsym01.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

abstract public class ResultWriter {

	private static Logger log = Logger.getLogger(ResultWriter.class);

    private static final String INFO_FILE_NAME = "info";
    private static final String PROP_FILE_NAME = "properties.info";
    private String resultDirectory;

    public String getResultDirectory() {
        return resultDirectory;
    }

    public ResultWriter() {
        super();
        resultDirectory = XProperties.getInstance().getResultDirectory() + File.separator + Tools.getInstance().getStartAsString();
        File dir = new File(resultDirectory);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new BGPSymException("unable to create dir " + dir.getAbsolutePath());
            }
        }
    }

    public void writeInfoFile(Collection<String> values) throws IOException {
        FileWriter fos = new FileWriter(new File(resultDirectory + File.separator + INFO_FILE_NAME));
        fos.write(Tools.getInstance().getStartAsString() + "\n");
        for (String tmp : values) {
            fos.write(tmp + "\n");
        }
        fos.close();
        fos = new FileWriter(new File(resultDirectory + File.separator + PROP_FILE_NAME));
        fos.write(XStreamFactory.getXStream().toXML(XProperties.getInstance()));
        fos.close();
    }

}

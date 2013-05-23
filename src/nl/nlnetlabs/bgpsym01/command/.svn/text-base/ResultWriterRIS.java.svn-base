package nl.nlnetlabs.bgpsym01.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import nl.nlnetlabs.bgpsym01.coordinator.events.PrefixData;
import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.route.NabsirUpdate;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

public class ResultWriterRIS extends ResultWriter {

    private Map<String, PrefixData> map;

    public void setMap(Map<String, PrefixData> map) {
        this.map = map;
    }

    private static final String OUTPUT_FILENAME_PREFIX = "output_";

    private static Logger log = Logger.getLogger(ResultWriterRIS.class);

    public ResultWriterRIS() {
        super();
    }

    List<NabsirUpdate> getSortedList(List<NabsirUpdate> list) {
        class X implements Comparator<NabsirUpdate> {

            @Override
            public int compare(NabsirUpdate n1, NabsirUpdate n2) {
                long t1 = n1.getTime() + n1.getAdditional(map, n1.getPrefix());
                long t2 = n2.getTime() + n2.getAdditional(map, n2.getPrefix());
                return t1 < t2 ? -1 : t1 == t2 ? 0 : 1;
            }

        }
        Collections.sort(list, new X());
        return list;
    }

    OutputStream getStream(ASIdentifier risAS) {
        try {
            boolean append = true;
            return new FileOutputStream(new File(getFilename(risAS)), append);
        } catch (FileNotFoundException e) {
            log.error(e);
            throw new BGPSymException(e);
        }
    }

    File getDirectory() {
        String dirName = XProperties.getInstance().getResultDirectory() + File.separator + Tools.getInstance().getStartAsString();
        File directory = new File(dirName);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new BGPSymException("Unable to make directory: " + dirName);
        }
        if (!directory.isDirectory()) {
            throw new BGPSymException(dirName + " is not a directory");
        }
        return directory;
    }


    String getFilename(ASIdentifier risAS) {
        return getDirectory().getAbsolutePath() + File.separator + OUTPUT_FILENAME_PREFIX + risAS.getASNum();
    }

    public void writeList(List<NabsirUpdate> list) {
        OutputStream s = null;
        try {
            for (NabsirUpdate update : getSortedList(list)) {
                if (s == null) {
                    s = getStream(update.getTo());
                }
                s.write(update.toString(map).getBytes());
                s.write("\n".getBytes());
            }
            if (s != null) {
                s.close();
            }
        } catch (IOException e) {
            log.error(e);
            throw new BGPSymException(e);
        }
    }

}

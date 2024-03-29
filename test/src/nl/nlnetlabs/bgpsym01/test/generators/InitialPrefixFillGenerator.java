package nl.nlnetlabs.bgpsym01.test.generators;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;
import nl.nlnetlabs.bgpsym01.xstream.XPrefix;
import nl.nlnetlabs.bgpsym01.xstream.XSystem;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

public class InitialPrefixFillGenerator {

    private static String PREFIX_FILE_LOCATION = "/home/jlf200/work/bgpdata/prefixes";
    private static String OUTPUT_FILE = "/home/jlf200/work/bgpdata/prefixes.xml";
    private static String NODES_FILE = "/home/jlf200/work/bgpdata/nodes.xml";
    private static Logger log = Logger.getLogger(InitialPrefixFillGenerator.class);
    private HashMap<String, ASIdentifier> asMap;

    private void run() throws IOException {
        // this method will bump out if the file does not exist
        checkIfFileExists();
        XStream xStream = XStreamFactory.getXStream();

        log.info("loading nodes");
        asMap = new HashMap<String, ASIdentifier>();
        XSystem xSystem = (XSystem) xStream.fromXML(new FileInputStream(NODES_FILE));
        xSystem.setNodes(null);
        ArrayList<ASIdentifier> list = xSystem.getAses();
        for (ASIdentifier asId : list) {
            asMap.put(asId.getId(), asId);
        }

        // load the prefixex from file
        log.info("generating prefixes");
        List<XPrefix> prefixes = generatePrefixesList();

        xStream.toXML(prefixes, new FileOutputStream(OUTPUT_FILE));
        log.info("prefixes written to " + OUTPUT_FILE + ", count=" + prefixes.size());

    }

    private List<XPrefix> generatePrefixesList() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(PREFIX_FILE_LOCATION));
        // int count = 100;
        int num = 0;
        List<XPrefix> prefixes = new LinkedList<XPrefix>();
        while (scanner.hasNext()) {
            String prefix = scanner.next();
            int as = scanner.nextInt();

            ASIdentifier asId = asMap.get("AS" + as);
            if (asId == null) {
                continue;
            }

            XPrefix xPrefix = new XPrefix(prefix, num++, "AS" + as, asId.getInternalId());
            prefixes.add(xPrefix);
            if (log.isDebugEnabled()) {
                // log.debug("read " + num);
            }
        }
        return prefixes;
    }

    private void checkIfFileExists() {
        File f = new File(PREFIX_FILE_LOCATION);
        if (!f.exists()) {
            log.error("file " + f.getName() + " does not exist, create it or change location in " + getClass().getName());
            System.exit(1);
        }
    }

    public static void main(String[] args) throws IOException {
        new InitialPrefixFillGenerator().run();
    }

}

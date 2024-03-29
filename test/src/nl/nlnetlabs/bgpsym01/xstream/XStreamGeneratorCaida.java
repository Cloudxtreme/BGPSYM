package nl.nlnetlabs.bgpsym01.xstream;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASType;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;
import nl.nlnetlabs.bgpsym01.route.PeerRelation;
import nl.nlnetlabs.bgpsym01.route.PolicyImplRel;

import com.thoughtworks.xstream.XStream;

class Relation {
    int as1;
    int as2;
    int rel;
}

public class XStreamGeneratorCaida {
    private static final String DEV_SHM_NODES_XML = "/dev/shm/nodes_outputTransitClique10k_2m";

    private static final String FILE_NAME = "/home/shaza/outputTransitClique10k.txt";
    // private static final String FILE_NAME =
    // "/home/wojciech/work/caida/as-input-20080107";

    // everything above it is route monitor!

    // private int[] procCounts = new int[] { 10, 16, 48 };
    // private int[] procCounts = new int[] { 48, 80 };
    // private int[] procCounts = new int[] { 80 };
    // private int[] procCounts = new int[] { 79 };
    // private int[] procCounts = new int[] { 32 };
    //private int[] procCounts = new int[] { 1, 32, 48, 64, 79 }; //default but then changed by me..
    private int[] procCounts = new int[] { 32, 64 };
    private int procCount;

    private LinkedHashMap<Integer, ArrayList<Relation>> map = new LinkedHashMap<Integer, ArrayList<Relation>>();

    private HashMap<Integer, ASIdentifier> asMap;

    void init(int count) {
        procCount = count;
        asMap = new LinkedHashMap<Integer, ASIdentifier>();
    }

    ASIdentifier getASId(int num, int internal) {
        ASIdentifier asId = new ASIdentifier("AS" + num, internal);
        asId.setProcessId(internal % procCount);
        asId.setPolicyClass(PolicyImplRel.class);
        if (num < 70000) {
            asId.setType(ASType.NORMAL);
        } else if (num < 80000) {
            asId.setType(ASType.ROUTEVIEW);
        } else {
            asId.setType(ASType.RIS);
        }
        return asId;
    }

    private void run(String[] args) throws IOException {
        createMap();

        for (int i : procCounts) {
            init(i);
            ArrayList<ASIdentifier> ases = getAses();

            ArrayList<XNode> nodes = getNodes();

            XSystem xSystem = new XSystem();
            xSystem.setAses(ases);
            xSystem.setNodes(nodes);

            int[] ar = new int[procCount];

            for (XNode node : xSystem.getNodes()) {
                ar[node.getAsIdentifier().getProcessId()] += node.getNeighbors().size();
            }
            for (int j = 0; j < ar.length; j++) {
                System.out.println(j + ": " + ar[j]);
            }

            writeToFile(xSystem);
        }

    }

    private ArrayList<XNode> getNodes() {
        ArrayList<XNode> nodes = new ArrayList<XNode>();

        for (Map.Entry<Integer, ArrayList<Relation>> entry : map.entrySet()) {
            XNode node = new XNode();
            node.setAsIdentifier(asMap.get(entry.getKey()));

            for (Relation relation : entry.getValue()) {
                XNeighbor neighbor = new XNeighbor();
                neighbor.setAttachment(PeerRelation.getByValue(relation.rel));
                ASIdentifier asX = asMap.get(relation.as2);

                if (asX == null) {
                    throw new NullPointerException("as2=" + relation.as2);
                }
                neighbor.setAsIdentifier(asX);
                node.addNeighbor(neighbor);
            }
            nodes.add(node);
        }
        return nodes;
    }

    private void writeToFile(XSystem xSystem) throws IOException {
        XStream xStream = XStreamFactory.getXStream();
        xStream.toXML(xSystem, new FileWriter(DEV_SHM_NODES_XML + "_" + procCount + ".xml"));
    }

    private ArrayList<ASIdentifier> getAses() {
        int asCounter = 0;

        ArrayList<ASIdentifier> ases = new ArrayList<ASIdentifier>();

        for (Integer as : map.keySet()) {

            ASIdentifier asId = getASId(as, asCounter);
            asMap.put(as, asId);
            asCounter++;
            ases.add(asId);
        }

        return ases;
    }

    private void createMap() throws FileNotFoundException {
        FileReader fr = new FileReader(FILE_NAME);
        Scanner sc = new Scanner(fr);

        while (sc.hasNext()) {
            Relation rel = new Relation();
            while (!sc.hasNextInt() && sc.hasNext()) {
                // we want to handle lines like: "rrc: rcc09\n"
                System.out.println(sc.next());
                System.out.println(sc.next());
            }
            rel.as1 = sc.nextInt();
            rel.as2 = sc.nextInt();
            rel.rel = sc.nextInt();

            getAS(rel.as1).add(rel);
        }
    }

    private ArrayList<Relation> getAS(int as) {
        ArrayList<Relation> list = map.get(as);
        if (list == null) {
            list = new ArrayList<Relation>();
            map.put(as, list);
        }
        return list;
    }

    public static void main(String[] args) throws IOException {
        new XStreamGeneratorCaida().run(args);
    }

}

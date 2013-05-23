package nl.nlnetlabs.bgpsym01.xstream;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.Annotations;

public class XStreamGeneratorHyperCube {

    int exp = 6;
    int size = 64;

    private XStream getXStream() {
        XStream xstream = new XStream();
        Annotations.configureAliases(xstream, XNode.class);
        Annotations.configureAliases(xstream, XNeighbor.class);
        Annotations.configureAliases(xstream, XRegistry.class);
        Annotations.configureAliases(xstream, ASIdentifier.class);
        Annotations.configureAliases(xstream, XSystem.class);
        return xstream;
    }

    public static void main(String[] args) {
        new XStreamGeneratorHyperCube().run();
    }

    private void run() {

        XSystem xSystem = new XSystem();

        xSystem.setAses(getASes());
        xSystem.setNodes(serializeNodes());

        try {
            getXStream().toXML(xSystem, new FileWriter("/dev/shm/nodes.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private XNeighbor getNeighbor(int i) {
        XNeighbor n = new XNeighbor();
        n.setAsIdentifier(ASFactory.getInstance(i));
        return n;
    }

    private ArrayList<XNode> serializeNodes() {

        ArrayList<XNode> nodes = new ArrayList<XNode>();

        for (int i = 0; i < size; i++) {
            XNode node = new XNode();
            // node.setName(i + "_" + j);
            ASIdentifier asId = ASFactory.getInstance(binary(i));
            node.setAsIdentifier(asId);
            System.out.println(asId);
            nodes.add(node);

            for (int j = 0; j < exp; j++) {
                int tmp = (1 << j) & i;
                if (tmp == 0) {
                    // nth bit in i is 0
                    tmp = i | (1 << j);
                } else {
                    // nth bit in i is 1
                    tmp = i & (~(1 << j));
                }
                node.addNeighbor(getNeighbor(tmp));
            }
            /*                node.addNeighbor(getNeighbor(i, (j + 1) % size));
                            node.addNeighbor(getNeighbor(i, (j - 1 + size) % size));
                            node.addNeighbor(getNeighbor((i + 1) % size, j));
                            node.addNeighbor(getNeighbor((i - 1 + size) % size, j));*/
        }

        return nodes;
    }

    private String binary(int x) {
        int tmp = 1 << (exp - 1);
        StringBuilder out = new StringBuilder();
        while (tmp != 0) {
            out.append((x & tmp) == 0 ? "0" : "1");
            tmp >>= 1;
        }
        return out.toString();
    }

    private ArrayList<ASIdentifier> getASes() {
        ASFactory.init(size);
        ArrayList<ASIdentifier> out = new ArrayList<ASIdentifier>();

        for (int i = 0; i < size; i++) {
            ASIdentifier as = ASFactory.createInstance(binary(i));
            as.setProcessId(i % exp);
            out.add(as);
        }

        return out;
    }

}

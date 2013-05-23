package nl.nlnetlabs.bgpsym01.xstream;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.Annotations;

public class XStreamGeneratorMesh2d {

    int size = 30;

    int processCount = 10;

    private XStream getXStream() {
        XStream xstream = new XStream();
        Annotations.configureAliases(xstream, XNode.class);
        Annotations.configureAliases(xstream, XNeighbor.class);
        Annotations.configureAliases(xstream, XRegistry.class);
        Annotations.configureAliases(xstream, ASIdentifier.class);
        Annotations.configureAliases(xstream, XSystem.class);
        return xstream;
    }

    private void run() {

        ASFactory.init(size * size);
        XSystem xSystem = new XSystem();

        xSystem.setAses(getASes());
        xSystem.setNodes(serializeNodes());
        serializeRegistries();

        try {
            getXStream().toXML(xSystem, new FileWriter("/dev/shm/nodes_mesh_10.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<ASIdentifier> getASes() {
        ArrayList<ASIdentifier> out = new ArrayList<ASIdentifier>();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                ASIdentifier as = ASFactory.createInstance(i + "_" + j);
                as.setProcessId(i % processCount);
                out.add(as);
            }
        }
        return out;
    }

    protected void serializeRegistries() {

        ArrayList<XRegistry> registries = new ArrayList<XRegistry>();
        for (int i = 0; i < size; i++) {
            registries.add(new XRegistry("localhost", 1100 + i));
        }

        XStream xStream = getXStream();
        try {
            xStream.toXML(registries, new FileWriter("/dev/shm/registries.xml"));
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

    }

    private XNeighbor getNeighbor(int i, int j) {
        String name = i + "_" + j;
        XNeighbor n = new XNeighbor();
        n.setAsIdentifier(ASFactory.getInstance(name));
        return n;
    }

    protected ArrayList<XNode> serializeNodes() {

        ArrayList<XNode> nodes = new ArrayList<XNode>();

        for (int i = 0; i < size; i++) {
            // ArrayList<XNode> myNodes = new ArrayList<XNode>();
            // nodes.add(myNodes);
            for (int j = 0; j < size; j++) {
                XNode node = new XNode();
                // node.setName(i + "_" + j);
                node.setAsIdentifier(ASFactory.getInstance(i + "_" + j));
                nodes.add(node);

                node.addNeighbor(getNeighbor(i, (j + 1) % size));
                node.addNeighbor(getNeighbor(i, (j - 1 + size) % size));
                node.addNeighbor(getNeighbor((i + 1) % size, j));
                node.addNeighbor(getNeighbor((i - 1 + size) % size, j));
            }
        }

        return nodes;
    }

    public static void main(String[] args) {
        new XStreamGeneratorMesh2d().run();
    }
}

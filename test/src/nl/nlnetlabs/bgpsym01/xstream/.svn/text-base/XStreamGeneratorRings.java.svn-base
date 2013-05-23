package nl.nlnetlabs.bgpsym01.xstream;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;
import nl.nlnetlabs.bgpsym01.route.PolicyImpl;
import nl.nlnetlabs.bgpsym01.route.PolicyImplRing;

import com.thoughtworks.xstream.XStream;

public class XStreamGeneratorRings extends TestCase {

    private XStream xStream = XStreamFactory.getXStream();

    private static final int PCOUNT = 60;
    int size = 30;

    private void generate() {
        ASFactory.init(size + size * size + size * size * size);
        XSystem xSystem = new XSystem();

        xSystem.setAses(getASes());
        xSystem.setNodes(serializeNodes());

        try {
            xStream.toXML(xSystem, new FileWriter("/dev/shm/nodes.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private XNeighbor getNeighbor(int level, int num) {
        XNeighbor xn = new XNeighbor();
        num = getNeighborNumber(level, num);
        xn.setAsIdentifier(ASFactory.getInstance(num));
        return xn;
    }

    private int getNeighborNumber(int level, int num) {
        if (level == 0) {
            num = (num + size) % size; // num
            // %=
            // size
        } else if (level == 1) {
            num -= size;
            num = (num + size * size) % (size * size); // num
            // %=
            // size**2
            num += size;
        } else if (level == 2) {
            num -= size * size + size;
            num = (num + size * size * size) % (size * size * size); // num
            // %=
            // size**3
            num += size * size + size;
        }
        return num;
    }

    public void testGetNeighborNumber() {

        assertEquals(getNeighborNumber(0, -1), size - 1);
        assertEquals(getNeighborNumber(0, 2), 2);

        // assertEquals(getNeighborNumber(1, 10), 10);
        // assertEquals(getNeighborNumber(1, 109), 109);
        // assertEquals(getNeighborNumber(1, 110), 10);
        assertEquals(getNeighborNumber(1, size - 1), size + size * size - 1);
        assertEquals(getNeighborNumber(1, size + 12), size + 12);

        // assertEquals(getNeighborNumber(2, 110), 110);

        assertEquals(getNeighborNumber(2, size + size * size + size * size * size), size + size * size);
        assertEquals(getNeighborNumber(2, size + size * size - 1), size + size * size + size * size * size - 1);
    }

    private ArrayList<XNode> serializeNodes() {
        // main ring
        ArrayList<XNode> nodes = new ArrayList<XNode>();
        for (int i = 0; i < size; i++) {
            XNode node = new XNode();
            ASIdentifier asId = ASFactory.getInstance("r0_" + i);

            // immediate neighbor
            node.setAsIdentifier(asId);
            node.addNeighbor(getNeighbor(0, i - 1));
            node.addNeighbor(getNeighbor(0, i + 1));

            /* neighbor down
             * if i'm i = 3 and size is 10 then my neighbors vary are <40, 49> (<3 * size + size, 3 * size + size + size>)
             */
            for (int j = size + i * size; j < size + (i + 1) * size; j++) {
                node.addNeighbor(getNeighbor(1, j));
            }
            nodes.add(node);
        }

        // middle ring
        for (int i = 0; i < size * size; i++) {
            int shift = size + size * size;

            XNode node = new XNode();
            ASIdentifier asId = ASFactory.getInstance("r1_" + i);
            node.setAsIdentifier(asId);
            // my neighbors in the ring
            node.addNeighbor(getNeighbor(1, shift + i - 1));
            node.addNeighbor(getNeighbor(1, shift + i + 1));

            // my parent
            node.addNeighbor(getNeighbor(0, i / size));

            // sons
            // if i'm i = 3 and size is 10 then my neighbors are <shift + i *
            // size ^ 2, shift + i * (size ^ 2 + 1)>
            for (int j = shift + i * size; j < shift + (i + 1) * size; j++) {
                node.addNeighbor(getNeighbor(2, j));
            }
            nodes.add(node);
        }

        // outer ring
        for (int i = 0; i < size * size * size; i++) {

            int shift = size + size * size;

            XNode node = new XNode();
            ASIdentifier asId = ASFactory.getInstance("r2_" + i);
            node.setAsIdentifier(asId);
            // in the ring
            node.addNeighbor(getNeighbor(2, shift + i - 1));
            node.addNeighbor(getNeighbor(2, shift + i + 1));

            // parent
            node.addNeighbor(getNeighbor(1, size + i / size));

            nodes.add(node);
        }
        return nodes;
    }

    private ArrayList<ASIdentifier> getASes() {
        int count = 0;
        ArrayList<ASIdentifier> ases = new ArrayList<ASIdentifier>();
        for (int i = 0; i < size; i++) {
            ASIdentifier asId = ASFactory.createInstance("r0_" + i);
            asId.setAttachment(0);
            asId.setPolicyClass(PolicyImplRing.class);
            asId.setProcessId(count++ % PCOUNT);
            ases.add(asId);
        }

        for (int i = 0; i < size * size; i++) {
            ASIdentifier asId = ASFactory.createInstance("r1_" + i);
            asId.setAttachment(1);
            asId.setPolicyClass(PolicyImpl.class);
            asId.setProcessId(count++ % PCOUNT);
            ases.add(asId);
        }

        for (int i = 0; i < size * size * size; i++) {
            ASIdentifier asId = ASFactory.createInstance("r2_" + i);
            asId.setAttachment(2);
            asId.setPolicyClass(PolicyImpl.class);
            asId.setProcessId(count++ % PCOUNT);
            ases.add(asId);
        }
        return ases;
    }

    public static void main(String[] args) {
        new XStreamGeneratorRings().generate();
    }

}

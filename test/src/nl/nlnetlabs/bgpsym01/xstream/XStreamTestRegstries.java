package nl.nlnetlabs.bgpsym01.xstream;

import junit.framework.TestCase;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

public class XStreamTestRegstries extends TestCase {

    private static Logger log = Logger.getLogger(XStreamTestRegstries.class);

    public void testRegistries() {
        XStream xStream = XStreamFactory.getXStream();
        XComputeNodes computeNodes = new XComputeNodes();

        XRegistry r1 = new XRegistry("host1", 0);
        XRegistry r2 = new XRegistry("host2", 2);
        XRegistry c = new XRegistry("host2", 1);

        computeNodes.addXRegistry(r1);
        computeNodes.addXRegistry(r2);

        computeNodes.setCoordinator(c);

        if (log.isInfoEnabled()) {
            log.info("xml: " + xStream.toXML(computeNodes));
        }

        String x = "<dasNodes><coordinator><host>node040.beowulf.cluster</host><port>10100</port></coordinator> <registries> <registry><host>node040.beowulf.cluster</host><port>10099</port></registry> <registry><host>node058.beowulf.cluster</host><port>10099</port></registry> <registry><host>node011.beowulf.cluster</host><port>10099</port></registry> <registry><host>node042.beowulf.cluster</host><port>10099</port></registry> <registry><host>node049.beowulf.cluster</host><port>10099</port></registry> <registry><host>node077.beowulf.cluster</host><port>10099</port></registry> <registry><host>node023.beowulf.cluster</host><port>10099</port></registry> <registry><host>node064.beowulf.cluster</host><port>10099</port></registry> <registry><host>node083.beowulf.cluster</host><port>10099</port></registry> <registry><host>node048.beowulf.cluster</host><port>10099</port></registry> <registry><host>node002.beowulf.cluster</host><port>10099</port></registry> <registry><host>node026.beowulf.cluster</host><port>10099</port></registry> <registry><host>node082.beowulf.cluster</host><port>10099</port></registry> <registry><host>node015.beowulf.cluster</host><port>10099</port></registry> <registry><host>node022.beowulf.cluster</host><port>10099</port></registry> <registry><host>node073.beowulf.cluster</host><port>10099</port></registry> <registry><host>node039.beowulf.cluster</host><port>10099</port></registry> <registry><host>node013.beowulf.cluster</host><port>10099</port></registry> <registry><host>node053.beowulf.cluster</host><port>10099</port></registry> <registry><host>node078.beowulf.cluster</host><port>10099</port></registry> <registry><host>node059.beowulf.cluster</host><port>10099</port></registry> <registry><host>node041.beowulf.cluster</host><port>10099</port></registry> <registry><host>node079.beowulf.cluster</host><port>10099</port></registry> <registry><host>node018.beowulf.cluster</host><port>10099</port></registry> <registry><host>node060.beowulf.cluster</host><port>10099</port></registry> <registry><host>node070.beowulf.cluster</host><port>10099</port></registry> <registry><host>node007.beowulf.cluster</host><port>10099</port></registry> <registry><host>node025.beowulf.cluster</host><port>10099</port></registry> <registry><host>node032.beowulf.cluster</host><port>10099</port></registry> <registry><host>node031.beowulf.cluster</host><port>10099</port></registry> <registry><host>node061.beowulf.cluster</host><port>10099</port></registry> <registry><host>node030.beowulf.cluster</host><port>10099</port></registry> <registry><host>node001.beowulf.cluster</host><port>10099</port></registry> </registries></dasNodes>";

        XComputeNodes nodes = (XComputeNodes) xStream.fromXML(x);
        if (log.isInfoEnabled()) {
            log.info(nodes.getRegistries());
        }

    }

}

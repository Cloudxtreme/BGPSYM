package nl.nlnetlabs.bgpsym01.coordinator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.nlnetlabs.bgpsym01.coordinator.events.framework.DataMeasurementImpl;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventBackendXStreamImpl;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventProcessor;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventStreamImpl;
import nl.nlnetlabs.bgpsym01.coordinator.helpers.CommandSenderHelper;
import nl.nlnetlabs.bgpsym01.coordinator.helpers.CommandSenderHelperImpl;
import nl.nlnetlabs.bgpsym01.coordinator.helpers.DisconnectHelper;
import nl.nlnetlabs.bgpsym01.coordinator.helpers.EventsSenderHelper;
import nl.nlnetlabs.bgpsym01.coordinator.helpers.PropagationHelper;
import nl.nlnetlabs.bgpsym01.coordinator.helpers.PropagationHelperImpl;
import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;
import nl.nlnetlabs.bgpsym01.xstream.XComputeNodes;
import nl.nlnetlabs.bgpsym01.xstream.XPrefix;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;
import nl.nlnetlabs.bgpsym01.xstream.XRegistry;
import nl.nlnetlabs.bgpsym01.xstream.XSystem;

import org.apache.log4j.Logger;

public class CoordinatorMain {

    private static final String COORDINATOR = "coordinator";

    private static Logger log = Logger.getLogger(CoordinatorMain.class);

    private Coordinator coordinator;

    private RunMonitor runMonitor;

    public CoordinatorMain() {
    }

    private void init(String[] args) {
        /*
         * 1. create your socket
         * 2. read config info so that you know what you're standing on
         * 3. listen for connection from the guys
         * 4. wait till guys say their ready
         * 5. ... 
         */

        try {
            // String workingDir = null;
            String registryFile = null;
            String propertiesFile = null;
            try {
                Thread.currentThread().setName(COORDINATOR);
                registryFile = args[0];
                // workingDir = args[1];
                propertiesFile = args[1];
            } catch (RuntimeException e2) {
                System.err.println("parameters parsing error");
                e2.printStackTrace(System.err);
                System.exit(1);
            }

            loadProperties(propertiesFile);
            runMonitor = new RunMonitor();
            runMonitor.start();

            Tools tools = Tools.getInstance();
            XComputeNodes computeNodes = loadRegistries(registryFile);

            coordinator = new Coordinator();
            coordinator.setRunMonitor(runMonitor);
            coordinator.setRegistries(computeNodes.getRegistries());

            tools.setProcNum(-1);

            Communicator communicator = new CommunicatorImpl(coordinator, computeNodes.getRegistries(), computeNodes.getCoordinator().getPort());
            coordinator.setCommunicator(communicator);

            if (log.isInfoEnabled()) {
                log.info("socket created set");
            }

            XSystem system = loadNodes();
            Prefix.init(XProperties.getInstance().getPrefixArraySize());
            // coordinator.setAses(system.getAses());

            CommandSenderHelperImpl commandSenderHelper = new CommandSenderHelperImpl();
            commandSenderHelper.setAses(system.getAses());
            commandSenderHelper.setCommunicator(communicator);
            commandSenderHelper.setRegistries(computeNodes.getRegistries());
            commandSenderHelper.setCommunicator(communicator);
            coordinator.setCommandSenderHelper(commandSenderHelper);

            DisconnectHelper disconnectHelper = new DisconnectHelper();
            disconnectHelper.setNodes(system.getNodes());
            disconnectHelper.setCommunicator(communicator);
            coordinator.setDisconnectHelper(disconnectHelper);

            coordinator.setPropagationHelper(getPropagationHelper(commandSenderHelper, disconnectHelper));

            tools.createDiagFile();

            coordinator.controlTheGame();

        } catch (IOException e) {
            log.error("", e);
            throw new BGPSymException(e);
        }

    }

    // its public only for tests...
    public PropagationHelper getPropagationHelper(CommandSenderHelper commandSenderHelper, DisconnectHelper disconnectHelper) {
        XProperties properties = XProperties.getInstance();

        if (properties.isUseEventsFile()) {
            EventsSenderHelper helper = new EventsSenderHelper();
            EventProcessor processor = new EventProcessor();
            processor.setDataMeasurement(new DataMeasurementImpl());
            EventStreamImpl stream = new EventStreamImpl();
            EventBackendXStreamImpl backend = null;
            try {
                backend = new EventBackendXStreamImpl(new FileReader(properties.getEventsFileName()));
            } catch (FileNotFoundException e) {
                log.error(e);
                throw new BGPSymException(e);
            } catch (IOException e) {
                log.error(e);
                throw new BGPSymException(e);
            }
            stream.setBackend(backend);
            processor.setEventStream(stream);
            helper.setProcessor(processor);
            stream.setCommandSenderHelper(commandSenderHelper);
            stream.setDisconnectHelper(disconnectHelper);
            return helper;
        } else {
            PropagationHelperImpl propagationHelper = new PropagationHelperImpl();
            propagationHelper.setCommandSenderHelper(commandSenderHelper);
            propagationHelper.setPrefixAggregationSize(properties.getPrefixAggregationSize());
            if (XProperties.getInstance().hasPrefixFile()) {
                List<XPrefix> prefixes = loadPrefixesFromFile();
                propagationHelper.setPrefixes(prefixes);
            }
            return propagationHelper;
        }
    }

    @SuppressWarnings("unchecked")
    private List<XPrefix> loadPrefixesFromFile() {
        String prefixesFile = XProperties.getInstance().getPrefixesFileName();
        List<XPrefix> prefixes;
        try {
            prefixes = (List<XPrefix>) XStreamFactory.getXStream().fromXML(new FileInputStream(prefixesFile));
        } catch (FileNotFoundException e) {
            log.error(e);
            throw new BGPSymException(e);
        }
        return prefixes;
    }

    private void loadProperties(String propertiesFile) throws FileNotFoundException {
        XProperties properties = (XProperties) XStreamFactory.getXStream().fromXML(new FileReader(propertiesFile));
        XProperties.setInstance(properties);
    }

    private XComputeNodes loadRegistries(String registryFile) throws FileNotFoundException {
        XComputeNodes computeNodes = (XComputeNodes) XStreamFactory.getXStream().fromXML(new FileReader(registryFile));
        ArrayList<XRegistry> registries = computeNodes.getRegistries();
        if (registries.size() != XProperties.getInstance().hostCount) {
            log.error("registries.size() == " + registries.size() + ", hostCount=" + XProperties.getInstance().hostCount);
            System.exit(1);
        }
        // coordinator = computeNodes.getCoordinator();
        return computeNodes;
    }

    private XSystem loadNodes() throws FileNotFoundException {
        XSystem xSystem = (XSystem) XStreamFactory.getXStream().fromXML(new FileReader(XProperties.getInstance().getNodesFileName()));
        ArrayList<ASIdentifier> ases = xSystem.getAses();
        ASFactory.init(ases.size());
        for (ASIdentifier as : xSystem.getAses()) {
            ASFactory.registerAS(as, as.getInternalId());
        }
        return xSystem;
    }

    public static void main(String[] args) {
        new CoordinatorMain().init(args);
    }

}

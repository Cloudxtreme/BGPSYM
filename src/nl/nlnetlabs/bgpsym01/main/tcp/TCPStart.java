package nl.nlnetlabs.bgpsym01.main.tcp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import nl.nlnetlabs.bgpsym01.cache.ResultWriterLog;
import nl.nlnetlabs.bgpsym01.callback.Callback;
import nl.nlnetlabs.bgpsym01.command.AckCommand;
import nl.nlnetlabs.bgpsym01.command.AnnounceCommand;
import nl.nlnetlabs.bgpsym01.command.SetRegistryCommand;
import nl.nlnetlabs.bgpsym01.main.StorageCompressor;
import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbors;
import nl.nlnetlabs.bgpsym01.neighbor.impl.NeighborImplTCP;
import nl.nlnetlabs.bgpsym01.neighbor.impl.TCPConnection;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.DiagnosticThread;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.factories.CallbackFactory;
import nl.nlnetlabs.bgpsym01.primitives.factories.PrefixStoreFactory;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;
import nl.nlnetlabs.bgpsym01.primitives.mocks.MRAITimerMock;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapStoreImpl;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeController;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.IBGPModelImpl;
import nl.nlnetlabs.bgpsym01.primitives.types.MessageInputGenerator;
import nl.nlnetlabs.bgpsym01.primitives.types.MessageQueue;
import nl.nlnetlabs.bgpsym01.primitives.types.MessageQueueImpl;
import nl.nlnetlabs.bgpsym01.primitives.types.StaticThread;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;
import nl.nlnetlabs.bgpsym01.route.MRAIStoreImpl;
import nl.nlnetlabs.bgpsym01.route.MRAITimer;
import nl.nlnetlabs.bgpsym01.route.MRAITimerImpl;
import nl.nlnetlabs.bgpsym01.route.Policy;
import nl.nlnetlabs.bgpsym01.xstream.XComputeNodes;
import nl.nlnetlabs.bgpsym01.xstream.XNeighbor;
import nl.nlnetlabs.bgpsym01.xstream.XNode;
import nl.nlnetlabs.bgpsym01.xstream.XPrefix;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;
import nl.nlnetlabs.bgpsym01.xstream.XRegistry;
import nl.nlnetlabs.bgpsym01.xstream.XSystem;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

/**
 * Abstract starting class reading configuration from XStream config files
 * 
 */
public class TCPStart {

    private static Logger log = Logger.getLogger(TCPStart.class);

    private static Random random = new Random(System.currentTimeMillis() * 19 + Tools.getInstance().getProcNum());

    private String registryFile;
    private String propertiesFile;
    private int myNum;

    private ArrayList<XRegistry> registries;

    private ArrayList<XNode> nodes;
    
    private List<XPrefix> prefixes;

    private Map<ASIdentifier, BGPProcess> processes = new LinkedHashMap<ASIdentifier, BGPProcess>();

    private XStream xStream;

    private ServerSocketThread serverSocketThread;

    private CommunicationSocketThread cst;

    private boolean gotAck;

    private OverloadMonitor monitor;

    private XRegistry coordinator;

    private XProperties properties;
    
    public int prefixAggregationSize = 1;
    
    /*
     *  how many prefix do we require in one message to double the sleeping time
     */
    private int prefixAggreagationSleeperMultiplier = 6;

    public TCPStart() {
        xStream = XStreamFactory.getXStream();
    }

    private void run(String[] args) {

        setAndLoad(args);

        // First I have to open my own socket
        registerMyself(myNum);

        initDiskCacheDir();

        // load prefixes from file
        if (properties.prefixStartingPoint != 0) {
            loadCompressedStorage();
        }

        // now connect to the coordinator
        registerCoordinator();

        synchronized (this) {
            while (!gotAck) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }

        registerToOthers();

        // help GC :)
        nodes = null;
        xStream = null;
        
        if (properties.hasPrefixFile()) {
        	loadOurPrefixes();
        	registerPrefixes();
        }       

        // send ack to coordinator - he will start sending us repropagation
        // commands
        AckCommand ack = new AckCommand();
        cst.sendCommand(ack);

        // Now I can already start diagnostic thread
        DiagnosticThread.init(processes.values(), cst);

        for (BGPProcess process : processes.values()) {
            try {
                process.join();
            } catch (InterruptedException e) {
                // nothing to do here
                log.error(e);
            }
        }
    }

    private void loadCompressedStorage() {
        new StorageCompressor().loadCompressedStorage(myNum);
    }

    private void initDiskCacheDir() {
        // we don't want to delete the files, because there might be more JVM's
        // running

        File f = new File(XProperties.getInstance().getDiskCacheDir());
        if (f.exists()) {
            /*            if (log.isDebugEnabled()) {
                            log.debug("directory " + f + " already exists, deleting files inside");
                        }
                        for (File inside : f.listFiles()) {
                            if (!inside.delete()) {
                                log.warn("unable to delete file " + inside);
                            }
                        }*/
        } else {
            f.mkdirs();
            if (!f.exists()) {
                String msg;
                try {
                    msg = "unable to create directory " + f + ", host=" + InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    log.error(e);
                    throw new BGPSymException(e);
                }
                log.error(msg);
                throw new BGPSymException(msg);
            }
            if (log.isInfoEnabled()) {
                // log.info("creating directory " + f + " DONE");
            }
        }
    }

    public synchronized void gotAck() {
        gotAck = true;
        notify();
    }

    /**
     * Load data from files and set everything so that it's ready for running.
     * 
     * @param args
     */
    private void setAndLoad(String[] args) {
        myNum = 0;
        registryFile = null;
        // workingDir = null;
        try {
            myNum = Integer.parseInt(args[0]);
            Tools.getInstance().setProcNum(myNum);
            Thread.currentThread().setName("main_" + myNum);
            registryFile = args[1];
            // workingDir = args[2];
            propertiesFile = args[2];
        } catch (RuntimeException e2) {
            System.err.println("parameters parsing error");
            e2.printStackTrace(System.err);
            System.exit(1);
        }

        changeLog4jFile();

        try {
            loadProperties();
            loadRegistries();
            if (log.isDebugEnabled()) {
                log.debug("myNum=" + myNum + ", regs[myNum]=" + registries.get(myNum).getHost());
            }
            loadNodes();
            Prefix.init(properties.getPrefixArraySize());
            if (properties.hasPrefixFile()) {
            	loadPrefixesFromFile();
            }
        } catch (FileNotFoundException e) {
            throw new BGPSymException(e);
        }
    }

    private void changeLog4jFile() {
        try {
            FileAppender fileAppender = (FileAppender) Logger.getRootLogger().getAppender("A2");
            fileAppender.setFile(fileAppender.getFile() + "_" + myNum);
            fileAppender.activateOptions();
        } catch (RuntimeException e1) {
            log.error("unable to change appenders", e1);
            e1.printStackTrace(System.err);
        }
    }

    private void loadProperties() throws FileNotFoundException {
		//File xmlFile = new File(propertiesFile);

		//properties = (XProperties) XStreamFactory.getXStream().fromXML(new FileInputStream(xmlFile));
        properties = (XProperties) XStreamFactory.getXStream().fromXML(new FileReader(propertiesFile));
        XProperties.setInstance(properties);
    }

    private void registerMyself(int myNum) {

        XRegistry registry = registries.get(myNum);

        serverSocketThread = new ServerSocketThread(registry.getPort());
        serverSocketThread.setName("sst_" + myNum);
        serverSocketThread.setDaemon(true);

        for (XNode node : nodes) {
            if (node.getAsIdentifier().getProcessId() != myNum) {
                continue;
            }

            Callback callback = CallbackFactory.getCallback(node.getAsIdentifier());
            BGPProcess process = new BGPProcess(callback);

            processes.put(node.getAsIdentifier(), process);
			process.setRegistries(registries);

            node.getAsIdentifier().setProcess(process);
            process.setAsIdentifier(node.getAsIdentifier());
			process.setResultWriterLog(new ResultWriterLog(process.getASIdentifier()));
        }
    }

    private MessageInputGenerator getMessageInputGenerator(MessageQueue queue, ASIdentifier asId, int asSize) {
        MessageInputGenerator messageInputGenerator = new MessageInputGenerator();
        messageInputGenerator.setMessageQueue(queue);
        messageInputGenerator.setAsId(asId);
        messageInputGenerator.setTimeController(getTimeController());
        // messageInputGenerator.setIBGPConvergenceTime(1000);
        messageInputGenerator.setIBGPmodel(new IBGPModelImpl(properties.iBgpMaxValue, properties.iBgpMaxNeighbors, asSize));
        messageInputGenerator.setMonitor(monitor);
        return messageInputGenerator;
    }

    private TimeController getTimeController() {
        return TimeControllerFactory.getTimeController();
    }

    private void registerToOthers() {

        // ArrayList<XNode> myNodes = nodes.get(myNum);
        if (nodes == null || nodes.size() == 0) {
            return;
        }

        for (XNode node : nodes) {

            if (node.getAsIdentifier().getProcessId() != myNum) {
                continue;
            }

            boolean hasRealMRAITimer = random.nextInt(100) <= properties.mraiProc;

            Neighbors neighbors = new Neighbors(node.getAsIdentifier());

            BGPProcess process = processes.get(node.getAsIdentifier());

            ArrayList<XNeighbor> xNeighbors = node.getNeighbors();
            for (XNeighbor xN : xNeighbors) {
                ASIdentifier id = xN.getAsIdentifier();

                XRegistry xRegistry = registries.get(id.getProcessId());

                if (xN.getRealNeighbor() == null) {

                    TCPConnection connection = (TCPConnection) xRegistry.getAttachment();
                    if (connection == null) {
                        connection = new TCPConnection(xRegistry.getHost(), xRegistry.getPort(), serverSocketThread, monitor);
                        xRegistry.setAttachment(connection);
                    }

                    NeighborImplTCP n = new NeighborImplTCP(id, connection);

                    n.setTimer(getMRAITimer(n, hasRealMRAITimer));

                    // copy the received attachment
                    n.setAttachment(xN.getAttachment());
                    xN.setRealNeighbor(n);
                }

                neighbors.addNeighbor(xN.getRealNeighbor());
            }

            Policy policy = null;
            try {
                if (node.getAsIdentifier().getPolicyClass() != null) {
                    policy = node.getAsIdentifier().getPolicyClass().newInstance();
                }
            } catch (Exception e) {
                throw new BGPSymException(e);
            }

            // MessageQueueOld queue = new MessageQueueOld();
            MessageQueueImpl queue = new MessageQueueImpl();
            //queue.setTimeController(TimeControllerFactory.getTimeController())
            // ;
            // queue.setIbgpModel(new IBGPModelImpl(properties.iBgpMaxValue,
            // properties.iBgpMaxNeighbors, node.getNeighbors().size()));
            assert node.getNeighbors().size() != 0;
            process.setMessageQueue(queue);
            queue.setTimeController(getTimeController());
            MessageInputGenerator messageInputGenerator = getMessageInputGenerator(queue, node.getAsIdentifier(), neighbors.size());
            process.setQueue(messageInputGenerator);
            MRAIStoreImpl mraiStore = new MRAIStoreImpl();
            mraiStore.setCallback(process.getCallback());
            queue.addInputGenerator(mraiStore);
            FlapStoreImpl flapStoreImpl = new FlapStoreImpl();
            flapStoreImpl.setCallback(process.getCallback());
            flapStoreImpl.setTimeController(getTimeController());
            queue.addInputGenerator(flapStoreImpl);
            queue.addInputGenerator(messageInputGenerator);

            process.setNeighbors(neighbors);
            process.setStore(PrefixStoreFactory.createStore(process.getAsIdentifier(), neighbors, process.getCallback(), policy, mraiStore, flapStoreImpl));
            process.start();
            Thread.yield();
        }

        serverSocketThread.start();
    }

    private MRAITimer getMRAITimer(NeighborImplTCP n, boolean hasRealMRAITimer) {
        if (1 < 2) {
            MRAITimerImpl timer = new MRAITimerImpl();
            int threshhold = hasRealMRAITimer ? 30000 : 0;
            timer.setThreshold(threshhold);
            timer.setAsIdentifier(n.getASIdentifier());
            return timer;
        } else {
            return new MRAITimerMock();
        }
    }

    public static void main(String[] args) {
        new TCPStart().run(args);

    }

    private void registerCoordinator() {
        try {
            /*
             * just create the connection to the coordinator and have it added
             * to the selector
             */

            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);

            assert coordinator != null;
            channel.connect(new InetSocketAddress(coordinator.getHost(), coordinator.getPort()));


            cst = new CommunicationSocketThread(channel, this);
            cst.start();
            cst.send(channel, new SetRegistryCommand(myNum));

            monitor = new OverloadMonitor(cst);

        } catch (IOException e) {
            throw new BGPSymException(e);
        }
    }

    private void loadNodes() throws FileNotFoundException {
        XSystem xSystem = (XSystem) xStream.fromXML(new FileReader(XProperties.getInstance().getNodesFileName()));

        ASFactory.init(xSystem.getAses().size());
        for (ASIdentifier as : xSystem.getAses()) {
            ASFactory.registerAS(as, as.getInternalId());
        }

        nodes = xSystem.getNodes();
    }

    private void loadRegistries() throws FileNotFoundException {
        XComputeNodes registriesNodes = (XComputeNodes) xStream.fromXML(new FileReader(registryFile));
        registries = registriesNodes.getRegistries();
        coordinator = registriesNodes.getCoordinator();
        if (registries.size() != XProperties.getInstance().hostCount) {
            log.error("registries.size() == " + registries.size() + ", hostCount=" + XProperties.getInstance().hostCount);
            System.exit(1);
        }
    }
    
    @SuppressWarnings("unchecked")
	private void loadPrefixesFromFile() {
    	String prefixesFile = properties.getPrefixesFileName();
    	
    	List<XPrefix> prefixes;
    	
    	 try {
             prefixes = (List<XPrefix>) XStreamFactory.getXStream().fromXML(new FileInputStream(prefixesFile));
         } catch (FileNotFoundException e) {
             log.error(e);
             throw new BGPSymException(e);
         }
    	 
    	 this.prefixes = prefixes;
    }
    
    private void loadOurPrefixes() {
        ArrayList<Integer> internalIds = new ArrayList<Integer>();
        
        Iterator<ASIdentifier> iterator = processes.keySet().iterator();
        while (iterator.hasNext()) {
        	internalIds.add(iterator.next().getInternalId());
        }
     
        List<XPrefix> ourPrefixes = new ArrayList<XPrefix>();
       
        log.info("total internalids: "+internalIds.size()+" loaded "+prefixes.size()+" prefixes from file");
        
        for (XPrefix xPrefix : prefixes) {
        	if (internalIds.contains(Integer.valueOf(xPrefix.getAsInternalId()))) {
        		ourPrefixes.add(xPrefix);
        	}
        }
        
        log.info("This slave has "+ourPrefixes.size()+" prefixes");
        
        this.prefixes = ourPrefixes;
    }
    
    private BGPUpdate getUpdate (List<Prefix> prefixList, ASIdentifier asId) {
    	BGPUpdate u = new BGPUpdate();
        u.setPrefixes(prefixList);
        Route route = new Route();
        route.createEmptyHops();
        u.setRoute(route);
        u.setSender(asId);
        
        return u;
    }
    
    private void registerPrefixes() {
    	long sleepingTime = properties.sleepingTime;
    	
    	Map<ASIdentifier, ArrayList<XPrefix>> prefixesMap = generatePrefixMap();
    	
    	List<Prefix> prefixList = new ArrayList<Prefix>(prefixAggregationSize);
    	
    	for (Map.Entry<ASIdentifier, ArrayList<XPrefix>> entry : prefixesMap.entrySet()) {
    		ASIdentifier asId = entry.getKey();
    		Iterator<XPrefix> iterator = entry.getValue().iterator();
            XPrefix last = null;
            while (iterator.hasNext()) {
            	last = iterator.next();
            	prefixList.add(Prefix.getInstance(last.getPrefixNum()));
            	
            	if (prefixList.size() == prefixAggregationSize || !iterator.hasNext()) {
            		asId.getProcess().getQueue().addMessage(getUpdate(prefixList, asId));
            		
            		long sleepTime = (long) (sleepingTime * getSleepingTimeMultiplier(prefixList));
            		StaticThread.sleep(sleepTime);
            	}
            }    		
    	}
    }
    
    /**
     * Generate prefixes map - we can send prefixes from one neighbor together
     * 
     * @return map of prefixes {@link ASIdentifier} -> {@link ArrayList}
     */
    private Map<ASIdentifier, ArrayList<XPrefix>> generatePrefixMap() {
        int count = properties.prefixStartingPoint;

        Map<ASIdentifier, ArrayList<XPrefix>> prefixesMap = new LinkedHashMap<ASIdentifier, ArrayList<XPrefix>>();

        // skip first X prefixes
        Iterator<XPrefix> iterator = prefixes.iterator();
        for (int i = 0; i < properties.prefixStartingPoint; i++) {
            iterator.next();
        }

        while (iterator.hasNext() && count < properties.prefixCount) {
            XPrefix prefix = iterator.next();

            ASIdentifier asId = ASFactory.getInstance(prefix.getAsInternalId());
            ArrayList<XPrefix> list = prefixesMap.get(asId);
            if (list == null) {
                list = new ArrayList<XPrefix>();
                prefixesMap.put(asId, list);
            }
            list.add(prefix);
            count++;
        }
        
        return prefixesMap;
    }

    public int getMyNum() {
        return myNum;
    }

    public Map<ASIdentifier, BGPProcess> getProcesses() {
        return processes;
    }

    public ServerSocketThread getServerSocketThread() {
        return serverSocketThread;
    }

    public CommunicationSocketThread getCst() {
        return cst;
    }

    public void setProcesses(Map<ASIdentifier, BGPProcess> processes) {
        this.processes = processes;
    }
    
    private double getSleepingTimeMultiplier(List<Prefix> prefixList) {
        return (1 + ((double) (prefixList.size() - 1) / (double) prefixAggreagationSleeperMultiplier));
    }

}

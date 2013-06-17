package nl.nlnetlabs.bgpsym01.xstream;

import java.io.File;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("properties")
public class XProperties {

    private static final String STORAGE_FILE = "storage_";

    private static final String DIAG_LOG_FILE = "diag.log";

    private static final String LOG_DIR = "log";

    private XProperties() {

    }

    public static void dummyInit() {
        XProperties properties = new XProperties();
        XProperties.setInstance(properties);
    }

    @XStreamOmitField
    private static XProperties instance;

    public int mraiProc;

    public int mraiEmpty;

    public int flapPercentage;

    public int iBgpMaxValue;

    public int iBgpMaxNeighbors;

    public boolean iBGPLog;

    public int flapDistribution;

    public int policyMulti;

    public int policyMultiPref;

    public int hostCount;

    public long sleepingTime;

    public String workingDir;

    public int prefixStartingPoint;

    private boolean useInputHandlerThread;

    public int prefixCount;

    private int prefixArraySize;

    private String storageDir;

    public int prefixCacheSize;

    private String diskCacheDir;

    private String nodesFileName = "nodes.xml";

    private String prefixesFile;

    private boolean useEventsFile;

    private String eventsFileName;

    private int prefixAggregationSize;

    public long diagnosticThreadSleep;

    private int createdLoggingInterval;

    public int bogusPrefixMin;

    public int timeScaler = 25;

    private String resultDirectory;

    private int introducedLoggingInterval;

    // default is false
    private boolean neighborsContainerCaching;

    private boolean useNeighborsMap;

    public long noiseSleepTime;
    
    public int maxPrefixes;

    public static XProperties getInstance() {
        return instance;
    }

    public static void setInstance(XProperties properties) {
        instance = properties;
    }

    public String getNodesFileName() {
        return workingDir + File.separator + nodesFileName;
    }

    public String getStorageOutName() {
        return STORAGE_FILE;
    }

    public String getStorageDir() {
        return storageDir;
    }

    public String getStorageFileName(int prefixCount, int procNum) {
        return getStorageDir() + File.separator + getStorageOutName() + prefixCount + "_" + procNum + ".tar.gz";
    }

    public String getDiskCacheDir() {
        return diskCacheDir;
    }

    public int getPrefixArraySize() {
        return prefixArraySize;
    }

    public String getDiagFile() {
        return workingDir + File.separator + LOG_DIR + File.separator + DIAG_LOG_FILE;
    }

    public void setPrefixArraySize(int prefixArraySize) {
        this.prefixArraySize = prefixArraySize;
    }

    public void setHostCount(int hostCount) {
        this.hostCount = hostCount;
    }

    public void setSleepingTime(long sleepingTime) {
        this.sleepingTime = sleepingTime;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public void setPrefixStartingPoint(int prefixStartingPoint) {
        this.prefixStartingPoint = prefixStartingPoint;
    }

    public void setPrefixCount(int prefixCount) {
        this.prefixCount = prefixCount;
    }

    public void setPrefixCacheSize(int prefixCacheSize) {
        this.prefixCacheSize = prefixCacheSize;
    }

    public void setDiskCacheDir(String diskCacheDir) {
        this.diskCacheDir = diskCacheDir;
    }

    public void setNodesFileName(String nodesFileName) {
        this.nodesFileName = nodesFileName;
    }

    public void setDiagnosticThreadSleep(long diagnosticThreadSleep) {
        this.diagnosticThreadSleep = diagnosticThreadSleep;
    }

    public String getPrefixesFileName() {
        return workingDir + File.separator + prefixesFile;
    }

    public boolean hasPrefixFile() {
        return prefixesFile != null;
    }

    public void setPrefixesFile(String prefixesFile) {
        this.prefixesFile = prefixesFile;
    }

    public int getPrefixAggregationSize() {
        return Math.max(prefixAggregationSize, 1);
    }

    public void setPrefixAggregationSize(int prefixAggregationSize) {
        this.prefixAggregationSize = prefixAggregationSize;
    }

    public boolean isNeighborsContainerCaching() {
        // default behavior is not to cache
        return neighborsContainerCaching;
    }

    public void setNeighborsContainerCaching(boolean neighborsContainerCaching) {
        this.neighborsContainerCaching = neighborsContainerCaching;
    }

    public boolean isUseInputHandlerThread() {
        return useInputHandlerThread;
    }

    public void setUseInputHandlerThread(boolean useInputHandlerThread) {
        this.useInputHandlerThread = useInputHandlerThread;
    }

    public boolean isUseEventsFile() {
        return useEventsFile;
    }

    public void setUseEventsFile(boolean useEventsFile) {
        this.useEventsFile = useEventsFile;
    }

    public String getEventsFileName() {
        return eventsFileName;
    }

    public void setEventsFileName(String eventsFileName) {
        this.eventsFileName = eventsFileName;
    }

    public int getCreatedLoggingInterval() {
        return createdLoggingInterval == 0 ? 50 : createdLoggingInterval;
    }

    public void setCreatedLoggingInterval(int createdLoggingInterval) {
        this.createdLoggingInterval = createdLoggingInterval;
    }

    public int getIntroducedLoggingInterval() {
        return introducedLoggingInterval == 0 ? 500 : introducedLoggingInterval;
    }

    public void setIntroducedLoggingInterval(int introducedLoggingInterval) {
        this.introducedLoggingInterval = introducedLoggingInterval;
    }

    public boolean isUseNeighborsMap() {
        return useNeighborsMap;
    }

    public void setUseNeighborsMap(boolean useNeighborsMap) {
        this.useNeighborsMap = useNeighborsMap;
    }

    public String getResultDirectory() {
        return resultDirectory;
    }

    public void setResultDirectory(String resultDirectory) {
        this.resultDirectory = resultDirectory;
    }
    
    public int getMaxPrefixes() {
    	return this.maxPrefixes;
    }

}

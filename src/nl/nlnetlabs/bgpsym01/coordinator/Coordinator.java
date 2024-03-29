package nl.nlnetlabs.bgpsym01.coordinator;

import java.io.File;
import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.command.MasterAckCommand;
import nl.nlnetlabs.bgpsym01.command.ShutdownCommand;
import nl.nlnetlabs.bgpsym01.command.SyncTimeCommand;
import nl.nlnetlabs.bgpsym01.coordinator.helpers.CommandSenderHelper;
import nl.nlnetlabs.bgpsym01.coordinator.helpers.DisconnectHelper;
import nl.nlnetlabs.bgpsym01.coordinator.helpers.PropagationHelper;
import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerImpl;
import nl.nlnetlabs.bgpsym01.primitives.types.StaticThread;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;
import nl.nlnetlabs.bgpsym01.xstream.XRegistry;

import org.apache.log4j.Logger;

/**
 * Entity responsible for coordinating the simulation process. All new events in
 * the system are introduced by it.
 * 
 * 
 * @see DisconnectHelper
 */
public class Coordinator {

    private static final int SYNC_CONSTANT = 500;

    private static Logger log = Logger.getLogger(Coordinator.class);

    private Communicator communicator;

    public volatile boolean finished;

    private ArrayList<XRegistry> registries;

    XProperties properties = XProperties.getInstance();

    private RunMonitor runMonitor;

    @SuppressWarnings("unused")
    private DisconnectHelper disconnectHelper;

    private PropagationHelper propagationHelper;

    private CommandSenderHelper commandSenderHelper;

    private boolean started = false;

    private synchronized void setStarted() {
        started = true;
        notifyAll();
    }

    public void controlTheGame() {
        
        setStarted();
       
        
        
        // wait till guys set up their server connections
        commandSenderHelper.waitForAllHosts();
        
        if (log.isInfoEnabled()) {
            log.info("sending acks");
        }

        commandSenderHelper.sendToAllHosts(new MasterAckCommand());
        commandSenderHelper.waitForAllHosts();

        log.info("all hosts finished");
        // sync the time
        if (!syncTheTime()) {
            shutdown();
            return;
        }
        
     // create results directory
      		String dirName = XProperties.getInstance().getResultDirectory() + File.separator + Tools.getInstance().getStartAsString();
      		File directory = new File(dirName);
      		if (!directory.exists() && !directory.mkdirs()) {
      			throw new BGPSymException("Unable to make directory: "+dirName);
      		}


        log.info("all set");

        // now we start sending updates
        
        propagationHelper.propagatePrefixes();

        if (log.isInfoEnabled()) {
            log.info("job's done");
        }

        shutdown();

        if (log.isInfoEnabled()) {
            log.info("coordinator finished");
        }
    }

    public boolean syncTheTime() {
        long startTime = System.currentTimeMillis() + SYNC_CONSTANT;
        TimeControllerFactory.getTimeController();
        TimeControllerImpl.setStartTime(startTime);
        SyncTimeCommand stc = new SyncTimeCommand(startTime);
        for (int i = 0; i < SystemConstants.TIME_SYNC_TIMES; i++) {
            commandSenderHelper.sendToAllHosts(stc);
            commandSenderHelper.waitForAllHosts();
            long end = System.currentTimeMillis();
            if (end - startTime > SYNC_CONSTANT) {
                log.error("time elapsed = " + (end - startTime) + ", CONSTANT=" + SYNC_CONSTANT);
                return false;
            }
        }
        return true;
    }

    public boolean isStarted() {
        return started;
    }

    private void shutdown() {

        // collapse them
        commandSenderHelper.sendToAllHosts(new ShutdownCommand());

        // let the message propagate
        StaticThread.sleep(1000);
        communicator.shutdown();

        runMonitor.finish();
    }

    public void end() {
        propagationHelper.end();
        if (log.isInfoEnabled()) {
            log.info("end called...");
        }
        finished = true;
    }

    public void setRunMonitor(RunMonitor runMonitor) {
        this.runMonitor = runMonitor;
    }

    public RunMonitor getRunMonitor() {
        return runMonitor;
    }

    public void setCommunicator(Communicator communicator) {
        this.communicator = communicator;
    }

    public void setPropagationHelper(PropagationHelper propagationHelper) {
        this.propagationHelper = propagationHelper;
    }

    public PropagationHelper getPropagationHelper() {
        return propagationHelper;
    }

    public void setCommandSenderHelper(CommandSenderHelper commandSenderHelper) {
        this.commandSenderHelper = commandSenderHelper;
    }

    public CommandSenderHelper getCommandSenderHelper() {
        return commandSenderHelper;
    }

    public void setDisconnectHelper(DisconnectHelper disconnectHelper) {
        this.disconnectHelper = disconnectHelper;
    }

    public ArrayList<XRegistry> getRegistries() {
        return registries;
    }

    public void setRegistries(ArrayList<XRegistry> registries) {
        this.registries = registries;
    }

}

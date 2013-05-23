package nl.nlnetlabs.bgpsym01.primitives;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import nl.nlnetlabs.bgpsym01.command.DiagnosticCommand;
import nl.nlnetlabs.bgpsym01.coordinator.AbstractChannelAttachment;
import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.main.tcp.CommunicationSocketThread;
import nl.nlnetlabs.bgpsym01.primitives.types.ByteFIFO;
import nl.nlnetlabs.bgpsym01.primitives.types.ShutdownadbleThread;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

public class DiagnosticThread extends ShutdownadbleThread {

    private static boolean isOn = false;

    private static Logger log = Logger.getLogger(DiagnosticThread.class);

    private static DiagnosticThread instance;

    private Collection<BGPProcess> processes;

    private CommunicationSocketThread cst;

    private boolean shutdown;

    public static DiagnosticThread getInstance() {
        return instance;
    }

    public static void init(Collection<BGPProcess> processes, CommunicationSocketThread cst) {
        instance = new DiagnosticThread();
        if (instance.isAlive()) {
            throw new IllegalStateException("thread is already running");
        }
        instance.processes = processes;
        instance.setDaemon(true);
        instance.setPriority(Thread.MIN_PRIORITY);
        instance.setName("diag_" + Tools.getInstance().getProcNum());
        instance.cst = cst;
        instance.start();
    }

    private DiagnosticThread() {
    }

    @Override
    public synchronized void shutdown() {
        shutdown = true;
        notify();
    }

    @Override
    public void run() {

        Runtime runtime = Runtime.getRuntime();
        long sleepTime = XProperties.getInstance().diagnosticThreadSleep;
        while (true) {
            synchronized (this) {
                try {
                    wait(sleepTime);
                } catch (InterruptedException e) {
                }
                // finish our job
                if (shutdown) {
                    return;
                }
            }
            if (!isOn) {
                continue;
            }
            int min = Integer.MAX_VALUE;
            int max = -1;
            int sum = 0;

            for (BGPProcess process : processes) {
                int size = process.getQueue().size();
                min = min <= size ? min : size;
                max = max >= size ? max : size;
                sum += size;
            }

            int freeMemory = (int) (runtime.freeMemory() / 1024 / 1024);
            int totalMemory = (int) (runtime.totalMemory() / 1024 / 1024);

            DiagnosticCommand dc = new DiagnosticCommand();
            dc.setProcessId(Tools.getInstance().getProcNum());

            dc.setFreeMemory(freeMemory);
            dc.setTotalMemory(totalMemory);

            dc.setMinQueueSize(min);
            dc.setAvgQueueSize(sum / processes.size());
            dc.setMaxQueueSize(max);

            int count = 0;
            try {
                Iterator<ByteFIFO> byteFIFOs = AbstractChannelAttachment.getFIFOsIterator();
                while (byteFIFOs.hasNext()) {
                    ByteFIFO fifo = byteFIFOs.next();
                    int size = fifo.available();
                    min = min <= size ? min : size;
                    max = max >= size ? max : size;
                    sum += size;
                    count++;
                }
            } catch (ConcurrentModificationException e) {
                log.error("concurrent", e);
                // it's OK - it can happen sometimes at the beginning (before
                // everything gets really set
                continue;
            }

            dc.setMinFIFOSize(min);
            if (count != 0) {
                dc.setAvgFIFOSize(sum / count);
            }
            dc.setMaxFIFOSize(max);

            // if we are testing right now than cst might be null
            if (cst != null) {
                cst.sendCommand(dc);
            }

        }
    }

    public static void setOn(boolean isOnX) {
        isOn = isOnX;
    }

}

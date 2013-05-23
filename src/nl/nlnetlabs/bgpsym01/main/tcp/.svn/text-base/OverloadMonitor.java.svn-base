package nl.nlnetlabs.bgpsym01.main.tcp;

import nl.nlnetlabs.bgpsym01.command.OverloadCommand;
import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.main.Tools;

import org.apache.log4j.Logger;

public class OverloadMonitor {

    private static Logger log = Logger.getLogger(OverloadMonitor.class);

    private static final int QUEUE_LOW_VALUE = 128;
    private static final int QUEUE_BIG_VALUE = 513;

    private static final int FIFO_LOW_VALUE = SystemConstants.BYTE_ARRAY_SIZE / 512;
    private static final int FIFO_BIG_VALUE = SystemConstants.BYTE_ARRAY_SIZE / 4;

    private CommunicationSocketThread cst;

    private int overloaded = 0;

    private int procNum = Tools.getInstance().getProcNum();

    public OverloadMonitor(CommunicationSocketThread cst) {
        this.cst = cst;
    }

    public void overload(int size) {
        synchronized (this) {
            overloaded++;
            if (overloaded != 1) {
                return;
            }
            log.warn("sending stop, size=" + size);
            OverloadCommand oc = new OverloadCommand();
            oc.setOverload(true);
            oc.setSenderId(procNum);
            cst.sendCommand(oc);
        }
    }

    public void backOK() {
        synchronized (this) {
            overloaded--;
            if (overloaded != 0) {
                return;
            }
            log.warn("sending start");
            OverloadCommand oc = new OverloadCommand();
            oc.setOverload(false);
            cst.sendCommand(oc);
        }
    }

    public final int getQueueBigValue() {
        return QUEUE_BIG_VALUE;
    }

    public final int getQueueLowValue() {
        return QUEUE_LOW_VALUE;
    }

    public final int getFifoLowValue() {
        return FIFO_LOW_VALUE;
    }

    public final int getFifoBigValue() {
        return FIFO_BIG_VALUE;
    }

}

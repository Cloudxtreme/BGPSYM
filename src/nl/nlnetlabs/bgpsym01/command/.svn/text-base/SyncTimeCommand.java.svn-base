package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerImpl;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.StaticThread;

public class SyncTimeCommand extends MasterCommand {

    private static long smallestDiscrepancy = Long.MAX_VALUE;

    private static long received = 0;

    private long startTime;

    private long currentTime;

    public SyncTimeCommand(long startTime) {
        this.startTime = startTime;
    }

    public SyncTimeCommand() {
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.SYNC_TIME;
    }

    @Override
    public void process() {
        long timeDiscrepancy = System.currentTimeMillis() - currentTime;
        if (Math.abs(timeDiscrepancy) < Math.abs(smallestDiscrepancy)) {
            smallestDiscrepancy = timeDiscrepancy;
        }
        if (++received == SystemConstants.TIME_SYNC_TIMES) {
            TimeControllerFactory.getTimeController();
            TimeControllerImpl.setStartTime(startTime + timeDiscrepancy);
        }
        // don't spam coordinator with responses too fast - he might not
        // have sent all the commands yet
        StaticThread.sleep(10);
        sendAckToCoordinator();
    }

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
        startTime = in.readLong();
        currentTime = in.readLong();
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
        out.writeLong(startTime);
        out.writeLong(System.currentTimeMillis());
    }

}

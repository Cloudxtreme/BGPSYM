package nl.nlnetlabs.bgpsym01.command;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;

import org.apache.log4j.Logger;

public class DiagnosticCommand extends SlaveCommand {

    private static Logger log = Logger.getLogger(DiagnosticCommand.class);
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm:ss.SSS");

    private int processId;

    private int freeMemory;
    private int totalMemory;
    private int minQueueSize;
    private int avgQueueSize;
    private int maxQueueSize;
    private int minFIFOSize;
    private int avgFIFOSize;
    private int maxFIFOSize;

    private long time;

    public DiagnosticCommand() {
        super();
        time = System.currentTimeMillis();

    }

    @Override
    public CommandType getCommandType() {
        return CommandType.DIAGNOSTIC;
    }

    @Override
    public void process() {
        StringBuilder builder = new StringBuilder();
        builder.append(dateFormat.format(new Date(time))).append("\t");
        builder.append("P ").append(processId).append(";\t");
        builder.append("M ").append(freeMemory).append("/").append(totalMemory).append(";\t");
        builder.append("Q ").append(minQueueSize + " - " + avgQueueSize + " - " + maxQueueSize + ";\t");
        builder.append("F ").append(minFIFOSize + " - " + avgFIFOSize + " - " + maxFIFOSize).append("\n");

        FileOutputStream diagFile = Tools.getInstance().getDiagFile();
        byte[] bytes = builder.toString().getBytes();
        synchronized (diagFile) {
            try {
                diagFile.write(bytes);
                // diagFile.getFD().sync();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
        super.readInternalData(in);
        time = in.readLong();
        processId = in.readInt();
        freeMemory = in.readInt();
        totalMemory = in.readInt();
        minQueueSize = in.readInt();
        avgQueueSize = in.readInt();
        maxQueueSize = in.readInt();
        minFIFOSize = in.readInt();
        avgFIFOSize = in.readInt();
        maxFIFOSize = in.readInt();

    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
        super.writeInternalData(out);
        out.writeLong(time);
        out.writeInt(processId);
        out.writeInt(freeMemory);
        out.writeInt(totalMemory);
        out.writeInt(minQueueSize);
        out.writeInt(avgQueueSize);
        out.writeInt(maxQueueSize);
        out.writeInt(minFIFOSize);
        out.writeInt(avgFIFOSize);
        out.writeInt(maxFIFOSize);
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(int freeMemory) {
        this.freeMemory = freeMemory;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(int totalMemory) {
        this.totalMemory = totalMemory;
    }

    public int getMinQueueSize() {
        return minQueueSize;
    }

    public void setMinQueueSize(int minQueueSize) {
        this.minQueueSize = minQueueSize;
    }

    public int getAvgQueueSize() {
        return avgQueueSize;
    }

    public void setAvgQueueSize(int avgQueueSize) {
        this.avgQueueSize = avgQueueSize;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public int getMinFIFOSize() {
        return minFIFOSize;
    }

    public void setMinFIFOSize(int minFIFOSize) {
        this.minFIFOSize = minFIFOSize;
    }

    public int getAvgFIFOSize() {
        return avgFIFOSize;
    }

    public void setAvgFIFOSize(int avgFIFOSize) {
        this.avgFIFOSize = avgFIFOSize;
    }

    public int getMaxFIFOSize() {
        return maxFIFOSize;
    }

    public void setMaxFIFOSize(int maxFIFOSize) {
        this.maxFIFOSize = maxFIFOSize;
    }

}

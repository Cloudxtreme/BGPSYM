package nl.nlnetlabs.bgpsym01.callback.sym;

public class SymCallbackImpl implements SymCallback {

    private volatile int byteFIFOWrittenBytes;

    private volatile int byteFIFOWrittenCount;

    private volatile int bytesReceivedBytes;
    private volatile int bytesReceivedCount;

    private volatile int bytesSentByte;
    private volatile int bytesSentCount;

    private volatile int prefixStoredCount;
    private volatile int prefixStoredAmount;
    private volatile int prefixStoredSize;

    private volatile long prefixStoredTime;

    public void byteFIFOWritten(int count) {
        byteFIFOWrittenBytes += count;
        byteFIFOWrittenCount++;
    }

    public void bytesReceived(int count) {
        bytesReceivedBytes += count;
        bytesReceivedCount++;
    }

    public void bytesSent(int count) {
        bytesSentByte += count;
        bytesSentCount++;
    }

    public void prefixStored(int count, int size, int blockSize) {
        prefixStoredSize += size;
        prefixStoredCount++;
        prefixStoredAmount += count;

    }

    public void prefixStored(long writtingTime) {
        prefixStoredTime += writtingTime;
    }

}

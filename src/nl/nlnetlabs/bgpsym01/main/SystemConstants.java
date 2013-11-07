package nl.nlnetlabs.bgpsym01.main;

public class SystemConstants {

    // maximum 2^19 prefixes =~ 254k
    public static final int PREFIX_SIZE_BITS = 18;

    public static final int AS_SIZE_BITS = 19;

    // public static final int BLOCK_SIZE = 4096 << 4;

    public static final int BYTE_ARRAY_SIZE = 2048 * 2048;

    public static final int DEFAULT_BYTE_FIFO_SIZE = BYTE_ARRAY_SIZE;

    public static final int TIME_SYNC_TIMES = 5;

    // this has been moved to Xproperties now
    // public static final int BOGUS_PREFIX_MIN = 8000;

    public static final int ROUTE_VIEW_THREAD_PRIORITY = Thread.NORM_PRIORITY + 1;

    public static final int COMMUNICATION_THREAD_PRIORITY = Thread.NORM_PRIORITY + 2;

}

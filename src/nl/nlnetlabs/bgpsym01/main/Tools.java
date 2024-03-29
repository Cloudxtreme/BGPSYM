package nl.nlnetlabs.bgpsym01.main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerImpl;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class Tools {

    private static final String[] LOGGED_PREFIXES = new String[] { "AS31080", "15_15", "1111110010", "r2_0", "AS1200" };

    private int procNum = -1;

    private static final Tools singleton = new Tools();

    private FileOutputStream fos;

    private Tools() {

    }

    public String getStartAsString() {
        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH:mm:ss.SSS");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH:mm");
        return dateFormat.format(new Date(TimeControllerImpl.getStartTime()));
    }

    public static Tools getInstance() {
        return singleton;
    }

    public boolean isLogOn(String name) {
        for (String l : LOGGED_PREFIXES) {
            if (name.startsWith(l + "|")) {
                return true;
            }
        }
        return false;
    }

    public boolean isLogOn(Thread currentThread) {
        return isLogOn(currentThread.getName());
    }

    public int getProcNum() {
        return procNum;
    }

    public void setProcNum(int procNum) {
        this.procNum = procNum;
    }

    public int getPrefixBlockSize(int neighborsCount) {
        /*
         * assumptions:
         * 1. average path length ~ 4
         * 2. we don't want to have more than 64 prefixes in the block
         * 3. perfect situation - 32 prefixes in the block
         * 4. we get info about each prefix from each neighbor
         * 5. no less than 1024, no more than 64k
         */
        int max = 1 << 16;
        int min = 1 << 10;

        // how much space will a prefix take (this is upper bound)
        int prefixSize = ((SystemConstants.AS_SIZE_BITS >> 3) + 1) * 4 * (neighborsCount + 1);

        // try to get 32 prefixes per block
        int outcome = 32 * prefixSize;
        outcome = outcome >= max ? max : outcome;
        outcome = outcome < min ? min : outcome;

        // make the value a multi of 1024
        outcome >>= 10;
        outcome <<= 10;

        return outcome;
    }

    public void createDiagFile() {
        try {
            fos = new FileOutputStream(XProperties.getInstance().getDiagFile());
        } catch (FileNotFoundException e) {
            throw new BGPSymException(e);
        }
    }

    public FileOutputStream getDiagFile() {
        return fos;
    }

}

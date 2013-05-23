package nl.nlnetlabs.bgpsym01.logging;

public class SparseLogger {

    enum LogType {
        T1
    };

    public static boolean canLog(LogType type) {
        return false;
    }

}

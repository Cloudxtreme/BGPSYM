package nl.nlnetlabs.bgpsym01.mock;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.log4j.Logger;

public class JUnitExceptionHandler implements UncaughtExceptionHandler {

    private static Logger log = Logger.getLogger(JUnitExceptionHandler.class);

    private AbstractTest test;

    public JUnitExceptionHandler(AbstractTest test) {
        this.test = test;
    }

    public void uncaughtException(Thread t, Throwable e) {
        log.error("thread=" + t.getName(), e);
        test.setFailed();
    }

}

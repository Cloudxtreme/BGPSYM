package nl.nlnetlabs.bgpsym01.primitives.types;

abstract public class PeriodicalThread extends ShutdownadbleThread {

    boolean shutdown = false;
    private long sleepTime;

    public PeriodicalThread(long sleepTime) {
        this.sleepTime = sleepTime;
        setName(getThreadName());
    }

    abstract protected String getThreadName();

    abstract public void process();

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                try {
                    wait(sleepTime);
                } catch (InterruptedException e) {
                }
            }
            if (shutdown) {
                return;
            }
            process();
        }
    }

    protected boolean isShutdown() {
        return shutdown;
    }

    @Override
    public synchronized void shutdown() {
        shutdown = true;
        notify();
    }

}

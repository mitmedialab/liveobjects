package edu.mit.media.obm.liveobjects.driver.wifi;

/**
 * Created by arata on 9/11/15.
 */
public abstract class StartableEntity {
    private boolean started = false;

    abstract protected void startEntity();
    abstract protected void stopEntity();

    public synchronized void start() {
        if (isStarted()) {
            throw new RuntimeException("must not try to register a broadcast receiver when another receiver has already been registered");
        }

        startEntity();
        started = true;
    }

    public synchronized void stop() {
        if (!isStarted()) {
            throw new RuntimeException("must not try to unregister broadcast receiver when no receiver has been registered");
        }

        stopEntity();
        started = false;
    }

    public synchronized void requireStarted() {
        if (!isStarted()) {
            throw new RuntimeException("Tried to execute a process without registration, even though it's required");
        }
    }

    private synchronized boolean isStarted() {
        return started;
    }
}

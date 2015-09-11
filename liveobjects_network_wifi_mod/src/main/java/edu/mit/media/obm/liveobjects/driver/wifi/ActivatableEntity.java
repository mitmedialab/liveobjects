package edu.mit.media.obm.liveobjects.driver.wifi;

/**
 * Created by arata on 9/11/15.
 */
public abstract class ActivatableEntity {
    private boolean started = false;

    abstract protected void activateEntity();
    abstract protected void deactivateEntity();

    public synchronized void activate() {
        if (isActivated()) {
            throw new RuntimeException("must not try to activate when already activated");
        }

        activateEntity();
        started = true;
    }

    public synchronized void deactivate() {
        if (!isActivated()) {
            throw new RuntimeException("must not try to deactivate when not activated");
        }

        deactivateEntity();
        started = false;
    }

    public synchronized void requireActivated() {
        if (!isActivated()) {
            throw new RuntimeException("Tried to execute entity before activation");
        }
    }

    private synchronized boolean isActivated() {
        return started;
    }
}

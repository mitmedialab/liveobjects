package edu.mit.media.obm.liveobjects.driver.wifi.base;

/**
 * Created by arata on 9/11/15.
 */
public abstract class ActivatableEntity {
    private boolean activated = false;

    abstract protected void activateEntity();
    abstract protected void deactivateEntity();

    public synchronized void activate() {
        if (isActivated()) {
            throw new IllegalStateException("must not try to activate when already activated");
        }

        activateEntity();
        activated = true;
    }

    public synchronized void deactivate() {
        if (!isActivated()) {
            throw new IllegalStateException("must not try to deactivate when not activated");
        }

        deactivateEntity();
        activated = false;
    }

    public synchronized void requireActivated() {
        if (!isActivated()) {
            throw new IllegalStateException("Tried to execute entity before activation");
        }
    }

    private synchronized boolean isActivated() {
        return activated;
    }
}

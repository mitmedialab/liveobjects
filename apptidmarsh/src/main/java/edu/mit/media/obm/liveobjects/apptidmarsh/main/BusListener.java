package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arata on 9/18/15.
 */
public abstract class BusListener {
    private boolean registered = false;

    protected abstract List<Bus> getBuses();

    protected void registerBuses() {
        if (!registered) {
            for (Bus bus : getBuses()) {
                bus.register(this);
            }

            registered = true;
        }
    }

    protected void unregisterBuses() {
        if (registered) {
            for (Bus bus : getBuses()) {
                bus.unregister(this);
            }

            registered = false;
        }
    }
}

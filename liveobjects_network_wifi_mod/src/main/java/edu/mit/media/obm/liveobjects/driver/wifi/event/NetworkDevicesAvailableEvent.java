package edu.mit.media.obm.liveobjects.driver.wifi.event;

import java.util.List;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;

/**
 * Created by artimo14 on 9/12/15.
 */
public class NetworkDevicesAvailableEvent {
    private final List<LiveObject> availableLiveObjects;

    public NetworkDevicesAvailableEvent(List<LiveObject> availableLiveObjects) {
        this.availableLiveObjects = availableLiveObjects;
    }

    public List<LiveObject> getAvailableLiveObjects() {
        return availableLiveObjects;
    }
}

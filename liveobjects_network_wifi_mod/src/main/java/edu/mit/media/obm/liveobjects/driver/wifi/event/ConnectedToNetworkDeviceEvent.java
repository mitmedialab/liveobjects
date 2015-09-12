package edu.mit.media.obm.liveobjects.driver.wifi.event;

/**
 * Created by artimo14 on 9/12/15.
 */
public class ConnectedToNetworkDeviceEvent {
    private final String connectedDeviceName;

    public ConnectedToNetworkDeviceEvent(String connectedDeviceName) {
        this.connectedDeviceName = connectedDeviceName;
    }

    public String getConnectedDeviceName() {
        return connectedDeviceName;
    }
}

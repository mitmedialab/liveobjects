package edu.mit.media.obm.liveobjects.driver.wifi.event;

/**
 * Created by artimo14 on 9/12/15.
 */
public class ConnectedToNetworkDeviceEvent {
    public enum ConnectionStatus {
        CONNECTED_TO_TARGET_DEVICE,
        CONNECTED_TO_WRONG_DEVICE,
        CONNECTION_FAILED_FOR_TIMEOUT,
        CONNECTION_FAILED_FOR_UNKNOWN_REASON
    };

    private final String connectedDeviceName;
    private final ConnectionStatus connectionStatus;

    public ConnectedToNetworkDeviceEvent(String connectedDeviceName, ConnectionStatus connectionStatus) {
        this.connectedDeviceName = connectedDeviceName;
        this.connectionStatus = connectionStatus;
    }

    public String getConnectedDeviceName() {
        return connectedDeviceName;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ConnectedToNetworkDeviceEvent)) {
            return false;
        }

        ConnectedToNetworkDeviceEvent event = (ConnectedToNetworkDeviceEvent) object;

        if (getConnectedDeviceName() != null) {
            return getConnectedDeviceName().equals(event.getConnectedDeviceName())
                    && getConnectionStatus().equals(event.getConnectionStatus());
        } else {
            return event.getConnectedDeviceName() == null
                    && getConnectionStatus().equals(event.getConnectionStatus());
        }
    }
}

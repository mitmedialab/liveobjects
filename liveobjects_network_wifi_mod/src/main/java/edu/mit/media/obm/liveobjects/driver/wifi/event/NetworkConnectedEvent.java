package edu.mit.media.obm.liveobjects.driver.wifi.event;

/**
 * Created by artimo14 on 9/12/15.
 */
public class NetworkConnectedEvent {
    public enum State {
        CONNECTED_TO_TARGET,
        CONNECTED_TO_NON_TARGET,
        NOT_CONNECTED_FOR_TIMEOUT,
        NOT_CONNECTED_FOR_SSID_ACQUISITION_FAILURE
    };

    private final String connectedDeviceName;
    private final State state;

    public NetworkConnectedEvent(String connectedDeviceName, State state) {
        this.connectedDeviceName = connectedDeviceName;
        this.state = state;
    }

    public String getConnectedDeviceName() {
        return connectedDeviceName;
    }

    public State getState() {
        return state;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof NetworkConnectedEvent)) {
            return false;
        }

        NetworkConnectedEvent event = (NetworkConnectedEvent) object;

        if (getConnectedDeviceName() != null) {
            return getConnectedDeviceName().equals(event.getConnectedDeviceName())
                    && getState().equals(event.getState());
        } else {
            return event.getConnectedDeviceName() == null
                    && getState().equals(event.getState());
        }
    }
}

package edu.mit.media.obm.liveobjects.driver.wifi.event;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;

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

    private final LiveObject liveObject;
    private final State state;

    public NetworkConnectedEvent(LiveObject liveObject, State state) {
        this.liveObject = liveObject;
        this.state = state;
    }

    public LiveObject getConnectedLiveObject() {
        return liveObject;
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

        if (getConnectedLiveObject() != null) {
            return getConnectedLiveObject().equals(event.getConnectedLiveObject())
                    && getState().equals(event.getState());
        } else {
            return event.getConnectedLiveObject() == null
                    && getState().equals(event.getState());
        }
    }
}

package edu.mit.media.obm.liveobjects.driver.wifi;

import com.squareup.otto.Bus;

/**
 * Created by artimo14 on 9/12/15.
 */
public class WifiNetworkBus {
    private static Bus bus = null;

    public static Bus getBus() {
        if (bus == null) {
            bus = new Bus();
        }

        return bus;
    }
}

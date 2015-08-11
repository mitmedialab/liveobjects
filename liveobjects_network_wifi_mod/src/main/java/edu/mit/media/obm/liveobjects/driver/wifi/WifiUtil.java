package edu.mit.media.obm.liveobjects.driver.wifi;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkUtil;

/**
 * Specific implementation of NetworkUtil for Wifi
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public enum  WifiUtil implements NetworkUtil {
    INSTANCE;

    private String SSID_PREFIX;

    @Override
    public boolean isLiveObject(String deviceId) {
        return deviceId.startsWith(SSID_PREFIX);
    }

    @Override
    public LiveObject convertDeviceIdToLiveObject(String deviceId) {
        int prefixLength = SSID_PREFIX.length();
        String liveObjectName = deviceId.substring(prefixLength);

        return new LiveObject(liveObjectName);
    }

    @Override
    public String convertLiveObjectToDeviceId(LiveObject liveObject){
        return SSID_PREFIX + liveObject.getLiveObjectName();
    }

    protected final void setSsidPrefix(String ssidPrefix) {
        SSID_PREFIX = ssidPrefix;
    }


}

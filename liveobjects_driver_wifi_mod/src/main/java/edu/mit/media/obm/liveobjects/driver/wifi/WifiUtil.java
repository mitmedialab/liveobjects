package edu.mit.media.obm.liveobjects.driver.wifi;

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
    public String convertDeviceIdToLiveObjectName(String deviceId) {
        return convertSSIDToLiveObjectName(deviceId);
    }

    @Override
    public String convertLiveObjectNameToDeviceId(String liveObjectName){
        return convertLiveObjectNameToSSID(liveObjectName);
    }

    private String convertSSIDToLiveObjectName(String ssid) {
        int prefixLength = SSID_PREFIX.length();
        return  ssid.substring(prefixLength);
    }

    private String convertLiveObjectNameToSSID(String liveObjectName) {
        return SSID_PREFIX + liveObjectName;
    }

    protected final void setSsidPrefix(String ssidPrefix) {
        SSID_PREFIX = ssidPrefix;
    }


}

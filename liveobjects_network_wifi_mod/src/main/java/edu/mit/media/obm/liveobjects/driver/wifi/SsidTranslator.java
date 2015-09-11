package edu.mit.media.obm.liveobjects.driver.wifi;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

/**
 * Specific implementation of DeviceIdTranslator for Wifi
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public enum SsidTranslator implements DeviceIdTranslator {
    INSTANCE;

    private String SSID_PREFIX;

    @Override
    public boolean isLiveObject(String deviceId) {
        return deviceId.startsWith(SSID_PREFIX);
    }

    @Override
    public LiveObject translateToLiveObject(String deviceId) {
        int prefixLength = SSID_PREFIX.length();
        String liveObjectName = deviceId.substring(prefixLength);

        return new LiveObject(liveObjectName);
    }

    @Override
    public String translateFromLiveObject(LiveObject liveObject){
        return SSID_PREFIX + liveObject.getLiveObjectName();
    }

    protected final void setSsidPrefix(String ssidPrefix) {
        SSID_PREFIX = ssidPrefix;
    }


}

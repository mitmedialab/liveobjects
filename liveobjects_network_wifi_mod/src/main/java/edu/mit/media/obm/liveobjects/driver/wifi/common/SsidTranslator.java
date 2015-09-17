package edu.mit.media.obm.liveobjects.driver.wifi.common;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

/**
 * Specific implementation of DeviceIdTranslator for Wifi
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class SsidTranslator implements DeviceIdTranslator {
    private String ssidPrefix;

    public SsidTranslator(String ssidPrefix) {
        this.ssidPrefix = ssidPrefix;
    }

    @Override
    public boolean isValidSsid(String deviceId) {
        return deviceId.startsWith(ssidPrefix);
    }

    @Override
    public boolean isValidLiveObject(LiveObject liveObject) {
        return true;
    }

    @Override
    public LiveObject translateToLiveObject(String deviceId) {
        int prefixLength = ssidPrefix.length();
        String liveObjectName = deviceId.substring(prefixLength);

        return new LiveObject(liveObjectName);
    }

    @Override
    public String translateFromLiveObject(LiveObject liveObject){
        return ssidPrefix + liveObject.getLiveObjectName();
    }
}

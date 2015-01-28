package edu.mit.media.obm.liveobjects.middleware;

/**
 * Created by Valerio Panzica La Manna on 08/12/14.
 */
public class Util {
    private static final String SSID_PREFIX = "live-objects-";
    public static String convertSSIDToLiveObjectName(String ssid) {
        int prefixLength = SSID_PREFIX.length();
        return  ssid.substring(prefixLength);
    }

    public static String convertLiveObjectNameToSSID(String liveObjectName) {
        return SSID_PREFIX + liveObjectName;
    }

    public static boolean isLiveObject(String ssid) {
        return ssid.startsWith(SSID_PREFIX);
    }
}

package edu.mit.media.obm.liveobjects.storage.wifi;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

/**
 * @author Arata Miyamoto <arata@media.mit.edu>
 */
public class WifiStorageConfig {
    private final static String LOG_TAG = WifiStorageConfig.class.getSimpleName();

    public static String getBasePath(Context context) {
        return String.format("http://%s/%s", getGatewayIpAddress(context), getMediaFolderName(context));
    }

    @SuppressWarnings("deprecation")
    private static String getGatewayIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        String gateway = Formatter.formatIpAddress(dhcpInfo.gateway);

        Log.d(LOG_TAG, "baseUrl = " + gateway);

        return gateway;
    }

    private static String getMediaFolderName(Context context) {
        return context.getString(R.string.media_folder_name);
    }
}

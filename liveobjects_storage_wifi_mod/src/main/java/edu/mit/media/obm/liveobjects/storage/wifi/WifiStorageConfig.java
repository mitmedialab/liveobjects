package edu.mit.media.obm.liveobjects.storage.wifi;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.text.format.Formatter;

import com.noveogroup.android.log.Log;


/**
 * @author Arata Miyamoto <arata@media.mit.edu>
 */
public class WifiStorageConfig {
    public static class ContextWrapper {
        private Context mContext;

        private ContextWrapper(Context context) {
            mContext = context;
        }

        public Object getSystemService(String name) {
            return mContext.getSystemService(name);
        }

        public String getString(int resId) {
            return mContext.getString(resId);
        }
    }

    public static String getBasePath(Context context) throws RemoteException{
        ContextWrapper contextWrapper = new ContextWrapper(context);
        return String.format("http://%s/",getGatewayIpAddress(contextWrapper));
    }

    public static String getMediaFolderPath(Context context) throws RemoteException {
        return getMediaFolderPath(new ContextWrapper(context));
    }

    public static String getMediaFolderPath(ContextWrapper contextWrapper) throws RemoteException {
        return String.format("http://%s/%s",
                getGatewayIpAddress(contextWrapper), getMediaFolderName(contextWrapper));
    }

    @SuppressWarnings("deprecation")
    private static String getGatewayIpAddress(ContextWrapper contextWrapper) throws RemoteException {
        WifiManager wifiManager = (WifiManager) contextWrapper.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

        if (dhcpInfo == null) {
            throw new RemoteException("cannot get DHCP info from a remote host");
        }

        String gateway = Formatter.formatIpAddress(dhcpInfo.gateway);
        Log.d("baseUrl = " + gateway);

        if ("0.0.0.0".equals(gateway)) {
            throw new RemoteException("failed to get the IP address of the gateway");
        }

        return gateway;
    }

    private static String getMediaFolderName(ContextWrapper contextWrapper) {
        return contextWrapper.getString(R.string.media_folder_name);
    }
}

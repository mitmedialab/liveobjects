package edu.mit.media.obm.liveobjects.driver.wifi;

import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.noveogroup.android.log.Log;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkListener;

/**
 * Created by arata on 9/11/15.
 */
class NetworkStateChangedReceiver extends BroadcastReceiver {
    private WifiManager wifiManager;
    private DeviceIdTranslator deviceIdTranslator;
    private NetworkListener networkListener;
    private WifiConnector wifiConnector;

    public NetworkStateChangedReceiver(WifiConnector wifiConnector, DeviceIdTranslator deviceIdTranslator, NetworkListener networkListener) {
        this.wifiConnector = wifiConnector;
        this.deviceIdTranslator = deviceIdTranslator;
        this.networkListener = networkListener;
    }

    public void onReceive(Context context, Intent intent) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        String action = intent.getAction();
        switch (action) {
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                handleWifiConnection(intent);
                break;

        }
    }

    private void handleWifiConnection(Intent intent) {
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        NetworkInfo.State state = networkInfo.getState();
        Log.v("networkInfo = " + networkInfo.toString());

        synchronized (WifiConnectionManager.class) {
            if (state.equals(NetworkInfo.State.CONNECTED) && wifiConnector.isConnecting()) {
                String ssid = networkInfo.getExtraInfo();
                if (ssid == null) {
                    // SSID in NetworkInfo may be null depending on the model of the device
                    ssid = wifiManager.getConnectionInfo().getSSID();
                }

                ssid = WifiManagerWrapper.unQuoteString(ssid);
                if (deviceIdTranslator.isLiveObject(ssid)) {
                    LiveObject connectedLiveObject = deviceIdTranslator.translateToLiveObject(ssid);
                    Log.d("connectedLiveObject = " + connectedLiveObject);
                    networkListener.onConnected(connectedLiveObject.getLiveObjectName());

                    wifiConnector.setConnecting(false);
                }
            }
        }
    }
}

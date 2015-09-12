package edu.mit.media.obm.liveobjects.driver.wifi.connector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.noveogroup.android.log.Log;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.driver.wifi.WifiConnectionManager;
import edu.mit.media.obm.liveobjects.driver.wifi.common.WifiManagerWrapper;
import edu.mit.media.obm.liveobjects.driver.wifi.event.ConnectedToNetworkDeviceEvent;
import edu.mit.media.obm.liveobjects.driver.wifi.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkListener;

/**
 * Created by arata on 9/11/15.
 */
public class NetworkStateChangedReceiver extends BroadcastReceiver {
    @Inject WifiManager wifiManager;
    @Inject DeviceIdTranslator deviceIdTranslator;
    @Inject Bus bus;

    private WifiConnector wifiConnector;

    public NetworkStateChangedReceiver(Context context, WifiConnector wifiConnector) {
        DependencyInjector.inject(this, context);

        this.wifiConnector = wifiConnector;
    }

    public void onReceive(Context context, Intent intent) {
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

                    ConnectedToNetworkDeviceEvent event = new ConnectedToNetworkDeviceEvent(connectedLiveObject.getLiveObjectName());
                    bus.post(event);

                    wifiConnector.setConnecting(false);
                }
            }
        }
    }
}

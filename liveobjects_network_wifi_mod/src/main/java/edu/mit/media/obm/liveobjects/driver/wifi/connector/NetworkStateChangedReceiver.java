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
import edu.mit.media.obm.liveobjects.driver.wifi.common.WifiManagerFacade;
import edu.mit.media.obm.liveobjects.driver.wifi.common.WifiManagerWrapper;
import edu.mit.media.obm.liveobjects.driver.wifi.event.ConnectedToNetworkDeviceEvent;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

/**
 * Created by arata on 9/11/15.
 */
public class NetworkStateChangedReceiver extends BroadcastReceiver {
    @Inject WifiManagerFacade wifiManagerFacade;
    @Inject DeviceIdTranslator deviceIdTranslator;
    @Inject Bus bus;

    private String connectingDeviceSsid = null;

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            if (isMonitoring()) {
                postNetWorkConnectionEvent(intent);
            } else {
                Log.d("not monitoring");
            }
        } else {
            throw new IllegalArgumentException("unexpected action received: " + action);
        }
    }

    private void postNetWorkConnectionEvent(Intent intent) {
        synchronized (WifiConnectionManager.class) {
            if (isMonitoring() && isConnectedToNetwork(intent)) {
                String ssid = getConnectedDeviceSsid(intent);

                postEventWithConnectedDeviceSsid(ssid);
                stopMonitoring();
            }
        }
    }

    private boolean isConnectedToNetwork(Intent intent) {
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        Log.v("networkInfo = " + networkInfo.toString());

        return NetworkInfo.State.CONNECTED.equals(networkInfo.getState());
    }

    private String getConnectedDeviceSsid(Intent intent) {
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        String ssid = networkInfo.getExtraInfo();

        if (ssid == null) {
            // SSID in NetworkInfo may be null depending on the model of the device
            ssid = wifiManagerFacade.getConnectedSsid();
        }

        return WifiManagerWrapper.unQuoteString(ssid);
    }

    private void postEventWithConnectedDeviceSsid(String ssid) {
        ConnectedToNetworkDeviceEvent.ConnectionStatus connectionStatus = getConnectionStatus(ssid);
        ConnectedToNetworkDeviceEvent event;

        if (deviceIdTranslator.isValidSsid(ssid)) {
            LiveObject connectedLiveObject = deviceIdTranslator.translateToLiveObject(ssid);
            Log.d("connectedLiveObject = " + connectedLiveObject);

            event = new ConnectedToNetworkDeviceEvent(
                    connectedLiveObject.getLiveObjectName(), connectionStatus);
        } else {
            event = new ConnectedToNetworkDeviceEvent(ssid, connectionStatus);
        }

        bus.post(event);
    }

    private ConnectedToNetworkDeviceEvent.ConnectionStatus getConnectionStatus(String ssid) {
        if (ssid == null) {
            return ConnectedToNetworkDeviceEvent.ConnectionStatus.CONNECTION_FAILED_FOR_UNKNOWN_REASON;
        } else if (connectingDeviceSsid.equals(ssid)) {
            return ConnectedToNetworkDeviceEvent.ConnectionStatus.CONNECTED_TO_TARGET_DEVICE;
        } else {
            return ConnectedToNetworkDeviceEvent.ConnectionStatus.CONNECTED_TO_WRONG_DEVICE;
        }
    }

    public void startMonitoring(String connectingDeviceSsid) {
        this.connectingDeviceSsid = connectingDeviceSsid;
    }

    public void stopMonitoring() {
        this.connectingDeviceSsid = null;
    }

    public boolean isMonitoring() {
        return (connectingDeviceSsid != null);
    }
}

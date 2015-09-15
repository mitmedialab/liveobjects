package edu.mit.media.obm.liveobjects.driver.wifi.connector;

import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.noveogroup.android.log.Log;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.driver.wifi.WifiConnectionManager;
import edu.mit.media.obm.liveobjects.driver.wifi.common.WifiManagerFacade;
import edu.mit.media.obm.liveobjects.driver.wifi.event.NetworkConnectedEvent;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

/**
 * Created by arata on 9/11/15.
 */
public class NetworkStateChangedReceiver extends BroadcastReceiver {
    WifiManagerFacade wifiManagerFacade;
    DeviceIdTranslator deviceIdTranslator;
    Bus bus;

    private String connectingDeviceSsid = null;

    @Inject
    public NetworkStateChangedReceiver(WifiManagerFacade wifiManagerFacade,
                                       DeviceIdTranslator deviceIdTranslator, Bus bus) {
        this.wifiManagerFacade = wifiManagerFacade;
        this.deviceIdTranslator = deviceIdTranslator;
        this.bus = bus;
    }

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
        String ssid = WifiManagerFacade.unquote(networkInfo.getExtraInfo());

        if (ssid == null || "<unknown ssid>".equals(ssid)) {
            // SSID in NetworkInfo may be null depending on the model of the device
            ssid = wifiManagerFacade.getConnectedSsid();
        }

        return ssid;
    }

    private void postEventWithConnectedDeviceSsid(String ssid) {
        LiveObject connectedLiveObject = (deviceIdTranslator.isValidSsid(ssid) ?
            deviceIdTranslator.translateToLiveObject(ssid) : null);

        Log.d("connectedLiveObject = " + connectedLiveObject);

        NetworkConnectedEvent.State state = getConnectionStatus(ssid);
        NetworkConnectedEvent event = new NetworkConnectedEvent(connectedLiveObject, state);
        bus.post(event);
    }

    private NetworkConnectedEvent.State getConnectionStatus(String ssid) {
        if (ssid == null) {
            return NetworkConnectedEvent.State.NOT_CONNECTED_FOR_SSID_ACQUISITION_FAILURE;
        } else if (connectingDeviceSsid.equals(ssid)) {
            return NetworkConnectedEvent.State.CONNECTED_TO_TARGET;
        } else {
            return NetworkConnectedEvent.State.CONNECTED_TO_NON_TARGET;
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

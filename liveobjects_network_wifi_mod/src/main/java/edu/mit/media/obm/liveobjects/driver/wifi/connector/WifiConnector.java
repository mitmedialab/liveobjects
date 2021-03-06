package edu.mit.media.obm.liveobjects.driver.wifi.connector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import com.noveogroup.android.log.Log;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.driver.wifi.base.BroadcastSubscriber;
import edu.mit.media.obm.liveobjects.driver.wifi.common.WifiManagerFacade;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

/**
 * Created by arata on 9/11/15.
 */
public class WifiConnector extends BroadcastSubscriber {
    WifiManagerFacade mWifiManagerFacade;
    DeviceIdTranslator mDeviceIdTranslator;

    IntentFilter mIntentFilter;
    BroadcastReceiver mBroadcastReceiver;

    @Inject
    public WifiConnector(Context context, WifiManagerFacade wifiManagerFacade, DeviceIdTranslator deviceIdTranslator,
                          IntentFilter intentFilter, BroadcastReceiver broadcastReceiver) {
        super(context);

        mWifiManagerFacade = wifiManagerFacade;
        mDeviceIdTranslator = deviceIdTranslator;
        mIntentFilter = intentFilter;
        mBroadcastReceiver = broadcastReceiver;
    }

    private int mConnectingNetworkId;

    public void initialize() {
        stopMonitoring();
    }

    @Override
    protected BroadcastReceiver createBroadcastReceiver() {
        return mBroadcastReceiver;
    }

    @Override
    protected IntentFilter createIntentFilter() {
        return mIntentFilter;
    }

    synchronized public void connect(LiveObject liveObject) throws IllegalStateException {
        requireActivated();

        if (isConnecting()) {
            throw new IllegalStateException("Must not try to connect when it's already connecting");
        }

        String deviceId = mDeviceIdTranslator.translateFromLiveObject(liveObject);
        mConnectingNetworkId = mWifiManagerFacade.connectToNetwork(deviceId);

        startMonitoring(deviceId);
    }

    synchronized public void cancelConnecting() throws IllegalStateException {
        requireActivated();

        if (!isConnecting()) {
            Log.w("trying to cancel connecting, but not connecting now");
            return;
        }

        mWifiManagerFacade.disconnectFromNetwork(mConnectingNetworkId);
        stopMonitoring();
    }

    public boolean isConnecting() {
        requireActivated();

        return isMonitoring();
    }

    private void startMonitoring(String ssid) {
        ((NetworkStateChangedReceiver) mBroadcastReceiver).startMonitoring(ssid);
    }

    private void stopMonitoring() {
        ((NetworkStateChangedReceiver) mBroadcastReceiver).stopMonitoring();
    }

    private boolean isMonitoring() {
        return ((NetworkStateChangedReceiver) mBroadcastReceiver).isMonitoring();
    }

    synchronized public void forgetNetworkConfigurations() throws IllegalStateException {
        requireActivated();

        if (isConnecting()) {
            throw new IllegalStateException("Must not try to disconnect when it's already connecting");
        }

        Log.v("deletes network configurations for all live objects");
        for (String ssid: mWifiManagerFacade.getRegisteredSsids()) {
            Log.v("found a network configuration for '" + ssid + "'");
            if (mDeviceIdTranslator.isValidSsid(ssid)) {
                Log.v("deleting a network configuration for live object '" + ssid + "'");
                mWifiManagerFacade.removeNetwork(ssid);
            }
        }
    }
}

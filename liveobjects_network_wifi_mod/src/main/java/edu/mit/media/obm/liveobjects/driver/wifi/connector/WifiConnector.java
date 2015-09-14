package edu.mit.media.obm.liveobjects.driver.wifi.connector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;

import com.noveogroup.android.log.Log;

import javax.inject.Inject;
import javax.inject.Named;

import edu.mit.media.obm.liveobjects.driver.wifi.R;
import edu.mit.media.obm.liveobjects.driver.wifi.base.BroadcastSubscriber;
import edu.mit.media.obm.liveobjects.driver.wifi.common.WifiManagerFacade;
import edu.mit.media.obm.liveobjects.driver.wifi.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

/**
 * Created by arata on 9/11/15.
 */
public class WifiConnector extends BroadcastSubscriber {
    @Inject WifiManagerFacade mWifiManagerFacade;
    @Inject DeviceIdTranslator mDeviceIdTranslator;

    @Inject @Named("connector") IntentFilter mIntentFilter;
    @Inject @Named("connector") BroadcastReceiver mBroadcastReceiver;

    private int mConnectingNetworkId;

    public void initialize() {
        setConnecting(false);
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

        setConnecting(true);

        String deviceId = mDeviceIdTranslator.translateFromLiveObject(liveObject);
        mConnectingNetworkId = mWifiManagerFacade.connectToNetwork(deviceId);
    }

    synchronized public void cancelConnecting() throws IllegalStateException {
        requireActivated();

        if (!isConnecting()) {
            Log.w("trying to cancel connecting, but not connecting now");
            return;
        }

        mWifiManagerFacade.disconnectFromNetwork(mConnectingNetworkId);
        setConnecting(false);
    }

    public boolean isConnecting() {
        requireActivated();

        return ((NetworkStateChangedReceiver) mBroadcastReceiver).isConnecting();
    }

    private void setConnecting(boolean connecting) {
        ((NetworkStateChangedReceiver) mBroadcastReceiver).setConnecting(connecting);
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

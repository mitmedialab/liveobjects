package edu.mit.media.obm.liveobjects.driver.wifi.connector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.noveogroup.android.log.Log;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import edu.mit.media.obm.liveobjects.driver.wifi.R;
import edu.mit.media.obm.liveobjects.driver.wifi.base.BroadcastSubscriber;
import edu.mit.media.obm.liveobjects.driver.wifi.common.WifiManagerWrapper;
import edu.mit.media.obm.liveobjects.driver.wifi.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

/**
 * Created by arata on 9/11/15.
 */
public class WifiConnector extends BroadcastSubscriber {
    private String NETWORK_PASSWORD;

    @Inject WifiManager mWifiManager;
    @Inject DeviceIdTranslator mDeviceIdTranslator;

    @Inject @Named("connector") IntentFilter mIntentFilter;
    @Inject @Named("connector") BroadcastReceiver mBroadcastReceiver;

    private int mConnectingNetworkId;

    public WifiConnector() {
        DependencyInjector.inject(this);

        initializeConstants();
    }

    public WifiConnector(Context context, WifiManager wifiManager, IntentFilter intentFilter,
                         BroadcastReceiver broadcastReceiver, DeviceIdTranslator deviceIdTranslator) {
        super(context);

        mWifiManager = wifiManager;
        mIntentFilter = intentFilter;
        mBroadcastReceiver = broadcastReceiver;
        mDeviceIdTranslator = deviceIdTranslator;

        initializeConstants();
    }

    private void initializeConstants() {
        Resources resources = context.getResources();
        NETWORK_PASSWORD = resources.getString(R.string.network_password);
    }

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
        if (isConnecting()) {
            throw new IllegalStateException("Must not try to connect when it's already connecting");
        }

        setConnecting(true);

        String deviceId = mDeviceIdTranslator.translateFromLiveObject(liveObject);

        WifiConfiguration config = WifiManagerWrapper.addNewNetwork(mWifiManager, deviceId, NETWORK_PASSWORD);
        WifiManagerWrapper.connectToConfiguredNetwork(context, mWifiManager, config, true);

        mConnectingNetworkId = config.networkId;

        mWifiManager.enableNetwork(mConnectingNetworkId, true);
    }

    synchronized public void cancelConnecting() throws IllegalStateException {
        if (!isConnecting()) {
            throw new IllegalStateException("Must not try to cancel when it's not connecting");
        }

        mWifiManager.disableNetwork(mConnectingNetworkId);
        setConnecting(false);
    }

    public boolean isConnecting() {
        return ((NetworkStateChangedReceiver) mBroadcastReceiver).isConnecting();
    }

    private void setConnecting(boolean connecting) {
        ((NetworkStateChangedReceiver) mBroadcastReceiver).setConnecting(connecting);
    }

    synchronized public void forgetNetworkConfigurations() throws IllegalStateException {
        if (isConnecting()) {
            throw new IllegalStateException("Must not try to disconnect when it's already connecting");
        }

        Log.v("deletes network configurations for all live objects");
        final List<WifiConfiguration> configurations = mWifiManager.getConfiguredNetworks();

        // configurations can be null when WiFi is disabled
        if (configurations == null) {
            return;
        }

        for (WifiConfiguration configuration: configurations) {
            String ssid = WifiManagerWrapper.unQuoteString(configuration.SSID);

            Log.v("found a network configuration for '" + ssid + "'");
            if (mDeviceIdTranslator.isLiveObject(ssid)) {
                Log.v("deleting a network configuration for live object '" + ssid + "'");
                WifiManagerWrapper.removeNetwork(mWifiManager, ssid);
            }
        }
    }
}

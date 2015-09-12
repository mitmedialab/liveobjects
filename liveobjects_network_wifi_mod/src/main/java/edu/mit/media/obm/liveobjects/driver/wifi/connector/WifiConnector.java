package edu.mit.media.obm.liveobjects.driver.wifi.connector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import com.noveogroup.android.log.Log;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import edu.mit.media.obm.liveobjects.driver.wifi.base.BroadcastSubscriber;
import edu.mit.media.obm.liveobjects.driver.wifi.R;
import edu.mit.media.obm.liveobjects.driver.wifi.common.WifiManagerWrapper;
import edu.mit.media.obm.liveobjects.driver.wifi.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkListener;

/**
 * Created by arata on 9/11/15.
 */
public class WifiConnector extends BroadcastSubscriber {
    private final String NETWORK_PASSWORD;

    private NetworkListener mNetworkListener;

    @Inject Context mContext;
    @Inject WifiManager mWifiManager;
    @Inject DeviceIdTranslator mDeviceIdTranslator;

    @Inject @Named("connector") IntentFilter mIntentFilter;
    @Inject @Named("connector") BroadcastReceiver mBroadcastReceiver;

    private boolean mConnecting;
    private int mConnectingNetworkId;

    public WifiConnector(Context context) {
        super(context);

        DependencyInjector.inject(this, context);

        Resources resources = mContext.getResources();
        NETWORK_PASSWORD = resources.getString(R.string.network_password);
    }

    public void initialize() {
        mConnecting = false;
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

        mConnecting = true;

        String deviceId = mDeviceIdTranslator.translateFromLiveObject(liveObject);

        // executes as an asynchronous task because WifiManager.getConfiguredNetwork() may block.
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                String deviceId = params[0];

                WifiConfiguration config = WifiManagerWrapper.addNewNetwork(mWifiManager, deviceId, NETWORK_PASSWORD);
                WifiManagerWrapper.connectToConfiguredNetwork(mContext, mWifiManager, config, true);

                mConnectingNetworkId = config.networkId;

                mWifiManager.enableNetwork(mConnectingNetworkId, true);
                return null;
            }
        }.execute(deviceId);
    }

    synchronized public void cancelConnecting() throws IllegalStateException{
        if (!isConnecting()) {
            throw new IllegalStateException("Must not try to cancel when it's not connecting");
        }

        mWifiManager.disableNetwork(mConnectingNetworkId);
        mConnecting = false;
    }

    public boolean isConnecting() {
        return mConnecting;
    }

    void setConnecting(boolean connecting) {
        mConnecting = connecting;
    }

    public void setNetworkListener(NetworkListener networkListener) {
        ((NetworkStateChangedReceiver) mBroadcastReceiver).setNetworkListener(networkListener);
    }

    synchronized public void forgetNetworkConfigurations() throws IllegalStateException {
        if (isConnecting()) {
            throw new IllegalStateException("Must not try to disconnect when it's already connecting");
        }

        // executes as an asynchronous task because WifiManager.getConfiguredNetwork() may block.
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                Log.v("deletes network configurations for all live objects");
                final List<WifiConfiguration> configurations = mWifiManager.getConfiguredNetworks();

                // configurations can be null when WiFi is disabled
                if (configurations == null) {
                    return null;
                }

                for (WifiConfiguration configuration: configurations) {
                    String ssid = WifiManagerWrapper.unQuoteString(configuration.SSID);

                    Log.v("found a network configuration for '" + ssid + "'");
                    if (mDeviceIdTranslator.isLiveObject(ssid)) {
                        Log.v("deleting a network configuration for live object '" + ssid + "'");
                        WifiManagerWrapper.removeNetwork(mWifiManager, ssid);
                    }
                }

                return null;
            }
        }.execute();
    }

}

package edu.mit.media.obm.liveobjects.driver.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkDriver;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkListener;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkUtil;

/**
 * This class implements a concrete driver for wifi network
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class WifiDriver implements NetworkDriver {
    private final static String LOG_TAG = WifiDriver.class.getSimpleName();

    private final String NETWORK_PASSWORD;
    protected final String SSID_PREFIX;
    private final char SSID_DELIMITER;


    private NetworkListener mNetworkListener;

    private WifiManager mWifiManager;

    private WifiReceiver mWifiReceiver;
    private boolean isWifiReceiverRegistered;

    private Context mContext;

    private IntentFilter mIntentFilter;

    private boolean mConnecting;
    private int mConnectingNetworkId;

    private NetworkUtil mNetworkUtil;

    public WifiDriver(Context context) {
        mContext = context;

        Resources resources = mContext.getResources();
        NETWORK_PASSWORD = resources.getString(R.string.network_password);
        SSID_PREFIX = resources.getString(R.string.ssid_prefix);
        // use only the first char as a delimiter
        // (ssid_delimiter should be 1 byte long string, though)
        SSID_DELIMITER = resources.getString(R.string.ssid_delimiter).charAt(0);

        int locationXLength = resources.getInteger(R.integer.map_location_coordinate_x_length);
        int locationYLength = resources.getInteger(R.integer.map_location_coordinate_y_length);
        int mapIdLength = resources.getInteger(R.integer.map_location_map_id_length);

        WifiLocationUtil.INSTANCE.setSsidFormat(
                SSID_PREFIX, SSID_DELIMITER, locationXLength, locationYLength, mapIdLength);
        mNetworkUtil = WifiLocationUtil.INSTANCE;
    }

    @Override
    public void initialize() {
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mWifiReceiver = new WifiReceiver();
        isWifiReceiverRegistered = false;

        mConnecting = false;
    }

    @Override
    synchronized public void start() {
        if (!isWifiReceiverRegistered) {
            mContext.registerReceiver(mWifiReceiver, mIntentFilter);
            isWifiReceiverRegistered = true;
        }
    }

    @Override
    synchronized public void startScan() {
        Log.v(LOG_TAG, "starting Wifi scan");
        mWifiManager.startScan();
    }

    @Override
    synchronized public void stop() {
        if (isWifiReceiverRegistered) {
            mContext.unregisterReceiver(mWifiReceiver);
            isWifiReceiverRegistered = false;
        }
    }

    @Override
    synchronized public void connect(String liveObjectName) throws IllegalStateException {
        if (isConnecting()) {
            throw new IllegalStateException("Must not try to connect when it's already connecting");
        }

        mConnecting = true;

        // executes as an asynchronous task because WifiManager.getConfiguredNetwork() may block.
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                String liveObjectName = params[0];
                LiveObject liveObject = new LiveObject(liveObjectName);
                String ssid = mNetworkUtil.convertLiveObjectToDeviceId(liveObject);

                WifiConfiguration config = WifiManagerWrapper.addNewNetwork(mWifiManager, ssid, NETWORK_PASSWORD);
                WifiManagerWrapper.connectToConfiguredNetwork(mContext, mWifiManager, config, true);

                mConnectingNetworkId = config.networkId;

                mWifiManager.enableNetwork(mConnectingNetworkId, true);
                return null;
            }
        }.execute(liveObjectName);
    }

    @Override
    synchronized public void cancelConnecting() throws IllegalStateException{
        if (!isConnecting()) {
            throw new IllegalStateException("Must not try to cancel when it's not connecting");
        }

        mWifiManager.disableNetwork(mConnectingNetworkId);
        mConnecting = false;
    }

    @Override
    public boolean isConnecting() {
        return mConnecting;
    }

    @Override
    public void setNetworkListener(NetworkListener networkListener) {
        mNetworkListener = networkListener;
    }

    @Override
    synchronized public void forgetNetworkConfigurations() throws IllegalStateException {
        if (isConnecting()) {
            throw new IllegalStateException("Must not try to disconnect when it's already connecting");
        }

        // executes as an asynchronous task because WifiManager.getConfiguredNetwork() may block.
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                Log.v(LOG_TAG, "deletes network configurations for all live objects");
                final List<WifiConfiguration> configurations = mWifiManager.getConfiguredNetworks();
                for (WifiConfiguration configuration: configurations) {
                    String ssid = WifiManagerWrapper.unQuoteString(configuration.SSID);

                    Log.v(LOG_TAG, "found a network configuration for '" + ssid + "'");
                    if (mNetworkUtil.isLiveObject(ssid)) {
                        Log.v(LOG_TAG, "deleting a network configuration for live object '" + ssid + "'");
                        WifiManagerWrapper.removeNetwork(mWifiManager, ssid);
                    }
                }

                return null;
            }
        }.execute();
    }

    class WifiReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION :
                    handleWifiScan();
                    break;

                case WifiManager.NETWORK_STATE_CHANGED_ACTION :
                    handleWifiConnection(intent);
                    break;

            }

        }

        private void handleWifiScan() {
            Log.v(LOG_TAG, "SCANNING WIFI");
            List<LiveObject> liveObjectList = new ArrayList<>();
            List<ScanResult> scanResults = mWifiManager.getScanResults();

            for (ScanResult scanResult : scanResults) {
                String deviceId = scanResult.SSID.toString();
                Log.v(LOG_TAG, "scanResult: " +  deviceId);

                if (mNetworkUtil.isLiveObject(deviceId)){
                    LiveObject liveObject = mNetworkUtil.convertDeviceIdToLiveObject(deviceId);
                    liveObject.setActive(true);

                    //network device representing a live object found
                    // add it to the list
                    liveObjectList.add(liveObject);
                }
            }
            if (!liveObjectList.isEmpty()) {
                // notifies the middleware about the presence of live-object devices
                mNetworkListener.onNetworkDevicesAvailable(liveObjectList);
            }
        }

        private void handleWifiConnection(Intent intent) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            NetworkInfo.State state = networkInfo.getState();
            Log.v(LOG_TAG, "networkInfo = " + networkInfo.toString());

            synchronized (WifiDriver.class) {
                if (state.equals(NetworkInfo.State.CONNECTED) && mConnecting == true) {
                    String ssid = networkInfo.getExtraInfo();
                    if (ssid == null) {
                        // SSID in NEtworkInfo may be null depending on the model of the device
                        ssid = mWifiManager.getConnectionInfo().getSSID();
                    }

                    ssid = WifiManagerWrapper.unQuoteString(ssid);
                    if (mNetworkUtil.isLiveObject(ssid)) {
                        LiveObject connectedLiveObject = mNetworkUtil.convertDeviceIdToLiveObject(ssid);
                        Log.d(LOG_TAG, "connectedLiveObject = " + connectedLiveObject);
                        mNetworkListener.onConnected(connectedLiveObject.getLiveObjectName());

                        mConnecting = false;
                    }
                }
            }
        }
    }
}

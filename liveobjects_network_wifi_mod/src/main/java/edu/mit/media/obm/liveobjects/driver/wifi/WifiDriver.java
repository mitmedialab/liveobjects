package edu.mit.media.obm.liveobjects.driver.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

/**
 * This class implements a concrete driver for wifi network
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class WifiDriver implements NetworkDriver {
    private final static String LOG_TAG = WifiDriver.class.getSimpleName();

    private final String NETWORK_PASSWORD;

    protected final String SSID_PREFIX;


    private NetworkListener mNetworkListener;

    private WifiManager mWifiManager;

    private WifiReceiver mWifiReceiver;

    private Context mContext;

    private IntentFilter mIntentFilter;

    private boolean mConnecting;
    private int mConnectingNetworkId;

    public WifiDriver(Context context) {
        mContext = context;
        NETWORK_PASSWORD = mContext.getResources().getString(R.string.network_password);
        SSID_PREFIX = mContext.getResources().getString(R.string.ssid_prefix);
        WifiUtil.INSTANCE.setSsidPrefix(SSID_PREFIX);

        mConnecting = false;
    }

    @Override
    public void initialize() {
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mWifiReceiver = new WifiReceiver();

    }

    @Override
    public void start() {
        mContext.registerReceiver(mWifiReceiver, mIntentFilter);
    }

    @Override
    public void startScan() {
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mWifiManager.startScan();
                return null;
            }
        }.execute();

    }

    @Override
    public void stop() {
        mContext.unregisterReceiver(mWifiReceiver);
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
                String ssid = WifiUtil.INSTANCE.convertLiveObjectNameToDeviceId(liveObjectName);

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

    private void removeDuplicatedNetwork(String ssid) {
        // scans over the entire list because there may be two or more network configurations with
        // the same SSID.
        for (WifiConfiguration wifiConfig : mWifiManager.getConfiguredNetworks()) {
            if (wifiConfig.SSID.equals(ssid)) {
                mWifiManager.removeNetwork(wifiConfig.networkId);
                Log.v(LOG_TAG, "removed a duplicated WiFi config (ssid = " + ssid + ")");
            }
        }
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
            Log.d(LOG_TAG, "SCANNING WIFI");
            List<LiveObject> liveObjectList = new ArrayList<>();
            List<ScanResult> scanResults = mWifiManager.getScanResults();

            for (ScanResult scanResult : scanResults) {
                String deviceId = scanResult.SSID.toString();
                Log.d(LOG_TAG, "scanResult: " +  deviceId);

                if (WifiUtil.INSTANCE.isLiveObject(deviceId)){
                    String liveObjectName = WifiUtil.INSTANCE.convertDeviceIdToLiveObjectName(deviceId);
                            LiveObject liveObject = new LiveObject(liveObjectName);

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
            Log.d(LOG_TAG, "networkInfo = " + networkInfo.toString());

            synchronized (WifiDriver.class) {
                if (state.equals(NetworkInfo.State.CONNECTED) && mConnecting == true) {
                    String ssid = mWifiManager.getConnectionInfo().getSSID();
                    ssid = polishSSID(ssid);
                    if (WifiUtil.INSTANCE.isLiveObject(ssid)) {
                        String connectedLiveObjectName = WifiUtil.INSTANCE.convertDeviceIdToLiveObjectName(ssid);
                        Log.d(LOG_TAG, "connectedLiveObjectName = " + connectedLiveObjectName);
                        mNetworkListener.onConnected(connectedLiveObjectName);

                        mConnecting = false;
                    }
                }
            }
        }

        // removes the "..." surrounding the ssid
        private String polishSSID(String ssid){
            return (ssid != null)? ssid.substring(1, ssid.length() - 1) : null;
        }

    }
}

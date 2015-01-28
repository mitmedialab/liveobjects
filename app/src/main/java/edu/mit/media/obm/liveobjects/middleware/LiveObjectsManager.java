package edu.mit.media.obm.liveobjects.middleware;

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

import java.util.List;



/**
 * Created by Valerio Panzica La Manna on 1/27/15.
 */
public class LiveObjectsManager {
    private static final String LOG_TAG = LiveObjectsManager.class.getSimpleName();

    public static final String EXTRA_LIVE_OBJECT = "LiveObject";

    private Context mContext;
    private WifiManager mWifiManager;
    private WifiReceiver mWifiReceiver;

    private List<ScanResult> mWifiList;
    static String[] NETWORKS;

    private static final String NETWORK_PASSWORD = "12345678";

    private LiveObject liveObjectToConnect = null;

    public interface DiscoveryListener {

        public void onDiscoveryStarted();

        public void onLiveObjectsAvailable(LiveObjectsList liveObjectsList);

    }

    public interface ConnectionListener {

        public void onConnectionStarted();

        public void onDisconnected();

        public void onConnected(LiveObject connectedLiveObject);

    }

    private DiscoveryListener mDiscoveryListener;
    private ConnectionListener mConnectionListener;

    public void initialize(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiReceiver = new WifiReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        context.registerReceiver(mWifiReceiver, intentFilter);

    }

    public void setDiscoveryListener(DiscoveryListener discoveryListener) {
        mDiscoveryListener = discoveryListener;
    }

    public void setConnectionListener(ConnectionListener connectionListener) {
        mConnectionListener = connectionListener;
    }

    public void startDiscovery() {

        new AsyncTask<Void, Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mWifiManager.startScan();
                mDiscoveryListener.onDiscoveryStarted();
                return null;
            }
        }.execute();

    }

    public void connect(LiveObject liveObject) {
        liveObjectToConnect = liveObject;
        String[] params = new String[1];
        params[0] = liveObjectToConnect.getName();
        new AsyncTask<String, Void, Void>(){
            @Override
            protected Void doInBackground(String... params) {
                String liveObjectName = params[0];
                String networkSSID = Util.convertLiveObjectNameToSSID(liveObjectName);
                WifiConfiguration conf = new WifiConfiguration();
                conf.SSID = "\"" + networkSSID + "\"";
                conf.preSharedKey = "\"" + NETWORK_PASSWORD + "\"";
                mWifiManager.addNetwork(conf);
                int netId = mWifiManager.addNetwork(conf);
                mWifiManager.disconnect();
                mWifiManager.enableNetwork(netId, true);
                mWifiManager.reconnect();
                mConnectionListener.onConnectionStarted();
                return null;
            }
        }.execute(params);

    }

    public void disconnect() {
        mWifiManager.disconnect();
    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION :
                    LiveObjectsList liveObjectsList = new LiveObjectsList();
                    Log.d(LOG_TAG, "SCANNING WIFI");
                    mWifiList = mWifiManager.getScanResults();
                    NETWORKS = new String[mWifiList.size()];
                    for (ScanResult scanResult : mWifiList) {
                        Log.d(LOG_TAG, "scanResult: " + scanResult.SSID.toString());
                        if (Util.isLiveObject(scanResult.SSID)){
                            String liveObjectName = Util.convertSSIDToLiveObjectName(scanResult.SSID);
                            LiveObject liveObject = new LiveObject(liveObjectName);
                            liveObjectsList.add(liveObject);
                        }
                    }
                    if (!liveObjectsList.isEmpty()) {
                        mDiscoveryListener.onLiveObjectsAvailable(liveObjectsList);
                    }

                    break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION :
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    NetworkInfo.State state = networkInfo.getState();
                    if(state.equals(NetworkInfo.State.CONNECTED))
                    {

                        String ssid = mWifiManager.getConnectionInfo().getSSID();
                        // removing "s surrounding ssid
                        ssid = (ssid != null)? ssid.substring(1, ssid.length() - 1) : null;
                        if (Util.isLiveObject(ssid)) {
                            String connectedLiveObjectName = Util.convertSSIDToLiveObjectName(ssid);
                            Log.d(LOG_TAG, "connectedLiveObjectName = " + connectedLiveObjectName);
                            mConnectionListener.onConnected(liveObjectToConnect);
                        }
                    }
                    else if (state.equals(NetworkInfo.State.DISCONNECTED)) {
                        mConnectionListener.onDisconnected();
                    }

            }

        }

    }

}

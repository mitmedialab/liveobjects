package edu.mit.media.obm.liveobjects.driver.wifi;

import android.bluetooth.BluetoothClass;
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

import com.noveogroup.android.log.Log;

import java.util.ArrayList;
import java.util.List;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkListener;

/**
 * Created by arata on 9/11/15.
 */
public class WifiScanner {
    private NetworkListener mNetworkListener;
    private WifiManager mWifiManager;
    private WifiReceiver mWifiReceiver;
    private Context mContext;
    private IntentFilter mIntentFilter;

    private DeviceIdTranslator mDeviceIdTranslator;

    public WifiScanner(Context context, DeviceIdTranslator deviceIdTranslator) {
        mContext = context;
        mDeviceIdTranslator = deviceIdTranslator;
    }

    public void initialize() {
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mWifiReceiver = new WifiReceiver();
    }

    protected void start() {
        mContext.registerReceiver(mWifiReceiver, mIntentFilter);
    }

    protected void stop() {
        mContext.unregisterReceiver(mWifiReceiver);
    }

    synchronized public void startScan() {
        Log.v("starting Wifi scan");
        mWifiManager.startScan();
    }

    public void setNetworkListener(NetworkListener networkListener) {
        mNetworkListener = networkListener;
    }

    class WifiReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION :
                    handleWifiScan();
                    break;
            }

        }

        private void handleWifiScan() {
            Log.v("SCANNING WIFI");
            List<LiveObject> liveObjectList = new ArrayList<>();
            List<ScanResult> scanResults = mWifiManager.getScanResults();

            for (ScanResult scanResult : scanResults) {
                String deviceId = scanResult.SSID.toString();
                Log.v("scanResult: " +  deviceId);

                if (mDeviceIdTranslator.isLiveObject(deviceId)) {
                    LiveObject liveObject = mDeviceIdTranslator.translateToLiveObject(deviceId);
                    liveObject.setStatus(LiveObject.STATUS_ACTIVE);

                    //network device representing a live object found
                    // add it to the list
                    liveObjectList.add(liveObject);
                }
            }

            // notifies the middleware about the presence of live-object devices
            // notifies even if no live-objects are discovered
            mNetworkListener.onNetworkDevicesAvailable(liveObjectList);
        }
    }
}

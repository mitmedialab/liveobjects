package edu.mit.media.obm.liveobjects.driver.wifi.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import com.noveogroup.android.log.Log;

import edu.mit.media.obm.liveobjects.driver.wifi.base.BroadcastSubscriber;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkListener;

/**
 * Created by arata on 9/11/15.
 */
public class WifiScanner extends BroadcastSubscriber {
    private NetworkListener mNetworkListener;
    private WifiManager mWifiManager;
    private Context mContext;
    private DeviceIdTranslator mDeviceIdTranslator;

    public WifiScanner(Context context, DeviceIdTranslator deviceIdTranslator) {
        super(context);

        mContext = context;
        mDeviceIdTranslator = deviceIdTranslator;
    }

    public void initialize() {
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    synchronized public void startScan() {
        Log.v("starting Wifi scan");
        mWifiManager.startScan();
    }

    public void setNetworkListener(NetworkListener networkListener) {
        mNetworkListener = networkListener;
    }

    @Override
    protected final BroadcastReceiver createBroadcastReceiver() {
        return new ScanResultsReceiver(mDeviceIdTranslator, mNetworkListener);
    }

    @Override
    protected final IntentFilter createIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        return intentFilter;
    }

}

package edu.mit.media.obm.liveobjects.driver.wifi.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import com.noveogroup.android.log.Log;

import javax.inject.Inject;
import javax.inject.Named;

import edu.mit.media.obm.liveobjects.driver.wifi.base.BroadcastSubscriber;
import edu.mit.media.obm.liveobjects.driver.wifi.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkListener;

/**
 * Created by arata on 9/11/15.
 */
public class WifiScanner extends BroadcastSubscriber {
    @Inject WifiManager mWifiManager;
    @Inject @Named("scanner") IntentFilter mIntentFilter;
    @Inject @Named("scanner") BroadcastReceiver mBroadcastReceiver;

    public WifiScanner(Context context) {
        super(context);

        DependencyInjector.inject(this, context);
    }

    public void initialize() {
    }

    synchronized public void startScan() {
        Log.v("starting Wifi scan");
        mWifiManager.startScan();
    }

    public void setNetworkListener(NetworkListener networkListener) {
        ((ScanResultsReceiver) mBroadcastReceiver).setNetworkListener(networkListener);
    }

    @Override
    protected final BroadcastReceiver createBroadcastReceiver() {
        return mBroadcastReceiver;
    }

    @Override
    protected final IntentFilter createIntentFilter() {
        return mIntentFilter;
    }
}
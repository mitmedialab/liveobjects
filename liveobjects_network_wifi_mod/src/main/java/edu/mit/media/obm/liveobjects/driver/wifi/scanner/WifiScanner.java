package edu.mit.media.obm.liveobjects.driver.wifi.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import com.noveogroup.android.log.Log;

import javax.inject.Inject;
import javax.inject.Named;

import edu.mit.media.obm.liveobjects.driver.wifi.base.BroadcastSubscriber;

/**
 * Created by arata on 9/11/15.
 */
public class WifiScanner extends BroadcastSubscriber {
    @Inject WifiManager mWifiManager;
    @Inject @Named("scanner") IntentFilter mIntentFilter;
    @Inject @Named("scanner") BroadcastReceiver mBroadcastReceiver;

    synchronized public void startScan() {
        requireActivated();

        Log.v("starting Wifi scan");
        mWifiManager.startScan();
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

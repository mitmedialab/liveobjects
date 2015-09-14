package edu.mit.media.obm.liveobjects.driver.wifi.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import com.noveogroup.android.log.Log;

import javax.inject.Inject;
import javax.inject.Named;

import edu.mit.media.obm.liveobjects.driver.wifi.base.BroadcastSubscriber;
import edu.mit.media.obm.liveobjects.driver.wifi.common.WifiManagerFacade;

/**
 * Created by arata on 9/11/15.
 */
public class WifiScanner extends BroadcastSubscriber {
    WifiManagerFacade mWifiManagerFacade;
    IntentFilter mIntentFilter;
    BroadcastReceiver mBroadcastReceiver;

    @Inject
    public WifiScanner(Context context, WifiManagerFacade wifiManagerFacade, IntentFilter intentFilter,
                       BroadcastReceiver broadcastReceiver) {
        super(context);

        mWifiManagerFacade = wifiManagerFacade;
        mIntentFilter = intentFilter;
        mBroadcastReceiver = broadcastReceiver;
    }

    synchronized public void startScan() {
        requireActivated();

        Log.v("starting Wifi scan");
        mWifiManagerFacade.startScan();
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

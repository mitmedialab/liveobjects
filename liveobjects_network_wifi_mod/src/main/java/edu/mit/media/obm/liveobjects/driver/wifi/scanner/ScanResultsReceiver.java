package edu.mit.media.obm.liveobjects.driver.wifi.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.noveogroup.android.log.Log;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.driver.wifi.event.NetworkDevicesAvailableEvent;
import edu.mit.media.obm.liveobjects.driver.wifi.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkListener;

/**
 * Created by arata on 9/11/15.
 */
public class ScanResultsReceiver extends BroadcastReceiver {
    @Inject WifiManager wifiManager;
    @Inject DeviceIdTranslator deviceIdTranslator;
    @Inject Bus bus;

    public ScanResultsReceiver(Context context) {
        DependencyInjector.inject(this, context);
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                handleWifiScan();
                break;
        }
    }

    private void handleWifiScan() {
        Log.v("SCANNING WIFI");
        List<LiveObject> liveObjectList = new ArrayList<>();
        List<ScanResult> scanResults = wifiManager.getScanResults();

        for (ScanResult scanResult : scanResults) {
            String deviceId = scanResult.SSID.toString();
            Log.v("scanResult: " + deviceId);

            if (deviceIdTranslator.isLiveObject(deviceId)) {
                LiveObject liveObject = deviceIdTranslator.translateToLiveObject(deviceId);
                liveObject.setStatus(LiveObject.STATUS_ACTIVE);

                //network device representing a live object found
                // add it to the list
                liveObjectList.add(liveObject);
            }
        }

        // notifies the middleware about the presence of live-object devices
        // notifies even if no live-objects are discovered
        NetworkDevicesAvailableEvent event = new NetworkDevicesAvailableEvent(liveObjectList);
        bus.post(event);
    }
}

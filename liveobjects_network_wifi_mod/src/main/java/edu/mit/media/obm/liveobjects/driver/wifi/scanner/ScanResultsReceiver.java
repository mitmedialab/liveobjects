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

import edu.mit.media.obm.liveobjects.driver.wifi.common.WifiManagerFacade;
import edu.mit.media.obm.liveobjects.driver.wifi.event.NetworkDevicesAvailableEvent;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

/**
 * Created by arata on 9/11/15.
 */
public class ScanResultsReceiver extends BroadcastReceiver {
    WifiManagerFacade wifiManagerFacade;
    DeviceIdTranslator deviceIdTranslator;
    Bus bus;

    @Inject
    public ScanResultsReceiver(
            WifiManagerFacade wifiManagerFacade, DeviceIdTranslator deviceIdTranslator, Bus bus) {
        this.wifiManagerFacade = wifiManagerFacade;
        this.deviceIdTranslator = deviceIdTranslator;
        this.bus = bus;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            handleWifiScan();
        } else {
            throw new IllegalArgumentException("unexpected action received: " + action);
        }
    }

    private void handleWifiScan() {
        Log.v("SCANNING WIFI");
        List<LiveObject> liveObjectList = new ArrayList<>();
        List<ScanResult> scanResults = wifiManagerFacade.getScanResults();

        for (ScanResult scanResult : scanResults) {
            String deviceId = scanResult.SSID.toString();
            Log.v("scanResult: " + deviceId);

            if (deviceIdTranslator.isValidSsid(deviceId)) {
                LiveObject liveObject = deviceIdTranslator.translateToLiveObject(deviceId);
                liveObject.setStatus(LiveObject.STATUS_ACTIVE);

                //network device representing a live object found
                // add it to the list
                liveObjectList.add(liveObject);
            }
        }

        // notifies the middleware about the presence of live-object devices
        // notifies even if no live-objects are discovered
        // post an event even when no live objects are detected to notify that a discovery process finished
        NetworkDevicesAvailableEvent event = new NetworkDevicesAvailableEvent(liveObjectList);
        bus.post(event);
    }
}

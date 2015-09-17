package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import com.noveogroup.android.log.Log;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.apptidmarsh.utils.FinishedDetectingInactiveLiveObjectEvent;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.LiveObjectNotifier;
import edu.mit.media.obm.liveobjects.driver.wifi.event.NetworkDevicesAvailableEvent;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;

/**
 * Created by arata on 9/17/15.
 */
public class DiscoveryRunner {
    NetworkController mNetworkController;
    LiveObjectNotifier mLiveObjectNotifier;
    Bus mBus;

    private boolean wifiDiscoveryProcessRunning = false;
    private boolean bluetoothDiscoveryProcessRunning = false;

    @Inject
    public DiscoveryRunner(NetworkController networkController, LiveObjectNotifier liveObjectNotifier, Bus bus) {
        mNetworkController = networkController;
        mLiveObjectNotifier = liveObjectNotifier;
        mBus = bus;
    }

    public void startDiscovery() {
        mBus.register(this);

        if (!wifiDiscoveryProcessRunning) {
            Log.v("starting WiFi discovery");
            mNetworkController.startDiscovery();
            wifiDiscoveryProcessRunning = true;
        }

        if (!bluetoothDiscoveryProcessRunning) {
            Log.v("starting Bluetooth discovery");
            mLiveObjectNotifier.wakeUp();
            bluetoothDiscoveryProcessRunning = true;
        }
    }

    public void stopDiscovery() {
        mBus.unregister(this);

        Log.v("stop discovery");
        /* no way to stop WiFi discovery because it continues running until Broadcast Receiver is
           unregistered? */
        wifiDiscoveryProcessRunning = false;

        if (bluetoothDiscoveryProcessRunning) {
            mLiveObjectNotifier.cancelWakeUp();
            bluetoothDiscoveryProcessRunning = false;
        }
    }

    @Subscribe
    public void finalizeWifiDiscovery(NetworkDevicesAvailableEvent event) {
        Log.v("finalizeWifiDiscovery()");
        wifiDiscoveryProcessRunning = false;
    }

    @Subscribe
    public void finalizeBluetoothDiscovery(FinishedDetectingInactiveLiveObjectEvent event) {
        Log.v("finalizeBluetoothDiscovery()");
        bluetoothDiscoveryProcessRunning = false;
    }
}

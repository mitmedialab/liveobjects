package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import com.noveogroup.android.log.Log;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import edu.mit.media.obm.liveobjects.apptidmarsh.utils.FinishedDetectingInactiveLiveObjectEvent;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.LiveObjectNotifier;
import edu.mit.media.obm.liveobjects.driver.wifi.event.NetworkDevicesAvailableEvent;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;

/**
 * Created by arata on 9/17/15.
 */
public class DiscoveryRunner extends BusListener {
    NetworkController mNetworkController;
    LiveObjectNotifier mLiveObjectNotifier;
    Bus mBus;
    Bus mNetworkConnectionBus;

    public boolean wifiDiscoveryProcessRunning = false;
    public boolean bluetoothDiscoveryProcessRunning = false;

    @Inject
    public DiscoveryRunner(NetworkController networkController, LiveObjectNotifier liveObjectNotifier,
                           Bus bus, @Named("network_wifi") Bus networkConnectionBus) {
        mNetworkController = networkController;
        mLiveObjectNotifier = liveObjectNotifier;
        mBus = bus;
        mNetworkConnectionBus = networkConnectionBus;
    }

    @Override
    protected List<Bus> getBuses() {
        return Arrays.asList(mBus, mNetworkConnectionBus);
    }

    public void startDiscovery() {
        registerBuses();

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
        unregisterBuses();

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

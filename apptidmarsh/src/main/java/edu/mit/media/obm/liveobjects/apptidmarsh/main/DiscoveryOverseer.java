package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import com.noveogroup.android.log.Log;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectPropertyProvider;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.FinishedDetectingInactiveLiveObjectEvent;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.InactiveLiveObjectDetectionEvent;
import edu.mit.media.obm.liveobjects.driver.wifi.event.NetworkDevicesAvailableEvent;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.common.MapLocation;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;

/**
 * Created by arata on 9/18/15.
 */
public class DiscoveryOverseer extends BusListener {
    DbController mDbController;
    DiscoveryInfo mDiscoveryInfo;
    DiscoveryRunner mDiscoveryRunner;
    Bus mBus;
    Bus mNetworkWifiBus;

    @Inject
    public DiscoveryOverseer(DbController dbController, DiscoveryInfo discoveryInfo,
                             DiscoveryRunner discoveryRunner, Bus bus, @Named("network_wifi") Bus networkWifiBus) {
        mDbController = dbController;
        mDiscoveryInfo = discoveryInfo;
        mDiscoveryRunner = discoveryRunner;
        mBus = bus;
        mNetworkWifiBus = networkWifiBus;
    }

    @Override
    protected List<Bus> getBuses() {
        return Arrays.asList(mBus, mNetworkWifiBus);
    }

    public void startDiscovery() {
        registerBuses();
        mDiscoveryRunner.startDiscovery();
    }

    public void stopDiscovery() {
        unregisterBuses();
        mDiscoveryRunner.stopDiscovery();
    }

    public LiveObject findLiveObject(String name) {
        LiveObject foundLiveObject = null;

        for (LiveObject liveObject : mDiscoveryInfo.getAllLiveObjects()) {
            String liveObjectName = liveObject.getName();

            if (name.equals(liveObjectName)) {
                foundLiveObject = liveObject;
            }
        }

        return foundLiveObject;
    }

    @Subscribe
    public void updateDiscoveredLiveObjectList(NetworkDevicesAvailableEvent event) {
        List<LiveObject> discoveredLiveObjects = event.getAvailableLiveObjects();

        Log.d("discovery successfully completed");
        mDiscoveryInfo.clearActiveLiveObject();
        Log.v("===");
        for (LiveObject liveObject : discoveredLiveObjects) {
            Log.v(liveObject.getName() + ", " + liveObject.getMapLocation().toString());
            mDiscoveryInfo.addActiveLiveObject(liveObject);
        }

        notifyDiscoveredLiveObjectChanged();
    }

    @Subscribe
    public void addDetectedBluetoothDevice(InactiveLiveObjectDetectionEvent event) {
        Log.v("addDetectedBluetoothDevice()");
        LiveObject liveObject = event.mLiveObject;
        mDiscoveryInfo.addSleepingLiveObject(liveObject);

        notifyDiscoveredLiveObjectChanged();
    }

    @Subscribe
    public void clearDetectedSleepingLiveObjects(FinishedDetectingInactiveLiveObjectEvent event) {
        Log.v("clearDetectedSleepingLiveObjects()");
        mDiscoveryInfo.clearSleepingLiveObject();
    }

    private void notifyDiscoveredLiveObjectChanged() {
        synchronizeWithDatabase();

        List<LiveObject> liveObjects = mDiscoveryInfo.getAllLiveObjects();
        DiscoveredLiveObjectUpdateEvent event = new DiscoveredLiveObjectUpdateEvent(liveObjects);

        mNetworkWifiBus.post(event);
    }

    public void synchronizeWithDatabase() {
        mDiscoveryInfo.clearLostLiveObject();

        List<Map<String, Object>> allLiveObjects = mDbController.getAllLiveObjectsProperties();
        for (Map<String, Object> liveObjectProperties : allLiveObjects) {
            MLProjectPropertyProvider provider = new MLProjectPropertyProvider(liveObjectProperties);
            String liveObjectName = provider.getId();
            MapLocation mapLocation = new MapLocation(
                    provider.getMapLocationX(), provider.getMapLocationY(), provider.getMapId());

            LiveObject liveObject = new LiveObject(liveObjectName, mapLocation);
            mDiscoveryInfo.addLostLiveObject(liveObject);
        }
    }

    public static class DiscoveredLiveObjectUpdateEvent {
        private final List<LiveObject> mLiveObjects;

        public DiscoveredLiveObjectUpdateEvent(List<LiveObject> liveObjects) {
            mLiveObjects = liveObjects;
        }

        public List<LiveObject> getLiveObjects() {
            return mLiveObjects;
        }
    }
}

package edu.mit.media.obm.liveobjects.apptidmarsh.utils;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.driver.wifi.WifiLocationUtil;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;

/**
 * Created by arata on 8/7/15.
 */
public class BeaconNotifier extends LiveObjectNotifier implements BeaconConsumer {
    private static final String LOG_TAG = BeaconNotifier.class.getSimpleName();

    @Inject Context mContext;
    @Inject BeaconManager mBeaconManager;

    @Inject BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    @Inject AdvertiseData mAdvertiseData;
    @Inject AdvertiseSettings mAdvertiseSettings;

    AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d(LOG_TAG, "onStartSuccess()");
            Log.d(LOG_TAG, settingsInEffect.toString());
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.d(LOG_TAG, "onStartFailure()");
            Log.d(LOG_TAG, Integer.toString(errorCode));
        }
    };

    public BeaconNotifier(Context appContext) {
        super(appContext);

        DependencyInjector.inject(this, appContext);
    }

    @Override
    public synchronized void wakeUp() {
        debug("wakeUp()");
        mBeaconManager.bind(this);

        mBluetoothLeAdvertiser.startAdvertising(
                mAdvertiseSettings, mAdvertiseData, mAdvertiseCallback);
    }

    @Override
    public synchronized void cancelWakeUp() {
        mBeaconManager.unbind(this);
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }

    @Override
    public void onBeaconServiceConnect() {
        debug("onBeaconServiceConnect()");
        mBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                debug("didRangeBeaconsInRegion()");
                debug(region.toString());
                for (Beacon beacon : collection) {
                    debug(beacon.toString());

                    String beaconId = beacon.getId1().toString();
                    beaconId = "liveobj-" + hexStringToString(beaconId.trim());
                    if (WifiLocationUtil.INSTANCE.isLiveObject(beaconId)) {
                        debug("detected live object: " + beaconId);

                        // ToDo; shouldn't use WiFiUtil directly
                        LiveObject liveObject = WifiLocationUtil.INSTANCE.convertDeviceIdToLiveObject(beaconId);

                        final InactiveLiveObjectDetectionEvent event =
                                new InactiveLiveObjectDetectionEvent(liveObject);
                        Handler handler = new Handler(mContext.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                debug("posting an event to the event bus");
                                mBus.post(event);
                            }
                        });
                    }
                }
            }
        });

        try {
            Region region = new Region("myMonitoringUniqueId", null, null, null);
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Context getApplicationContext() {
        return mContext;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        mContext.unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return mContext.bindService(intent, serviceConnection, i);
    }

    private String hexStringToString(String hexString) {
        if (hexString.startsWith("0x")) {
            hexString = hexString.substring(2);
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < hexString.length(); i += 2) {
            String hexChar = hexString.substring(i, i + 2);
            char charValue = (char)Integer.parseInt(hexChar, 16);
            builder.append(charValue);
        }

        return builder.toString();
    }

    private void debug(String message) {
        Log.d(LOG_TAG, message);
    }
}

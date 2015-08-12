package edu.mit.media.obm.liveobjects.apptidmarsh.utils;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;

/**
 * Created by arata on 8/7/15.
 */
public class BeaconNotifier extends LiveObjectNotifier implements BeaconConsumer {
    private static final String LOG_TAG = BeaconNotifier.class.getSimpleName();

    @Inject Context mContext;
    @Inject BeaconManager mBeaconManager;

    public BeaconNotifier(Context appContext) {
        super(appContext);

        DependencyInjector.inject(this, appContext);
    }

    @Override
    public synchronized void wakeUp() {
        debug("wakeUp()");
        mBeaconManager.bind(this);
    }

    @Override
    public synchronized void cancelWakeUp() {
        mBeaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        debug("onBeaconServiceConnect()");
        mBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                debug("didEnterRegion()");
                debug(region.toString());
            }

            @Override
            public void didExitRegion(Region region) {
                debug("didExitRegion()");
                debug(region.toString());
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                debug("didDetermineStateForRegion()");
                debug(region.toString());
            }
        });

        try {
            Region region = new Region("myMonitoringUniqueId", null, null, null);
            mBeaconManager.startMonitoringBeaconsInRegion(region);
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

    private void debug(String message) {
        Log.d(LOG_TAG, message);
    }
}

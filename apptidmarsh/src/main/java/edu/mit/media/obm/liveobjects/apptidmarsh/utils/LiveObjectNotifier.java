package edu.mit.media.obm.liveobjects.apptidmarsh.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;

/**
 * Created by arata on 7/14/15.
 */
public abstract class LiveObjectNotifier {
    protected Context mContext;

    @Inject protected Bus mBus;

    LiveObjectNotifier(Context appContext) {
        mContext = appContext;

        DependencyInjector.inject(this, mContext);
    }

    abstract public void wakeUp();

    abstract public void cancelWakeUp();

    // This method shouldn't be defined in this generic class
    protected boolean isBleSupported() {
        PackageManager packageManager = mContext.getPackageManager();

        return packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    // This method shouldn't be defined in this generic class
    protected void promptEnablingBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(enableBtIntent);
    }
}

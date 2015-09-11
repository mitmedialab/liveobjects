package edu.mit.media.obm.liveobjects.apptidmarsh.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.noveogroup.android.log.Log;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.driver.wifi.PositionedSsidTranslator;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;

/**
 * Created by arata on 8/7/15.
 */
public class BluetoothNotifier extends LiveObjectNotifier {
    @Inject BluetoothAdapter mBluetoothAdapter;
    private BluetoothDetectionReceiver mBroadcastReceiver = null;

    public BluetoothNotifier(Context appContext) {
        super(appContext);

        DependencyInjector.inject(this, appContext);

        if (mBluetoothAdapter == null) {
            throw new RuntimeException("Failed to get default Bluetooth Adapter");
        }

        if (!isBleSupported()) {
            throw new RuntimeException("This device doesn't support Bluetooth Low Energy");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            promptEnablingBluetooth();
        }
    }

    @Override
    public synchronized void wakeUp() {
        cancelWakeUp();

        Log.d("start awakening...");

        mBroadcastReceiver = new BluetoothDetectionReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        mContext.registerReceiver(mBroadcastReceiver, filter);

        mBluetoothAdapter.startDiscovery();

        Log.d("num bonded devices:" + mBluetoothAdapter.getBondedDevices().size());
        for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
            Log.d("device = " + device.getName());
        }
    }

    @Override
    public synchronized void cancelWakeUp() {
        Log.d("cancel awakening...");

        if (mBroadcastReceiver != null) {
            mBluetoothAdapter.cancelDiscovery();
            mContext.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private class BluetoothDetectionReceiver extends BroadcastReceiver {
        private BluetoothGatt mBluetoothGatt = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();

                if (deviceName != null) {
                    Log.d("detected device: " + deviceName);
                }

                // ToDo; shouldn't use WiFiUtil directly
                if (deviceName != null && PositionedSsidTranslator.INSTANCE.isLiveObject(deviceName)) {
                    Log.d("trying to connect to BLE device '%s'", deviceName);
                    mBluetoothGatt = device.connectGatt(mContext, true, mGattCallback);

                    // ToDo; shouldn't use WiFiUtil directly
                    LiveObject liveObject = PositionedSsidTranslator.INSTANCE.translateToLiveObject(deviceName);
                    mBus.post(new InactiveLiveObjectDetectionEvent(liveObject));
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("finished BLE discovery");
                cancelWakeUp();

                mBus.post(new FinishedDetectingInactiveLiveObjectEvent());
            }
        }

        private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d("disconecting from GATT");
                    mBluetoothGatt.disconnect();
                }
            }
        };
    }
}

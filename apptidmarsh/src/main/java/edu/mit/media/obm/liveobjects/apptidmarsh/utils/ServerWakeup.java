package edu.mit.media.obm.liveobjects.apptidmarsh.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.apptidmarsh.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.driver.wifi.WifiUtil;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkUtil;

/**
 * Created by arata on 7/14/15.
 */
public class ServerWakeup {
    private static final String LOG_TAG = ServerWakeup.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDetectionReceiver mBroadcastReceiver = null;

    private Context mContext;

    @Inject Bus mBus;

    public ServerWakeup(Context appContext) {
        LiveObjectsApplication app = (LiveObjectsApplication) appContext;
        app.injectObjectGraph(this);

        mContext = appContext;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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

    public synchronized void wakeUp() {
        cancelWakeUp();

        debug("start awakening...");

        mBroadcastReceiver = new BluetoothDetectionReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        mContext.registerReceiver(mBroadcastReceiver, filter);

        mBluetoothAdapter.startDiscovery();

        debug("num bonded devices:" + mBluetoothAdapter.getBondedDevices().size());
        for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
            debug("device = " + device.getName());
        }
    }

    public synchronized void cancelWakeUp() {
        debug("cancel awakening...");

        if (mBroadcastReceiver != null) {
            mBluetoothAdapter.cancelDiscovery();
            mContext.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private boolean isBleSupported() {
        PackageManager packageManager = mContext.getPackageManager();

        return packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private void promptEnablingBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(enableBtIntent);
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
                    debug("detected device: " + deviceName);
                }

                // ToDo; shouldn't use WiFiUtil directly
                if (deviceName != null && WifiUtil.INSTANCE.isLiveObject(deviceName)) {
                    debug(String.format("trying to connect to BLE device '%s'", deviceName));
                    mBluetoothGatt = device.connectGatt(mContext, true, mGattCallback);

                    Toast.makeText(mContext, String.format("Awakening '%s'", deviceName), Toast.LENGTH_SHORT).show();

                    // ToDo; shouldn't use WiFiUtil directly
                    String liveObjectName =
                            WifiUtil.INSTANCE.convertDeviceIdToLiveObjectName(deviceName);
                    mBus.post(new DeviceDetectedEvent(liveObjectName));
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                debug("finished BLE discovery");
                cancelWakeUp();
            }
        }

        private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    debug("disconecting from GATT");
                    mBluetoothGatt.disconnect();
                }
            }
        };
    }

    public class DeviceDetectedEvent {
        public final String mDeviceName;

        public DeviceDetectedEvent(String deviceName) {
            mDeviceName = deviceName;
        }
    }

    private void debug(String message) {
        Log.d(LOG_TAG, message);
    }
}

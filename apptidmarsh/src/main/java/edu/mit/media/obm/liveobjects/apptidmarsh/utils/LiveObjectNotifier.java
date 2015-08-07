package edu.mit.media.obm.liveobjects.apptidmarsh.utils;

import android.bluetooth.BluetoothAdapter;
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
import edu.mit.media.obm.liveobjects.driver.wifi.WifiLocationUtil;
import edu.mit.media.obm.liveobjects.driver.wifi.WifiUtil;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;

/**
 * Created by arata on 7/14/15.
 */
public interface LiveObjectNotifier {
    void wakeUp();
    void cancelWakeUp();
}

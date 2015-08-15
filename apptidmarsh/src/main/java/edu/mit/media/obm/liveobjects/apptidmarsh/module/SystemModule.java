package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.MediaController;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;

import java.util.UUID;

import butterknife.BindInt;
import butterknife.BindString;
import butterknife.ButterKnife;
import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.media.VideoViewFragment;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.BleUtil;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.BluetoothNotifier;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.AnimationArrayAdapter;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * Created by arata on 8/3/15.
 */
@Module(
        library = true,
        complete = false,
        injects = {
                VideoViewFragment.class,
                AnimationArrayAdapter.class,
                BluetoothNotifier.class
        }
)
public class SystemModule {
    Context mContext;

    @BindString(R.string.ibeacon_notification_uuid) String IBEACON_NOTIFICATION_UUID;
    @BindInt(R.integer.ibeacon_notification_major) int IBEACON_NOTIFICATION_MAJOR;
    @BindInt(R.integer.ibeacon_notification_minor) int IBEACON_NOTIFICATION_MINOR;
    @BindInt(R.integer.ibeacon_notification_txpower) int IBEACON_NOTIFICATION_TXPOWER;

    public SystemModule(Context context) {
        mContext = context;
        ButterKnife.bind(this, (Activity)mContext);
    }

    @Provides Context provideContext() {
        return mContext;
    }

    @Provides LayoutInflater provideLayoutInflater(Context context) {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Provides MediaController provideMediaController(Context context) {
        return new MediaController(context);
    }

    @Provides BluetoothAdapter provideBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    @Provides BeaconManager provideBeaconManager(Context context) {
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(context);
        BeaconParser beaconParser = new BeaconParser().setBeaconLayout(
                "m:2-3=0215,i:4-23,p:24-24");
        beaconManager.getBeaconParsers().add(beaconParser);

        return beaconManager;
    }

    @Provides BluetoothLeAdvertiser provideBluetoothLeAdvertiser(BluetoothAdapter adapter) {
        return adapter.getBluetoothLeAdvertiser();
    }

    @Provides AdvertiseData provideAdvertiseData() {
        return BleUtil.createIBeaconAdvertiseData(
                UUID.fromString(IBEACON_NOTIFICATION_UUID),

                (short) IBEACON_NOTIFICATION_MAJOR,
                (short) IBEACON_NOTIFICATION_MINOR,
                (byte) IBEACON_NOTIFICATION_TXPOWER);
    }

    @Provides AdvertiseSettings provideAdvertiseSetting() {
        return BleUtil.createAdvSettings(
                false,
                0);
    }
}

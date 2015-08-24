package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.app.AlarmManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.MediaController;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.main.MainActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.media.VideoViewFragment;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.BluetoothNotifier;

/**
 * Created by arata on 8/3/15.
 */
@Module(
        library = true,
        complete = false,
        injects = {
                VideoViewFragment.class,
                BluetoothNotifier.class,
                MainActivity.class


        }
)
public class SystemModule {
    Context mContext;


    public SystemModule(Context context) {
        mContext = context;
        //ButterKnife.bind(this, (Activity)mContext);
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

    @Provides AlarmManager provideAlarmManager(Context context) {
        return (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
    }

}

package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.MediaController;

import org.altbeacon.beacon.BeaconManager;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.media.VideoViewFragment;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.BluetoothNotifier;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.AnimationArrayAdapter;

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

    public SystemModule(Context context) {
        mContext = context;
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
        return BeaconManager.getInstanceForApplication(context);
    }
}

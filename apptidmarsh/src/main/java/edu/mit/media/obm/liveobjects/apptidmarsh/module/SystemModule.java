package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.MediaController;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.media.VideoViewFragment;

/**
 * Created by arata on 8/3/15.
 */
@Module(
        library = true,
        complete = false,
        injects = {
                VideoViewFragment.class,
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
}

package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.content.Context;

import com.squareup.otto.Bus;

import org.altbeacon.beacon.Beacon;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.BeaconNotifier;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.BluetoothNotifier;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.LiveObjectNotifier;

/**
 * Created by artimo14 on 8/1/15.
 */
@Module(
        library = true,
        complete = false,
        includes = SystemModule.class,
        injects = {
                BluetoothNotifier.class,
                BeaconNotifier.class
        }
)
public class ApplicationModule {
    public ApplicationModule() {
    }

    @Provides
    LiveObjectNotifier provideLiveObjectNotifier(Context context) {
        return new BluetoothNotifier(context);
    }

    @Provides @Singleton Bus provideBus() {
        return new Bus();
    }
}

package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.content.Context;
import android.util.Log;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
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
                BluetoothNotifier.class

        }
)
public class ApplicationModule {
    private static Bus mBus = null;

    public ApplicationModule() {
    }

    @Provides
    LiveObjectNotifier provideLiveObjectNotifier(Context context) {
        return new BluetoothNotifier(context);
    }

    @Provides @Singleton Bus provideBus() {
        // @Singleton annotation guarantees that the returned object exists one-per-objectGraph,
        // not one-per-application
        if (mBus == null) {
            Log.v("ApplicationModule", "create Bus");
            mBus = new Bus();
        }

        return mBus;
    }
}

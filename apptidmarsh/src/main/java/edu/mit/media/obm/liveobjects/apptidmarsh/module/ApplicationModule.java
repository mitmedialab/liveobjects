package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;

import com.noveogroup.android.log.Log;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.main.MainActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.notifications.AlarmReceiver;
import edu.mit.media.obm.liveobjects.apptidmarsh.notifications.DiscoveryService;
import edu.mit.media.obm.liveobjects.apptidmarsh.notifications.PeriodicAlarmManager;
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
                DiscoveryService.class,
                PeriodicAlarmManager.class,
                MainActivity.class


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
            Log.v("create Bus");
            mBus = new Bus();
        }

        return mBus;
    }

    @Provides
    Intent provideAlarmReceiverIntent(Context context) {
        return new Intent(context, AlarmReceiver.class);
    }

    @Provides
    PeriodicAlarmManager providePeriodicAlarmManager(Intent alarmReceiverIntent, Context context, AlarmManager alarmManager) {
        return new PeriodicAlarmManager(alarmReceiverIntent, context, alarmManager
        );
    }

}

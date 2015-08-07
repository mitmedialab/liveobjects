package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.content.Context;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.LiveObjectNotifier;

/**
 * Created by artimo14 on 8/1/15.
 */
@Module(
        library = true,
        complete = false,
        includes = SystemModule.class,
        injects = {
                LiveObjectNotifier.class
        }
)
public class ApplicationModule {
    public ApplicationModule() {
    }

    @Provides
    LiveObjectNotifier provideServerWakeup(Context context) {
        return new LiveObjectNotifier(context);
    }

    @Provides @Singleton Bus provideBus() {
        return new Bus();
    }
}

package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.app.Activity;
import android.content.Context;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.apptidmarsh.main.MainActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.main.MainFragment;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.ServerWakeup;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;

/**
 * Created by artimo14 on 8/1/15.
 */
@Module(
        library = true,
        complete = false,
        includes = SystemModule.class,
        injects = {
                ServerWakeup.class
        }
)
public class ApplicationModule {
    public ApplicationModule() {
    }

    @Provides ServerWakeup provideServerWakeup(Context context) {
        return new ServerWakeup(context);
    }

    @Provides @Singleton Bus provideBus() {
        return new Bus();
    }
}

package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.app.Activity;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.apptidmarsh.main.MainActivity;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;

/**
 * Created by artimo14 on 8/2/15.
 */
@Module(injects = MainActivity.class)
public class MainActivityModule {
    Context mContext;

    public MainActivityModule(Context context) {
        mContext = context;
    }

    @Provides NetworkController provideNetworkController(MiddlewareInterface middleware) {
        return middleware.getNetworkController();
    }

    @Provides MiddlewareInterface provideMiddlewareInterface() {
        Activity activity = (Activity) mContext;
        LiveObjectsApplication application = (LiveObjectsApplication) activity.getApplication();
        return application.getMiddleware();
    }
}

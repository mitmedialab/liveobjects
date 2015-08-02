package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.app.Activity;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.apptidmarsh.main.MainFragment;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.ServerWakeup;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObjectsMiddleware;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;

/**
 * Created by artimo14 on 8/1/15.
 */
@Module(injects = MainFragment.class)
public class MainFragmentModule {
    Context mContext;

    public MainFragmentModule(Context context) {
        mContext = context;
    }

    @Provides ServerWakeup provideServerWakeup() {
        return new ServerWakeup(mContext);
    }

    @Provides NetworkController provideNetworkController(MiddlewareInterface middlewareInterface) {
        return middlewareInterface.getNetworkController();
    }

    @Provides MiddlewareInterface provideMiddlewareInterface() {
        Activity activity = (Activity) mContext;
        LiveObjectsApplication application = (LiveObjectsApplication) activity.getApplication();
        return application.getMiddleware();
    }
}

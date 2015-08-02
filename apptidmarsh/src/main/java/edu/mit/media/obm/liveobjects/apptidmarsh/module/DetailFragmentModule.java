package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.app.Activity;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.apptidmarsh.detail.DetailFragment;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;

/**
 * Created by artimo14 on 8/1/15.
 */
@Module(injects = DetailFragment.class)
public class DetailFragmentModule {
    Context mContext;

    public DetailFragmentModule(Context context) {
        mContext = context;
    }

    @Provides MiddlewareInterface provideMiddlewareInterface() {
        Activity activity = (Activity) mContext;
        LiveObjectsApplication application = (LiveObjectsApplication) activity.getApplication();
        return application.getMiddleware();
    }

    @Provides
    ContentController provideContentController(MiddlewareInterface middleware) {
        return middleware.getContentController();
    }

    @Provides DbController provideDbController(MiddlewareInterface middleware) {
        return middleware.getDbController();
    }
}

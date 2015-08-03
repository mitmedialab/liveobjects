package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.apptidmarsh.detail.DetailFragment;
import edu.mit.media.obm.liveobjects.apptidmarsh.detail.WrapUpFragment;
import edu.mit.media.obm.liveobjects.apptidmarsh.history.SavedLiveObjectsFragment;
import edu.mit.media.obm.liveobjects.apptidmarsh.main.MainActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.main.MainFragment;
import edu.mit.media.obm.liveobjects.apptidmarsh.media.MediaViewActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.AnimationArrayAdapter;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;

/**
 * Created by arata on 8/3/15.
 */
@Module(library = true,
        complete = false,
        includes = SystemModule.class,
        injects = {
                MainActivity.class,
                MainFragment.class,
                AnimationArrayAdapter.class,
                SavedLiveObjectsFragment.class,
                DetailFragment.class,
                MediaViewActivity.class,
                WrapUpFragment.class
        }
)
public class MiddlewareModule {
    public MiddlewareModule() {
    }

    @Provides
    MiddlewareInterface provideMiddlewareInterface(Context appContext) {
        LiveObjectsApplication application = (LiveObjectsApplication) appContext;
        return application.getMiddleware();
    }

    @Provides
    ContentController provideContentController(MiddlewareInterface middleware) {
        return middleware.getContentController();
    }

    @Provides
    DbController provideDbController(MiddlewareInterface middleware) {
        return middleware.getDbController();
    }

    @Provides
    NetworkController provideNetworkController(MiddlewareInterface middleware) {
        return middleware.getNetworkController();
    }
}

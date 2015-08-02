package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.app.Activity;
import android.content.Context;
import android.widget.MediaController;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.apptidmarsh.media.VideoViewFragment;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;

/**
 * Created by artimo14 on 8/1/15.
 */
@Module(injects = VideoViewFragment.class)
public class VideoViewFragmentModule {
    Context mContext;

    public VideoViewFragmentModule(Context context) {
        mContext = context;
    }

    @Provides MiddlewareInterface provideMiddlewareInterface() {
        Activity activity = (Activity) mContext;
        LiveObjectsApplication application = (LiveObjectsApplication) activity.getApplication();
        return application.getMiddleware();
    }

    @Provides
    MediaController provideMediaController() {
        return new MediaController(mContext);
    }

    @Provides DbController provideDbController(MiddlewareInterface middleware) {
        return middleware.getDbController();
    }
}

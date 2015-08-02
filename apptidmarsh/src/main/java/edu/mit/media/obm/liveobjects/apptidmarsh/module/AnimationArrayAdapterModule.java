package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.LiveObjectsApplication;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.AnimationArrayAdapter;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;

/**
 * Created by artimo14 on 8/1/15.
 */
@Module(injects = AnimationArrayAdapter.class)
public class AnimationArrayAdapterModule {
    Context mContext;

    public AnimationArrayAdapterModule(Context context) {
        mContext = context;
    }

    @Provides MiddlewareInterface provideMiddlewareInterface() {
        Activity activity = (Activity) mContext;
        LiveObjectsApplication application = (LiveObjectsApplication) activity.getApplication();
        return application.getMiddleware();
    }

    @Provides ContentController provideContentController(MiddlewareInterface middleware) {
        return middleware.getContentController();
    }

    @Provides DbController provideDbController(MiddlewareInterface middleware) {
        return middleware.getDbController();
    }

    @Provides LayoutInflater provideLayoutInflater() {
        return (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
}

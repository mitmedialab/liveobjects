package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.detail.ContentBrowserActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.detail.ContentBrowserAdapter;
import edu.mit.media.obm.liveobjects.apptidmarsh.detail.ContentBrowserFragment;
import edu.mit.media.obm.liveobjects.apptidmarsh.detail.DetailActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.detail.DetailFragment;
import edu.mit.media.obm.liveobjects.apptidmarsh.history.SavedLiveObjectsActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.history.SavedLiveObjectsAdapter;
import edu.mit.media.obm.liveobjects.apptidmarsh.history.SavedLiveObjectsFragment;
import edu.mit.media.obm.liveobjects.apptidmarsh.main.MainFragment;
import edu.mit.media.obm.liveobjects.apptidmarsh.main.MainActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.media.MediaViewActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.media.PdfViewFragment;
import edu.mit.media.obm.liveobjects.apptidmarsh.profile.ProfileActivity;
import edu.mit.media.obm.liveobjects.driver.wifi.DummyDriver;
import edu.mit.media.obm.liveobjects.driver.wifi.WifiDriver;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObjectsMiddleware;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.ContentBridge;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.CouchDbController;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkBridge;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkDriver;
import edu.mit.media.obm.liveobjects.middleware.storage.LocalStorageDriver;
import edu.mit.media.obm.liveobjects.middleware.storage.RemoteStorageDriver;
import edu.mit.media.obm.liveobjects.storage.local.FileLocalStorageDriver;
import edu.mit.media.obm.liveobjects.storage.wifi.WifiStorageDriver;

/**
 * Created by arata on 8/3/15.
 */
@Module(library = true,
        complete = false,
        includes = SystemModule.class,
        injects = {
                MainActivity.class,
                ProfileActivity.class,
                SavedLiveObjectsActivity.class,
                MainFragment.class,
                SavedLiveObjectsFragment.class,
                SavedLiveObjectsAdapter.class,
                ContentBrowserActivity.class,
                ContentBrowserFragment.class,
                ContentBrowserAdapter.class,
                DetailActivity.class,
                DetailFragment.class,
                MediaViewActivity.class,
                PdfViewFragment.class
        }
)
public class MiddlewareModule {
    private static MiddlewareInterface mMiddleware = null;

    public MiddlewareModule() {
    }

    @Provides @Singleton
    MiddlewareInterface provideMiddlewareInterface(Context context) {
        if (mMiddleware == null) {
            NetworkDriver networkDriver = new WifiDriver(context);
            NetworkController networkController = new NetworkBridge(networkDriver);

            LocalStorageDriver localStorageDriver = new FileLocalStorageDriver(context);
            RemoteStorageDriver remoteStorageDriver = new WifiStorageDriver(context);

            ContentController contentController = new ContentBridge(context, localStorageDriver, remoteStorageDriver);

            DbController dbController = new CouchDbController(context);

            mMiddleware = new LiveObjectsMiddleware(networkController, contentController, dbController);
        }

        return mMiddleware;
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

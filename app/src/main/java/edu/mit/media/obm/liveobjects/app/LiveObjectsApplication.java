package edu.mit.media.obm.liveobjects.app;

import android.app.Application;

import edu.mit.media.obm.liveobjects.driver.wifi.WifiConnectionManager;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObjectsMiddleware;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.ContentBridge;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.CouchDbController;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkBridge;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkConnectionManager;
import edu.mit.media.obm.liveobjects.middleware.storage.LocalStorageDriver;
import edu.mit.media.obm.liveobjects.middleware.storage.RemoteStorageDriver;
import edu.mit.media.obm.liveobjects.storage.local.FileLocalStorageDriver;
import edu.mit.media.obm.liveobjects.storage.wifi.WifiStorageDriver;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class LiveObjectsApplication extends Application {

    private MiddlewareInterface middleware;



    @Override
    public void onCreate() {
        super.onCreate();
        middleware = createMiddleware();

    }

    public final MiddlewareInterface getMiddleware() {
        return  middleware;
    }


    private MiddlewareInterface createMiddleware() {
        NetworkConnectionManager networkConnectionManager = new WifiConnectionManager(this);
        NetworkController networkController = new NetworkBridge(networkConnectionManager);


        LocalStorageDriver localStorageDriver = new FileLocalStorageDriver(this);
        RemoteStorageDriver remoteStorageDriver = new WifiStorageDriver(this);

        ContentController contentController = new ContentBridge(this, localStorageDriver, remoteStorageDriver);

        DbController dbController = new CouchDbController(this);

        return new LiveObjectsMiddleware(networkController, contentController, dbController);
    }

}

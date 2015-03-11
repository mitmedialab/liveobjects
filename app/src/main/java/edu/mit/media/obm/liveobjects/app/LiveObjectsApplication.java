package edu.mit.media.obm.liveobjects.app;

import android.app.Application;
import android.os.RemoteException;

import edu.mit.media.obm.liveobjects.driver.wifi.WifiDriver;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObjectsMiddleware;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;
import edu.mit.media.obm.liveobjects.middleware.control.ContentBridge;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkBridge;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkDriver;
import edu.mit.media.obm.liveobjects.middleware.storage.LocalStorageDriver;
import edu.mit.media.obm.liveobjects.middleware.storage.RemoteStorageDriver;
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
        NetworkDriver networkDriver = new WifiDriver(this);
        NetworkController networkController = new NetworkBridge(networkDriver);
        LocalStorageDriver localStorageDriver = null;

        RemoteStorageDriver remoteStorageDriver = null;
        try {
            remoteStorageDriver = new WifiStorageDriver(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        ContentController contentController = new ContentBridge(this, localStorageDriver, remoteStorageDriver);
        return new LiveObjectsMiddleware(networkController, contentController);
    }

}

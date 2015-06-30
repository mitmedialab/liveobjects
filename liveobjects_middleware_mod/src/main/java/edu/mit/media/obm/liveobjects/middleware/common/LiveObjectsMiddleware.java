package edu.mit.media.obm.liveobjects.middleware.common;

import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;

/**
 * This class is used to instantiate and get access to all the controllers of the middleware.
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public final class LiveObjectsMiddleware implements MiddlewareInterface{

    private final NetworkController mNetworkController;
    private final ContentController mContentController;
    private final DbController mDbController;

    public LiveObjectsMiddleware(NetworkController networkController, ContentController contentController, DbController dbController) {
        mNetworkController = networkController;
        mContentController = contentController;
        mDbController = dbController;
    }

    @Override
    public NetworkController getNetworkController() {
        return mNetworkController;
    }

    @Override
    public ContentController getContentController() {
        return mContentController;
    }

    @Override
    public DbController getDbController() {
        return mDbController;
    }
}

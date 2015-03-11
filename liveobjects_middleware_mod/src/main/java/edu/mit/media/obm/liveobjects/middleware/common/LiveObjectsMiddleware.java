package edu.mit.media.obm.liveobjects.middleware.common;

import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;

/**
 * This class is used to instantiate and get access to all the controllers of the middleware.
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public final class LiveObjectsMiddleware implements MiddlewareInterface{

    private final NetworkController mNetworkController;
    private final ContentController mContentController;

    public LiveObjectsMiddleware(NetworkController networkController, ContentController contentController) {
        mNetworkController = networkController;
        mContentController = contentController;
    }

    @Override
    public NetworkController getNetworkController() {
        return mNetworkController;
    }

    @Override
    public ContentController getContentController() {
        return mContentController;
    }
}

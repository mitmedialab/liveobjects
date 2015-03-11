package edu.mit.media.obm.liveobjects.middleware.common;

import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;

/**
 * This is an interface used to access the Live Objects Middleware.
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface MiddlewareInterface {

    NetworkController getNetworkController();

    ContentController getContentController();


}

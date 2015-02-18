package edu.mit.media.obm.liveobjects.middleware.control;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface ConnectionListener {


    /**
     * Callback to notify the application when a live object is connected
     * @param connectedLiveObject
     */
    public void onConnected(LiveObject connectedLiveObject);


}

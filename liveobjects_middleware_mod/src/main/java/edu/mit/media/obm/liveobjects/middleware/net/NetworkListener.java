package edu.mit.media.obm.liveobjects.middleware.net;

import java.util.List;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;

/**
 * Interface that notifies events coming from the network
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface NetworkListener {

    /**
     * Callback providing a list of discovered live objects
     * @param liveObjectList list of discovered live objects
     */
    void onNetworkDevicesAvailable(List<LiveObject> liveObjectList);

    /**
     * Callback informing the middleware that a connection with a live object has been performed
     * @param liveObjectName the name of the connected live object
     */
    void onConnected(String liveObjectName);

}

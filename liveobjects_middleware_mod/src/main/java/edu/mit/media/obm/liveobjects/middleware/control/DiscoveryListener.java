package edu.mit.media.obm.liveobjects.middleware.control;

import java.util.List;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;

/**
 * This interface defines the callback methods related to discovery.
 *
 * Callbacks methods are implemented in the application.
 *
 * @see edu.mit.media.obm.liveobjects.middleware.control.NetworkController
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface DiscoveryListener {

    public void onDiscoveryStarted();


    public void onLiveObjectsDiscovered(List<LiveObject> liveObjectList);
}

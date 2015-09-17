package edu.mit.media.obm.liveobjects.middleware.control;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;

/**
 * This is the communication interface for discovering and connecting to live objects.
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface NetworkController extends Controller {
    /**
     * Start network controller
     */
    public void start();

    /**
     * Stop network controller
     */
    public void stop();

    /**
     * Start discovering live objects
     */
    public void startDiscovery();

    /**
     * Start connecting to the liveObject
     * @param liveObject to connect
     */
    public void connect(LiveObject liveObject);

    /**
     * Cancel currently running trial to establish a connection with a live object
     */
    public void cancelConnecting();

    /**
     * Checks if the network driver is trying to connect to a live object
     */
    public boolean isConnecting();

    /**
     * Delete all the network configurations for live objects
     */
    public void forgetNetworkConfigurations();
}

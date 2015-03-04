package edu.mit.media.obm.liveobjects.middleware.control;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;

/**
 * This is the communication interface for discovering and connecting to live objects.
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface NetworkController extends Controller {

    /**
     * Set the listener for discovery callbacks
     * @param discoveryListener
     */
    public void setDiscoveryListener(DiscoveryListener discoveryListener);

    /**
     * Start network controller
     */
    public void start();

    /**
     * Stop network controller
     */
    public void stop();


    /**
     * Set the listener for connection callbacks
     * @param connectionListener
     */
    public void setConnectionListener(ConnectionListener connectionListener);

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
     * Checks if the network driver is trying to connect to a live object
     */
    public boolean isConnecting();
}

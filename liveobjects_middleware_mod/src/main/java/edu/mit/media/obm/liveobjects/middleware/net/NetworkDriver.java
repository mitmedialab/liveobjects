package edu.mit.media.obm.liveobjects.middleware.net;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;

/**
 * This interface defines the interaction between the middleware and
 * low-level network drivers
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface NetworkDriver {

    /**
     * Initializing the driver
     */
    void initialize();

    /**
     * Start network driver
     */
    void start();

   /**
     * Stop network driver
     */
    void stop();

    /**
     * Setting the network listener to receive network events
     * @param networkListener
     */
    void setNetworkListener(NetworkListener networkListener);

    /**
     * Starts scan of available live objects
     */
    void startScan();

    /**
     * Starts a connection with a live object
     * @param liveObject the live object to connect with
     */
    void connect(LiveObject liveObject);

    /**
     * Cancel currently running trial to establish a connection with a live object
     */
    void cancelConnecting();

    /**
     * Checks if the network driver is trying to connect to a live object
     */
    boolean isConnecting();

    /**
     * Delete all the network configurations for live objects
     */
    void forgetNetworkConfigurations();
}

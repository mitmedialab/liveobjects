package edu.mit.media.obm.liveobjects.middleware.net;

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
    public void initialize();

    /**
     * Start network driver
     */
    public void start();

   /**
     * Stop network driver
     */
    public void stop();

    /**
     * Setting the network listener to receive network events
     * @param networkListener
     */
    public void setNetworkListener(NetworkListener networkListener);

    /**
     * Starts scan of available live objects
     */
    public void startScan();

    /**
     * Starts a connection with a live object
     * @param liveObjectName the name of the live object to connect with
     */
    public void connect(String liveObjectName);

}

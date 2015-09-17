package edu.mit.media.obm.liveobjects.middleware.control;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkConnectionManager;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class NetworkBridge implements NetworkController {

    //TODO extending to a list of NetworkConnectionManager to have multiple discovery?
    private NetworkConnectionManager mNetworkConnectionManager;
    private Bus bus;

    public NetworkBridge(NetworkConnectionManager networkConnectionManager) {
        mNetworkConnectionManager = networkConnectionManager;
        mNetworkConnectionManager.initialize();
        bus = mNetworkConnectionManager.getEventBus();
    }

    @Override
    public void start() {
        mNetworkConnectionManager.start();
        bus.register(this);
    }

    @Override
    public void startDiscovery() {
        mNetworkConnectionManager.startScan();
    }

    @Override
    public void stop() {
        mNetworkConnectionManager.stop();
        bus.unregister(this);
    }

    @Override
    public void connect(LiveObject liveObject) {
        mNetworkConnectionManager.connect(liveObject);
    }

    @Override
    public void cancelConnecting() {
        mNetworkConnectionManager.cancelConnecting();
    }

    @Override
    public boolean isConnecting() {
        return mNetworkConnectionManager.isConnecting();
    }

    @Override
    public void forgetNetworkConfigurations() {
        mNetworkConnectionManager.forgetNetworkConfigurations();
    }
}

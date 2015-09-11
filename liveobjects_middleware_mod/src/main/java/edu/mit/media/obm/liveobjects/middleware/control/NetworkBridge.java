package edu.mit.media.obm.liveobjects.middleware.control;

import java.util.List;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkConnectionManager;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkListener;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class NetworkBridge implements NetworkController, NetworkListener{

    //TODO extending to a list of NetworkConnectionManager to have multiple discovery?
    private NetworkConnectionManager mNetworkConnectionManager;
    private DiscoveryListener mDiscoveryListener;
    private ConnectionListener mConnectionListener;

    private LiveObject liveObjectToConnectWith;

    public NetworkBridge(NetworkConnectionManager networkConnectionManager){
        mNetworkConnectionManager = networkConnectionManager;
        mNetworkConnectionManager.initialize();
        mNetworkConnectionManager.setNetworkListener(this);

    }

    @Override
    public void setDiscoveryListener(DiscoveryListener discoveryListener) {
        mDiscoveryListener = discoveryListener;

    }

    @Override
    public void start() {
        mNetworkConnectionManager.start();
    }

    @Override
    public void startDiscovery() {
        mNetworkConnectionManager.startScan();
    }

    @Override
    public void stop() {
        mNetworkConnectionManager.stop();
    }

    @Override
    public void setConnectionListener(ConnectionListener connectionListener) {
        mConnectionListener = connectionListener;
    }

    @Override
    public void connect(LiveObject liveObject) {
        liveObjectToConnectWith = liveObject;
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
    public void onNetworkDevicesAvailable(List<LiveObject> liveObjectList) {
        mDiscoveryListener.onLiveObjectsDiscovered(liveObjectList);
    }

    @Override
    public void onConnected(String liveObjectName) {
        if (liveObjectToConnectWith != null && liveObjectToConnectWith.getLiveObjectName().equals(liveObjectName)){
            mConnectionListener.onConnected(liveObjectToConnectWith);
        }
    }

    @Override
    public void forgetNetworkConfigurations() {
        mNetworkConnectionManager.forgetNetworkConfigurations();
    }
}

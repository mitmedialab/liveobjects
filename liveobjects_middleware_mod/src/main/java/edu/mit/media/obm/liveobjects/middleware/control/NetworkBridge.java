package edu.mit.media.obm.liveobjects.middleware.control;

import java.util.List;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkDriver;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkListener;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class NetworkBridge implements NetworkController, NetworkListener{

    //TODO extending to a list of NetworkDriver to have multiple discovery?
    private NetworkDriver mNetworkDriver;
    private DiscoveryListener mDiscoveryListener;
    private ConnectionListener mConnectionListener;

    private LiveObject liveObjectToConnectWith;

    public NetworkBridge(NetworkDriver networkDriver){
        mNetworkDriver = networkDriver;
        mNetworkDriver.initialize();
        mNetworkDriver.setNetworkListener(this);

    }

    @Override
    public void setDiscoveryListener(DiscoveryListener discoveryListener) {
        mDiscoveryListener = discoveryListener;

    }

    @Override
    public void start() {
        mNetworkDriver.start();
    }

    @Override
    public void startDiscovery() {
        mNetworkDriver.startScan();
    }

    @Override
    public void stop() {
        mNetworkDriver.stop();
    }

    @Override
    public void setConnectionListener(ConnectionListener connectionListener) {
        mConnectionListener = connectionListener;
    }

    @Override
    public void connect(LiveObject liveObject) {
        liveObjectToConnectWith = liveObject;
        mNetworkDriver.connect(liveObject.getLiveObjectName());
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
}

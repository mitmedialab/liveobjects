package edu.mit.media.obm.liveobjects.driver.wifi;

import android.content.Context;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.driver.wifi.base.ActivatableEntity;
import edu.mit.media.obm.liveobjects.driver.wifi.connector.WifiConnector;
import edu.mit.media.obm.liveobjects.driver.wifi.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.driver.wifi.scanner.WifiScanner;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkConnectionManager;

/**
 * This class implements a concrete driver for wifi network
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class WifiConnectionManager extends ActivatableEntity implements NetworkConnectionManager {
    @Inject WifiScanner mWifiScanner;
    @Inject WifiConnector mWifiConnector;
    @Inject Bus bus;

    public WifiConnectionManager(Context context) {
        DependencyInjector.setApplicationContext(context);
        DependencyInjector.inject(this);
    }

    @Override
    public void initialize() {
        mWifiScanner.initialize();
        mWifiConnector.initialize();
    }

    @Override
    public void start() {
        activate();
    }

    @Override
    public void stop() {
        deactivate();
    }

    @Override
    protected void activateEntity() {
        mWifiScanner.activate();
        mWifiConnector.activate();
    }

    @Override
    protected void deactivateEntity() {
        mWifiScanner.deactivate();
        mWifiConnector.deactivate();
    }

    @Override
    synchronized public void startScan() {
        requireActivated();
        mWifiScanner.startScan();
    }

    @Override
    synchronized public void connect(LiveObject liveObject) throws IllegalStateException {
        requireActivated();
        mWifiConnector.connect(liveObject);
    }

    @Override
    synchronized public void cancelConnecting() throws IllegalStateException {
        requireActivated();
        mWifiConnector.cancelConnecting();
    }

    @Override
    public boolean isConnecting() {
        requireActivated();
        return mWifiConnector.isConnecting();
    }

    @Override
    public void forgetNetworkConfigurations() {
        requireActivated();
        mWifiConnector.forgetNetworkConfigurations();
    }

    @Override
    public Bus getEventBus() {
        return bus;
    }
}

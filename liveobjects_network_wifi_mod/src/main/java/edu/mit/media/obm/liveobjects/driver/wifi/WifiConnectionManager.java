package edu.mit.media.obm.liveobjects.driver.wifi;

import android.content.Context;
import android.content.res.Resources;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkConnectionManager;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkListener;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

/**
 * This class implements a concrete driver for wifi network
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class WifiConnectionManager extends ActivatableEntity implements NetworkConnectionManager {
    private WifiScanner mWifiScanner;
    private WifiConnector mWifiConnector;

    public WifiConnectionManager(Context context) {
        DeviceIdTranslator deviceIdTranslator = buildDeviceIdTranslator(context);

        mWifiScanner = new WifiScanner(context, deviceIdTranslator);
        mWifiConnector = new WifiConnector(context, deviceIdTranslator);
    }

    private DeviceIdTranslator buildDeviceIdTranslator(Context context) {
        Resources resources = context.getResources();
        String ssidPrefix = resources.getString(R.string.ssid_prefix);
        // use only the first char as a delimiter
        // (ssid_delimiter should be 1 byte long string, though)
        char ssidDelimiter = resources.getString(R.string.ssid_delimiter).charAt(0);

        int locationXLength = resources.getInteger(R.integer.map_location_x_length);
        int locationYLength = resources.getInteger(R.integer.map_location_y_length);
        int mapIdLength = resources.getInteger(R.integer.map_location_id_length);

        return new PositionedSsidTranslator(
                ssidPrefix, ssidDelimiter, locationXLength, locationYLength, mapIdLength);
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
    public void setNetworkListener(NetworkListener networkListener) {
        mWifiScanner.setNetworkListener(networkListener);
        mWifiConnector.setNetworkListener(networkListener);
    }
}

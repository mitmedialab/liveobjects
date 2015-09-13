package edu.mit.media.obm.liveobjects.driver.wifi.module;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.wifi.WifiManager;

import com.squareup.otto.Bus;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.driver.wifi.WifiNetworkBus;
import edu.mit.media.obm.liveobjects.driver.wifi.common.PositionedSsidTranslator;
import edu.mit.media.obm.liveobjects.driver.wifi.common.WifiManagerFacade;
import edu.mit.media.obm.liveobjects.driver.wifi.connector.NetworkStateChangedReceiver;
import edu.mit.media.obm.liveobjects.driver.wifi.connector.WifiConnector;
import edu.mit.media.obm.liveobjects.driver.wifi.scanner.ScanResultsReceiver;
import edu.mit.media.obm.liveobjects.driver.wifi.scanner.WifiScanner;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;
import edu.mit.media.obm.liveobjects.driver.wifi.R;

/**
 * Created by artimo14 on 9/12/15.
 */
@Module(
        library = true,
        injects = {
                WifiScanner.class
        }
)
public class NetworkWifiModule {
    private Context applicationContext;

    public NetworkWifiModule(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Provides @Named("application")
    public Context provideApplicationContext() {
        return applicationContext;
    }

    @Provides
    public WifiManager provideWifiManager(@Named("application") Context applicationContext) {
        return (WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE);
    }

    @Provides
    public WifiManagerFacade provideWifiManagerFacade(WifiManager wifiManager) {
        return new WifiManagerFacade(wifiManager);
    }

    @Provides
    public DeviceIdTranslator provideDeviceIdTranslator() {
        Resources resources = applicationContext.getResources();
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

    @Provides @Singleton
    public WifiScanner provideWifiScanner() {
        return new WifiScanner();
    }

    @Provides @Singleton
    public WifiConnector provideWifiConnector() {
        return new WifiConnector();
    }

    @Provides @Named("scanner") @Singleton
    public IntentFilter provideScannerIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        return intentFilter;
    }

    @Provides @Named("scanner") @Singleton
    public BroadcastReceiver provideScannerBroadcastReceiver() {
        return new ScanResultsReceiver();
    }

    @Provides @Named("connector") @Singleton
    public IntentFilter provideConnectorIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        return intentFilter;
    }

    @Provides @Named("connector") @Singleton
    public BroadcastReceiver provideConnectorBroadcastReceiver() {
        return new NetworkStateChangedReceiver();
    }

    @Provides
    public Bus provideBus() {
        return WifiNetworkBus.getBus();
    }

}

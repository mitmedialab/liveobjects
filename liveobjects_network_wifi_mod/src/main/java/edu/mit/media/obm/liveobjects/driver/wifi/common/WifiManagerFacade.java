package edu.mit.media.obm.liveobjects.driver.wifi.common;

import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.driver.wifi.R;
import edu.mit.media.obm.liveobjects.driver.wifi.module.DependencyInjector;

/**
 * Created by arata on 9/2/15.
 */
public class WifiManagerFacade {
    private String WIFI_DEFAULT_PASSWORD;

    @Inject Context context;
    @Inject WifiManager wifiManager;

    @Inject
    public WifiManagerFacade(Context context, WifiManager wifiManager) {
        this.context = context;
        this.wifiManager = wifiManager;

        Resources resources = context.getResources();
        WIFI_DEFAULT_PASSWORD = resources.getString(R.string.network_password);
    }

    public void startScan() {
        wifiManager.startScan();
    }

    public List<ScanResult> getScanResults() {
        return wifiManager.getScanResults();
    }

    public int connectToNetwork(String ssid) {
        int networkId = addNetworkConfiguration(ssid);
        connectToNetwork(networkId);

        return networkId;
    }

    private int addNetworkConfiguration(String ssid) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = quote(ssid);
        wifiConfiguration.preSharedKey = quote(WIFI_DEFAULT_PASSWORD);

        return wifiManager.addNetwork(wifiConfiguration);
    }

    private void connectToNetwork(int networkId) {
        wifiManager.disconnect();
        wifiManager.enableNetwork(networkId, true);
        wifiManager.reconnect();
    }

    public void disconnectFromNetwork(int networkId) {
        wifiManager.disableNetwork(networkId);
    }

    public String getConnectedSsid() {
        String ssid = wifiManager.getConnectionInfo().getSSID();

        return unquote(ssid);
    }

    public List<String> getRegisteredSsids() {
        List<String> registeredSsids = new ArrayList<>();

        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();

        // configurations can be null when WiFi is disabled
        if (configurations != null) {
            for (WifiConfiguration configuration : configurations) {
                String ssid = unquote(configuration.SSID);
                registeredSsids.add(ssid);
            }
        }

        return registeredSsids;
    }

    public void removeNetwork(String ssid) {
        WifiConfiguration configuration = getConfiguration(ssid);

        if (configuration != null) {
            wifiManager.removeNetwork(configuration.networkId);
        }
    }

    private WifiConfiguration getConfiguration(String ssid) {
        WifiConfiguration foundConfiguration = null;

        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();

        // configurations can be null when WiFi is disabled
        if (configurations != null) {
            for (WifiConfiguration configuration : configurations) {
                String registeredSsid = unquote(configuration.SSID);

                if (registeredSsid.equals(ssid)) {
                    foundConfiguration = configuration;
                }
            }
        }

        return foundConfiguration;
    }

    private String quote(String text) {
        return String.format("\"%s\"", text);
    }

    private String unquote(String text) {
        String unquotedText = text;

        if (unquotedText.startsWith("\"")) {
            unquotedText = unquotedText.substring(1);
        }

        if (unquotedText.endsWith("\"")) {
            unquotedText = unquotedText.substring(0, unquotedText.length() - 1);
        }

        return unquotedText;
    }
}

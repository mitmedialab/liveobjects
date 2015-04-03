/*
 * adapter from Wifi Connecter project:
 * 
 * Copyright (c) 20101 Kevin Yuan (farproc@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 **/

package edu.mit.media.obm.liveobjects.driver.wifi;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.net.wifi.WifiConfiguration.Protocol;
import android.text.TextUtils;
import android.util.Log;

public class WifiManagerWrapper {

    public static WifiConfiguration addNewNetwork(WifiManager wifiManager, String ssid, String password) {
        WifiConfiguration oldConfig = getWifiConfigurationBySsid(wifiManager, ssid);

        if (oldConfig != null) {
            wifiManager.removeNetwork(oldConfig.networkId);
        }

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = convertToQuotedString(ssid);
        conf.preSharedKey = convertToQuotedString(password);

        wifiManager.addNetwork(conf);
        wifiManager.saveConfiguration();

        return getWifiConfigurationBySsid(wifiManager, ssid);
    }

    /**
     * Connect to a configured network.
     *
     * @param wifiManager
     * @param config
     * @param numOpenNetworksKept Settings.Secure.WIFI_NUM_OPEN_NETWORKS_KEPT
     * @return
     */
    public static boolean connectToConfiguredNetwork(final Context ctx, final WifiManager wifiMgr, WifiConfiguration config, boolean reassociate) {
        int oldPri = config.priority;
        // Make it the highest priority.
        int newPri = getMaxPriority(wifiMgr) + 1;
        if (newPri > MAX_PRIORITY) {
            newPri = shiftPriorityAndSave(wifiMgr);
            config = getWifiConfiguration(wifiMgr, config);
            if (config == null) {
                return false;
            }
        }

        // Set highest priority to this configured network
        config.priority = newPri;
        int networkId = wifiMgr.updateNetwork(config);
        if (networkId == -1) {
            return false;
        }

        // Do not disable others
        if (!wifiMgr.enableNetwork(networkId, false)) {
            config.priority = oldPri;
            return false;
        }

        if (!wifiMgr.saveConfiguration()) {
            config.priority = oldPri;
            return false;
        }

        // We have to retrieve the WifiConfiguration after save.
        config = getWifiConfiguration(wifiMgr, config);
        if (config == null) {
            return false;
        }
        //
        // Disable others, but do not save.
        // Just to force the WifiManager to connect to it.
        /*  disable only if you don't want to reconnect to this network automatically again
        WifiInfo current = wifiMgr.getConnectionInfo();
        if (current != null) {
            if (current.getSSID() != config.SSID){
                wifiMgr.disableNetwork(current.getNetworkId());
            }
        }
        */
        //TODO: verify if to disable others is needed if not change second param to false
        if (!wifiMgr.enableNetwork(config.networkId, true)) {
            return false;
        }

        //final boolean connect = reassociate ? wifiMgr.reassociate() : wifiMgr.reconnect();
        final boolean connect = wifiMgr.reconnect();
        if (!connect) {
            return false;
        }

        return true;
    }

    private static void sortByPriority(final List<WifiConfiguration> configurations) {
        java.util.Collections.sort(configurations, new Comparator<WifiConfiguration>() {

            @Override
            public int compare(WifiConfiguration object1,
                               WifiConfiguration object2) {
                return object1.priority - object2.priority;
            }
        });
    }

    private static final int MAX_PRIORITY = 99999;

    private static int shiftPriorityAndSave(final WifiManager wifiMgr) {
        final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        sortByPriority(configurations);
        final int size = configurations.size();
        for (int i = 0; i < size; i++) {
            final WifiConfiguration config = configurations.get(i);
            config.priority = i;
            wifiMgr.updateNetwork(config);
        }
        wifiMgr.saveConfiguration();
        return size;
    }

    private static int getMaxPriority(final WifiManager wifiManager) {
        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        int pri = 0;
        for (final WifiConfiguration config : configurations) {
            if (config.priority > pri) {
                pri = config.priority;
            }
        }
        return pri;
    }

    public static WifiConfiguration getWifiConfigurationBySsid(final WifiManager wifiMgr, String ssid) {
        ssid = convertToQuotedString(ssid);
        WifiConfiguration config = null;
        boolean found = false;

        INFO("getWifiConfigurationBySsid");

        List<WifiConfiguration> configs = wifiMgr.getConfiguredNetworks();

        if (configs == null)
            return null;

        Iterator<WifiConfiguration> it = configs.iterator();

        INFO("search " + ssid);

        while (it.hasNext() && !found) {
            //for (Iterator<WifiConfiguration> it = configs.iterator(); it.hasNext() && !found;) {
            WifiConfiguration configTemp = it.next();
            INFO("config " + configTemp.networkId + " " + configTemp.SSID);
            if (configTemp.SSID.equals(ssid)) {
                found = true;
                config = configTemp;
            }
        }

        return config;
    }


    public static WifiConfiguration getWifiConfigurationByNetworkId(final WifiManager wifiMgr, final int netid) {
        WifiConfiguration config = null;
        boolean found = false;
        List<WifiConfiguration> configs = wifiMgr.getConfiguredNetworks();

        for (Iterator<WifiConfiguration> it = configs.iterator(); it.hasNext() && !found; ) {
            WifiConfiguration test = it.next();
            if (test.networkId == netid) {
                found = true;
                config = test;
            }
        }

        return config;
    }

    public static WifiConfiguration getWifiConfiguration(final WifiManager wifiMgr, final WifiConfiguration configToFind) {
        final String ssid = configToFind.SSID;
        if (ssid.length() == 0)
            return null;

        final String bssid = configToFind.BSSID;

        final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();

        for (final WifiConfiguration config : configurations) {
            if (config.SSID == null || !ssid.equals(config.SSID))
                continue;

            if (config.BSSID == null || bssid == null || bssid.equals(config.BSSID)) {
                return config;
            }
        }
        return null;
    }

    public static String convertToQuotedString(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }

        final int lastPos = string.length() - 1;
        if (lastPos < 0 || (string.charAt(0) == '"' && string.charAt(lastPos) == '"')) {
            return string;
        }

        return "\"" + string + "\"";
    }

    private final static String TAG = "Wifiutils:MP2PWifi";

    private static void INFO(String msg) {
        Log.i(TAG, msg);
    }

    private static void WARN(String msg) {
        Log.w(TAG, msg);
    }

    private static void ERROR(String msg) {
        Log.e(TAG, msg);
    }

}

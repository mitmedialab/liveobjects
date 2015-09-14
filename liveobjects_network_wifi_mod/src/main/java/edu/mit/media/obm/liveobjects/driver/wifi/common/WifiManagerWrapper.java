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

package edu.mit.media.obm.liveobjects.driver.wifi.common;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.noveogroup.android.log.Log;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class WifiManagerWrapper {
    public static boolean removeNetwork(WifiManager wifiManager, String ssid) {
        WifiConfiguration config = getWifiConfigurationBySsid(wifiManager, ssid);

        if (config == null) {
            return false;
        }

        wifiManager.removeNetwork(config.networkId);

        return true;
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

}

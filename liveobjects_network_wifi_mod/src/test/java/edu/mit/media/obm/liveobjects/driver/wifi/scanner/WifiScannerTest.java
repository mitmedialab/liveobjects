package edu.mit.media.obm.liveobjects.driver.wifi.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import com.noveogroup.android.log.Log;

import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.driver.wifi.module.DependencyInjector;

import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by artimo14 on 9/12/15.
 */
@PrepareForTest(Log.class)
public class WifiScannerTest extends PowerMockTestCase {
    @Mock @Inject Context context;
    @Mock @Inject WifiManager wifiManager;
    @Mock @Inject IntentFilter intentFilter;
    @Mock @Inject BroadcastReceiver broadcastReceiver;
    private WifiScanner wifiScanner;

    @Module(
            injects = WifiScannerTest.class
    )
    static class TestModule {
        @Provides @Singleton
        Context provideContext() {
            return mock(Context.class);
        }

        @Provides @Singleton
        WifiManager provideWifiManager() {
            return mock(WifiManager.class);
        }

        @Provides @Singleton
        IntentFilter provideIntentFilter() {
            return mock(IntentFilter.class);
        }

        @Provides @Singleton
        BroadcastReceiver provideBroadcastReceiver() {
            return mock(BroadcastReceiver.class);
        }
    }

    @BeforeMethod
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Log.class);

        DependencyInjector.inject(this, new TestModule());
        wifiScanner = new WifiScanner(context, wifiManager, intentFilter, broadcastReceiver);
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @DataProvider(name = "registrationSequenceSet")
    public Object[][] provideRegistrationSequenceSet() {
        return new Object[][] {
                { "rs",        false, "should start scan after registered" },
                { "rus",       true,  "should throw when tried to scan after unregistered" },
                { "r",         false, "should register intent forwarder on registered" },
                { "rr",        true,  "should throw on double registration" },
                { "u",         true,  "should throw on unregistration before registration" },
                { "ru",        false, "should unregister intent forwarder on unregistered" },
                { "rur",       false, "should register again" },
                { "rurururu",  false, "should register and unregister many times" },
                { "rururururs", false, "should start scan after many registration and unregistration" }
        };
    }

    @Test(dataProvider = "registrationSequenceSet")
    public void shouldStartScanWhenRegistered(String registrationSequence, boolean shouldThrow, String description) throws Exception {
        try {
            for (char c : registrationSequence.toCharArray()) {
                if (c == 'r') {
                    wifiScanner.activate();
                } else if (c == 'u') {
                    wifiScanner.deactivate();
                } else if (c == 's') {
                    wifiScanner.startScan();
                }
            }

            assertFalse(shouldThrow, description + ": This test should throw an exception, but didn't");

            int activationCount = countCharacters(registrationSequence, 'r');
            int deactivationCount = countCharacters(registrationSequence, 'u');
            int scanCount = countCharacters(registrationSequence, 's');
            verify(context, times(activationCount)).registerReceiver(broadcastReceiver, intentFilter);
            verify(context, times(deactivationCount)).unregisterReceiver(broadcastReceiver);
            verify(wifiManager, times(scanCount)).startScan();
        } catch (IllegalStateException e) {
            assertTrue(shouldThrow, description + ": This test should not throw an exception, but threw " + e);
        }
    }

    private int countCharacters(String string, char c) {
        return string.length() - string.replace(String.valueOf(c), "").length();
    }
}
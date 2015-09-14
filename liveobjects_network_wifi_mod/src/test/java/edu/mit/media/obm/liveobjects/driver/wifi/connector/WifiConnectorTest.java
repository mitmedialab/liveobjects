package edu.mit.media.obm.liveobjects.driver.wifi.connector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;

import com.noveogroup.android.log.Log;

import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.driver.wifi.common.WifiManagerFacade;
import edu.mit.media.obm.liveobjects.driver.wifi.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.driver.wifi.scanner.ScanResultsReceiver;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Created by artimo14 on 9/12/15.
 */
@PrepareForTest({
        Log.class,
        NetworkStateChangedReceiver.class // need to suppress execution of BroadcastReceiver constructor
})
public class WifiConnectorTest extends PowerMockTestCase {
    private static final String TEST_DEVICE_ID = "device_id";
    private static final String VALID_SSID = "valid_ssid";
    private static final String INVALID_SSID = "invalid_ssid";

    @Mock @Inject @Named("application") Context context;
    @Mock @Inject WifiManagerFacade wifiManagerFacade;
    @Mock @Inject @Named("connector") IntentFilter intentFilter;
    @Mock @Inject @Named("connector") BroadcastReceiver broadcastReceiver;
    @Mock @Inject DeviceIdTranslator deviceIdTranslator;
    @Inject WifiConnector wifiConnector;

    private NetworkStateChangedReceiver networkStateChangedReceiver;

    @Module(injects = WifiConnectorTest.class)
    static class TestModule {
        @Provides @Named("application") @Singleton
        Context provideContext() {
            return mock(Context.class);
        }

        @Provides @Singleton
        WifiManagerFacade provideWifiManagerFacade() {
            return mock(WifiManagerFacade.class);
        }

        @Provides @Named("connector") @Singleton
        IntentFilter provideIntentFilter() {
            return mock(IntentFilter.class);
        }

        @Provides @Named("connector") @Singleton
        BroadcastReceiver provideBroadcastReceiver() {
            return mock(NetworkStateChangedReceiver.class);
        }

        @Provides @Singleton
        DeviceIdTranslator provideDeviceIdTranslator() {
            return mock(DeviceIdTranslator.class);
        }
    }

    @BeforeMethod
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Log.class);

        DependencyInjector.inject(this, new TestModule());

        Resources resources = mock(Resources.class);
        stub(resources.getString(anyInt())).toReturn("PASSWORD");
        stub(context.getResources()).toReturn(resources);

        DependencyInjector.inject(this, new TestModule());
        networkStateChangedReceiver = (NetworkStateChangedReceiver) broadcastReceiver;

        LiveObject liveObject = new LiveObject(TEST_DEVICE_ID);
        stub(deviceIdTranslator.translateFromLiveObject(liveObject)).toReturn(TEST_DEVICE_ID);
        stub(deviceIdTranslator.isValidSsid(VALID_SSID)).toReturn(true);
        stub(deviceIdTranslator.isValidSsid(INVALID_SSID)).toReturn(false);

        stub(networkStateChangedReceiver.isMonitoring()).toReturn(false);
    }

    @Test
    public void testInitialize() throws Exception {
        wifiConnector.initialize();

        verify(networkStateChangedReceiver).stopMonitoring();
    }

    @Test
    public void shouldReturnInjectedBroadcastReceiver() throws Exception {
        BroadcastReceiver broadcastReceiver = wifiConnector.createBroadcastReceiver();
        assertEquals(broadcastReceiver, networkStateChangedReceiver);
    }

    @Test
    public void shouldReturnInjectedIntentReceiver() throws Exception {
        IntentFilter intentFilter = wifiConnector.createIntentFilter();
        assertEquals(intentFilter, this.intentFilter);
    }

    @DataProvider(name = "registrationSequenceSet")
    public Object[][] provideRegistrationSequenceSet() {
        return new Object[][] {
                { "rs",        false,  "should connect after registered" },
                { "rus",       true,   "should throw when tried to connect after unregistered" },
                { "r",         false,  "should register intent forwarder on registered" },
                { "rr",        true,   "should throw on double registration" },
                { "u",         true,   "should throw on unregistration before registration" },
                { "ru",        false,  "should unregister intent forwarder on unregistered" },
                { "rur",       false,  "should register again" },
                { "rurururu",  false,  "should register and unregister many times" },
                { "rururururs", false, "should connect after many registration and unregistration" }
        };
    }

    @Test(dataProvider = "registrationSequenceSet")
    public void shouldConnectWhenRegistered(String registrationSequence, boolean shouldThrow, String description) throws Exception {
        try {
            for (char c : registrationSequence.toCharArray()) {
                if (c == 'r') {
                    wifiConnector.activate();
                } else if (c == 'u') {
                    wifiConnector.deactivate();
                } else if (c == 's') {
                    wifiConnector.connect(new LiveObject(TEST_DEVICE_ID));
                }
            }

            assertFalse(shouldThrow, description + ": This test should throw an exception, but didn't");

            int registerCount = countCharacters(registrationSequence, 'r');
            int unregisterCount = countCharacters(registrationSequence, 'u');
            int connectCount = countCharacters(registrationSequence, 's');
            verify(context, times(registerCount)).registerReceiver(broadcastReceiver, intentFilter);
            verify(context, times(unregisterCount)).unregisterReceiver(broadcastReceiver);
            verify(wifiManagerFacade, times(connectCount)).connectToNetwork(TEST_DEVICE_ID);

            if (connectCount > 0) {
                verify(networkStateChangedReceiver).startMonitoring(TEST_DEVICE_ID);
            }
        } catch (RuntimeException e) {
            assertTrue(shouldThrow, description + ": This test should not throw an exception, but threw " + e);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowIfTryToConnectWhenAlreadyConnecting() {
        stub(networkStateChangedReceiver.isMonitoring()).toReturn(true);

        wifiConnector.activate();
        wifiConnector.connect(new LiveObject(TEST_DEVICE_ID));
    }

    private int countCharacters(String string, char c) {
        return string.length() - string.replace(String.valueOf(c), "").length();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowIfTryToCancelConnectingWhenNotActivated() throws Exception {
        wifiConnector.cancelConnecting();
    }

    @Test
    public void shouldNotDisableNetworkWhenNotConnecting() throws Exception {
        wifiConnector.activate();
        wifiConnector.cancelConnecting();

        assertFalse(networkStateChangedReceiver.isMonitoring());
        verify(wifiManagerFacade, never()).disconnectFromNetwork(anyInt());
    }

    @Test
    public void shouldDisableNetworkWhenConnecting() throws Exception {
        stub(networkStateChangedReceiver.isMonitoring()).toReturn(true);

        wifiConnector.activate();
        wifiConnector.cancelConnecting();

        verify(wifiManagerFacade).disconnectFromNetwork(anyInt());
        verify(networkStateChangedReceiver).stopMonitoring();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowIfTryToGetConnectingStatusWhenNotActivated() throws Exception {
        wifiConnector.isConnecting();
    }

    @Test
    public void shouldReturnFalseWhenNotConnecting() throws Exception {
        wifiConnector.activate();

        assertFalse(wifiConnector.isConnecting());
    }

    @Test
    public void shouldReturnTrueWhenConnecting() throws Exception {
        stub(networkStateChangedReceiver.isMonitoring()).toReturn(true);

        wifiConnector.activate();

        assertTrue(wifiConnector.isConnecting());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowIfTryToForgetNetworkConfigurationsWhenNotActivated() throws Exception {
        wifiConnector.forgetNetworkConfigurations();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowIfTryToForgetNetworkConfigurationsWhenConnecting() throws Exception {
        stub(networkStateChangedReceiver.isMonitoring()).toReturn(true);

        wifiConnector.activate();

        wifiConnector.forgetNetworkConfigurations();
    }

    @DataProvider(name = "addedNetworkConfigurations")
    public Object[][] provideAddedNetworkConfigurations() {
        return new Object[][] {
                { Arrays.asList() },
                { Arrays.asList(VALID_SSID) },
                { Arrays.asList(VALID_SSID, VALID_SSID, VALID_SSID, VALID_SSID) },
                { Arrays.asList(INVALID_SSID) },
                { Arrays.asList(INVALID_SSID, INVALID_SSID, INVALID_SSID, INVALID_SSID) },
                { Arrays.asList(VALID_SSID, VALID_SSID, INVALID_SSID, INVALID_SSID) }
        };
    }

    @Test(dataProvider = "addedNetworkConfigurations")
    public void shouldForgetNetworkConfigurations(List<String> ssids) throws Exception {
        stub(wifiManagerFacade.getRegisteredSsids()).toReturn(ssids);

        wifiConnector.activate();
        wifiConnector.forgetNetworkConfigurations();

        int numValidSsid = Collections.frequency(ssids, VALID_SSID);
        verify(wifiManagerFacade, times(numValidSsid)).removeNetwork(VALID_SSID);
        verify(wifiManagerFacade, never()).removeNetwork(INVALID_SSID);
    }
}
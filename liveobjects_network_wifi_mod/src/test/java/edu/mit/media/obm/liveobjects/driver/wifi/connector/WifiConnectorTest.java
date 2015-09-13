package edu.mit.media.obm.liveobjects.driver.wifi.connector;

import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.wifi.WifiManager;

import com.noveogroup.android.log.Log;

import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.driver.wifi.common.WifiManagerFacade;
import edu.mit.media.obm.liveobjects.driver.wifi.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.driver.wifi.scanner.WifiScanner;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Created by artimo14 on 9/12/15.
 */
@PrepareForTest(Log.class)
public class WifiConnectorTest extends PowerMockTestCase {
    private static final String TEST_DEVICE_ID = "device_id";

    @Inject Context context;
    @Inject WifiManagerFacade wifiManagerFacade;
    @Inject IntentFilter intentFilter;
    @Inject NetworkStateChangedReceiver networkStateChangedReceiver;
    @Inject DeviceIdTranslator deviceIdTranslator;
    private WifiConnector wifiConnector;

    @Module(
            injects = WifiConnectorTest.class
    )
    static class TestModule {
        @Provides @Singleton
        Context provideContext() {
            return mock(Context.class);
        }

        @Provides @Singleton
        WifiManagerFacade provideWifiManagerFacade() {
            return mock(WifiManagerFacade.class);
        }

        @Provides @Singleton
        IntentFilter provideIntentFilter() {
            return mock(IntentFilter.class);
        }

        @Provides @Singleton
        NetworkStateChangedReceiver provideNetworkStateChangedReceiver() {
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

        wifiConnector = new WifiConnector(context, wifiManagerFacade, intentFilter, networkStateChangedReceiver, deviceIdTranslator);

        LiveObject liveObject = new LiveObject(TEST_DEVICE_ID);
        stub(deviceIdTranslator.translateFromLiveObject(liveObject)).toReturn(TEST_DEVICE_ID);
    }

    @Test
    public void testInitialize() throws Exception {
        doNothing().when(networkStateChangedReceiver).setConnecting(anyBoolean());
        wifiConnector.initialize();

        verify(networkStateChangedReceiver).setConnecting(false);
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
            verify(context, times(registerCount)).registerReceiver(networkStateChangedReceiver, intentFilter);
            verify(context, times(unregisterCount)).unregisterReceiver(networkStateChangedReceiver);
            verify(wifiManagerFacade, times(connectCount)).connectToNetwork(TEST_DEVICE_ID);
        } catch (RuntimeException e) {
            assertTrue(shouldThrow, description + ": This test should not throw an exception, but threw " + e);
        }
    }

    private int countCharacters(String string, char c) {
        return string.length() - string.replace(String.valueOf(c), "").length();
    }

    @Test
    public void testConnect() throws Exception {

    }

    @Test
    public void testCancelConnecting() throws Exception {

    }

    @Test
    public void testIsConnecting() throws Exception {

    }

    @Test
    public void testForgetNetworkConfigurations() throws Exception {

    }
}
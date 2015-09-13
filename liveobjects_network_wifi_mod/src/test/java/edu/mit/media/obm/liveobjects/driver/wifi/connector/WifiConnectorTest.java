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
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.driver.wifi.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.driver.wifi.scanner.WifiScanner;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Created by artimo14 on 9/12/15.
 */
@PrepareForTest(Log.class)
public class WifiConnectorTest extends PowerMockTestCase {
    @Inject Context context;
    @Inject WifiManager wifiManager;
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
        WifiManager provideWifiManager() {
            return mock(WifiManager.class);
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

        wifiConnector = new WifiConnector(context, wifiManager, intentFilter, networkStateChangedReceiver, deviceIdTranslator);
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
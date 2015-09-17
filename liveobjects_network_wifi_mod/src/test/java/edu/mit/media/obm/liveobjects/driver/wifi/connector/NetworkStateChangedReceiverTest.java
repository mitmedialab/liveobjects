package edu.mit.media.obm.liveobjects.driver.wifi.connector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.noveogroup.android.log.Log;
import com.squareup.otto.Bus;

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
import edu.mit.media.obm.liveobjects.driver.wifi.common.WifiManagerFacade;
import edu.mit.media.obm.liveobjects.driver.wifi.event.NetworkConnectedEvent;
import edu.mit.media.obm.liveobjects.driver.wifi.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Created by arata on 9/14/15.
 */
@PrepareForTest({
        Log.class,
        NetworkStateChangedReceiver.class // need to suppress execution of BroadcastReceiver constructor
})
public class NetworkStateChangedReceiverTest extends PowerMockTestCase {
    private static final String VALID_SSID = "valid_ssid";
    private static final String INVALID_SSID = "invalid_ssid";

    @Mock @Inject WifiManagerFacade wifiManagerFacade;
    @Mock @Inject DeviceIdTranslator deviceIdTranslator;
    @Mock @Inject Bus bus;
    @Inject NetworkStateChangedReceiver networkStateChangedReceiver;

    private Context dummyContext;
    private NetworkInfo networkInfo;
    private Intent intent;

    @Module(injects = NetworkStateChangedReceiverTest.class)
    static class TestModule {
        @Provides @Singleton
        WifiManagerFacade provideWifiManagerFacade() {
            return mock(WifiManagerFacade.class);
        }

        @Provides @Singleton
        DeviceIdTranslator provideDeviceIdTranslator() {
            return mock(DeviceIdTranslator.class);
        }

        @Provides @Singleton
        Bus provideBus() {
            return mock(Bus.class);
        }
    }

    @BeforeMethod
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Log.class);
        PowerMockito.suppress(PowerMockito.constructor(BroadcastReceiver.class));

        DependencyInjector.inject(this, new TestModule());

        stub(deviceIdTranslator.isValidSsid(VALID_SSID)).toReturn(true);
        stub(deviceIdTranslator.isValidSsid(INVALID_SSID)).toReturn(false);
        stub(deviceIdTranslator.translateToLiveObject(VALID_SSID)).toReturn(new LiveObject(VALID_SSID));

        dummyContext = mock(Context.class);

        networkInfo = mock(NetworkInfo.class);
        stub(networkInfo.getState()).toReturn(NetworkInfo.State.CONNECTED);
        stub(networkInfo.getExtraInfo()).toReturn(VALID_SSID);

        intent = mock(Intent.class);
        stub(intent.getAction()).toReturn(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        stub(intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)).toReturn(networkInfo);
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowForUnrecognizedAction() throws Exception {
        stub(intent.getAction()).toReturn("Unrecognized Action");

        networkStateChangedReceiver.onReceive(dummyContext, intent);
    }

    @DataProvider(name = "networkInfoState")
    public Object[][] provideNetworkInfoState() {
        return new Object[][] {
                { "CONNECTING" },
                { "DISCONNECTED" },
                { "DISCONNECTING" },
                { "SUSPENDED" },
                { "UNKNOWN" }
        };
    }

    private NetworkInfo.State toNetworkInfoState(String string) {
        if ("CONNECTING".equals(string)) {
            return NetworkInfo.State.CONNECTING;
        } else if ("DISCONNECTED".equals(string)) {
            return NetworkInfo.State.DISCONNECTED;
        } else if ("DISCONNECTING".equals(string)) {
            return NetworkInfo.State.DISCONNECTING;
        } else if ("SUSPENDED".equals(string)) {
            return NetworkInfo.State.SUSPENDED;
        } else if ("UNKNOWN".equals(string)) {
            return NetworkInfo.State.UNKNOWN;
        } else {
            throw new IllegalArgumentException("illegal network info state");
        }
    }

    @Test(dataProvider = "networkInfoState")
    public void shouldNotPostIfNetworkStatusIsOtherThanConnected(String networkState) throws Exception {
        stub(networkInfo.getState()).toReturn(toNetworkInfoState(networkState));

        networkStateChangedReceiver.startMonitoring(VALID_SSID);
        networkStateChangedReceiver.onReceive(dummyContext, intent);

        verify(bus, never()).post(anyObject());
        assertTrue(networkStateChangedReceiver.isMonitoring());
    }

    @Test
    public void shouldPostWithTargetStatusWhenConnectedToTargetDevice() throws Exception {
        networkStateChangedReceiver.startMonitoring(VALID_SSID);
        networkStateChangedReceiver.onReceive(dummyContext, intent);

        NetworkConnectedEvent event = new NetworkConnectedEvent(
                new LiveObject(VALID_SSID), NetworkConnectedEvent.State.CONNECTED_TO_TARGET);
        verify(bus).post(event);
        assertFalse(networkStateChangedReceiver.isMonitoring());
    }

    @Test
    public void shouldPostWithWrongStatusWhenConnectedToNonLiveObjectDevice() throws Exception {
        stub(networkInfo.getExtraInfo()).toReturn(INVALID_SSID);

        networkStateChangedReceiver.startMonitoring(VALID_SSID);
        networkStateChangedReceiver.onReceive(dummyContext, intent);

        NetworkConnectedEvent event = new NetworkConnectedEvent(
                null, NetworkConnectedEvent.State.CONNECTED_TO_NON_TARGET);
        verify(bus).post(event);
        assertFalse(networkStateChangedReceiver.isMonitoring());
    }

    @Test
    public void shouldPostWithWrongStatusWhenConnectedToNonTargetDevice() throws Exception {
        final String ANOTHER_VALID_SSID = "another_valid_ssid";

        stub(deviceIdTranslator.isValidSsid(ANOTHER_VALID_SSID)).toReturn(true);

        networkStateChangedReceiver.startMonitoring(ANOTHER_VALID_SSID);
        networkStateChangedReceiver.onReceive(dummyContext, intent);

        NetworkConnectedEvent event = new NetworkConnectedEvent(
                new LiveObject(VALID_SSID), NetworkConnectedEvent.State.CONNECTED_TO_NON_TARGET);
        verify(bus).post(event);
        assertFalse(networkStateChangedReceiver.isMonitoring());
    }

    @Test
    public void shouldPostIfFailedToGetConnectedSsid() throws Exception {
        stub(networkInfo.getExtraInfo()).toReturn(null);

        networkStateChangedReceiver.startMonitoring(VALID_SSID);
        networkStateChangedReceiver.onReceive(dummyContext, intent);

        NetworkConnectedEvent event = new NetworkConnectedEvent(null,
                NetworkConnectedEvent.State.NOT_CONNECTED_FOR_SSID_ACQUISITION_FAILURE);
        verify(bus).post(event);
        assertFalse(networkStateChangedReceiver.isMonitoring());
    }

    @Test
    public void shouldNotPostIfNotMonitoring() throws Exception {
        networkStateChangedReceiver.onReceive(dummyContext, intent);

        verify(bus, never()).post(anyObject());
    }

    @Test
    public void shouldNotPostTwiceWithConnectedToTargetFlagSet() throws Exception {
        networkStateChangedReceiver.startMonitoring(VALID_SSID);
        networkStateChangedReceiver.onReceive(dummyContext, intent);
        networkStateChangedReceiver.onReceive(dummyContext, intent);

        verify(bus, times(1)).post(anyObject());
        assertFalse(networkStateChangedReceiver.isMonitoring());
    }

    @Test
    public void shouldNotPostWhenConnectedToUnknownSsid() throws Exception {
        stub(networkInfo.getExtraInfo()).toReturn("<unknown ssid>");

        networkStateChangedReceiver.startMonitoring(VALID_SSID);
        networkStateChangedReceiver.onReceive(dummyContext, intent);

        verify(bus, never()).post(anyObject());
        assertTrue(networkStateChangedReceiver.isMonitoring());
    }

    @Test
    public void shouldStartMonitoring() throws Exception {
        networkStateChangedReceiver.startMonitoring(VALID_SSID);

        assertTrue(networkStateChangedReceiver.isMonitoring());
    }

    @Test
    public void shouldStopMonitoring() throws Exception {
        networkStateChangedReceiver.startMonitoring(VALID_SSID);
        networkStateChangedReceiver.stopMonitoring();

        assertFalse(networkStateChangedReceiver.isMonitoring());
    }
}
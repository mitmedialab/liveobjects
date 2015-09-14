package edu.mit.media.obm.liveobjects.driver.wifi.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.noveogroup.android.log.Log;
import com.squareup.otto.Bus;

import org.mockito.ArgumentCaptor;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.driver.wifi.event.NetworkDevicesAvailableEvent;
import edu.mit.media.obm.liveobjects.driver.wifi.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.DeviceIdTranslator;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Created by artimo14 on 9/12/15.
 */
@PrepareForTest({
        Log.class,
        ScanResultsReceiver.class // need to suppress execution of BroadcastReceiver constructor
})
public class ScanResultsReceiverTest extends PowerMockTestCase {
    /*
    private static final String VALID_SSID = "valid_ssid";
    private static final String INVALID_SSID = "invalid_ssid";

    @Inject WifiManager wifiManager;
    @Inject DeviceIdTranslator deviceIdTranslator;
    @Inject Bus bus;

    private ScanResultsReceiver scanResultsReceiver;
    private Context dummyContext;

    @Module(injects = ScanResultsReceiverTest.class)
    static class TestModule {
        @Provides @Singleton
        WifiManager provideWifiManager() {
            return mock(WifiManager.class);
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
        scanResultsReceiver = new ScanResultsReceiver(wifiManager, deviceIdTranslator, bus);

        stub(deviceIdTranslator.isValidSsid(VALID_SSID)).toReturn(true);
        stub(deviceIdTranslator.isValidSsid(INVALID_SSID)).toReturn(false);
        stub(deviceIdTranslator.translateToLiveObject(VALID_SSID)).toReturn(new LiveObject(VALID_SSID));

        dummyContext = mock(Context.class);
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test(expectedExceptions = RuntimeException.class)
    public void shouldThrowForUnrecognizedAction() throws Exception {
        Intent intent = mock(Intent.class);
        stub(intent.getAction()).toReturn("Unrecognized Action");

        scanResultsReceiver.onReceive(dummyContext, intent);
    }

    @DataProvider(name = "DetectedAndPostSsidMap")
    public Object[][] provideDetectedAndPostSsidSet() {
        return new Object[][] {
                {
                        Arrays.asList(),
                        Arrays.asList(),
                        "should post nothing for an empty scan results"
                },
                {
                        Arrays.asList(
                                INVALID_SSID,
                                INVALID_SSID,
                                INVALID_SSID,
                                INVALID_SSID),
                        Arrays.asList(),
                        "should not post non live object devices"
                },
                {
                        Arrays.asList(VALID_SSID),
                        Arrays.asList(VALID_SSID),
                        "should post a detected live object"
                },
                {
                        Arrays.asList(
                                VALID_SSID,
                                VALID_SSID,
                                VALID_SSID,
                                VALID_SSID),
                        Arrays.asList(
                                VALID_SSID,
                                VALID_SSID,
                                VALID_SSID,
                                VALID_SSID),
                        "should post multiple detected live objects"
                },
                {
                        Arrays.asList(
                                VALID_SSID,
                                VALID_SSID,
                                INVALID_SSID,
                                INVALID_SSID),
                        Arrays.asList(
                                VALID_SSID,
                                VALID_SSID),
                        "should post only live objects"
                }
        };
    }

    @Test(dataProvider = "DetectedAndPostSsidMap")
    public void shouldPostOnlyLiveObjectsAmongAllTheWifiDevices(
            List<String> detectedSsids, List<String> postSsids, String description) {
        // arrange
        Intent intent = mock(Intent.class);
        stub(intent.getAction()).toReturn(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        List<ScanResult> scanResults = new ArrayList<>();
        for (String ssid : detectedSsids) {
            ScanResult scanResult = mock(ScanResult.class);
            scanResult.SSID = ssid;
            scanResults.add(scanResult);
        }

        stub(wifiManager.getScanResults()).toReturn(scanResults);

        // act
        scanResultsReceiver.onReceive(dummyContext, intent);

        // assert
        List<LiveObject> detectedLiveObjects = new ArrayList<>();
        for (String ssid : postSsids) {
            detectedLiveObjects.add(deviceIdTranslator.translateToLiveObject(ssid));
        }

        try {
            if (detectedLiveObjects.size() > 0) {
                NetworkDevicesAvailableEvent event = new NetworkDevicesAvailableEvent(detectedLiveObjects);
                ArgumentCaptor<NetworkDevicesAvailableEvent> argumentCaptor = ArgumentCaptor.forClass(NetworkDevicesAvailableEvent.class);
                verify(bus).post(argumentCaptor.capture());

                assertEquals(argumentCaptor.getValue().getAvailableLiveObjects(), event.getAvailableLiveObjects());
            } else {
                verify(bus, never()).post(anyObject());
            }
        } catch (MockitoAssertionError error) {
            throw new AssertionError(description, error);
        }
    }
    */
}
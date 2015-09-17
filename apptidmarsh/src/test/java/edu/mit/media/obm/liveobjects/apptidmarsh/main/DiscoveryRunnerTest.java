package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import com.squareup.otto.Bus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.LiveObjectNotifier;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by arata on 9/17/15.
 */
@RunWith(RobolectricTestRunner.class)
public class DiscoveryRunnerTest {
    @Mock @Inject NetworkController networkController;
    @Mock @Inject LiveObjectNotifier liveObjectNotifier;
    @Mock @Inject Bus bus;
    @Mock @Inject @Named("network_wifi") Bus networkWifiBus;
    @Inject DiscoveryRunner discoveryRunner;

    @Module(injects = DiscoveryRunnerTest.class)
    class TestModule {
        @Provides @Singleton
        NetworkController provideNetworkController() {
            return mock(NetworkController.class);
        }

        @Provides @Singleton
        LiveObjectNotifier provideLiveObjectNotifier() {
            return mock(LiveObjectNotifier.class);
        }

        @Provides @Singleton
        Bus provideBus() {
            return mock(Bus.class);
        }

        @Provides @Named("network_wifi") @Singleton
        Bus provideNetworkWifiBus() {
            return mock(Bus.class);
        }
    }

    @Before
    public void setUp() throws Exception {
        ObjectGraph objectGraph = ObjectGraph.create(new TestModule());
        objectGraph.inject(this);
    }

    @Test
    public void shouldRegisterBus() throws Exception {
        discoveryRunner.startDiscovery();

        verify(bus).register(discoveryRunner);
    }

    @Test
    public void shouldStartWifiDiscovery() throws Exception {
        discoveryRunner.wifiDiscoveryProcessRunning = false;
        discoveryRunner.bluetoothDiscoveryProcessRunning = false;

        discoveryRunner.startDiscovery();

        verify(networkController).startDiscovery();
        verify(liveObjectNotifier).wakeUp();
        assertThat(discoveryRunner.wifiDiscoveryProcessRunning).isEqualTo(true);
        assertThat(discoveryRunner.bluetoothDiscoveryProcessRunning).isEqualTo(true);
    }

    @Test
    public void shouldStartBluetoothDiscovery() throws Exception {
        discoveryRunner.wifiDiscoveryProcessRunning = true;
        discoveryRunner.bluetoothDiscoveryProcessRunning = false;

        discoveryRunner.startDiscovery();

        verify(networkController, never()).startDiscovery();
        verify(liveObjectNotifier).wakeUp();
        assertThat(discoveryRunner.wifiDiscoveryProcessRunning).isEqualTo(true);
        assertThat(discoveryRunner.bluetoothDiscoveryProcessRunning).isEqualTo(true);
    }

    @Test
    public void shouldNotStartWifiDiscoveryIfAlreadyStarted() throws Exception {
        discoveryRunner.wifiDiscoveryProcessRunning = false;
        discoveryRunner.bluetoothDiscoveryProcessRunning = true;

        discoveryRunner.startDiscovery();

        verify(networkController).startDiscovery();
        verify(liveObjectNotifier, never()).wakeUp();
        assertThat(discoveryRunner.wifiDiscoveryProcessRunning).isEqualTo(true);
        assertThat(discoveryRunner.bluetoothDiscoveryProcessRunning).isEqualTo(true);
    }

    @Test
    public void shouldNotStartBluetoothDiscoveryIfAlreadyStarted() throws Exception {
        discoveryRunner.wifiDiscoveryProcessRunning = true;
        discoveryRunner.bluetoothDiscoveryProcessRunning = true;

        discoveryRunner.startDiscovery();

        verify(networkController, never()).startDiscovery();
        verify(liveObjectNotifier, never()).wakeUp();
        assertThat(discoveryRunner.wifiDiscoveryProcessRunning).isEqualTo(true);
        assertThat(discoveryRunner.bluetoothDiscoveryProcessRunning).isEqualTo(true);
    }

    @Test
    public void shouldUnregisterBus() throws Exception {
        discoveryRunner.stopDiscovery();

        verify(bus).unregister(discoveryRunner);
    }

    @Test
    public void shouldClearDiscoveryFlags() throws Exception {
        discoveryRunner.wifiDiscoveryProcessRunning = true;
        discoveryRunner.bluetoothDiscoveryProcessRunning = true;

        discoveryRunner.stopDiscovery();

        assertThat(discoveryRunner.wifiDiscoveryProcessRunning).isEqualTo(false);
        assertThat(discoveryRunner.bluetoothDiscoveryProcessRunning).isEqualTo(false);
    }

    @Test
    public void shouldStopBluetoothNotificationWhenDiscovering() throws Exception {
        discoveryRunner.bluetoothDiscoveryProcessRunning = true;

        discoveryRunner.stopDiscovery();

        verify(liveObjectNotifier).cancelWakeUp();
    }

    @Test
    public void shouldStopBluetoothNotificationWhenNotDiscovering() throws Exception {
        discoveryRunner.stopDiscovery();

        verify(liveObjectNotifier, never()).cancelWakeUp();
    }

    @Test
    public void testFinalizeWifiDiscovery() throws Exception {

    }

    @Test
    public void testFinalizeBluetoothDiscovery() throws Exception {

    }
}
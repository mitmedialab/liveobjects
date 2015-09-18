package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import com.squareup.otto.Bus;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.LiveObjectNotifier;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

/**
 * Created by arata on 9/18/15.
 */
@RunWith(RobolectricTestRunner.class)
public class DiscoveryOverseerTest {
    @Mock @Inject DbController dbController;
    @Mock @Inject DiscoveryInfo discoveryInfo;
    @Mock @Inject DiscoveryRunner discoveryRunner;
    @Mock @Inject Bus bus;
    @Mock @Inject @Named("network_wifi") Bus networkWifiBus;
    @Inject DiscoveryOverseer discoveryOverseer;

    @Module(injects = DiscoveryOverseerTest.class)
    class TestModule {
        @Provides @Singleton
        DbController provideDbController() {
            return mock(DbController.class);
        }

        @Provides @Singleton
        DiscoveryInfo provideDiscoveryInfo() {
            return mock(DiscoveryInfo.class);
        }

        @Provides @Singleton
        DiscoveryRunner provideDiscoveryRunner() {
            return mock(DiscoveryRunner.class);
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

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldFindLiveObject() throws Exception {
        List<LiveObject> liveObjects = new ArrayList<>();
        liveObjects.add(new LiveObject("testObject01"));
        liveObjects.add(new LiveObject("testObject02"));
        liveObjects.add(new LiveObject("testObject03"));
        liveObjects.add(new LiveObject("testObject04"));

        stub(discoveryInfo.getAllLiveObjects()).toReturn(liveObjects);

        LiveObject foundLiveObject = discoveryOverseer.findLiveObject("testObject01");
        assertThat(foundLiveObject).isNotNull();
        assertThat(foundLiveObject.getName()).isEqualTo("testObject01");
    }

    @Test
    public void shouldFailFindLiveObject() throws Exception {
        List<LiveObject> liveObjects = new ArrayList<>();
        liveObjects.add(new LiveObject("testObject01"));
        liveObjects.add(new LiveObject("testObject02"));
        liveObjects.add(new LiveObject("testObject03"));
        liveObjects.add(new LiveObject("testObject04"));

        stub(discoveryInfo.getAllLiveObjects()).toReturn(liveObjects);

        LiveObject foundLiveObject = discoveryOverseer.findLiveObject("testObject05");
        assertThat(foundLiveObject).isNull();
    }
}
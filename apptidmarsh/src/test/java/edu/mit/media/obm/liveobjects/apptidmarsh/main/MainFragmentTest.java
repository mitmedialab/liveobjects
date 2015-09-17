package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import com.squareup.otto.Bus;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.robolectric.RobolectricTestRunner;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.LiveObjectNotifier;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

/**
 * Created by arata on 9/14/15.
 */
@RunWith(RobolectricTestRunner.class)
public class MainFragmentTest {
    @Mock @Inject NetworkController mNetworkController;
    @Mock @Inject DbController mDbController;
    @Mock @Inject ContentController mContentController;
    @Mock @Inject LiveObjectNotifier mLiveObjectNotifier;
    @Mock @Inject DiscoveryInfo mDiscoveryInfo;
    @Mock @Inject Bus mBus;

    @Inject MainFragment mainFragment;

    @Module(injects = MainFragmentTest.class)
    class TestModule {
        @Provides @Singleton
        public NetworkController provideNetworkController() {
            return mock(NetworkController.class);
        }

        @Provides @Singleton
        public DbController provideDbController() {
            return mock(DbController.class);
        }

        @Provides @Singleton
        public ContentController provideContentController() {
            return mock(ContentController.class);
        }

        @Provides @Singleton
        public LiveObjectNotifier provideLiveObjectNotifier() {
            return mock(LiveObjectNotifier.class);
        }

        @Provides @Singleton
        public DiscoveryInfo provideDiscoveryInfo() {
            return mock(DiscoveryInfo.class);
        }

        @Provides @Singleton
        public Bus provideBus() {
            return mock(Bus.class);
        }

        @Provides @Named("network_wifi") @Singleton
        public Bus provideNetworkWifiBus() {
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
}
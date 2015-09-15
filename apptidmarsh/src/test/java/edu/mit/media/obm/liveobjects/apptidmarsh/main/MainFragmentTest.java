package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import com.squareup.otto.Bus;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.LiveObjectNotifier;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;
import edu.mit.media.obm.liveobjects.middleware.control.NetworkController;

/**
 * Created by arata on 9/14/15.
 */
@RunWith(RobolectricTestRunner.class)
public class MainFragmentTest {
    @Mock @Inject NetworkController mNetworkController;
    @Mock @Inject DbController mDbController;
    @Mock @Inject ContentController mContentController;
    @Mock @Inject LiveObjectNotifier mLiveObjectNotifier;
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
        public Bus provideBus() {
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
    public void testUpdateDiscoveredLiveObjectList() throws Exception {
        List<LiveObject> liveObjectList = Whitebox.getInternalState(mainFragment, "mLiveObjectList");
        List<LiveObject> activeLiveObjectList = Whitebox.getInternalState(mainFragment, "mActiveLiveObjectList");
        List<LiveObject> sleepingLiveObjectList = Whitebox.getInternalState(mainFragment, "mSleepingLiveObjectList");
        List<LiveObject> previouslyDetectedLiveObjectList = Whitebox.getInternalState(mainFragment, "mPreviouslyDetectedLiveObjectList");

        activeLiveObjectList.add(new LiveObject("liveObject01"));
        activeLiveObjectList.add(new LiveObject("liveObject02"));
        activeLiveObjectList.add(new LiveObject("liveObject03"));
        activeLiveObjectList.add(new LiveObject("liveObject04"));

        for (LiveObject liveObject : activeLiveObjectList) {
            liveObject.setStatus(LiveObject.STATUS_ACTIVE);
        }

        sleepingLiveObjectList.add(new LiveObject("liveObject03"));
        sleepingLiveObjectList.add(new LiveObject("liveObject04"));
        sleepingLiveObjectList.add(new LiveObject("liveObject05"));
        sleepingLiveObjectList.add(new LiveObject("liveObject06"));

        for (LiveObject liveObject : sleepingLiveObjectList) {
            liveObject.setStatus(LiveObject.STATUS_SLEEPING);
        }

        previouslyDetectedLiveObjectList.add(new LiveObject("liveObject02"));
        previouslyDetectedLiveObjectList.add(new LiveObject("liveObject04"));
        previouslyDetectedLiveObjectList.add(new LiveObject("liveObject06"));
        previouslyDetectedLiveObjectList.add(new LiveObject("liveObject07"));

        for (LiveObject liveObject : previouslyDetectedLiveObjectList) {
            liveObject.setStatus(LiveObject.STATUS_OUT_OF_SITE);
        }

        Whitebox.invokeMethod(mainFragment, "updateLiveObjectList");

        LiveObject[] expectedLiveObjectList = new LiveObject[] {
                activeLiveObjectList.get(0),
                activeLiveObjectList.get(1),
                activeLiveObjectList.get(2),
                activeLiveObjectList.get(3),
                sleepingLiveObjectList.get(2),
                sleepingLiveObjectList.get(3),
                previouslyDetectedLiveObjectList.get(3),
        };
        Assert.assertArrayEquals(expectedLiveObjectList, liveObjectList.toArray());
    }
}
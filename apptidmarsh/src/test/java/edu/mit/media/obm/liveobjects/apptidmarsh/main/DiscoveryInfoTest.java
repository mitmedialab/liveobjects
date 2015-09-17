package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

/**
 * Created by arata on 9/14/15.
 */
public class DiscoveryInfoTest {
    @Inject DiscoveryInfo discoveryInfo;
    @Inject DbController dbController;

    private final LiveObject testLiveObject = new LiveObject("test_name");

    @Module(injects = DiscoveryInfoTest.class)
    class TestModule {
        @Inject @Singleton
        DbController provideDbController() {
            return mock(DbController.class);
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
        List<LiveObject> liveObjectList = discoveryInfo.mLiveObjectList;
        List<LiveObject> activeLiveObjectList = discoveryInfo.mActiveLiveObjectList;
        List<LiveObject> sleepingLiveObjectList = discoveryInfo.mSleepingLiveObjectList;
        List<LiveObject> previouslyDetectedLiveObjectList = discoveryInfo.mPreviouslyDetectedLiveObjectList;

        // live objects stored originally in the list must be removed
        liveObjectList.add(new LiveObject("liveObjectXX"));
        liveObjectList.add(new LiveObject("liveObjectYY"));
        liveObjectList.add(new LiveObject("liveObjectZZ"));

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

        discoveryInfo.updateLiveObjectList();

        LiveObject[] expectedLiveObjectList = new LiveObject[] {
                activeLiveObjectList.get(0),
                activeLiveObjectList.get(1),
                activeLiveObjectList.get(2),
                activeLiveObjectList.get(3),
                sleepingLiveObjectList.get(2),
                sleepingLiveObjectList.get(3),
                previouslyDetectedLiveObjectList.get(3),
        };
        assertArrayEquals(expectedLiveObjectList, liveObjectList.toArray());
    }

    @Test
    public void shouldAddActiveLiveObject() throws Exception {
        stub(dbController.isLiveObjectEmpty(anyString())).toReturn(true);
        final int NUM_LIVE_OBJECTS = 4;
        for (int i = 0; i < NUM_LIVE_OBJECTS; i++) {
            discoveryInfo.addActiveLiveObject(testLiveObject);
        }

        List<LiveObject> activeLiveObjectList = discoveryInfo.mActiveLiveObjectList;

        assertEquals(NUM_LIVE_OBJECTS, activeLiveObjectList.size());
        for (LiveObject liveObject : activeLiveObjectList) {
            assertEquals("test_name", liveObject.getLiveObjectName());
            assertEquals(LiveObject.STATUS_ACTIVE, liveObject.getStatus());
            assertTrue(liveObject.getConnectedBefore());
        }
    }

    @Test
    public void testAddSleepingLiveObject() throws Exception {

    }
}
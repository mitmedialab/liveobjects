package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

/**
 * Created by arata on 9/14/15.
 */
@RunWith(DataProviderRunner.class)
public class DiscoveryInfoTest {
    @Inject DiscoveryInfo discoveryInfo;
    @Inject DbController dbController;

    private final LiveObject testLiveObject = new LiveObject("test_name");

    @Module(injects = DiscoveryInfoTest.class)
    class TestModule {
        @Provides @Singleton
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

    private static class StoredLiveObject {
        String liveObjectName;
        boolean connectedBefore;

        public StoredLiveObject(String liveObjectName, boolean connectedBefore) {
            this.liveObjectName = liveObjectName;
            this.connectedBefore = connectedBefore;
        }
    }

    @DataProvider
    public static Object[][] provideLiveObjectsForAddTests() {
        return new Object[][] {
                {
                        Arrays.asList(
                                new StoredLiveObject("liveObject01", false))
                },
                {
                        Arrays.asList(
                                new StoredLiveObject("liveObject01", true))
                },
                {
                        Arrays.asList(
                                new StoredLiveObject("liveObject01", false),
                                new StoredLiveObject("liveObject02", false),
                                new StoredLiveObject("liveObject03", false),
                                new StoredLiveObject("liveObject04", false))
                },
                {
                        Arrays.asList(
                                new StoredLiveObject("liveObject01", true),
                                new StoredLiveObject("liveObject02", true),
                                new StoredLiveObject("liveObject03", true),
                                new StoredLiveObject("liveObject04", true))
                },
                {
                        Arrays.asList(
                                new StoredLiveObject("liveObject01", true),
                                new StoredLiveObject("liveObject02", false),
                                new StoredLiveObject("liveObject03", true),
                                new StoredLiveObject("liveObject04", false))
                },
        };
    }

    private void stubIsLiveObjectEmpty(List<StoredLiveObject> storedLiveObjects) {
        for (StoredLiveObject storedLiveObject : storedLiveObjects) {
            String liveObjectName = storedLiveObject.liveObjectName;
            boolean isConnectedBefore = storedLiveObject.connectedBefore;
            stub(dbController.isLiveObjectEmpty(liveObjectName)).toReturn(!isConnectedBefore);
        }
    }

    private void analyzeAddedLiveObjects(List<StoredLiveObject> storedLiveObjects, List<LiveObject> resultLiveObjects, int expectedStatus) {
        assertEquals(storedLiveObjects.size(), resultLiveObjects.size());
        for (int i = 0; i < storedLiveObjects.size(); i++) {
            assertEquals(storedLiveObjects.get(i).liveObjectName, resultLiveObjects.get(i).getLiveObjectName());
            assertEquals(expectedStatus, resultLiveObjects.get(i).getStatus());
            assertEquals(storedLiveObjects.get(i).connectedBefore, resultLiveObjects.get(i).getConnectedBefore());
        }
    }

    @Test @UseDataProvider("provideLiveObjectsForAddTests")
    public void shouldAddActiveLiveObject(List<StoredLiveObject> storedLiveObjects) throws Exception {
        stubIsLiveObjectEmpty(storedLiveObjects);

        for (StoredLiveObject storedLiveObject : storedLiveObjects) {
            LiveObject liveObject = new LiveObject(storedLiveObject.liveObjectName);
            discoveryInfo.addActiveLiveObject(liveObject);
        }

        analyzeAddedLiveObjects(storedLiveObjects, discoveryInfo.mActiveLiveObjectList, LiveObject.STATUS_ACTIVE);
    }

    @Test @UseDataProvider("provideLiveObjectsForAddTests")
    public void shouldAddSleepingObject(List<StoredLiveObject> storedLiveObjects) throws Exception {
        stubIsLiveObjectEmpty(storedLiveObjects);

        for (StoredLiveObject storedLiveObject : storedLiveObjects) {
            LiveObject liveObject = new LiveObject(storedLiveObject.liveObjectName);
            discoveryInfo.addSleepingLiveObject(liveObject);
        }

        analyzeAddedLiveObjects(storedLiveObjects, discoveryInfo.mSleepingLiveObjectList, LiveObject.STATUS_SLEEPING);
    }
}
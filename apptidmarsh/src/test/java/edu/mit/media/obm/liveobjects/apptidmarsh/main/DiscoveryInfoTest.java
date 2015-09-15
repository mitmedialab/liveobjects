package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.List;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;

import static org.mockito.Mockito.mock;

/**
 * Created by arata on 9/14/15.
 */
public class DiscoveryInfoTest {
    private DiscoveryInfo discoveryInfo;

    @Before
    public void setUp() throws Exception {
        discoveryInfo = new DiscoveryInfo();
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
        Assert.assertArrayEquals(expectedLiveObjectList, liveObjectList.toArray());
    }
}
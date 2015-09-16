package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.bouncycastle.util.Store;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import static org.assertj.core.api.Assertions.*;

/**
 * Created by arata on 9/14/15.
 */
@RunWith(DataProviderRunner.class)
public class DiscoveryInfoTest {
    @Inject DiscoveryInfo discoveryInfo;
    @Inject DbController dbController;

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
        List<LiveObject> previouslyDetectedLiveObjectList = discoveryInfo.mPreviouslyDetectedLiveObjectList;

        // live objects stored originally in the list must be removed
        liveObjectList.add(new LiveObject("liveObjectXX"));
        liveObjectList.add(new LiveObject("liveObjectYY"));
        liveObjectList.add(new LiveObject("liveObjectZZ"));

        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject02"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject03"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject04"));

        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject03"));
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject04"));
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject05"));
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject06"));

        previouslyDetectedLiveObjectList.add(new LiveObject("liveObject02"));
        previouslyDetectedLiveObjectList.add(new LiveObject("liveObject04"));
        previouslyDetectedLiveObjectList.add(new LiveObject("liveObject06"));
        previouslyDetectedLiveObjectList.add(new LiveObject("liveObject07"));

        for (LiveObject liveObject : previouslyDetectedLiveObjectList) {
            liveObject.setStatus(LiveObject.STATUS_OUT_OF_SITE);
        }

        discoveryInfo.updateLiveObjectList();

        assertThat(liveObjectList)
                .extracting("liveObjectName", "status")
                .containsExactly(
                        tuple("liveObject01", LiveObject.STATUS_ACTIVE),
                        tuple("liveObject02", LiveObject.STATUS_ACTIVE),
                        tuple("liveObject03", LiveObject.STATUS_ACTIVE),
                        tuple("liveObject04", LiveObject.STATUS_ACTIVE),
                        tuple("liveObject05", LiveObject.STATUS_SLEEPING),
                        tuple("liveObject06", LiveObject.STATUS_SLEEPING),
                        tuple("liveObject07", LiveObject.STATUS_OUT_OF_SITE));
    }

    @Test
    public void shouldAddLiveObjectNotConnectedBefore() {
        stub(dbController.isLiveObjectEmpty(anyString())).toReturn(true);

        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.updateLiveObjectList();

        assertThat(discoveryInfo.mLiveObjectList)
                .extracting("liveObjectName", "connectedBefore")
                .containsOnly(tuple("liveObject01", false));
    }

    @Test
    public void shouldAddLiveObjectConnectedBefore() {
        stub(dbController.isLiveObjectEmpty(anyString())).toReturn(false);

        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.updateLiveObjectList();

        assertThat(discoveryInfo.mLiveObjectList)
                .extracting("liveObjectName", "connectedBefore")
                .containsOnly(tuple("liveObject01", true));
    }

    @Test
    public void shouldAddMultipleLiveObjectNotConnectedBefore() {
        stub(dbController.isLiveObjectEmpty(anyString())).toReturn(true);

        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject02"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject03"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject04"));
        discoveryInfo.updateLiveObjectList();

        assertThat(discoveryInfo.mLiveObjectList)
                .extracting("liveObjectName", "connectedBefore")
                .containsOnly(
                        tuple("liveObject01", false),
                        tuple("liveObject02", false),
                        tuple("liveObject03", false),
                        tuple("liveObject04", false));
    }

    @Test
    public void shouldAddMultipleLiveObjectConnectedBefore() {
        stub(dbController.isLiveObjectEmpty(anyString())).toReturn(false);

        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject02"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject03"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject04"));
        discoveryInfo.updateLiveObjectList();

        assertThat(discoveryInfo.mLiveObjectList)
                .extracting("liveObjectName", "connectedBefore")
                .containsOnly(
                        tuple("liveObject01", true),
                        tuple("liveObject02", true),
                        tuple("liveObject03", true),
                        tuple("liveObject04", true));
    }

    @Test
    public void shouldAddMultipleLiveObjectBothConnectedAndNotConnectedBefore() {
        stub(dbController.isLiveObjectEmpty("liveObject01")).toReturn(true);
        stub(dbController.isLiveObjectEmpty("liveObject02")).toReturn(false);
        stub(dbController.isLiveObjectEmpty("liveObject03")).toReturn(true);
        stub(dbController.isLiveObjectEmpty("liveObject04")).toReturn(false);

        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject02"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject03"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject04"));
        discoveryInfo.updateLiveObjectList();

        assertThat(discoveryInfo.mLiveObjectList)
                .extracting("liveObjectName", "connectedBefore")
                .containsOnly(
                        tuple("liveObject01", false),
                        tuple("liveObject02", true),
                        tuple("liveObject03", false),
                        tuple("liveObject04", true));
    }

    @Test
    public void shouldClearActiveLiveObject() throws Exception {
        final int NUM_LIVE_OBJECTS = 10;

        for (int i = 0; i < NUM_LIVE_OBJECTS; i++) {
            String name = String.format("liveObject%02d", i);
            discoveryInfo.addActiveLiveObject(new LiveObject(name));
        }

        discoveryInfo.clearActiveLiveObject();
        discoveryInfo.updateLiveObjectList();

        assertEquals(0, discoveryInfo.mLiveObjectList.size());
    }

    @Test
    public void shouldClearSleepingLiveObject() throws Exception {
        final int NUM_LIVE_OBJECTS = 10;

        for (int i = 0; i < NUM_LIVE_OBJECTS; i++) {
            String name = String.format("liveObject%02d", i);
            discoveryInfo.addSleepingLiveObject(new LiveObject(name));
        }

        discoveryInfo.clearSleepingLiveObject();
        discoveryInfo.updateLiveObjectList();

        assertEquals(0, discoveryInfo.mLiveObjectList.size());
    }
}
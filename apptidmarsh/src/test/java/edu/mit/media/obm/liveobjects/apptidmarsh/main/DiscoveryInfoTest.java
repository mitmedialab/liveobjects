package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import com.tngtech.java.junit.dataprovider.DataProviderRunner;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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

import static org.assertj.core.api.Assertions.*;

/**
 * Created by arata on 9/14/15.
 */
@RunWith(DataProviderRunner.class)
public class DiscoveryInfoTest {
    @Inject DiscoveryInfo discoveryInfo;
    @Inject DbController dbController;

    private final String FIELD_NAME = "name";
    private final String FIELD_STATUS = "status";
    private final String FIELD_CONNECTED_BEFORE = "connectedBefore";

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
    public void shouldAddActiveLiveObjects() throws Exception {
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject02"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject03"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject04"));

        List<LiveObject> allDiscoveredLiveObjects = discoveryInfo.getAllLiveObjects();

        assertThat(allDiscoveredLiveObjects)
                .extracting("name", "status")
                .containsExactly(
                        tuple("liveObject01", LiveObject.STATUS_ACTIVE),
                        tuple("liveObject02", LiveObject.STATUS_ACTIVE),
                        tuple("liveObject03", LiveObject.STATUS_ACTIVE),
                        tuple("liveObject04", LiveObject.STATUS_ACTIVE));
    }

    @Test
    public void shouldAddSleepingLiveObjects() throws Exception {
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject02"));
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject03"));
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject04"));

        List<LiveObject> allDiscoveredLiveObjects = discoveryInfo.getAllLiveObjects();

        assertThat(allDiscoveredLiveObjects)
                .extracting(FIELD_NAME, FIELD_STATUS)
                .containsExactly(
                        tuple("liveObject01", LiveObject.STATUS_SLEEPING),
                        tuple("liveObject02", LiveObject.STATUS_SLEEPING),
                        tuple("liveObject03", LiveObject.STATUS_SLEEPING),
                        tuple("liveObject04", LiveObject.STATUS_SLEEPING));
    }

    @Test
    public void shouldAddLostLiveObjects() throws Exception {
        discoveryInfo.addLostLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.addLostLiveObject(new LiveObject("liveObject02"));
        discoveryInfo.addLostLiveObject(new LiveObject("liveObject03"));
        discoveryInfo.addLostLiveObject(new LiveObject("liveObject04"));

        List<LiveObject> allDiscoveredLiveObjects = discoveryInfo.getAllLiveObjects();

        assertThat(allDiscoveredLiveObjects)
                .extracting(FIELD_NAME, FIELD_STATUS)
                .containsExactly(
                        tuple("liveObject01", LiveObject.STATUS_LOST),
                        tuple("liveObject02", LiveObject.STATUS_LOST),
                        tuple("liveObject03", LiveObject.STATUS_LOST),
                        tuple("liveObject04", LiveObject.STATUS_LOST));
    }

    @Test
    public void shouldAddActiveLiveObjectWithHigherPriority() throws Exception {
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject01"));

        List<LiveObject> allDiscoveredLiveObjects = discoveryInfo.getAllLiveObjects();

        assertThat(allDiscoveredLiveObjects)
                .extracting(FIELD_NAME, FIELD_STATUS)
                .containsExactly(
                        tuple("liveObject01", LiveObject.STATUS_ACTIVE));
    }

    @Test
    public void shouldAddSleepingLiveObjectWithHigherPriority() throws Exception {
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.addLostLiveObject(new LiveObject("liveObject01"));

        List<LiveObject> allDiscoveredLiveObjects = discoveryInfo.getAllLiveObjects();

        assertThat(allDiscoveredLiveObjects)
                .extracting(FIELD_NAME, FIELD_STATUS)
                .containsExactly(
                        tuple("liveObject01", LiveObject.STATUS_SLEEPING));
    }

    @Test
    public void shouldAddAllTheStoredLiveObjectCorrectly() throws Exception {
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject02"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject03"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject04"));

        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject03"));
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject04"));
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject05"));
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject06"));

        discoveryInfo.addLostLiveObject(new LiveObject("liveObject02"));
        discoveryInfo.addLostLiveObject(new LiveObject("liveObject04"));
        discoveryInfo.addLostLiveObject(new LiveObject("liveObject06"));
        discoveryInfo.addLostLiveObject(new LiveObject("liveObject07"));

        List<LiveObject> allDiscoveredLiveObjects = discoveryInfo.getAllLiveObjects();

        assertThat(allDiscoveredLiveObjects)
                .extracting(FIELD_NAME, FIELD_STATUS)
                .containsExactly(
                        tuple("liveObject01", LiveObject.STATUS_ACTIVE),
                        tuple("liveObject02", LiveObject.STATUS_ACTIVE),
                        tuple("liveObject03", LiveObject.STATUS_ACTIVE),
                        tuple("liveObject04", LiveObject.STATUS_ACTIVE),
                        tuple("liveObject05", LiveObject.STATUS_SLEEPING),
                        tuple("liveObject06", LiveObject.STATUS_SLEEPING),
                        tuple("liveObject07", LiveObject.STATUS_LOST));
    }

    @Test
    public void shouldAddLiveObjectNotConnectedBefore() {
        stub(dbController.isLiveObjectEmpty(anyString())).toReturn(true);

        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject01"));
        List<LiveObject> allDiscoveredLiveObjects = discoveryInfo.getAllLiveObjects();

        assertThat(allDiscoveredLiveObjects)
                .extracting(FIELD_NAME, FIELD_CONNECTED_BEFORE)
                .containsOnly(tuple("liveObject01", false));
    }

    @Test
    public void shouldAddLiveObjectConnectedBefore() {
        stub(dbController.isLiveObjectEmpty(anyString())).toReturn(false);

        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject01"));
        List<LiveObject> allDiscoveredLiveObjects = discoveryInfo.getAllLiveObjects();

        assertThat(allDiscoveredLiveObjects)
                .extracting(FIELD_NAME, FIELD_CONNECTED_BEFORE)
                .containsOnly(tuple("liveObject01", true));
    }

    @Test
    public void shouldAddMultipleLiveObjectNotConnectedBefore() {
        stub(dbController.isLiveObjectEmpty(anyString())).toReturn(true);

        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject02"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject03"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject04"));
        List<LiveObject> allDiscoveredLiveObjects = discoveryInfo.getAllLiveObjects();

        assertThat(allDiscoveredLiveObjects)
                .extracting(FIELD_NAME, FIELD_CONNECTED_BEFORE)
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
        List<LiveObject> allDiscoveredLiveObjects = discoveryInfo.getAllLiveObjects();

        assertThat(allDiscoveredLiveObjects)
                .extracting(FIELD_NAME, FIELD_CONNECTED_BEFORE)
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
        List<LiveObject> allDiscoveredLiveObjects = discoveryInfo.getAllLiveObjects();

        assertThat(allDiscoveredLiveObjects)
                .extracting(FIELD_NAME, FIELD_CONNECTED_BEFORE)
                .containsOnly(
                        tuple("liveObject01", false),
                        tuple("liveObject02", true),
                        tuple("liveObject03", false),
                        tuple("liveObject04", true));
    }

    @Test
    public void shouldClearActiveLiveObject() throws Exception {
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject02"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject03"));
        discoveryInfo.addActiveLiveObject(new LiveObject("liveObject04"));

        discoveryInfo.clearActiveLiveObject();
        List<LiveObject> allDiscoveredLiveObjects = discoveryInfo.getAllLiveObjects();

        assertThat(allDiscoveredLiveObjects).isEmpty();
    }

    @Test
    public void shouldClearSleepingLiveObject() throws Exception {
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject02"));
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject03"));
        discoveryInfo.addSleepingLiveObject(new LiveObject("liveObject04"));

        discoveryInfo.clearSleepingLiveObject();
        List<LiveObject> allDiscoveredLiveObjects = discoveryInfo.getAllLiveObjects();

        assertThat(allDiscoveredLiveObjects).isEmpty();
    }

    @Test
    public void shouldClearLostLiveObject() throws Exception {
        discoveryInfo.addLostLiveObject(new LiveObject("liveObject01"));
        discoveryInfo.addLostLiveObject(new LiveObject("liveObject02"));
        discoveryInfo.addLostLiveObject(new LiveObject("liveObject03"));
        discoveryInfo.addLostLiveObject(new LiveObject("liveObject04"));

        discoveryInfo.clearLostLiveObject();
        List<LiveObject> allDiscoveredLiveObjects = discoveryInfo.getAllLiveObjects();

        assertThat(allDiscoveredLiveObjects).isEmpty();
    }
}
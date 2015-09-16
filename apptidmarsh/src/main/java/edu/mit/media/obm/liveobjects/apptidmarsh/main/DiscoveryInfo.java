package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectContract;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.common.MapLocation;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;

/**
 * Created by arata on 9/14/15.
 */
public class DiscoveryInfo {
    DbController mDbController;

    private List<LiveObject> mActiveLiveObjectList = new ArrayList<>();
    private List<LiveObject> mSleepingLiveObjectList = new ArrayList<>();
    private List<LiveObject> mLostLiveObjectList = new ArrayList<>();

    @Inject
    public DiscoveryInfo(DbController dbController) {
        mDbController = dbController;
    }

    public List<LiveObject> getAllLiveObjects() {
        List<LiveObject> liveObjects = new ArrayList<>();
        liveObjects.addAll(mActiveLiveObjectList);

        // add only ones in active list if the same live object exists both in active and in
        // sleeping lists.
        addLiveObjectsWithUniqueNames(liveObjects, mSleepingLiveObjectList);
        addLiveObjectsWithUniqueNames(liveObjects, mLostLiveObjectList);

        return liveObjects;
    }

    private static void addLiveObjectsWithUniqueNames(
            List<LiveObject> destinationLiveObjectList, List<LiveObject> sourceLiveObjectList) {

        for (LiveObject liveObject : sourceLiveObjectList) {
            if (!isLiveObjectWithSameNameIncluded(liveObject, destinationLiveObjectList)) {
                destinationLiveObjectList.add(liveObject);
            }
        }
    }

    private static boolean isLiveObjectWithSameNameIncluded(LiveObject liveObject, List<LiveObject> liveObjectList) {
        for (LiveObject liveObjectInList : liveObjectList) {
            String name = liveObject.getName();
            String nameInList = liveObjectInList.getName();
            if (name.equals(nameInList)) {
                return true;
            }
        }

        return false;
    }

    public void addActiveLiveObject(LiveObject liveObject) {
        addLiveObjectToDb(mActiveLiveObjectList, LiveObject.STATUS_ACTIVE, liveObject);
    }

    public void addSleepingLiveObject(LiveObject liveObject) {
        addLiveObjectToDb(mSleepingLiveObjectList, LiveObject.STATUS_SLEEPING, liveObject);
    }

    public void addLostLiveObject(LiveObject liveObject) {
        addLiveObjectToDb(mLostLiveObjectList, LiveObject.STATUS_LOST, liveObject);
    }

    private void addLiveObjectToDb(List<LiveObject> listObjectList, int liveObjectStatus, LiveObject liveObject) {
        liveObject.setStatus(liveObjectStatus);
        liveObject.setConnectedBefore(isConnectedBefore(liveObject));
        listObjectList.add(liveObject);

        addLiveObjectToDb(liveObject);
    }

    private boolean isConnectedBefore(LiveObject liveObject) {
        String liveObjectName = liveObject.getName();

        return !mDbController.isLiveObjectEmpty(liveObjectName);
    }

    private void addLiveObjectToDb(LiveObject liveObject) {
        if (!isLiveObjectInDb(liveObject)) {
            // register all the detected live objects with empty properties
            Map<String, Object> emptyProperties = new HashMap<>();
            // add map location to properties
            MapLocation mapLocation = liveObject.getMapLocation();
            emptyProperties.put(MLProjectContract.MAP_LOCATION_X, mapLocation.getX());
            emptyProperties.put(MLProjectContract.MAP_LOCATION_Y, mapLocation.getY());
            emptyProperties.put(MLProjectContract.MAP_ID, mapLocation.getId());
            emptyProperties.put(MLProjectContract.IS_FAVORITE, MLProjectContract.IS_FAVORITE_FALSE);
            mDbController.putLiveObject(liveObject.getName(), emptyProperties);
        }
    }

    private boolean isLiveObjectInDb(LiveObject liveObject) {
        List<String> allLiveObjectNamesInDb = mDbController.getLiveObjectsIds();
        String liveObjectName = liveObject.getName();

        return (allLiveObjectNamesInDb.contains(liveObjectName));
    }

    public void clearActiveLiveObject() {
        mActiveLiveObjectList.clear();
    }

    public void clearSleepingLiveObject() {
        mSleepingLiveObjectList.clear();
    }

    public void  clearLostLiveObject() {
        mLostLiveObjectList.clear();
    }
}

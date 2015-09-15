package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;

/**
 * Created by arata on 9/14/15.
 */
public class DiscoveryInfo {
    DbController mDbController;

    public ArrayList<LiveObject> mLiveObjectList = new ArrayList<>();
    public ArrayList<LiveObject> mActiveLiveObjectList = new ArrayList<>();
    public ArrayList<LiveObject> mSleepingLiveObjectList = new ArrayList<>();
    public ArrayList<LiveObject> mPreviouslyDetectedLiveObjectList = new ArrayList<>();

    @Inject
    public void DiscoveryInfo(DbController dbController) {
        mDbController = dbController;
    }

    public void updateLiveObjectList() {
        mLiveObjectList.clear();
        mLiveObjectList.addAll(mActiveLiveObjectList);

        // add only ones in active list if the same live object exists both in active and in
        // sleeping lists.
        addLiveObjectsWithUniqueNames(mLiveObjectList, mSleepingLiveObjectList);
        addLiveObjectsWithUniqueNames(mLiveObjectList, mPreviouslyDetectedLiveObjectList);
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
            String name = liveObject.getLiveObjectName();
            String nameInList = liveObjectInList.getLiveObjectName();
            if (name.equals(nameInList)) {
                return true;
            }
        }

        return false;
    }

    public void addActiveLiveObject(LiveObject liveObject) {
        liveObject.setConnectedBefore(isConnectedBefore(liveObject));
        mActiveLiveObjectList.add(liveObject);
    }

    public void addSleepingLiveObject(LiveObject liveObject) {
        liveObject.setStatus(LiveObject.STATUS_SLEEPING);
        liveObject.setConnectedBefore(isConnectedBefore(liveObject));
        mSleepingLiveObjectList.add(liveObject);
    }

    private boolean isConnectedBefore(LiveObject liveObject) {
        return !mDbController.isLiveObjectEmpty(liveObject.getLiveObjectName());
    }
}

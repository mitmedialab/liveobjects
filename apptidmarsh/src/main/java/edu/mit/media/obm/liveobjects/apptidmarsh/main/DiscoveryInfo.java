package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import java.util.ArrayList;
import java.util.List;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;

/**
 * Created by arata on 9/14/15.
 */
public class DiscoveryInfo {
    public ArrayList<LiveObject> mLiveObjectList = new ArrayList<>();
    public ArrayList<LiveObject> mActiveLiveObjectList = new ArrayList<>();
    public ArrayList<LiveObject> mSleepingLiveObjectList = new ArrayList<>();
    public ArrayList<LiveObject> mPreviouslyDetectedLiveObjectList = new ArrayList<>();

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
}

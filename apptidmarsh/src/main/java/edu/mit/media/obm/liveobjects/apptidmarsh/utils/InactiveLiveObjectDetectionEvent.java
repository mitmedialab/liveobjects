package edu.mit.media.obm.liveobjects.apptidmarsh.utils;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;

/**
 * Created by arata on 8/7/15.
 */
public class InactiveLiveObjectDetectionEvent {
    public final LiveObject mLiveObject;

    public InactiveLiveObjectDetectionEvent(LiveObject liveObject) {
        mLiveObject = liveObject;
    }
}

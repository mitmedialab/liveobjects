package edu.mit.media.obm.liveobjects.apptidmarsh.utils;

import android.content.Context;
import android.util.Log;

/**
 * Created by arata on 8/7/15.
 */
public class BeaconNotifier extends LiveObjectNotifier {
    private static final String LOG_TAG = BeaconNotifier.class.getSimpleName();

    public BeaconNotifier(Context appContext) {
        super(appContext);
    }

    @Override
    public synchronized void wakeUp() {
    }

    @Override
    public synchronized void cancelWakeUp() {
    }

    private void debug(String message) {
        Log.d(LOG_TAG, message);
    }
}

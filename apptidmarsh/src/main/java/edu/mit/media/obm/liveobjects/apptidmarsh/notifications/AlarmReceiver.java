package edu.mit.media.obm.liveobjects.apptidmarsh.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receives periodic events from the alarm manager and triggers periodic live objects discovery
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = AlarmReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, DiscoveryService.class);

        Log.d(LOG_TAG, "starting Discovery Service");

        context.startService(serviceIntent);
    }

}

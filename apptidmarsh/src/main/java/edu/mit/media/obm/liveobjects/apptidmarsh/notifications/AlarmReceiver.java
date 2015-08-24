package edu.mit.media.obm.liveobjects.apptidmarsh.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.noveogroup.android.log.Log;

/**
 * Receives periodic events from the alarm manager and triggers periodic live objects discovery
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, DiscoveryService.class);

        Log.d("starting Discovery Service");

        context.startService(serviceIntent);
    }

}

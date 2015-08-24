package edu.mit.media.obm.liveobjects.apptidmarsh.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.noveogroup.android.log.Log;

import javax.inject.Inject;

/**
 * Manages the periodic triggering of discovery events.
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class PeriodicAlarmManager {
    private static final String LOG_TAG = PeriodicAlarmManager.class.getSimpleName();
    private static final long SECOND = 1000;

    @Inject Intent mAlarmReceiverIntent;
    @Inject Context mContext;
    @Inject AlarmManager mAlarm;

    public PeriodicAlarmManager(Intent alarmReceiverIntent, Context context, AlarmManager alarmManager) {
        mAlarmReceiverIntent = alarmReceiverIntent;
        mContext = context;
        mAlarm = alarmManager;
    }

    public void startPeriodicService() {
        Log.d("startingPeriodicService");
        long triggeringNow = System.currentTimeMillis();
        long periodicInterval = 60 * SECOND;


        PendingIntent pendingIntent = getPendingIntent();
        mAlarm.setRepeating(AlarmManager.RTC_WAKEUP, triggeringNow, periodicInterval, pendingIntent);

    }


    public void stopPeriodicService() {
        Log.d(LOG_TAG, "stopping periodic service");
        PendingIntent pendingIntent = getPendingIntent();
        pendingIntent.cancel();
        mAlarm.cancel(pendingIntent);

    }

    private PendingIntent getPendingIntent() {
        return PendingIntent.getBroadcast(mContext, 0, mAlarmReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

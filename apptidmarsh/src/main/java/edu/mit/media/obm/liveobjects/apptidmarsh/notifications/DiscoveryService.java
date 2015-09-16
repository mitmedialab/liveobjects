package edu.mit.media.obm.liveobjects.apptidmarsh.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.noveogroup.android.log.Log;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.apptidmarsh.main.MainActivity;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.BluetoothNotifier;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.InactiveLiveObjectDetectionEvent;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.LiveObjectNotifier;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * Background service that performs live-objects discovery and send a notification
 * when a live object has been recently discovered.
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */

public class DiscoveryService extends Service {
    private static final String LOG_TAG = DiscoveryService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;


    private Context mContext;

    private LiveObjectNotifier mLiveObjectNotifier;

    @Inject Bus mBus;

    @Override
    public void onCreate() {
        super.onCreate();
        DependencyInjector.inject(this, this);

        mContext = this;
        mLiveObjectNotifier = new BluetoothNotifier(mContext);

        mBus.register(this);


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        performDiscovery();
        return START_NOT_STICKY;
    }


    private void performDiscovery() {

        Log.d(LOG_TAG, "starting Bluetooth discovery");

        mLiveObjectNotifier.wakeUp();


    }


    @Subscribe
    public void addDetectedBluetoothDevice(InactiveLiveObjectDetectionEvent event) {

        Log.d(LOG_TAG, "received InactiveLiveObjectDetectionEvent");
        LiveObject liveObject = event.mLiveObject;
        String liveObjectName = liveObject.getName();

        Log.d(LOG_TAG, "send a notification for liveobject: " + liveObjectName);

        sendNotification(liveObjectName);

        stopSelf();


    }



    private void sendNotification(String msg) {
        NotificationManager notificationManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Live Object discovered")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        Notification note = mBuilder.build();
        note.defaults |= Notification.DEFAULT_ALL;
        notificationManager.notify(NOTIFICATION_ID, note);


    }

    private void clean() {
        Log.d(LOG_TAG, "clean");
        mBus.unregister(this);
        mLiveObjectNotifier.cancelWakeUp();


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        clean();

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(LOG_TAG, "onTaskRemoved");
        clean();
    }
}

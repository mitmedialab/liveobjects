package edu.mit.media.obm.liveobjects.apptidmarsh.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import edu.mit.media.obm.liveobjects.apptidmarsh.TidmarshApplication;
import edu.mit.media.obm.liveobjects.apptidmarsh.main.MainActivity;
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
public class DiscoveryService extends IntentService {
    private static final String LOG_TAG = DiscoveryService.class.getSimpleName();

    public static final int NOTIFICATION_ID = 1;

    //todo create with dependency injection
    private LiveObjectNotifier mLiveObjectNotifier;


    private Bus mBus;
    private NotificationManager mNotificationManager;

    public DiscoveryService() {
        super("DiscoveryService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // registering the service on the bus
        mBus = ((TidmarshApplication) getApplication()).getBus();
        mBus.register(this);
        mLiveObjectNotifier = new BluetoothNotifier(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        performDiscovery();

    }

    private void performDiscovery() {
        Log.v(LOG_TAG, "starting Bluetooth discovery");
        mLiveObjectNotifier.wakeUp();
    }

    @Subscribe
    public void addDetectedBluetoothDevice(InactiveLiveObjectDetectionEvent event) {
        Log.v(LOG_TAG, "received InactiveLiveObjectDetectionEvent");
        LiveObject liveObject = event.mLiveObject;
        String liveObjectName = liveObject.getLiveObjectName();

        Log.v(LOG_TAG, "send a notification for liveobject: " + liveObjectName);
        String liveobjectName = "";
        sendNotification(liveobjectName);

    }


    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Live Object discovered")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        Notification note = mBuilder.build();
        note.defaults |= Notification.DEFAULT_VIBRATE;
        mNotificationManager.notify(NOTIFICATION_ID, note);
    }


}

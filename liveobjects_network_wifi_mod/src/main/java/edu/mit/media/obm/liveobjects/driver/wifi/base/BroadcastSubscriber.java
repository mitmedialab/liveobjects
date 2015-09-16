package edu.mit.media.obm.liveobjects.driver.wifi.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

/**
 * Created by arata on 9/11/15.
 */
public abstract class BroadcastSubscriber extends ActivatableEntity {
    protected Context context;

    protected BroadcastReceiver broadcastReceiver;

    public BroadcastSubscriber(Context context) {
        this.context = context;
    }

    @Override
    protected void activateEntity() {
        broadcastReceiver = createBroadcastReceiver();
        IntentFilter intentFilter = createIntentFilter();

        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void deactivateEntity() {
        context.unregisterReceiver(broadcastReceiver);
    }

    protected abstract BroadcastReceiver createBroadcastReceiver();
    protected abstract IntentFilter createIntentFilter();
}

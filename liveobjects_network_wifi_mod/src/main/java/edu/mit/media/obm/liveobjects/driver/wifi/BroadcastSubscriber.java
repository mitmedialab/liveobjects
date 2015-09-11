package edu.mit.media.obm.liveobjects.driver.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

/**
 * Created by arata on 9/11/15.
 */
public abstract class BroadcastSubscriber extends ActivatableEntity {
    Context context;

    protected BroadcastReceiver broadcastReceiver;

    public BroadcastSubscriber(Context context) {
        this.context = context;
    }

    @Override
    protected final void activateEntity() {
        broadcastReceiver = createBroadcastReceiver();
        IntentFilter intentFilter = createIntentFilter();

        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected final void deactivateEntity() {
        context.unregisterReceiver(broadcastReceiver);
    }

    protected abstract BroadcastReceiver createBroadcastReceiver();
    protected abstract IntentFilter createIntentFilter();
}

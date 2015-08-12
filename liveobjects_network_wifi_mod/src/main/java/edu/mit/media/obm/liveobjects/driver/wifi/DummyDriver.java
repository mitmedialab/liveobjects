package edu.mit.media.obm.liveobjects.driver.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkDriver;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkListener;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkUtil;

/**
 * Created by arata on 8/11/15.
 */
public class DummyDriver implements NetworkDriver {
    private final static String LOG_TAG = DummyDriver.class.getSimpleName();

    public DummyDriver(Context context) {
        // do nothing
    }

    @Override
    public void initialize() {
        // do nothing
    }

    @Override
    synchronized public void start() {
        // do nothing
    }

    @Override
    synchronized public void startScan() {
        final Timer timer = new Timer();
        final TimerTask timerTask = new CustomTimerTask(timer);

        timer.schedule(timerTask, 10000);
    }

    private class CustomTimerTask extends TimerTask {
        private Timer mTimer;

        public CustomTimerTask(Timer timer) {
            mTimer = timer;
        }

        @Override
        public void run() {
            mTimer.schedule(this, 10000);
        }
    }

    @Override
    synchronized public void stop() {
        // do nothing
    }

    @Override
    synchronized public void connect(LiveObject liveObject) {
        throw new UnsupportedOperationException("do not connect to a dummy object!");
    }

    @Override
    synchronized public void cancelConnecting() throws IllegalStateException{
        // do nothing
    }

    @Override
    public boolean isConnecting() {
        return false;
    }

    @Override
    public void setNetworkListener(NetworkListener networkListener) {
        mNetworkListener = networkListener;
    }

    @Override
    synchronized public void forgetNetworkConfigurations() throws IllegalStateException {
        // do nothing
    }
}

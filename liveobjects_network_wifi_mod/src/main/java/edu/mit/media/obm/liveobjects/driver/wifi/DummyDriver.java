package edu.mit.media.obm.liveobjects.driver.wifi;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.common.MapLocation;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkDriver;
import edu.mit.media.obm.liveobjects.middleware.net.NetworkListener;

/**
 * Created by arata on 8/11/15.
 */
public class DummyDriver implements NetworkDriver {
    private NetworkListener mNetworkListener = null;

    private final int MAX_LOCATION_X;
    private final int MAX_LOCATION_Y;
    private final int MAX_MAP_ID;

    List<LiveObject> mLiveObjectList = new ArrayList<>();

    Random mRandom = new Random();

    public DummyDriver(Context context) {
        Resources resources = context.getResources();
        int locationXLength = resources.getInteger(R.integer.map_location_coordinate_x_length);
        int locationYLength = resources.getInteger(R.integer.map_location_coordinate_y_length);
        int mapIdLength = resources.getInteger(R.integer.map_location_map_id_length);

        MAX_LOCATION_X = 16 << ((locationXLength - 1) * 4);
        MAX_LOCATION_Y = 16 << ((locationYLength - 1) * 4);
        MAX_MAP_ID = 16 << ((mapIdLength - 1) * 4);

        String ssidPrefix = resources.getString(R.string.ssid_prefix);
        // use only the first char as a delimiter
        // (ssid_delimiter should be 1 byte long string, though)
        char ssidDelimiter = resources.getString(R.string.ssid_delimiter).charAt(0);

        // need to initialize WifiLocationUtil since BluetoothNotifier use this class
        WifiLocationUtil.INSTANCE.setSsidFormat(
                ssidPrefix, ssidDelimiter, locationXLength, locationYLength, mapIdLength);
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
        String liveObjectName = String.format("DummyObject%02d", mLiveObjectList.size());
        int locationX = mRandom.nextInt(MAX_LOCATION_X);
        int locationY = mRandom.nextInt(MAX_LOCATION_Y);
        int mapId = mRandom.nextInt(MAX_MAP_ID);

        MapLocation mapLocation = new MapLocation(locationX, locationY, mapId);
        LiveObject liveObject = new LiveObject(liveObjectName, mapLocation);
        mLiveObjectList.add(liveObject);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                mNetworkListener.onNetworkDevicesAvailable(mLiveObjectList);
            }
        });
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

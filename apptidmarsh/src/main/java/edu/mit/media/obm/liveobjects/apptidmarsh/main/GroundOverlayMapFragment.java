package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.collect.Range;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.ButterKnife;
import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectPropertyProvider;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.Util;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.BitmapEditor;
import edu.mit.media.obm.liveobjects.middleware.common.ContentId;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.common.MapLocation;
import edu.mit.media.obm.liveobjects.middleware.control.ContentController;
import edu.mit.media.obm.liveobjects.middleware.control.DbController;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * Created by arata on 8/11/15.
 */
public class GroundOverlayMapFragment extends SupportMapFragment {
    private static final String LOG_TAG = GroundOverlayMapFragment.class.getSimpleName();

    private final int NUM_GRID_X = 256;
    private final int NUM_GRID_Y = 256;
    private final int NUM_MAP_ID = 16;

    private final LatLng SOUTH_WEST_BOUND = new LatLng(-0.005, -0.005);
    private final LatLng NORTH_EAST_BOUND = new LatLng(0.005, 0.005);

    private GoogleMap mMap;
    private RandomColorGenerator mRandomColorGenerator;

    @Inject DbController mDbController;
    @Inject ContentController mContentController;
    @BindString(R.string.dir_contents) String DIR_CONTENTS;
    @BindDimen(R.dimen.map_marker_icon_size) int MAP_MARKER_ICON_SIZE;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        DependencyInjector.inject(this, getActivity());
        ButterKnife.bind(this, getActivity());

        setUpMap();

        return rootView;
    }

    private void setUpMap() {
        mMap = getMap();

        final LatLng overlayPosition = new LatLng(0, 0);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.main_map);

        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(bitmapDescriptor)
                .position(overlayPosition, 1000f, 1000f);
        mMap.addGroundOverlay(newarkMap);

        CustomCameraChangeListener customCameraChangeListener = new CustomCameraChangeListener(
                mMap, SOUTH_WEST_BOUND, NORTH_EAST_BOUND, 16, 18, 0f, 0f, 0f, 0f);
        mMap.setOnCameraChangeListener(customCameraChangeListener);

        mRandomColorGenerator = new RandomColorGenerator();
    }

    public void setOnMarkerClickListener(GoogleMap.OnMarkerClickListener listener) {
        mMap.setOnMarkerClickListener(listener);
    }

    public void addLiveObjectMarker(LiveObject liveObject) {
        String liveObjectName = liveObject.getLiveObjectName();
        MapLocation mapLocation = liveObject.getMapLocation();
        int gridX = mapLocation.getCoordinateX();
        int gridY = mapLocation.getCoordinateY();
        int mapId = mapLocation.getMapId();

        checkArgumentRange("gridX", gridX, 0, NUM_GRID_X - 1);
        checkArgumentRange("gridY", gridY, 0, NUM_GRID_Y - 1);
        checkArgumentRange("mapId", mapId, 0, NUM_MAP_ID - 1);
        LatLng gridLocationInLagLng = gridToLatLng(gridX, gridY);

        try {
            BitmapDescriptor iconBitmapDescriptor =
                    BitmapDescriptorFactory.fromBitmap(createMarkerIcon(liveObjectName));
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(gridLocationInLagLng)
                    .icon(iconBitmapDescriptor)
                    .title(liveObjectName);
            mMap.addMarker(markerOptions);
        } catch (Exception e) {
            Log.e(LOG_TAG, "failed to add a marker for the live object '" + liveObjectName + "'");
        }
    }

    private Bitmap createMarkerIcon(String liveObjectName) throws IOException, RemoteException {
        Bitmap iconBitmap;

        if (!mDbController.isLiveObjectEmpty(liveObjectName)) {
            iconBitmap = getLiveObjectIcon(liveObjectName);
            iconBitmap = iconBitmap.createScaledBitmap(
                    iconBitmap, MAP_MARKER_ICON_SIZE, MAP_MARKER_ICON_SIZE, true);

            BitmapEditor bitmapEditor = new BitmapEditor(getActivity());
            bitmapEditor.blurBitmap(iconBitmap, 2);
        }
        else {
            int color = mRandomColorGenerator.generateColor(liveObjectName);
            iconBitmap = Bitmap.createBitmap(
                    MAP_MARKER_ICON_SIZE, MAP_MARKER_ICON_SIZE, Bitmap.Config.ARGB_8888);
            iconBitmap.eraseColor(color);
        }

        iconBitmap = roundBitmap(iconBitmap, MAP_MARKER_ICON_SIZE);

        return iconBitmap;
    }

    private Bitmap getLiveObjectIcon(String liveObjectName) throws IOException, RemoteException {
        Map<String, Object> liveObjectProperties = mDbController.getProperties(liveObjectName);
        MLProjectPropertyProvider provider = new MLProjectPropertyProvider(liveObjectProperties);
        String iconFileName = provider.getIconFileName();
        ContentId iconContentId = new ContentId(liveObjectName, DIR_CONTENTS, iconFileName);
        InputStream imageInputStream = mContentController.getInputStreamContent(iconContentId);

        return Util.getBitmap(imageInputStream);
    }

    private static Bitmap roundBitmap(Bitmap bitmap, int radius) {
        Bitmap scaledBitmap = (bitmap.getWidth() != radius || bitmap.getHeight() != radius ?
                Bitmap.createScaledBitmap(bitmap, radius, radius, false) : bitmap);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
        final float circleX = scaledBitmap.getWidth() / 2 + 0.7f;
        final float circleY = scaledBitmap.getHeight() / 2 - 0.7f;
        final float circleRadius = scaledBitmap.getWidth() / 2 * 0.86f;

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        Bitmap resultBitmap = Bitmap.createBitmap(
                scaledBitmap.getWidth(), scaledBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        Bitmap shadowBitmap = resultBitmap.copy(resultBitmap.getConfig(), true);
        Canvas shadowCanvas = new Canvas(shadowBitmap);

        Canvas scaledCanvas = new Canvas(scaledBitmap);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        paint.setColor(0x10000000);
        scaledCanvas.drawRect(rect, paint);

        paint.setColor(Color.parseColor("#BAB399"));
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        canvas.drawCircle(circleX, circleY, circleRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledBitmap, rect, rect, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        paint.setShadowLayer(6.0f, 0.0f, 4.0f, 0xff000000);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        shadowCanvas.drawCircle(circleX, circleY, circleRadius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        shadowCanvas.drawBitmap(resultBitmap, rect, rect, paint);

        return shadowBitmap;
    }

    private static class RandomColorGenerator {
        private final static int HUE_OFFSET = 150;
        private int mCurrentHue;
        private Map<String, Integer> colorMap;

        public RandomColorGenerator() {
            mCurrentHue = 0;
            colorMap = new HashMap<>();
        }

        public int generateColor(String id) {
            if (colorMap.containsKey(id)) {
                return colorMap.get(id);
            }

            float[] hsv = new float[3];
            hsv[0] = (float) mCurrentHue;
            hsv[1] = 1.0f;
            hsv[2] = 0.75f;

            mCurrentHue = (mCurrentHue + HUE_OFFSET) % 360;
            int color = Color.HSVToColor(hsv);

            colorMap.put(id, color);

            return color;
        }
    }

    private void checkArgumentRange(String argName, int argValue, int minVaue, int maxValue) {
        if (!Range.closed(minVaue, maxValue).contains(argValue)) {
            String errorMessage = String.format("arg %s (%d) is out of the range '%d <= %s <= %d'",
                    argName, argValue, minVaue, argName, maxValue);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private LatLng gridToLatLng(int gridX, int gridY) {
        double latitudeScale = NORTH_EAST_BOUND.latitude - SOUTH_WEST_BOUND.latitude;
        double longitudeScale = NORTH_EAST_BOUND.longitude - SOUTH_WEST_BOUND.longitude;
        double latitudeStep = latitudeScale / (NUM_GRID_X - 1);
        double longitudeStep = longitudeScale / (NUM_GRID_Y - 1);
        double latitude = gridX * latitudeStep + SOUTH_WEST_BOUND.latitude;
        double longitude = gridY * longitudeStep + SOUTH_WEST_BOUND.longitude;

        return new LatLng(latitude, longitude);
    }

    private static class CustomCameraChangeListener implements GoogleMap.OnCameraChangeListener {
        private GoogleMap mMap;

        private LatLng mSouthWestBound;
        private LatLng mNorthEastBound;
        private float mMinZoom;
        private float mMaxZoom;
        private float mMinTilt;
        private float mMaxTilt;
        private float mMinBearing;
        private float mMaxBearing;


        int ANIMATION_DURATION_MS = 300;

        public CustomCameraChangeListener(GoogleMap map,
                                          LatLng southWestBound, LatLng northEastBound,
                                          float minZoom, float maxZoom,
                                          float minTilt, float maxTilt,
                                          float minBearing, float maxBearing) {
            mMap = map;

            mSouthWestBound = southWestBound;
            mNorthEastBound = northEastBound;
            mMinZoom = minZoom;
            mMaxZoom = maxZoom;
            mMinTilt = minTilt;
            mMaxTilt = maxTilt;
            mMinBearing = minBearing;
            mMaxBearing = maxBearing;
        }

        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            Range<Float> zoomRange = Range.closed(mMinZoom, mMaxZoom);
            Range<Double> latitudeRange = Range.closed(mSouthWestBound.latitude, mNorthEastBound.latitude);
            Range<Double> longitudeRange = Range.closed(mSouthWestBound.longitude, mNorthEastBound.longitude);
            Range<Float> tiltRange = Range.closed(mMinTilt, mMaxTilt);
            Range<Float> bearingRange = Range.closed(mMinBearing, mMaxBearing);

            if (!zoomRange.contains(cameraPosition.zoom) ||
                    !latitudeRange.contains(cameraPosition.target.latitude) ||
                    !longitudeRange.contains(cameraPosition.target.longitude) ||
                    !tiltRange.contains(cameraPosition.tilt) ||
                    !bearingRange.contains(cameraPosition.bearing)) {

                float zoom = saturate(cameraPosition.zoom, zoomRange);
                double latitude = saturate(cameraPosition.target.latitude, latitudeRange);
                double longitude = saturate(cameraPosition.target.longitude, longitudeRange);
                float tilt = saturate(cameraPosition.tilt, tiltRange);
                float bearing = saturate(cameraPosition.bearing, bearingRange);

                LatLng latLng = new LatLng(latitude, longitude);

                CameraPosition newCameraPosition = CameraPosition.builder()
                        .target(latLng).zoom(zoom).tilt(tilt).bearing(bearing)
                        .build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(newCameraPosition);

                mMap.animateCamera(cameraUpdate, ANIMATION_DURATION_MS, null);
            }

            Log.v(LOG_TAG, cameraPosition.toString());
        }

        <T extends Comparable<T>> T saturate(T value, Range<T> range) {
            T saturatedValue;

            if (value.compareTo(range.lowerEndpoint()) < 0) {
                saturatedValue = range.lowerEndpoint();
            } else if (value.compareTo(range.upperEndpoint()) > 0) {
                saturatedValue = range.upperEndpoint();
            } else {
                saturatedValue = value;
            }

            return saturatedValue;
        }

    }
}

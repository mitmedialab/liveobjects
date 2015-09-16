package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.collect.Range;
import com.noveogroup.android.log.Log;
import com.squareup.otto.Bus;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.ButterKnife;
import edu.mit.media.obm.liveobjects.apptidmarsh.data.MLProjectPropertyProvider;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.CameraChangeEvent;
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
    private final int NUM_GRID_X = 256;
    private final int NUM_GRID_Y = 256;
    private final int NUM_MAP_ID = 16;

    private final float MIN_ZOOM = 16f;
    private final float MAX_ZOOM = 18f;
    private final float MIN_TILT = 0f;
    private final float MAX_TILT = 0f;
    private final float MIN_BEARING = 0f;
    private final float MAX_BEARING = 0f;

    private final LatLng SOUTH_WEST_BOUND = new LatLng(-0.005, -0.005);
    private final LatLng NORTH_EAST_BOUND = new LatLng(0.005, 0.005);

    private GoogleMap mMap;
    private RandomColorGenerator mRandomColorGenerator;

    Map<String, Marker> mLiveObjectMarkers = new HashMap<>();

    @Inject DbController mDbController;
    @Inject ContentController mContentController;
    @Inject Bus mBus;

    @BindString(R.string.dir_contents) String DIR_CONTENTS;
    @BindDimen(R.dimen.map_marker_icon_size) int MAP_MARKER_ICON_SIZE;
    @BindDimen(R.dimen.map_marker_font_size) int MAP_MARKER_FONT_SIZE;
    @BindDimen(R.dimen.map_marker_arrow_size) int MAP_MARKER_ARROW_SIZE;
    @BindColor(R.color.theme_transparent_background) int MAP_MARKER_ARROW_COLOR;

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

        LatLng defaultTarget = new LatLng(
                (SOUTH_WEST_BOUND.latitude + NORTH_EAST_BOUND.latitude) / 2,
                (SOUTH_WEST_BOUND.longitude + NORTH_EAST_BOUND.longitude) / 2);
        float defaultZoom = (MIN_ZOOM + MAX_ZOOM) / 2;
        float overlayHeight = (float)(NORTH_EAST_BOUND.latitude - SOUTH_WEST_BOUND.latitude) * 110574;
        float overlayWidth = (float)(NORTH_EAST_BOUND.longitude - SOUTH_WEST_BOUND.longitude) * 111320;

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.main_map);

        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(bitmapDescriptor)
                .position(defaultTarget, overlayWidth, overlayHeight);
        mMap.addGroundOverlay(newarkMap);

        mMap.setOnCameraChangeListener(new CustomCameraChangeListener(
                mMap, SOUTH_WEST_BOUND, NORTH_EAST_BOUND, MIN_ZOOM, MAX_ZOOM, MIN_TILT, MAX_TILT,
                MIN_BEARING, MAX_BEARING));

        mMap.setOnMapClickListener(new CustomOnMapClickListener());

        CameraUpdate updateToDefault =
                CameraUpdateFactory.newLatLngZoom(defaultTarget, defaultZoom);
        mMap.moveCamera(updateToDefault);

        mRandomColorGenerator = new RandomColorGenerator();
    }

    public void setOnMarkerClickListener(GoogleMap.OnMarkerClickListener listener) {
        mMap.setOnMarkerClickListener(listener);
    }

    public void updateLiveObjectMarker(LiveObject liveObject, boolean currentLocation, boolean visited) {
        String liveObjectName = liveObject.getName();

        // assumes that each live object has an identical name
        if (mLiveObjectMarkers.containsKey(liveObjectName)) {
            Marker oldMarker = mLiveObjectMarkers.remove(liveObjectName);
            oldMarker.remove();
        }

        Marker marker = addLiveObjectMarker(liveObject, currentLocation, visited);

        mLiveObjectMarkers.put(liveObject.getName(), marker);

        Log.v(mLiveObjectMarkers.keySet().toString());
    }

    private Marker addLiveObjectMarker(LiveObject liveObject, boolean currentLocation, boolean visited) {
        String liveObjectName = liveObject.getName();
        MapLocation mapLocation = liveObject.getMapLocation();

        checkArgumentRange("X", mapLocation.getX(), 0, NUM_GRID_X - 1);
        checkArgumentRange("Y", mapLocation.getY(), 0, NUM_GRID_Y - 1);
        checkArgumentRange("Id", mapLocation.getId(), 0, NUM_MAP_ID - 1);
        LatLng gridLocationInLagLng = gridToLatLng(mapLocation);

        Marker marker;
        try {
            BitmapDescriptor iconBitmapDescriptor =
                    BitmapDescriptorFactory.fromBitmap(createMarkerIcon(liveObjectName, currentLocation, visited));
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(gridLocationInLagLng)
                    .icon(iconBitmapDescriptor)
                    .anchor(0.5f, 1.0f)
                    .title(liveObjectName);
            marker = mMap.addMarker(markerOptions);
        } catch (Exception e) {
            Log.e("failed to add a marker for the live object '" + liveObjectName + "'");
            Log.e(e.toString());

            throw new RuntimeException();
        }

        return marker;
    }

    private Bitmap createMarkerIcon(String liveObjectName, boolean currentLocation, boolean visited)
            throws IOException, RemoteException {
        Bitmap iconBitmap = null;

        if (!mDbController.isLiveObjectEmpty(liveObjectName)) {
            iconBitmap = getLiveObjectIcon(liveObjectName);

            if (iconBitmap != null) {
                iconBitmap = Bitmap.createScaledBitmap(
                        iconBitmap, MAP_MARKER_ICON_SIZE, MAP_MARKER_ICON_SIZE, true);

                BitmapEditor bitmapEditor = new BitmapEditor(getActivity());
                bitmapEditor.blurBitmap(iconBitmap, 2);
            }
        }

        if (iconBitmap == null) {
            int color = mRandomColorGenerator.generateColor(liveObjectName);
            iconBitmap = Bitmap.createBitmap(
                    MAP_MARKER_ICON_SIZE, MAP_MARKER_ICON_SIZE, Bitmap.Config.ARGB_8888);
            iconBitmap.eraseColor(color);
        }

        iconBitmap = roundBitmap(iconBitmap, MAP_MARKER_ICON_SIZE);
        printTitleOnBitmap(iconBitmap, liveObjectName);
        iconBitmap = addArrowToBitmap(iconBitmap);

        if (currentLocation) {
            iconBitmap = addPersonIcon(iconBitmap);
        }

        if (visited) {
            iconBitmap = addCheckIcon(iconBitmap);
        }

        return iconBitmap;
    }

    private Bitmap getLiveObjectIcon(String liveObjectName) throws IOException, RemoteException {
        Map<String, Object> liveObjectProperties = mDbController.getProperties(liveObjectName);
        MLProjectPropertyProvider provider = new MLProjectPropertyProvider(liveObjectProperties);

        // ToDo: use the icon of the first content for the moment
        String iconFileName = provider.getIconFileName(0);

        Bitmap bitmap;
        ContentId iconContentId = new ContentId(liveObjectName, DIR_CONTENTS, iconFileName);
        if (mContentController.isContentLocallyAvailable(iconContentId)) {
            InputStream imageInputStream = mContentController.getInputStreamContent(iconContentId);
            bitmap = Util.getBitmap(imageInputStream);
        } else {
            // json file exists, but icon has not been downloaded.
            bitmap = null;
        }

        return bitmap;
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

    private void printTitleOnBitmap(Bitmap bitmap, String title) {
        // create a text view that automatically over-wraps at the end of the line
        TextView textView = new TextView(getActivity());

        textView.setDrawingCacheEnabled(true);
        textView.layout(0, 0, bitmap.getWidth() * 9 / 10, bitmap.getHeight() * 9 / 10);

        // define text parameters
        textView.setText(title);
        textView.setBackgroundColor(Color.TRANSPARENT);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, MAP_MARKER_FONT_SIZE);
        textView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        textView.setGravity(Gravity.CENTER);
        textView.setTextAlignment(TextView.TEXT_ALIGNMENT_GRAVITY);
        textView.setShadowLayer(2f, 4f, 4f, Color.BLACK);

        textView.buildDrawingCache(true);
        Bitmap textViewBitmap = textView.getDrawingCache();

        // draw the text view onto the original bitmap
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bitmap);
        int textBottom = textView.getLineBounds(textView.getLineCount() - 1, null);
        canvas.drawBitmap(textViewBitmap,
                (bitmap.getWidth() - textView.getWidth()) / 2,
                (bitmap.getHeight() - textBottom) / 2,
                paint);
    }

    private Bitmap addArrowToBitmap(Bitmap bitmap) {
        int size_including_arrow = MAP_MARKER_ICON_SIZE + MAP_MARKER_ARROW_SIZE;

        Bitmap bitmapWithArrow = Bitmap.createBitmap(
                MAP_MARKER_ICON_SIZE, size_including_arrow, Bitmap.Config.ARGB_8888);


        Path arrowPath = new Path();
        arrowPath.moveTo(0.5f * MAP_MARKER_ICON_SIZE, size_including_arrow);
        arrowPath.lineTo(0.55f * MAP_MARKER_ICON_SIZE, 0.8f * MAP_MARKER_ICON_SIZE);
        arrowPath.lineTo(0.45f * MAP_MARKER_ICON_SIZE, 0.8f * MAP_MARKER_ICON_SIZE);
        arrowPath.close();

        Paint iconPaint = new Paint();
        Paint arrowPaint = new Paint();
        arrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        arrowPaint.setColor(MAP_MARKER_ARROW_COLOR);
        arrowPaint.setAntiAlias(true);
        arrowPaint.setShadowLayer(2f, 4f, 4f, Color.BLACK);

        Canvas canvas = new Canvas(bitmapWithArrow);
        canvas.drawPath(arrowPath, arrowPaint);
        canvas.drawBitmap(bitmap, 0, 0, iconPaint);

        return bitmapWithArrow;
    }

    private Bitmap addPersonIcon(Bitmap bitmap) {
        Bitmap personBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.person);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(personBitmap, 0, bitmap.getHeight() - personBitmap.getHeight(), null);

        return bitmap;
    }

    private Bitmap addCheckIcon(Bitmap bitmap) {
        Bitmap personBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.check);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(personBitmap, bitmap.getWidth() - personBitmap.getWidth(),
                bitmap.getHeight() - personBitmap.getHeight(), null);

        return bitmap;
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

    private LatLng gridToLatLng(MapLocation mapLocation) {
        double latitudeScale = NORTH_EAST_BOUND.latitude - SOUTH_WEST_BOUND.latitude;
        double longitudeScale = NORTH_EAST_BOUND.longitude - SOUTH_WEST_BOUND.longitude;
        double latitudeStep = latitudeScale / (NUM_GRID_Y - 1);
        double longitudeStep = longitudeScale / (NUM_GRID_X - 1);
        double latitude = mapLocation.getY() * latitudeStep + SOUTH_WEST_BOUND.latitude;
        double longitude = mapLocation.getX() * longitudeStep + SOUTH_WEST_BOUND.longitude;

        return new LatLng(latitude, longitude);
    }

    private MapLocation latLngToGrid(LatLng latLng) {
        double latitudeScale = NORTH_EAST_BOUND.latitude - SOUTH_WEST_BOUND.latitude;
        double longitudeScale = NORTH_EAST_BOUND.longitude - SOUTH_WEST_BOUND.longitude;
        double latitudeStep = latitudeScale / (NUM_GRID_Y - 1);
        double longitudeStep = longitudeScale / (NUM_GRID_X - 1);

        double latitudeDistance = latLng.latitude - SOUTH_WEST_BOUND.latitude;
        double longitudeDistance = latLng.longitude - SOUTH_WEST_BOUND.longitude;

        int gridY = (int) Math.round(latitudeDistance / latitudeStep);
        int gridX = (int) Math.round(longitudeDistance / longitudeStep);

        return new MapLocation(gridX, gridY, 0);
    }

    private class CustomCameraChangeListener implements GoogleMap.OnCameraChangeListener {
        private GoogleMap mMap;

        private LatLng mSouthWestBound;
        private LatLng mNorthEastBound;
        private float mMinZoom;
        private float mMaxZoom;
        private float mMinTilt;
        private float mMaxTilt;
        private float mMinBearing;
        private float mMaxBearing;

        private CameraPosition mLastCameraPosition = null;

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
            LatLngBounds visibleLatLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            double visibleLatitudeHalfLength =
                    (visibleLatLngBounds.northeast.latitude - visibleLatLngBounds.southwest.latitude) / 2.0;
            double visibleLongitudeHalfLength =
                    (visibleLatLngBounds.northeast.longitude - visibleLatLngBounds.southwest.longitude) / 2.0;
            double boundLatitudeHalfLength = (mNorthEastBound.latitude - mSouthWestBound.latitude) / 2.0;
            double boundLongitudeHalfLength = (mNorthEastBound.longitude - mSouthWestBound.longitude) / 2.0;
            double boundLatitudeCompensation = Math.min(visibleLatitudeHalfLength, boundLatitudeHalfLength);
            double boundLongitudeCompensation = Math.min(visibleLongitudeHalfLength, boundLongitudeHalfLength);

            float zoomCompensation = mMinZoom;
            float latitudeRatio = (float) boundLatitudeHalfLength / (float) visibleLatitudeHalfLength;
            float longitudeRatio = (float) boundLongitudeHalfLength / (float) visibleLongitudeHalfLength;
            if (latitudeRatio < 1.0 && latitudeRatio > longitudeRatio) {
                Log.v("lat" + Float.toString(latitudeRatio));
                zoomCompensation = (float) (cameraPosition.zoom + Math.log(1.0 / latitudeRatio) / Math.log(2.0));
            } else if (longitudeRatio < 1.0 && longitudeRatio > latitudeRatio) {
                Log.v("lng" + Float.toString(longitudeRatio));
                zoomCompensation = (float) (cameraPosition.zoom + Math.log(1.0 / longitudeRatio) / Math.log(2.0));
            }

            Range<Float> zoomRange = Range.closed(zoomCompensation, mMaxZoom);
            Range<Double> latitudeRange = Range.closed(
                    mSouthWestBound.latitude + boundLatitudeCompensation,
                    mNorthEastBound.latitude - boundLatitudeCompensation);
            Range<Double> longitudeRange = Range.closed(
                    mSouthWestBound.longitude + boundLongitudeCompensation,
                    mNorthEastBound.longitude - boundLongitudeCompensation);
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

                // update camera parameters only when it has changed since last time
                if (mLastCameraPosition == null ||
                        cameraPosition.zoom != mLastCameraPosition.zoom ||
                        cameraPosition.target.latitude != mLastCameraPosition.target.latitude ||
                        cameraPosition.target.longitude != mLastCameraPosition.target.longitude ||
                        cameraPosition.tilt != mLastCameraPosition.tilt ||
                        cameraPosition.bearing != mLastCameraPosition.bearing) {
                    LatLng latLng = new LatLng(latitude, longitude);

                    CameraPosition newCameraPosition = CameraPosition.builder()
                            .target(latLng).zoom(zoom).tilt(tilt).bearing(bearing)
                            .build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(newCameraPosition);

                    mMap.animateCamera(cameraUpdate, ANIMATION_DURATION_MS, null);
                }
            }

            Log.v(cameraPosition.toString());

            mLastCameraPosition = cameraPosition;
            mBus.post(new CameraChangeEvent());
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

    private class CustomOnMapClickListener implements GoogleMap.OnMapClickListener {
        @Override
        public void onMapClick(LatLng latLng) {
            MapLocation mapLocation = latLngToGrid(latLng);
            Log.v("clicked location = %s (in grid coordinates)", mapLocation);
        }
    }
}

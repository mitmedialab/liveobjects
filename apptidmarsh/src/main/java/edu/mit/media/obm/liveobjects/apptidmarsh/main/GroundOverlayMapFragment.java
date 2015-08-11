package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import android.os.Bundle;
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

import butterknife.ButterKnife;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.liveobjects.middleware.common.LiveObject;
import edu.mit.media.obm.liveobjects.middleware.common.MapLocation;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
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
        MarkerOptions markerOptions = new MarkerOptions()
                .position(gridLocationInLagLng)
                .title(liveObjectName);
        mMap.addMarker(markerOptions);
    }

    private void checkArgumentRange(String argName, int argValue, int minVaue, int maxValue) {
        if (argValue < minVaue || argValue > maxValue) {
            String errorMessage = String.format("arg %s (%d) is out of the range '%d <= %s <= %d'",
                    argName, argValue, minVaue, argName, maxValue);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private LatLng gridToLatLng(int grid_x, int grid_y) {
        if (grid_x < 0 || grid_x > NUM_GRID_X - 1) {
            throw new IllegalArgumentException(
                    String.format("grid_x (%d) is out of range", grid_x));
        }

        if (grid_y < 0 || grid_y > NUM_GRID_Y - 1) {
            throw new IllegalArgumentException(
                    String.format("grid_y (%d) is out of range", grid_y));
        }

        double latitudeScale = NORTH_EAST_BOUND.latitude - SOUTH_WEST_BOUND.latitude;
        double longitudeScale = NORTH_EAST_BOUND.longitude - SOUTH_WEST_BOUND.longitude;
        double latitudeStep = latitudeScale / (NUM_GRID_X - 1);
        double longitudeStep = longitudeScale / (NUM_GRID_Y - 1);
        double latitude = grid_x * latitudeStep + SOUTH_WEST_BOUND.latitude;
        double longitude = grid_x * longitudeStep + SOUTH_WEST_BOUND.longitude;

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

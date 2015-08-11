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
        double latitudeScale = NORTH_EAST_BOUND.latitude - SOUTH_WEST_BOUND.latitude;
        double longitudeScale = NORTH_EAST_BOUND.longitude - SOUTH_WEST_BOUND.longitude;
        double latitudeStep = latitudeScale / (NUM_GRID_X - 1);
        double longitudeStep = longitudeScale / (NUM_GRID_Y - 1);

        return new LatLng(0.0f, 0.0f);
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
            SaturationResult<Float> zoomSaturation =
                    saturate(cameraPosition.zoom, mMinZoom, mMaxZoom);
            SaturationResult<Double> latitudeSaturation =
                    saturate(cameraPosition.target.latitude,
                            mSouthWestBound.latitude, mNorthEastBound.latitude);
            SaturationResult<Double> longitudeSaturation =
                    saturate(cameraPosition.target.longitude,
                            mSouthWestBound.longitude, mNorthEastBound.longitude);
            SaturationResult<Float> tiltSaturation =
                    saturate(cameraPosition.tilt, mMinTilt, mMaxTilt);
            SaturationResult<Float> bearingSaturation =
                    saturate(cameraPosition.bearing, mMinBearing, mMaxBearing);

            if (zoomSaturation.mIsSaturated || latitudeSaturation.mIsSaturated ||
                    longitudeSaturation.mIsSaturated || tiltSaturation.mIsSaturated ||
                    bearingSaturation.mIsSaturated) {
                float zoom = zoomSaturation.mSaturatedValue;
                LatLng latLng = new LatLng(
                        latitudeSaturation.mSaturatedValue, longitudeSaturation.mSaturatedValue);
                float tilt = tiltSaturation.mSaturatedValue;
                float bearing = bearingSaturation.mSaturatedValue;

                CameraPosition newCameraPosition = CameraPosition.builder()
                        .target(latLng).zoom(zoom).tilt(tilt).bearing(bearing)
                        .build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(newCameraPosition);

                mMap.animateCamera(cameraUpdate, ANIMATION_DURATION_MS, null);
            }

            Log.v(LOG_TAG, cameraPosition.toString());
        }

        private static <T extends Comparable<T>>
        SaturationResult<T> saturate(T value, T minValue, T maxValue) {
            SaturationResult saturationResult;

            if (value.compareTo(minValue) < 0) {
                saturationResult = new SaturationResult(minValue, true);
            } else if (value.compareTo(maxValue) > 0) {
                saturationResult = new SaturationResult(maxValue, true);
            } else {
                saturationResult = new SaturationResult(value, false);
            }

            return saturationResult;
        }

        private static class SaturationResult<T extends Comparable<T>> {
            public T mSaturatedValue;
            public boolean mIsSaturated;

            public SaturationResult(T saturatedValue, boolean isSaturated) {
                mSaturatedValue = saturatedValue;
                mIsSaturated = isSaturated;
            }
        }
    }
}

package edu.mit.media.obm.liveobjects.apptidmarsh.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;

import butterknife.ButterKnife;
import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;
import edu.mit.media.obm.shair.liveobjects.R;

/**
 * Created by artimo14 on 8/9/15.
 */
public class GroundOverlayMapFragment extends SupportMapFragment {
    private static final String LOG_TAG = GroundOverlayMapFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setUpMap();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setUpMap() {
        GoogleMap map = getMap();

        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker");
        map.addMarker(markerOptions);

        final LatLng overlayPosition = new LatLng(0, 0);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.main_map);

        map.setMapType(GoogleMap.MAP_TYPE_NONE);
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(bitmapDescriptor)
                .position(overlayPosition, 1000f, 1000f);
        map.addGroundOverlay(newarkMap);

        CustomCameraChangeListener customCameraChangeListener =
                new CustomCameraChangeListener(map, 16, 18, new LatLng(-0.005, -0.005), new LatLng(0.005, 0.005));
        map.setOnCameraChangeListener(customCameraChangeListener);
    }

    private static class CustomCameraChangeListener implements GoogleMap.OnCameraChangeListener {
        private GoogleMap mMap;
        private float mMaxZoom;
        private float mMinZoom;

        private LatLng mSouthWestBound;
        private LatLng mNorthEastBound;

        public CustomCameraChangeListener(GoogleMap map, float minZoom, float maxZoom, LatLng southWestBound, LatLng northEastBound) {
            mMap = map;

            mMaxZoom = maxZoom;
            mMinZoom = minZoom;
            mSouthWestBound = southWestBound;
            mNorthEastBound = northEastBound;
        }

        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            if (cameraPosition.zoom > mMaxZoom) {
                mMap.moveCamera(CameraUpdateFactory.zoomTo(mMaxZoom));
            }

            if (cameraPosition.zoom < mMinZoom) {
                mMap.moveCamera(CameraUpdateFactory.zoomTo(mMinZoom));
            }

            if (cameraPosition.target.latitude < mSouthWestBound.latitude) {
                LatLng latLng = new LatLng(mSouthWestBound.latitude, cameraPosition.target.longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }

            if (cameraPosition.target.longitude < mSouthWestBound.longitude) {
                LatLng latLng = new LatLng(cameraPosition.target.latitude, mSouthWestBound.longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }

            if (cameraPosition.target.latitude > mNorthEastBound.latitude) {
                LatLng latLng = new LatLng(mNorthEastBound.latitude, cameraPosition.target.longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }

            if (cameraPosition.target.longitude > mNorthEastBound.longitude) {
                LatLng latLng = new LatLng(cameraPosition.target.latitude, mNorthEastBound.longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }

            Log.v(LOG_TAG, cameraPosition.toString());
        }
    }
}

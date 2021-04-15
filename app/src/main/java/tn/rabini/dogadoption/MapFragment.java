package tn.rabini.dogadoption;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends DialogFragment {

    private final boolean edit;
    private GoogleMap map;
    private double lat, lng;
    private SupportMapFragment mapFragment;
    private MarkerOptions markerOptions;
    private LocationManager locationManager;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                mapFragment.getMapAsync(googleMap -> {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                    locationManager.removeUpdates(locationListener);
                    map = googleMap;
                    LatLng myLocation = new LatLng(lat, lng);
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                    markerOptions = new MarkerOptions().position(myLocation).draggable(true);
                    map.addMarker(markerOptions);
                    dragMarkerListener();
                });
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    public MapFragment(boolean edit, double lat, double lng) {
        this.edit = edit;
        this.lat = lat == 0 ? 35.97149 : lat;
        this.lng = lng == 0 ? -96.46391 : lng;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        Button okButton = v.findViewById(R.id.okButton);
        Button cancelButton = v.findViewById(R.id.cancelButton);
        okButton.setOnClickListener(view -> {
            this.dismiss();
            setValues();
            Log.v("looooooooooooooooooooog", getLat() + ", " + getLng());
        });
        cancelButton.setOnClickListener(view -> {
            this.dismiss();
            setValues();
        });
        mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (edit) {
            //            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            assert mapFragment != null;
            mapFragment.getMapAsync(googleMap -> {
                map = googleMap;
                LatLng Tunisia = new LatLng(lat, lng);
                markerOptions = new MarkerOptions().position(Tunisia).draggable(true);
                map.addMarker(markerOptions);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(Tunisia, 15));
                dragMarkerListener();
            });
        } else {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                mapFragment.getMapAsync(googleMap -> {
                    map = googleMap;
                    LatLng Tunisia = new LatLng(lat, lng);
                    markerOptions = new MarkerOptions().position(Tunisia).draggable(true);
                    map.addMarker(markerOptions);
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(Tunisia, 15));
                    dragMarkerListener();
                });
            }
        }


        return v;
    }

    private void dragMarkerListener() {
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng latLng = marker.getPosition();
                lat = latLng.latitude;
                lng = latLng.longitude;
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                markerOptions.position(latLng);
                Log.v("looooooooooooooooooooog", getLat() + ", " + getLng());
            }
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void setValues() {
        Bundle result = new Bundle();
        result.putString("latValue", String.valueOf(lat));
        result.putString("lngValue", String.valueOf(lng));
        getParentFragmentManager().setFragmentResult("MAPPED", result);
    }
}
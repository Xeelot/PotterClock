package com.apps.xeelot.potterclock;

import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        AwsManager.MarkerLocationCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    private final String LOG_MAP = "MAP";
    private final int DEF_RADIUS = 1;

    // Create local variables
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private AwsManager awsManager;
    private Resources res;
    private HashMap<LatLng,Marker> tableMarkers;
    private Marker newMarker = null;
    private Circle newCircle = null;
    private Boolean infoOpen = false;
    private Marker infoMarker = null;
    private Circle infoCircle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Connect to the Google API Client and set this class as the listener for responses
        Log.d(LOG_MAP, "Instantiating Google API client...");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Gather the resources and static instance of AwsManager
        awsManager = AwsManager.getAwsManager(getApplicationContext());
        awsManager.registerMarkerLocationCallback(this);
        res = getResources();

        // Instantiate our map containers
        tableMarkers = new HashMap<LatLng, Marker>();
    }


    // These functions are used to connect and disconnect to the Location Services
    // TODO: think about how to have these running in a background task
    protected void onStart() {
        Log.d(LOG_MAP, "Connecting to Google API client...");
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        Log.d(LOG_MAP, "Disconnecting from Google API client...");
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    // These functions handle the google api client responses and update to current location if successful
    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOG_MAP, "Google API Client connection was suspended!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_MAP, "Google API Client connection failed!");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location currentLocation = null;
        Log.d(LOG_MAP, "Google API Client connection successful!");
        try {
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch(SecurityException e) {
            e.printStackTrace();
        }
        if (currentLocation != null) {
            LatLng latlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 5));
        }
    }


    // Callback after scanning AWS for existing markers
    @Override
    public void markerLocationCallback(final ArrayList<MarkerLocation> ml) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (MarkerLocation mloc : ml) {
                        LatLng latLng = new LatLng(mloc.getLatitude(),mloc.getLongitude());
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(mloc.getName())
                                .snippet(res.getStringArray(R.array.index_names)[mloc.getIndex()])
                                .icon(BitmapDescriptorFactory.defaultMarker(359.0F)));
                        //TODO: fix marker colors
                        tableMarkers.put(latLng,marker);
                    }
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    // Once the map is created and ready to be manipulated
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_MAP, "Map is ready, applying settings...");
        mMap = googleMap;
        try {
            mMap.setMyLocationEnabled(true);
        } catch(SecurityException e) {
            Log.e(LOG_MAP, "GPS settings are disabled on device!");
            e.printStackTrace();
        }
        UiSettings ui = mMap.getUiSettings();
        ui.setTiltGesturesEnabled(false);
        ui.setIndoorLevelPickerEnabled(false);
        ui.setMapToolbarEnabled(false);
        ui.setZoomControlsEnabled(true);
        
        // Set our custom info window and listeners
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getLayoutInflater()));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);

        // Scan AWS table for all map locations
        awsManager.getAllMarkers();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(LOG_MAP, "Map click captured...");
        // Check to see if an info window is open (new or existing)
        if(infoOpen) {
            if(newMarker != null) {
                Log.d(LOG_MAP, "Removed new marker from map");
                // Remove the previous new items from the map
                newMarker.remove();
                newCircle.remove();
                newMarker = null;
                newCircle = null;
            }
            else if(infoMarker != null){
                Log.d(LOG_MAP, "Hiding info window from open marker");
                // Hide the window on the open marker
                infoMarker.hideInfoWindow();
                infoCircle.remove();
                infoMarker = null;
                infoCircle = null;
            }
            infoOpen = false;
        }
        else {
            Log.d(LOG_MAP, "Added new marker to map");
            // Add a new marker and default circle to the map
            infoOpen = true;
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            newMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(res.getString(R.string.new_marker))
                    .snippet(res.getStringArray(R.array.index_names)[0])
                    .icon(BitmapDescriptorFactory.defaultMarker(359.0F)));
            newMarker.showInfoWindow();
            if(newCircle != null) {
                // Remove any existing circles before creating a new one
                newCircle.remove();
            }
            newCircle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(DEF_RADIUS)
                    .strokeColor(Color.GRAY)
                    .fillColor(Color.GRAY & 0x55FFFFFF));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(LOG_MAP, "Marker click captured...");
        if(infoOpen) {
            if (newMarker != null) {
                if (marker.equals(newMarker)) {
                    Log.d(LOG_MAP, "New marker exists, new marker clicked, do nothing");
                    // Clicking on the new marker does nothing
                } else {
                    Log.d(LOG_MAP, "New marker exists, existing marker clicked, removing new marker");
                    // Remove the new marker and open the info window for the existing marker
                    newMarker.remove();
                    newMarker = null;
                    infoMarker = marker;
                    marker.showInfoWindow();
                    if (newCircle != null) {
                        newCircle.remove();
                    }
                    if(infoCircle != null) {
                        infoCircle.remove();
                    }
                    infoCircle = mMap.addCircle(new CircleOptions()
                            .center(marker.getPosition())
                            .radius(DEF_RADIUS)
                            .strokeColor(Color.BLUE)
                            .fillColor(Color.BLUE & 0x55FFFFFF));
                    //TODO: get radius from list and update colors
                }
            }
            else {
                Log.d(LOG_MAP, "Existing marker open, closing existing marker, opening new");
                // Close the existing marker and open the new one
                infoMarker.hideInfoWindow();
                infoMarker = marker;
                marker.showInfoWindow();
                if(infoCircle != null) {
                    infoCircle.remove();
                }
                infoCircle = mMap.addCircle(new CircleOptions()
                        .center(marker.getPosition())
                        .radius(DEF_RADIUS)
                        .strokeColor(Color.BLUE)
                        .fillColor(Color.BLUE & 0x55FFFFFF));
                //TODO: get radius from list and update colors
            }
        }
        else {
            Log.d(LOG_MAP, "No new marker, no existing marker, showing info window");
            // Simply show the info window of an existing marker
            infoOpen = true;
            marker.showInfoWindow();
            infoMarker = marker;
            if(infoCircle != null) {
                infoCircle.remove();
            }
            infoCircle = mMap.addCircle(new CircleOptions()
                    .center(marker.getPosition())
                    .radius(DEF_RADIUS)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.BLUE & 0x55FFFFFF));
            //TODO: get radius from list and update colors
        }
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d(LOG_MAP, "Info window click captured!");

    }
}

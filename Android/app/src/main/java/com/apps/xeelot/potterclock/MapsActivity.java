package com.apps.xeelot.potterclock;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        AwsManager.MarkerScanCallback,
        AwsManager.MarkerDeleteCallback,
        AwsManager.MarkerUpdateCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        MarkerDialog.MarkerDialogCancel,
        MarkerDialog.MarkerDialogDelete,
        MarkerDialog.MarkerDialogUpdate {

    private final String LOG_MAP = "MAP";
    private final int DEF_RADIUS = 10;
    private final int CIRCLE_MASK = 0x44FFFFFF;

    // Create local variables
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private AwsManager awsManager;
    private Resources res;
    private HashMap<LatLng, MarkerLocation> tableMarkers = null;
    private boolean markersAdded = false;
    private Marker newMarker = null; // Keep track of new markers not committed yet
    private Circle newCircle = null;
    private Marker infoMarker = null; // Keep track of existing active marker
    private Circle infoCircle = null;
    private boolean infoOpen = false;

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
        awsManager.registerMarkerDeleteCallback(this);
        awsManager.registerMarkerUpdateCallback(this);
        // If the markers are ready, grab them
        markersAdded = false;
        if(AwsManager.getMarkerMapReady()) {
            tableMarkers = AwsManager.getMarkerMap();
        }
        else {
            awsManager.registerMarkerScanCallback(this);
        }
        res = getResources();
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
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (currentLocation != null) {
            LatLng latlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
        }
    }


    // Callback after scanning AWS for existing markers
    @Override
    public void markerScanCallback() {
        tableMarkers = AwsManager.getMarkerMap();
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addMarkersToMap();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMarkersToMap() {
        if (!markersAdded) {
            markersAdded = true;
            for (HashMap.Entry<LatLng, MarkerLocation> entry : tableMarkers.entrySet()) {
                mMap.addMarker(new MarkerOptions()
                        .position(entry.getKey())
                        .title(entry.getValue().getName())
                        .snippet(entry.getValue().getIndexName(res))
                        .icon(entry.getValue().getIconDescriptor()));
            }
        }
    }


    // Once the map is created and ready to be manipulated
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_MAP, "Map is ready, applying settings...");
        mMap = googleMap;
        // Check for location permission before calling setupTheMap
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        else {
            setupTheMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // Had to request location access, how embarrassing, now setup the map
        setupTheMap();
    }

    public void setupTheMap() {
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

        if(tableMarkers != null) {
            addMarkersToMap();
        }
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
                    .snippet(res.getString(R.string.index_lost))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.grey_marker)));
            newMarker.showInfoWindow();
            if(newCircle != null) {
                // Remove any existing circles before creating a new one
                newCircle.remove();
            }
            newCircle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(DEF_RADIUS)
                    .strokeColor(Color.GRAY)
                    .fillColor(Color.GRAY & CIRCLE_MASK));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(LOG_MAP, "Marker click captured...");
        if(infoOpen) {
            if (newMarker != null) {
                if (marker.equals(newMarker)) {
                    Log.d(LOG_MAP, "New marker exists, new marker clicked, do nothing");
                    // Clicking on the new marker updates the drawing only
                    marker.showInfoWindow();
                } else {
                    Log.d(LOG_MAP, "New marker exists, existing marker clicked, removing new marker");
                    // Remove the new marker and open the info window for the existing marker
                    newMarker.remove();
                    newMarker = null;
                    infoMarker = marker;
                    infoMarker.showInfoWindow();
                    if (newCircle != null) {
                        newCircle.remove();
                    }
                    if(infoCircle != null) {
                        infoCircle.remove();
                    }
                    LatLng latLng = marker.getPosition();
                    infoCircle = mMap.addCircle(new CircleOptions()
                            .center(latLng)
                            .radius(tableMarkers.get(latLng).getRadius())
                            .strokeColor(tableMarkers.get(latLng).getCircleColor())
                            .fillColor(tableMarkers.get(latLng).getCircleColor() & CIRCLE_MASK));
                }
            }
            else {
                if(infoMarker.equals(marker)) {
                    Log.d(LOG_MAP, "Existing marker open, same marker clicked, do nothing");
                    // Clicking on the same existing marker updates the drawing only
                    marker.showInfoWindow();
                }
                else {
                    Log.d(LOG_MAP, "Existing marker open, closing existing marker, opening new");
                    // Close the existing marker and open the new one
                    infoMarker.hideInfoWindow();
                    infoMarker = marker;
                    infoMarker.showInfoWindow();
                    if (infoCircle != null) {
                        infoCircle.remove();
                    }
                    LatLng latLng = marker.getPosition();
                    infoCircle = mMap.addCircle(new CircleOptions()
                            .center(latLng)
                            .radius(tableMarkers.get(latLng).getRadius())
                            .strokeColor(tableMarkers.get(latLng).getCircleColor())
                            .fillColor(tableMarkers.get(latLng).getCircleColor() & CIRCLE_MASK));
                }
            }
        }
        else {
            Log.d(LOG_MAP, "No new marker, no existing marker, showing info window");
            // Simply show the info window of an existing marker
            infoOpen = true;
            infoMarker = marker;
            infoMarker.showInfoWindow();
            if(infoCircle != null) {
                infoCircle.remove();
            }
            LatLng latLng = marker.getPosition();
            infoCircle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(tableMarkers.get(latLng).getRadius())
                    .strokeColor(tableMarkers.get(latLng).getCircleColor())
                    .fillColor(tableMarkers.get(latLng).getCircleColor() & CIRCLE_MASK));
        }
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d(LOG_MAP, "Info window click captured!");
        MarkerDialog md = new MarkerDialog();
        if(newMarker == null) {
            md.setExisting(true);
            md.setMarkerInfo(tableMarkers.get(infoMarker.getPosition()));
        }
        md.registerMarkerDialogCancel(this);
        md.registerMarkerDialogDelete(this);
        md.registerMarkerDialogUpdate(this);
        md.show(getFragmentManager(), "MarkerDialogPopUp");
    }

    @Override
    public void markerDialogCancel() {
        Log.d(LOG_MAP, "Info window cancelled");
    }

    @Override
    public void markerDialogDelete(MarkerLocation ml) {
        Log.d(LOG_MAP, "Info window delete requested...");
        // Only infoMarker can be deleted since newMarkers are not in the DB yet
        ml.setLatitude(infoMarker.getPosition().latitude);
        ml.setLongitude(infoMarker.getPosition().longitude);
        awsManager.deleteMarker(ml);
    }

    @Override
    public void markerDialogUpdate(MarkerLocation ml) {
        Log.d(LOG_MAP, "Info window create/update requested...");
        // Should receive name, radius, and index from dialog. Need to add lat/long
        if(newMarker != null) {
            ml.setLatitude(newMarker.getPosition().latitude);
            ml.setLongitude(newMarker.getPosition().longitude);
        }
        else {
            ml.setLatitude(infoMarker.getPosition().latitude);
            ml.setLongitude(infoMarker.getPosition().longitude);
        }
        awsManager.updateMarker(ml);
    }

    @Override
    public void markerDeleteCallback() {
        Log.d(LOG_MAP, "Marker deleted response from AWS");
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(tableMarkers.containsKey(infoMarker.getPosition())) {
                        tableMarkers.remove(infoMarker.getPosition());
                        AwsManager.removeMapMarker(infoMarker.getPosition());
                    }
                    infoMarker.remove();
                    infoCircle.remove();
                    infoMarker = null;
                    infoCircle = null;
                    infoOpen = false;
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void markerUpdateCallback(final MarkerLocation ml) {
        Log.d(LOG_MAP, "Marker updated response from AWS");
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // If item exists, delete and replace with updated item, otherwise create
                    LatLng latLng = new LatLng(ml.getLatitude(), ml.getLongitude());
                    if(tableMarkers.containsKey(latLng)) {
                        tableMarkers.remove(latLng);
                    }
                    tableMarkers.put(latLng, ml);
                    AwsManager.addMapMarker(latLng, ml);
                    if(newMarker != null) {
                        // Delete the "new marker" and create a new "info marker"
                        infoMarker = newMarker;
                        infoCircle = newCircle;
                        newMarker = null;
                        newCircle = null;
                    }
                    infoMarker.setTitle(ml.getName());
                    infoMarker.setSnippet(ml.getIndexName(res));
                    infoMarker.setIcon(ml.getIconDescriptor());
                    infoMarker.showInfoWindow();
                    infoCircle.setRadius(ml.getRadius());
                    infoCircle.setStrokeColor(ml.getCircleColor());
                    infoCircle.setFillColor(ml.getCircleColor() & CIRCLE_MASK);
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

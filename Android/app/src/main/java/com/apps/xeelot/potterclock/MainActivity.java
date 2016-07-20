package com.apps.xeelot.potterclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        AwsManager.CurrentLocationCallback,
        AwsManager.CurrentUpdateCallback,
        AwsManager.MarkerScanCallback,
        AwsManager.AwsConnectionReady,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Constants
    private final String LOG_MAIN = "MAIN";
    private final String PREFS_MAIN = "PotterClockPrefs";
    private final String PREFS_USER = "SelectedUser";
    private final int INTENT_CODE = 33;
    private final double EARTH_RADIUS = 3960.0;
    private final double PI_OVER_180 = Math.PI / 180.0;
    private final double DEGREE_90 = 90.0;
    private final double MILE2METER = 1609.344;

    // Initialize local variables and objects we want to keep track of
    private TextView debug;
    private Button mapButton;
    private Button userButton;
    private Button updateButton;
    private AwsManager awsManager;
    private GoogleApiClient mGoogleApiClient;
    private HashMap<LatLng,MarkerLocation> markerMap;
    private HashMap<String,CurrentLocation> userMap;
    private String selectedUser;
    private Resources res;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the resources for string-array indexing
        res = getResources();

        // Get our saved state / shared preferences
        sp = getSharedPreferences(PREFS_MAIN, Context.MODE_PRIVATE);
        selectedUser = sp.getString(PREFS_USER, res.getString(R.string.select_user));

        // Find the objects specified in the XML file
        mapButton = (Button)findViewById(R.id.buttonMap);
        userButton = (Button)findViewById(R.id.buttonUser);
        updateButton = (Button)findViewById(R.id.buttonUpdate);
        debug = (TextView)findViewById(R.id.textMain);

        // Set values and listeners on objects
        mapButton.setOnClickListener(this);
        userButton.setOnClickListener(this);
        userButton.setText(selectedUser);
        updateButton.setOnClickListener(this);

        // Grab an instance of the AwsManager
        Log.d(LOG_MAIN, "Calling to get AwsManager...");
        awsManager = AwsManager.getAwsManager(getApplicationContext());
        awsManager.registerAwsConnectionReady(this);
        awsManager.registerCurrentLocationCallback(this);
        awsManager.registerCurrentUpdateCallback(this);
        awsManager.registerMarkerScanCallback(this);

        // Local instance of Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Initialize our empty user hashmap
        userMap = new HashMap<String,CurrentLocation>();
    }

    // onStart comes after onCreate and checks to make sure we have a user selected
    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_MAIN, "Connecting to Google API client...");
        mGoogleApiClient.connect();
        // If no user is selected, call to select user
        if(selectedUser.equals(res.getString(R.string.select_user))) {
            selectUserDialog();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_MAIN, "Disconnecting from Google API client...");
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(LOG_MAIN, "Google API Client connected successfully!");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_MAIN, "Google API Client connection suspended!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(LOG_MAIN, "Google API Client connection failed!");
    }


    // Connection to AWS is ready to receive calls, get current info
    @Override
    public void awsConnectionReady() {
        // Get the user's current locations
        //TODO: make this more dynamic
        awsManager.getCurrentLocation(res.getString(R.string.user_joe));
        awsManager.getCurrentLocation(res.getString(R.string.user_hannah));
        awsManager.getAllMarkers();
    }


    // Response from AWS with a current location
    @Override
    public void currentLocationCallback(final CurrentLocation cl) {
        Log.d(LOG_MAIN, "Current location callback received!");
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(userMap.containsKey(cl.getUserid())) {
                        userMap.remove(cl.getUserid());
                    }
                    userMap.put(cl.getUserid(), cl);
                    debug.setText(cl.getUserid());
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Response from AWS after updating a location
    @Override
    public void currentUpdateCallback(final CurrentLocation cl) {
        Log.d(LOG_MAIN, "Current update callback received!");
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(userMap.containsKey(cl.getUserid())) {
                        userMap.remove(cl.getUserid());
                    }
                    userMap.put(cl.getUserid(), cl);
                    debug.setText(cl.getUserid() + " updated!");
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    // Response from AWS that markers are ready
    @Override
    public void markerScanCallback() {
        Log.d(LOG_MAIN, "Retrieved markers callback received!");
        markerMap = AwsManager.getMarkerMap();
    }


    // Button handler, currently only 2 options
    @Override
    public void onClick(View view) {
        Log.d(LOG_MAIN, "Button click received!");
        if(view.getId() == R.id.buttonMap) {
            // Create a new intent and launch the map activity
            Intent intent = new Intent(this, MapsActivity.class);
            startActivityForResult(intent, INTENT_CODE);
        }
        else if(view.getId() == R.id.buttonUser) {
            // Open the select user dialog
            selectUserDialog();
        }
        else if(view.getId() == R.id.buttonUpdate) {
            // Update the current location of the selected user based on location
            // Get current location, loop through markers, calculate distance, if less than radius, update to that, else lost
            Location currentLocation = null;
            LatLng currentLatLng = null;
            boolean locUpdated = false;
            CurrentLocation cl = userMap.get(selectedUser);
            try {
                currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            if(currentLocation != null) {
                currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                cl.setLatitude(currentLatLng.latitude);
                cl.setLongitude(currentLatLng.longitude);
            }
            if(currentLatLng != null) {
                for (HashMap.Entry<LatLng, MarkerLocation> entry : markerMap.entrySet()) {
                    // Calculate our phi and theta components for calculating Earth distance
                    //double distance = SphericalUtil.computeDistanceBetween(currentLatLng, entry.getKey());
                    double phi1 = (DEGREE_90 - currentLatLng.latitude) * PI_OVER_180;
                    double phi2 = (DEGREE_90 - entry.getValue().getLatitude()) * PI_OVER_180;
                    double theta1 = currentLatLng.longitude * PI_OVER_180;
                    double theta2 = entry.getValue().getLongitude() * PI_OVER_180;
                    // Calculate the earth distance given the components above
                    double distance = EARTH_RADIUS *
                            Math.acos(Math.sin(phi1) * Math.sin(phi2) * Math.cos(theta1 - theta2) + Math.cos(phi1) * Math.cos(phi2));
                    // Convert our distance in miles to meters
                    distance = distance * MILE2METER;
                    if (distance <= entry.getValue().getRadius()) {
                        cl.setIndex(entry.getValue().getIndex());
                        awsManager.updateCurrentLocation(cl);
                        locUpdated = true;
                        break;
                    }
                }
            }
            if(!locUpdated) {
                cl.setIndex(0);
                awsManager.updateCurrentLocation(cl);
            }
        }
    }


    // Open a dialog to select a user
    public void selectUserDialog() {
        // Create an alert dialog with users to pick from
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_user);
        builder.setSingleChoiceItems(R.array.user_array, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectedUser = res.getStringArray(R.array.user_array)[i];
                Log.d(LOG_MAIN, "User " + selectedUser + " selected.");
            }
        });
        builder.setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(LOG_MAIN, "User " + selectedUser + " accepted!");
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(PREFS_USER, selectedUser);
                editor.apply();
                userButton.setText(selectedUser);
                AwsManager.setCurrentUser(selectedUser);
            }
        });
        builder.create();
        builder.show();
    }


    // After map operations are complete, update the local map in this activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == INTENT_CODE) {
            Log.d(LOG_MAIN, "MapActivity closed, retrieving updated map from AwsManager");
            markerMap = AwsManager.getMarkerMap();
        }
    }
}

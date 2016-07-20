package com.apps.xeelot.potterclock;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;


public class AwsManager {
    private static final String LOG_AWS = "AWS";

    // Initialize the components needed to access Amazon Web Services
    private static CognitoCachingCredentialsProvider credentialsProvider;
    private static AmazonDynamoDBClient ddbClient;
    private static DynamoDBMapper mapper;
    private static Context appContext;

    // Create a singleton of the class when getAwsManager is called
    private static AwsManager awsManager;

    // Create a singleton of the map that will hold all the markers from the DB
    private static HashMap<LatLng,MarkerLocation> markerMap;
    private static boolean markersReady;
    private static String currentUser;


    // Interface for receiving when the database is ready
    interface AwsConnectionReady {
        void awsConnectionReady();
    }
    static AwsConnectionReady awsConnectionReady;
    void registerAwsConnectionReady(AwsConnectionReady callback) {
        Log.d(LOG_AWS, "AWS connection ready callback registered.");
        awsConnectionReady = callback;
    }


    // Interface for responding to CurrentLocation polls
    interface CurrentLocationCallback {
        void currentLocationCallback(CurrentLocation cl);
    }
    CurrentLocationCallback currentLocationCallback;
    void registerCurrentLocationCallback(CurrentLocationCallback callback) {
        Log.d(LOG_AWS, "Current location callback registered.");
        currentLocationCallback = callback;
    }


    // Interface for responding to CurrentLocation polls
    interface CurrentUpdateCallback {
        void currentUpdateCallback(CurrentLocation cl);
    }
    CurrentUpdateCallback currentUpdateCallback;
    void registerCurrentUpdateCallback(CurrentUpdateCallback callback) {
        Log.d(LOG_AWS, "Current update callback registered.");
        currentUpdateCallback = callback;
    }


    // Interface for responding to MarkerLocation scans
    interface MarkerScanCallback {
        void markerScanCallback();
    }
    MarkerScanCallback markerScanCallback;
    void registerMarkerScanCallback(MarkerScanCallback callback) {
        Log.d(LOG_AWS, "Marker scan callback registered.");
        markerScanCallback = callback;
    }


    // Interface for responding to MarkerLocation scans
    interface MarkerUpdateCallback {
        void markerUpdateCallback(MarkerLocation ml);
    }
    MarkerUpdateCallback markerUpdateCallback;
    void registerMarkerUpdateCallback(MarkerUpdateCallback callback) {
        Log.d(LOG_AWS, "Marker update callback registered.");
        markerUpdateCallback = callback;
    }


    // Interface for responding to MarkerLocation scans
    interface MarkerDeleteCallback {
        void markerDeleteCallback();
    }
    MarkerDeleteCallback markerDeleteCallback;
    void registerMarkerDeleteCallback(MarkerDeleteCallback callback) {
        Log.d(LOG_AWS, "Marker delete callback registered.");
        markerDeleteCallback = callback;
    }


    // Interface to grab the singleton class for AwsManager
    public static AwsManager getAwsManager(final Context context) {
        // Create singleton if it hasn't been made yet
        Log.d(LOG_AWS, "AwsManager get singleton requested...");
        if(awsManager == null) {
            Log.d(LOG_AWS, "AwsManager being instantiated...");
            awsManager = new AwsManager();
            markerMap = new HashMap<LatLng,MarkerLocation>();
            markersReady = false;
            appContext = context;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Initialize the Amazon Cognito credentials provider
                    credentialsProvider = new CognitoCachingCredentialsProvider(
                            context,
                            "us-east-1:abd9a12b-b02c-475a-b9bb-4c43edf66f2b", // Identity Pool ID
                            Regions.US_EAST_1 // Region
                    );

                    // Initialize a Dynamo DB Client object
                    ddbClient = new AmazonDynamoDBClient(credentialsProvider);
                    // Server is located on US_WEST_2
                    ddbClient.setRegion(Region.getRegion(Regions.US_WEST_2));
                    // Initialize a Dynamo DB Mapper object
                    mapper = new DynamoDBMapper(ddbClient);
                    // Alert listeners when ready
                    awsConnectionReady.awsConnectionReady();
                }
            }).start();
        }
        return awsManager;
    }


    // Functions to handle marker availability. Idea is to call getMarkerMapReady and if true, call
    // getMarkerMap. If not ready, getMarkerMap will return null. If it's not ready, can register for
    // the callback when the scan is returned. Not sure on timing of scan as it scales, so the interface
    // is a little goofy for worst case scenario
    public static boolean getMarkerMapReady() {
        return markersReady;
    }
    public static HashMap<LatLng,MarkerLocation> getMarkerMap() {
        if(markersReady) {
            return markerMap;
        }
        return null;
    }
    public static void addMapMarker(LatLng key, MarkerLocation value) {
        if(markersReady) {
            if(markerMap.containsKey(key)) {
                markerMap.remove(key);
            }
            markerMap.put(key, value);
        }
    }
    public static void removeMapMarker(LatLng key) {
        if(markersReady) {
            if(markerMap.containsKey(key)) {
                markerMap.remove(key);
            }
        }
    }

    // Set function that MainActivity will call to keep track of the current user
    public static void setCurrentUser(String user) {
        currentUser = user;
    }


    // Function to get a CurrentLocation back, assumes you have a callback registered
    public void getCurrentLocation(final String userid) {
        Log.d(LOG_AWS, "Current Location: " + userid + " requested...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CurrentLocation cl = mapper.load(CurrentLocation.class, userid);
                    currentLocationCallback.currentLocationCallback(cl);
                } catch (AmazonClientException e) {
                    Toast t = Toast.makeText(appContext, "Current Location get failed", Toast.LENGTH_SHORT);
                    t.show();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Function to update a CurrentLocation
    public void updateCurrentLocation(final CurrentLocation cl) {
        Log.d(LOG_AWS, "Current Location: " + cl.getUserid() + " update sent...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mapper.save(cl);
                    Log.d(LOG_AWS, "Current Location update successful!");
                    currentUpdateCallback.currentUpdateCallback(cl);
                }
                catch (AmazonClientException e) {
                    Toast t = Toast.makeText(appContext, "Current Location update failed", Toast.LENGTH_SHORT);
                    t.show();
                    e.printStackTrace();
                }
            }
        }).start();
    }


    // Function to get all MarkerLocation objects from the table
    public void getAllMarkers() {
        Log.d(LOG_AWS, "Performing SCAN on AWS marker location table...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
                    PaginatedScanList<MarkerLocation> result = mapper.scan(MarkerLocation.class, scanExpression);
                    LatLng latlng;
                    for (MarkerLocation mloc : result) {
                        latlng = new LatLng(mloc.getLatitude(),mloc.getLongitude());
                        markerMap.put(latlng,mloc);
                    }
                    markersReady = true;
                    markerScanCallback.markerScanCallback();
                }
                catch (AmazonClientException e) {
                    Toast t = Toast.makeText(appContext, "Marker Location scan failed", Toast.LENGTH_SHORT);
                    t.show();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Function to create a new or update an existing MarkerLocation object in the table
    public void updateMarker(final MarkerLocation ml) {
        Log.d(LOG_AWS, "Marker Location: (" + ml.getLatitude() + "," + ml.getLongitude() + ") update sent...");
        // Everything is set in MapActivity except for the user id, set that here
        ml.setUserid(currentUser);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mapper.save(ml);
                    Log.d(LOG_AWS, "Marker Location update successful!");
                    markerUpdateCallback.markerUpdateCallback(ml);
                }
                catch (AmazonClientException e) {
                    Toast t = Toast.makeText(appContext, "Marker Location update failed", Toast.LENGTH_SHORT);
                    t.show();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Function to delete an existing MarkerLocation object from the table
    public void deleteMarker(final MarkerLocation ml) {
        Log.d(LOG_AWS, "Marker Location: (" + ml.getLatitude() + "," + ml.getLongitude() + ") delete sent...");
        // Everything is set in MapActivity except for the user id, set that here
        ml.setUserid(currentUser);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mapper.delete(ml);
                    Log.d(LOG_AWS, "Marker Location delete successful!");
                    markerDeleteCallback.markerDeleteCallback();
                }
                catch (AmazonClientException e) {
                    Toast t = Toast.makeText(appContext, "Marker Location delete failed", Toast.LENGTH_SHORT);
                    t.show();
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

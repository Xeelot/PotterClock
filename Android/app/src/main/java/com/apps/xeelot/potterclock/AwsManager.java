package com.apps.xeelot.potterclock;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.ArrayList;


public class AwsManager {
    private static final String LOG_AWS = "AWS";

    // Initialize the components needed to access Amazon Web Services
    private static CognitoCachingCredentialsProvider credentialsProvider;
    private static AmazonDynamoDBClient ddbClient;
    private static DynamoDBMapper mapper;
    private static Context appContext;

    // Create a singleton of the class when getAwsManager is called
    private static AwsManager awsManager;


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


    // Interface for responding to MarkerLocation scans
    interface MarkerLocationCallback {
        void markerLocationCallback(ArrayList<MarkerLocation> ml);
    }
    MarkerLocationCallback markerLocationCallback;
    void registerMarkerLocationCallback(MarkerLocationCallback callback) {
        Log.d(LOG_AWS, "Marker location callback registered.");
        markerLocationCallback = callback;
    }


    // Interface to grab the singleton class for AwsManager
    public static AwsManager getAwsManager(final Context context) {
        // Create singleton if it hasn't been made yet
        Log.d(LOG_AWS, "AwsManager get singleton requested...");
        if(awsManager == null) {
            Log.d(LOG_AWS, "AwsManager being instantiated...");
            awsManager = new AwsManager();
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
                    ArrayList<MarkerLocation> resultList = new ArrayList<MarkerLocation>();
                    for (MarkerLocation mloc : result) {
                        resultList.add(mloc);
                    }
                    markerLocationCallback.markerLocationCallback(resultList);
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mapper.save(ml);
                    Log.d(LOG_AWS, "Marker Location update successful!");
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mapper.delete(ml);
                    Log.d(LOG_AWS, "Marker Location delete successful!");
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

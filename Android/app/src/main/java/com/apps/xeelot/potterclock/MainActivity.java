package com.apps.xeelot.potterclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        AwsManager.CurrentLocationCallback,
        AwsManager.AwsConnectionReady {

    // Constants
    private final String LOG_MAIN = "MAIN";
    private final String PREFS_MAIN = "PotterClockPrefs";
    private final String PREFS_USER = "SelectedUser";

    // Initialize local variables and objects we want to keep track of
    private TextView debug;
    private Button mapButton;
    private Button userButton;
    private Button updateButton;
    private AwsManager awsManager;
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
    }

    // onStart comes after onCreate and checks to make sure we have a user selected
    @Override
    public void onStart() {
        super.onStart();
        // If no user is selected, call to select user
        if(selectedUser == res.getString(R.string.select_user)) {
            selectUserDialog();
        }
    }


    // Connection to AWS is ready to receive calls, get current info
    @Override
    public void awsConnectionReady() {
        // Get the user's current locations
        //TODO: make this more dynamic
        awsManager.getCurrentLocation("Joe");
        awsManager.getCurrentLocation("Hannah");
    }


    // Response from AWS with a current location
    @Override
    public void currentLocationCallback(final CurrentLocation cl) {
        Log.d(LOG_MAIN, "Current location callback received!");
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    debug.setText(cl.getUserid());
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    // Button handler, currently only 2 options
    @Override
    public void onClick(View view) {
        Log.d(LOG_MAIN, "Button click received!");
        if(view.getId() == R.id.buttonMap) {
            // Create a new intent and launch the map activity
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }
        else {
            // Open the select user dialog
            selectUserDialog();
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
            }
        });
        builder.create();
        builder.show();
    }
}

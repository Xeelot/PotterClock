package com.app.xeelot.potterclock;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

import java.util.Calendar;

public class DebugActivity extends AppCompatActivity implements View.OnClickListener {

    void setupListener(int inputId) {
        Button temp = (Button)findViewById(inputId);
        temp.setOnClickListener(this);
    }

    AwsDb ddb;
    CognitoCachingCredentialsProvider credentialsProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        setupListener(R.id.buttonWork);
        setupListener(R.id.buttonHome);
        setupListener(R.id.buttonPark);
        setupListener(R.id.buttonBarn);
        setupListener(R.id.buttonLost);
        setupListener(R.id.buttonPeril);
        setupListener(R.id.buttonFlorida);
        setupListener(R.id.buttonTexas);
        setupListener(R.id.buttonTransit);
        setupListener(R.id.buttonHoliday);
        setupListener(R.id.buttonPub);
        setupListener(R.id.buttonGrocery);

        // Initialize the Amazon Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:baa7edb4-4d3c-403c-8fee-007239f09082", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        // Create our AwsDb class for interfacing with DynamoDB
        ddb = new AwsDb();
        ddb.initDb(credentialsProvider);
    }


    @Override
    public void onClick(View v) {
        // Get the app context and retrieve the button that was pressed
        Context context = getApplicationContext();
        Button pressed = (Button)findViewById(v.getId());
        CharSequence locText = pressed.getText();
        int duration = Toast.LENGTH_SHORT;

        // Check name field to ensure it's filled in
        EditText nameObject = (EditText)findViewById(R.id.nameField);
        CharSequence nameText = nameObject.getText().toString().trim();
        Toast toast;
        if(nameText.equals("")) {
            // No name, show name toast and exit
            toast = Toast.makeText(context, "Fill in name", duration);
        }
        else {
            // Name + button is known, create json object with data
            Calendar calendar = Calendar.getInstance();
            Current c = new Current();
            c.setUserId(nameText.toString());
            c.setPosition(locText.toString());
            c.setMonth(calendar.get(Calendar.MONTH));
            c.setDay(calendar.get(Calendar.DATE));
            c.setYear(calendar.get(Calendar.YEAR));
            c.setHour(calendar.get(Calendar.HOUR));
            c.setMinute(calendar.get(Calendar.MINUTE));
            c.setSecond(calendar.get(Calendar.SECOND));

            ddb.saveCurrent(c);

            toast = Toast.makeText(context, locText, duration);
            // Attempt to POST the json data
            //try {
                //CharSequence response = jData.post();
            //    toast = Toast.makeText(context, locText, duration);
            //} catch(IOException e) {
            //    e.printStackTrace();
            //}
        }

        // Output a toast on the button press for visual satisfaction
        toast.show();
    }

}

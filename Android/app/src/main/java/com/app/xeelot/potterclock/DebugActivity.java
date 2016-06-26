package com.app.xeelot.potterclock;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;


public class DebugActivity extends AppCompatActivity implements View.OnClickListener {

    void setupListener(int inputId) {
        Button temp = (Button)findViewById(inputId);
        if(temp != null) {
            temp.setOnClickListener(this);
        }
    }

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
    }

    CurrentRestClientUsage currentRest = new CurrentRestClientUsage();

    @Override
    public void onClick(View v) {
        // Get the app context and retrieve the button that was pressed
        Context context = getApplicationContext();
        Button pressed = (Button)findViewById(v.getId());
        CharSequence locText = pressed.getText();
        int duration = Toast.LENGTH_SHORT;

        // Check name field to ensure it's filled in
        Spinner nameObject = (Spinner)findViewById(R.id.spinner);
        CharSequence nameText = nameObject.getSelectedItem().toString();
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

            currentRest.postCurrent(c.getParams());

            toast = Toast.makeText(context, locText, duration);
        }

        // Output a toast on the button press for visual satisfaction
        toast.show();
    }

}

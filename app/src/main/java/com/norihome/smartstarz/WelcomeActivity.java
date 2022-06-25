package com.norihome.smartstarz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity {

    private String userName = "";
    private static final String ADMIN_CODE = "1008";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);



        Intent intent = getIntent();
        // Get the user name passed in from MainActivity
        userName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Set name in welcome text view
        TextView welcomeView = (TextView) findViewById(R.id.welcome_textview);
        welcomeView.setText("Welcome " + userName);
    }

    public void onAdminButtonClick(View v) {
        //get the access code that the user enters and compare with correct code
        EditText accessCode = (EditText) findViewById(R.id.Password_field);
        String code = accessCode.getText().toString();
        System.out.println("*********** FOUND ACCESS CODE " + code );

        if (code == null || code.length() == 0) {
            Toast.makeText(this, "Please enter an access code", Toast.LENGTH_SHORT).show();
            System.out.println("USER DID NOT ENTER A CODE ");
            return;

        }
        //validating access code and starting activity
        if (code.equals(ADMIN_CODE)) {
            Intent intent = new Intent(this, AdminActivity.class);
            intent.putExtra(MainActivity.EXTRA_MESSAGE, userName);
            startActivity(intent);
        } else {
            Toast.makeText(this, "The code is incorrect, try again.", Toast.LENGTH_SHORT).show();
            return;
        }

    }

    public void onStudentButtonClick(View v) {


        // Start EventFeedFragment
        Intent intent = new Intent(this, StudentActivity.class);
        intent.putExtra(MainActivity.EXTRA_MESSAGE, userName);
        startActivity(intent);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_signout:


                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


}

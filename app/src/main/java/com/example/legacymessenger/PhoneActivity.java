package com.example.legacymessenger;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.legacymessenger.VerificationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hbb20.CountryCodePicker;

public class PhoneActivity extends AppCompatActivity implements View.OnClickListener {


    //Initializing UI components
    private CountryCodePicker codePicker;
    private EditText phoneNumber;
    private Button sendVerification;
    private boolean connectionCheck;

    //Data
    private String countryCode;
    private String userInput;
    private String number;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);


        checkUser();



        codePicker = findViewById(R.id.codePicker);
        phoneNumber = findViewById(R.id.phoneNumber);
        sendVerification = findViewById(R.id.sendVerification);

        //setting up OnClick Listners
        sendVerification.setOnClickListener(this);

    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    private void checkUser(){

        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {

            if (currentUser.getPhotoUrl() != null && currentUser.getDisplayName() != null) {

                startActivity(new Intent(this, Dashboard.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            }else {

                startActivity(new Intent(this, DataActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.sendVerification:

                proceed();
                break;

        }

    }

    private void proceed() {

        if (checkConnection()) {

            getNumber();

            if (phoneNumber.getText().toString().isEmpty()){

                phoneNumber.setError("Phone Number is Required");
                phoneNumber.requestFocus();
                return;
            }

            confirm();
        }else {

            switchOnInternet();
        }
    }

    private void switchOnInternet() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.MyDialogTheme);

        dialog.setTitle("No Internet Connection");
        dialog.setMessage("Please make sure you are connected to the internet");
        dialog.setPositiveButton("Internet Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));

            }
        });

        dialog.show();

    }

    public boolean checkConnection() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork != null){

            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){

                connectionCheck = true;

            }else {

                connectionCheck = false;
            }
        }else {

            connectionCheck = false;
        }

        return connectionCheck;
    }

    private void confirm() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.MyDialogTheme);

        dialog.setTitle("Confirmation");
        dialog.setMessage("Please make sure your entered the correct number");
        dialog.setPositiveButton("I'm Sure!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                //Sending user to new activity to enter the verification code
                startActivity(new Intent(PhoneActivity.this, VerificationActivity.class).putExtra("number", number));


            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        dialog.show();
    }

    private void getNumber() {

        countryCode = codePicker.getSelectedCountryCode();
        userInput = phoneNumber.getText().toString();

        number = "+" + countryCode + userInput;

    }


}

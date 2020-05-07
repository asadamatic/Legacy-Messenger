package com.example.legacymessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerificationActivity extends AppCompatActivity implements View.OnClickListener {

    //Components
    private EditText verificationCode;
    private TextView expireMessage;
    private Button login, resendCode;

    //Data
    String number, mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;

    //Firebase components
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser;



    //Timer for calcultaing the timeout for verification code
    //60 seconds
    private CountDownTimer timer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long l) {

            expireMessage.setText("Code expires in "+ l/1000 + " seconds");
        }

        @Override
        public void onFinish() {

            expireMessage.setText("code  has expired");
            expireMessage.setTextColor(Color.RED);
            resendCode.setEnabled(true);
        }
    };
    //


    //.............................ONCREATE.....................................................
    //.............................ONCREATE.....................................................
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);




        //Getting string extras  from intent in the previous activity
        number = getIntent().getStringExtra("number");


        //Referencing components
        verificationCode = findViewById(R.id.verificationCode);
        expireMessage = findViewById(R.id.expireMessage);
        login = findViewById(R.id.login);
        resendCode = findViewById(R.id.resendCode);

        //setting up OnClick Listners
        login.setOnClickListener(this);
        resendCode.setOnClickListener(this);

        sendCode();

    }

    //.............................ONCREATE.....................................................
    //.............................ONCREATE.....................................................



    //.............................ONSTART.....................................................
    //.............................ONSTART.....................................................

    @Override
    protected void onStart() {
        super.onStart();

    }

    //.............................ONSTART.....................................................
    //.............................ONSTART.....................................................



    //.............................ONCLICK LISTNER.....................................................
    //.............................ONCLICK LISTNER.....................................................
    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.login:

                verifyCode();
                break;

            case R.id.resendCode:

                sendCode();
                break;
        }
    }

    //.............................ONCLICK LISTNER.....................................................
    //.............................ONCLICK LISTNER.....................................................


    //.............................VERIFY CODE MANUALLY.....................................................
    //.............................VERIFY CODE MANUALLY.....................................................
    private void verifyCode() {

        String code = verificationCode.getText().toString();

        if (code.isEmpty()){

            verificationCode.setError("Verification code is required");
            verificationCode.requestFocus();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        signInWithPhoneAuthCredential(credential);
    }

    //.............................VERIFY CODE MANUALLY.....................................................
    //.............................VERIFY CODE MANUALLY.....................................................


    //.............................GENERALIZED ALERT DIALOG FOR ANY FAILURE IN PROCESS.....................................................
    //.............................GENERALIZED ALERT DIALOG FOR ANY FAILURE IN PROCESS.....................................................
    private void failure(String title, String message){

        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        dialog.setTitle(title).setMessage(message);
        dialog.setNeutralButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                finish();

            }
        });

        dialog.show();
    }

    //.............................GENERALIZED ALERT DIALOG FOR ANY FAILURE IN PROCESS.....................................................
    //.............................GENERALIZED ALERT DIALOG FOR ANY FAILURE IN PROCESS.....................................................


    //.............................SEND CODE.....................................................
    //.............................SEND CODE.....................................................
    private void sendCode() {

        resendCode.setEnabled(false);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                this,
                mCallBacks
        );
    }

    //.............................SEND CODE.....................................................
    //.............................SEND CODE.....................................................



    //Setting up onVerficationChangedCallbacks to be invoked as a result of verification implementation
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            //Gettting SMS code to be displayed in place verificationCode( Optional )
            signInWithPhoneAuthCredential(phoneAuthCredential);

            verificationCode.setText(phoneAuthCredential.getSmsCode());
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

            if (e instanceof FirebaseAuthInvalidCredentialsException) {

                failure("Invalid Phone Number", "Please enter a valid phone number");

            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
                failure("Too many requests", "SMS quota has been exceeded Please try again later");
            }else if (e instanceof FirebaseNetworkException){

                failure("No internet connection", "Please check your internet connection");
            }
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {

            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;

            //Starting timer to calculate timeout for verficcation code
            timer.start();
            // ...

            expireMessage.setTextColor(getResources().getColor(R.color.darkgray));
        }


    };


    //Method to take user credentials "Phone  Number" and "Verifiaction Code" and authenticate the suer
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {



                            // Sign in success, update UI with the signed-in user's information
                            startActivity(new Intent(VerificationActivity.this, DataActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));

                            currentUser = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                                Toast.makeText(VerificationActivity.this, "Incorrect Code", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    //

}

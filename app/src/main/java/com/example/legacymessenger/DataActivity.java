package com.example.legacymessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class DataActivity extends AppCompatActivity implements View.OnClickListener{

    //Components
    private CircularImageView userImageEdit;
    private EditText userNameEdit;
    private Button continueButton;
    private Button saveButton, backButton;
    private ProgressBar imageLoadingProgress, dataUploadingProgress;

    //Progress Dialogs
    private ProgressDialog imageUploadingProgress;
    private ProgressDialog savingDataProgress;


    //Data
    String displayName;
    Uri imageUri, profileUri;
    Bitmap bitmap, savedBitmap;

    //Connection check
    boolean connectionCheck;


    boolean ImageChangesSaved = false, NameChangesSaved = false;

    //Editting Check
    boolean editCheck = false;

    //checking data loading
    boolean nameLoaded = false, imageLoaded = false;

    //Detect change in data
    boolean changedName = false, changedImage = false;

    boolean settingsFlag = false;

    //Firebase components
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    StorageReference parentReference = FirebaseStorage.getInstance().getReference();
    FirebaseUser currentUser = firebaseAuth.getCurrentUser();




    //.............................ONCREATE.....................................................
    //.............................ONCREATE......................................


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        //Loading User data from Firebase
        //Display Name
        //Profile Image



        //Getting data from Intent called inDashboard activity
        //Tracking if the user has entered viaDashboard Activity or MobilrVerification Activvity
        editCheck = getIntent().getBooleanExtra("editCheck", false);


        //Referencing UI components
        userImageEdit = findViewById(R.id.userImageEdit);
        userNameEdit = findViewById(R.id.userNameEdit);
        continueButton = findViewById(R.id.continueButton);
        imageLoadingProgress = findViewById(R.id.imageLoadingProgress);
        saveButton = findViewById(R.id.saveButton);


        //Initializing Progress Dialogs
        imageUploadingProgress = new ProgressDialog(this);
        savingDataProgress = new ProgressDialog(this);






        loadUserData();







        //Ading on text change listner
        userNameEdit.setTag(false);

        //Tracking if the user has cahnged name
        userNameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                userNameEdit.setTag(true);
            }
        });

        userNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if ((Boolean) userNameEdit.getTag()){

                    //Tracking any change in the userName
                    //True means the userName has changed
                    //The save button has to be enabled
                    changedName = true;

                    //making saveButton visible since the userName has been changed
                    saveButton.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



        //Setting up OnClickListners
        userImageEdit.setOnClickListener(this);
        continueButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

    }


    //.............................ONCREATE.....................................................
    //.............................ONCREATE......................................



    //.............................Loading User Data.....................................................
    //.............................Loading User Data.....................................................


    private void loadUserData() {
        if (checkConnection()) {


            if (currentUser != null) {


                if (currentUser.getPhotoUrl() != null || currentUser.getDisplayName() != null) {

                    //Disabling continue button to stop user from skipping data upload activity
                    continueButton.setEnabled(false);


                    //Displaying progress bar to show thw progress of loading user data from firebase
                    imageLoadingProgress.setVisibility(View.VISIBLE);


                    if (currentUser.getDisplayName() != null) {

                        userNameEdit.setText(currentUser.getDisplayName());

                        //Confirming that the user name has been loaded
                        nameLoaded = true;
                    }

                    if (currentUser.getPhotoUrl() != null) {

                        Glide.with(this).load(currentUser.getPhotoUrl().toString()).into(userImageEdit);

                        //Confirming that the user image has been loaded
                        imageLoaded = true;

                    }
                }


                if (nameLoaded && imageLoaded) {

                    //Enabling user to skip data upload since user had previously uploaded his data
                    continueButton.setEnabled(true);

                    //Hiding progress bar since the user data has been loaded
                    imageLoadingProgress.setVisibility(View.GONE);

                } else {

                    //Disabling continue button to stop user from skipping data upload activity
                    continueButton.setEnabled(false);

                    //Hiding progress bar to allow user to upload his data
                    imageLoadingProgress.setVisibility(View.GONE);
                }
            }

        } else {

            //Provoking user to switch on device's internet
            switchOnInternet();
        }

    }

    //.............................Loading User Data.....................................................
    //.............................Loading User Data.....................................................


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.userImageEdit:

                getUserImage();
                break;

            case R.id.continueButton:

                continuetoProfile();

                break;

            case R.id.saveButton:

                saveData();
                break;
        }
    }


    //.............................CONTINUE  TO PROFILE.....................................................
    //.............................CONTINUE  TO PROFILE.....................................................


    private void continuetoProfile() {


        displayName = userNameEdit.getText().toString();

        if (currentUser.getDisplayName() == null) {

            //Checking if the Username is Empty
            if (displayName.isEmpty()) {

                userNameEdit.setError("Name is required");
                userNameEdit.requestFocus();
                Toast.makeText(this, "Name  is required", Toast.LENGTH_SHORT).show();
                return;
            }
        }else if (currentUser.getDisplayName() != null) {

            //Checking if the Username is Empty
            if (displayName.isEmpty()) {

                userNameEdit.setError("Name is required");
                userNameEdit.requestFocus();
                Toast.makeText(this, "Name  is required", Toast.LENGTH_SHORT).show();
                return;
            }


        }


        if (currentUser.getPhotoUrl() == null) {

            //Checking if imageview  is empty
            if (profileUri == null) {

                userImageEdit.setBorderWidth(4);
                userImageEdit.setBorderColor(Color.RED);

                Toast.makeText(this, "Image is required", Toast.LENGTH_SHORT).show();
                return;
            }
        }


        //Checking if the user had made any changes to his data
        if (changedName && changedImage){


            //AND ASkingthe user to save changes if any unsaved changes are detected
            if (NameChangesSaved && ImageChangesSaved){

                openProfile();

            }else {

                saveChanges();

            }

        }else if (changedName || changedImage){

            //Checking if the user had made any changes to his Name
            if (changedName){


                //AND ASkingthe user to save changes if any unsaved changes to Name are detected
                if (NameChangesSaved){

                    openProfile();

                }else {

                    saveChanges();

                }

            }

            //Checking if the user had made any changes to his profile Image
            if (changedImage){

                //AND ASkingthe user to save changes if any unsaved changes to profile Image are detected
                if (ImageChangesSaved){

                    openProfile();

                }else {

                    saveChanges();
                }

            }

        }else if (!changedName && !changedImage){

            openProfile();

        }
    }

    //.............................CONTINUE  TO PROFILE.....................................................
    //.............................CONTINUE  TO PROFILE.....................................................



    //.............................SAVE CHANGES BEFORE LEAVING.....................................................
    //.............................SAVE CHANGES BEFORE LEAVING.....................................................

    private void saveChanges(){


        final AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.MyDialogTheme);

        dialog.setTitle("Unsaved Changes");
        dialog.setMessage("Please save changes before you leave");
        dialog.setPositiveButton("Save Data", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                saveData();


            }
        }).setNegativeButton("Ignore Changes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                openProfile();
            }
        });

        dialog.show();

    }

    //.............................SAVE CHANGES BEFORE LEAVING.....................................................
    //.............................SAVE CHANGES BEFORE LEAVING.....................................................


    //.............................OPEN PROFILE.....................................................
    //.............................OPEN PROFILE.....................................................

    private void openProfile() {

        if (editCheck) {

            finish();
        } else {

            displayName = userNameEdit.getText().toString();


            startActivity(new Intent(this,Dashboard.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("username", displayName));
        }

    }
    //.............................OPEN PROFILE.....................................................
    //.............................OPEN PROFILE......................................

    //.............................TEST METHOD.....................................................
    //.............................TEST METHOD......................................

    private void testing() {

        if (!checkConnection()){

            switchOnInternet();

            Toast.makeText(this, String.valueOf(changedName), Toast.LENGTH_SHORT).show();

        }else {

            Toast.makeText(this, String.valueOf(changedName), Toast.LENGTH_SHORT).show();
        }
    }

    //.............................TEST METHOD.....................................................
    //.............................TEST METHOD........



    //.............................UPLOADING IMAGE.....................................................
    //.............................UPLOADING IMAGE......................................

    //Starting to getDashboard Image

    //1st step
    //Starting crop activity to allow user to select and crop image from mobile gallery or camera
    private void getUserImage() {

        CropImage.activity().setAspectRatio(1, 1).setRequestedSize(150, 150)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(DataActivity.this);

    }

    //2nd Step
    //Getting result from the crop activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();

                uploadImage();
            }


        }
    }

    //3rd Step
    //Uploading the mage octained from the Crop Activity Result ontpo Firebasse Storage
    private void uploadImage() {

        userImageEdit.setBorderWidth(4);
        userImageEdit.setBorderColor(Color.parseColor("#e2e2e2"));

        StorageReference childReference = parentReference.child("ProfileImages/" + System.currentTimeMillis() + ".jpg");

        //Setting up Progress dialog for uploading image to the database
        imageUploadingProgress.setMessage("Uploading profile Picture");
        imageUploadingProgress.setCancelable(false);
        imageUploadingProgress.show();

        childReference.putFile(imageUri).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                //Dismissing the progres dialog since the uploading process has failed
                imageUploadingProgress.dismiss();


                //Handling Firebase exception
                Toast.makeText(DataActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imageUploadingProgress.dismiss();

                Task<Uri> taskUri = taskSnapshot.getStorage().getDownloadUrl();

                while (!taskUri.isSuccessful()) ;

                Uri downloadUri = taskUri.getResult();

                profileUri = downloadUri;


                //displaying profile image once it has been uploaded
                userImageEdit.setImageURI(imageUri);

                //Tracking any change in the userImage
                //True means the userName has changed
                //The save button has to be enabled
                changedImage = true;

                //making saveButton visible since the userImage has been changed
                saveButton.setVisibility(View.VISIBLE);

            }
        });

    }
    //..........................UPLOADING IMAGE...........................................
    //..........................UPLOADING IMAGE...........................................



    //..........................SAVING DATA TO FIREBASE........................................
    //..........................SAVING DATA TO FIREBASE........................................

    //Saving userdata on Firebase
    private void saveData() {

        displayName = userNameEdit.getText().toString();


        if (checkConnection()) {

            if (currentUser != null) {

                if (currentUser.getDisplayName() == null) {

                    //Checking if the Username is Empty
                    if (displayName.isEmpty()) {

                        userNameEdit.setError("Name is required");
                        userNameEdit.requestFocus();
                        Toast.makeText(this, "Name  is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else if (currentUser.getDisplayName() != null){

                    //Checking if the Username is Empty
                    if (displayName.isEmpty()) {

                        userNameEdit.setError("Name is required");
                        userNameEdit.requestFocus();
                        Toast.makeText(this, "Name  is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }


                if (currentUser.getPhotoUrl() == null) {

                    //Checking if imageview  is empty
                    if (profileUri == null) {

                        userImageEdit.setBorderWidth(4);
                        userImageEdit.setBorderColor(Color.RED);

                        Toast.makeText(this, "Image is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (changedName) {

                    //Setting up progress dialog to show progress for saving user data onto database
                    savingDataProgress.setMessage("Saving your data");
                    savingDataProgress.setCancelable(false);
                    savingDataProgress.show();

                    //CreatingDashboardChangeRequest for user displayName
                    UserProfileChangeRequest userNameChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(displayName).build();

                    currentUser.updateProfile(userNameChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {


                            if (task.isSuccessful()) {

                                NameChangesSaved = true;

                                //Since the changes have been updated successfully
                                //Turning the value  of "Name  change detector (changedName)" false
                                //To avoid any garbage update requests
                                changedName = false;

                                onChangeSaved();

                            } else {

                                onChangeSaved();
                            }
                        }
                    });
                }

                if (changedImage) {

                    //Setting up progress dialog to show progress for saving user data onto database
                    savingDataProgress.setMessage("Saving your data");
                    savingDataProgress.setCancelable(false);
                    savingDataProgress.show();

                    //CreatingDashboardChangeRequest for user displayName
                    UserProfileChangeRequest userImageChangeRequest = new UserProfileChangeRequest.Builder().setPhotoUri(profileUri).build();

                    currentUser.updateProfile(userImageChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                ImageChangesSaved = true;

                                //Since the changes have been updated successfully
                                //Turning the value  of "Image  change detector (changedImage)" false
                                //To avoid any garbage update requests
                                changedImage = false;

                                onChangeSaved();

                            } else {


                                onChangeSaved();
                            }

                        }
                    });

                }

            }



        }else {

            //Provoking user to switch on device's internet
            switchOnInternet();
        }

    }
    //..........................SAVING DATA TO FIREBASE........................................
    //..........................SAVING DATA TO FIREBASE........................................


    //..........................ONCHANGES SAVED........................................
    //..........................ONCHANGES SAVED........................................


    //Calling a method as a result of Success or Failure of saveData funstion
    private void onChangeSaved(){

        Toast.makeText(DataActivity.this, String.valueOf(NameChangesSaved), Toast.LENGTH_SHORT).show();

        //Checking if any of the user data is updated
        if (NameChangesSaved || ImageChangesSaved) {

            //Making saving detector variable false to detect any future changes
            if (NameChangesSaved){

                NameChangesSaved = false;
            }

            //Making saving detector variable false to detect any future changes
            if (ImageChangesSaved){

                ImageChangesSaved = false;
            }

            //Hiding prgoress dialog since the changes have been saved
            savingDataProgress.dismiss();

            ///Making save button invisible untile use makes any changes
            saveButton.setVisibility(View.GONE);

            continueButton.setEnabled(true);

            Toast.makeText(this, "Changes Saved", Toast.LENGTH_SHORT).show();
        }else {

            //Hiding prgoress dialog since the changes have been saved
            savingDataProgress.dismiss();

            ///Making save button invisible untile use makes any changes
            saveButton.setVisibility(View.GONE);

            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

    }
    //..........................ONCHANGES SAVED........................................
    //..........................ONCHANGES SAVED........................................




    //..........................Checking Internet connectivity........................................
    //..........................Checking Internet connectivity........................................

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

    //..........................Checking Internet connectivity........................................
    //..........................Checking Internet connectivity........................................



    //..........................Switch on Internet........................................
    //..........................Switch on Internet........................................

    private void switchOnInternet() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.MyDialogTheme);

        dialog.setTitle("No Internet Connection");
        dialog.setMessage("Please make sure you are connected to the internet");
        dialog.setPositiveButton("Internet Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                settingsFlag = true;

                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));

            }
        });

        dialog.show();
    }

    //..........................Switch on Internet........................................
    //..........................Switch on Internet........................................


    //..........................ONRESUME........................................
    //..........................ONRESUME........................................

    @Override
    protected void onResume() {
        super.onResume();

        if (settingsFlag){

            startActivity(new Intent(this, DataActivity.class));
        }
    }


    //..........................ONRESUME........................................
    //..........................ONRESUME........................................



    //..........................ONBACK PRESSED........................................
    //..........................ONBACK PRESSED........................................

    @Override
    public void onBackPressed() {


        //Checking if the user had made any changes to his data
        if (changedName && changedImage){


            //AND ASkingthe user to save changes if any unsaved changes are detected
            if (NameChangesSaved && ImageChangesSaved){

                super.onBackPressed();

            }else {

                saveChanges();

            }

        }else if (changedName || changedImage){

            //Checking if the user had made any changes to his Name
            if (changedName){


                //AND ASkingthe user to save changes if any unsaved changes to Name are detected
                if (NameChangesSaved){

                    super.onBackPressed();

                }else {

                    saveChanges();

                }

            }

            //Checking if the user had made any changes to his profile Image
            if (changedImage){

                //AND ASkingthe user to save changes if any unsaved changes to profile Image are detected
                if (ImageChangesSaved){

                    super.onBackPressed();

                }else {

                    saveChanges();
                }

            }

        }else if (!changedName && !changedImage){

            super.onBackPressed();

        }

    }


    //..........................ONBACK PRESSED........................................
    //..........................ONBACK PRESSED........................................


}

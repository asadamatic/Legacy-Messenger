package com.example.legacymessenger;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.legacymessenger.Data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

public class Dashboard extends AppCompatActivity {


    //Data
    String displayName;
    String userId;


    //FirebaseUser data
    User user;
    String userName, phoneNumber, profileImage;

    //Firebase Components
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser;

    DatabaseReference databaseReference;

    //Design setup
    private ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        displayName = getIntent().getStringExtra("username");

        updateUserData();


        uploadUserData();

        //Adapter for viewpager
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());


        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);


        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        tabs.getTabAt(0).setIcon(R.drawable.chat_cion);
        tabs.getTabAt(1).setIcon(R.drawable.group_cion);
        tabs.getTabAt(2).setIcon(R.drawable.phone_icon);

        Toolbar toolbar = findViewById(R.id.dashboardtoolBar);

        //Setting up custom toolbar for dashboard
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Messenger");
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setIcon(R.drawable.chat_cion);

        viewPager.setOffscreenPageLimit(2);

    }

    private void uploadUserData() {

        databaseReference = FirebaseDatabase.getInstance().getReference().child("UID");

        userId = firebaseAuth.getCurrentUser().getUid();

        currentUser = firebaseAuth.getCurrentUser();


        userName = currentUser.getDisplayName();
        phoneNumber = currentUser.getPhoneNumber();
        profileImage = String.valueOf(currentUser.getPhotoUrl());

        user = new User();

        user.setDisplayName(userName);
        user.setPhoneNumber(phoneNumber);
        user.setProfileImage(profileImage);
        user.setUserId(userId);

        databaseReference.child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.dashboard_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //.............................ON OPTIONS ITEM SELECTED.....................................................
    //.............................ON OPTIONS ITEM SELECTED.....................................................


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.editProfileOption:

                startActivity(new Intent(Dashboard.this, DataActivity.class).putExtra("editCheck", true));

                break;

            case R.id.logoutOption:

                FirebaseAuth.getInstance().signOut();
                finish();

                break;

        }
        return super.onOptionsItemSelected(item);
    }


    //.............................ON OPTIONS ITEM SELECTED.....................................................
    //.............................ON OPTIONS ITEM SELECTED.....................................................


    @Override
    public void onBackPressed() {

        if (viewPager.getCurrentItem() == 0){

            super.onBackPressed();
        }else {

            //if user is in "Groups" or "Calls" tab
            //Send him to "Chats" tab on pressing back button
            viewPager.setCurrentItem(0);
        }


    }

    //.............................UPDATE USER DISPLAY NAME.....................................................
    //.............................UPDATE USER DISPLAY NAME.....................................................

    private void updateUserData() {

        currentUser = firebaseAuth.getCurrentUser();



        if (displayName != null) {


            UserProfileChangeRequest userNameChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(displayName).build();

            currentUser.updateProfile(userNameChangeRequest);
        }
    }


    //.............................UPDATE USER DISPLAY NAME.....................................................
    //.............................UPDATE USER DISPLAY NAME.....................................................


}
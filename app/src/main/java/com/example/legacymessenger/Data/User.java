package com.example.legacymessenger.Data;

import android.net.Uri;

import java.io.Serializable;

public class User implements Serializable {

    String displayName;
    String phoneNumber;
    String profileImage;
    String userId;

    public User(){

    }

    public User(String displayName, String phoneNumber, String profileImage, String userId) {
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

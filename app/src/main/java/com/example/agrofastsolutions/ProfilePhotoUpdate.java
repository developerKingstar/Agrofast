package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

/**
 * Minimal payload for updating ONLY the profile photo URL.
 */
public class ProfilePhotoUpdate {

    @SerializedName("profile_photo_url")
    private String profilePhotoUrl;

    public ProfilePhotoUpdate(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
}
package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

/**
 * Minimal payload for updating a user's profile.
 * Only includes fields users can change after signup.
 */
public class UpdateProfile {

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("location")
    private String location;

    public UpdateProfile(String name, String phone, String location) {
        this.name = name;
        this.phone = phone;
        this.location = location;
    }

    public String getName()     { return name; }
    public String getPhone()    { return phone; }
    public String getLocation() { return location; }
}
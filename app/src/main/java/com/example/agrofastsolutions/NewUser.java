package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

/**
 * Minimal payload for creating a public.users row.
 * Sent AFTER Supabase Auth creates the auth.users row.
 */
public class NewUser {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("name")
    private String name;

    @SerializedName("location")
    private String location;

    @SerializedName("status")
    private String status;

    public NewUser(String userId, String email, String phone,
                   String name, String location, String status) {
        this.userId = userId;
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.location = location;
        this.status = status;
    }

    // Getters
    public String getUserId()   { return userId; }
    public String getEmail()    { return email; }
    public String getPhone()    { return phone; }
    public String getName()     { return name; }
    public String getLocation() { return location; }
    public String getStatus()   { return status; }
}
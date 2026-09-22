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

    // ===== Read-only fields (populated when fetching) =====

    @SerializedName("profile_photo_url")
    private String profilePhotoUrl;

    @SerializedName("rating_avg")
    private double ratingAvg;

    @SerializedName("review_count")
    private int reviewCount;

    @SerializedName("created_at")
    private String createdAt;

    public NewUser() {}   // ← add this line

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
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public double getRatingAvg()       { return ratingAvg; }
    public int    getReviewCount()     { return reviewCount; }
    public String getCreatedAt()       { return createdAt; }
}
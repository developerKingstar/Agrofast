package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

/**
 * A favourite row from the `favourites` table.
 * Uses NewUser for the joined favourited user.
 * Column is `favorited_user_id` (American spelling) — matches Supabase.
 */
public class Favourite {

    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("favorited_user_id")
    private String favoritedUserId;

    @SerializedName("created_at")
    private String createdAt;

    // Joined user details
    @SerializedName("favorite_user")
    private NewUser favoriteUser;

    public Favourite() {}

    public String getId()              { return id; }
    public String getUserId()          { return userId; }
    public String getFavoritedUserId() { return favoritedUserId; }
    public String getCreatedAt()       { return createdAt; }
    public NewUser getFavoriteUser()   { return favoriteUser; }

    public String getFavoriteName() {
        return (favoriteUser != null) ? favoriteUser.getName() : null;
    }

    public String getFavoritePhotoUrl() {
        return (favoriteUser != null) ? favoriteUser.getProfilePhotoUrl() : null;
    }

    public String getFavoriteLocation() {
        return (favoriteUser != null) ? favoriteUser.getLocation() : null;
    }

    public double getFavoriteRating() {
        return (favoriteUser != null) ? favoriteUser.getRatingAvg() : 0;
    }
}
package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

public class NewFavourite {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("favorited_user_id")
    private String favoritedUserId;

    public NewFavourite(String userId, String favoritedUserId) {
        this.userId          = userId;
        this.favoritedUserId = favoritedUserId;
    }

    public String getUserId()          { return userId; }
    public String getFavoritedUserId() { return favoritedUserId; }
}
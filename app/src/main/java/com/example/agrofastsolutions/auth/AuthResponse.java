package com.example.agrofastsolutions.auth;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("expires_in")
    private int expiresIn;

    @SerializedName("user")
    private AuthUser user;

    // For signup without email confirmation enabled, we might not get tokens immediately
    // In that case, only "user" comes back.
    @SerializedName("id")
    private String id;  // fallback for signup response

    public String getAccessToken()  { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType()    { return tokenType; }
    public int getExpiresIn()       { return expiresIn; }
    public AuthUser getUser()       { return user; }
    public String getId()           { return id; }

    // ===== Nested user object =====
    public static class AuthUser {
        @SerializedName("id")
        public String id;

        @SerializedName("email")
        public String email;

        @SerializedName("user_metadata")
        public UserMetadata userMetadata;

        public static class UserMetadata {
            @SerializedName("name")
            public String name;

            @SerializedName("phone")
            public String phone;

            @SerializedName("location")
            public String location;
        }
    }
}
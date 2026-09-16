package com.example.agrofastsolutions.auth;

import com.google.gson.annotations.SerializedName;

public class SignUpRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    // We also want to store additional user info in metadata
    @SerializedName("data")
    private UserMeta data;

    public SignUpRequest(String email, String password, String name, String phone, String location) {
        this.email = email;
        this.password = password;
        this.data = new UserMeta(name, phone, location);
    }

    public static class UserMeta {
        @SerializedName("name")
        public String name;

        @SerializedName("phone")
        public String phone;

        @SerializedName("location")
        public String location;

        public UserMeta(String name, String phone, String location) {
            this.name = name;
            this.phone = phone;
            this.location = location;
        }
    }
}
package com.example.agrofastsolutions.api;

import com.example.agrofastsolutions.auth.AuthResponse;
import com.example.agrofastsolutions.auth.LoginRequest;
import com.example.agrofastsolutions.auth.SignUpRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthApiService {

    // ==========================================
    // SIGN UP — create a new auth user
    // POST /auth/v1/signup
    // ==========================================
    @POST("signup")
    Call<AuthResponse> signUp(@Body SignUpRequest request);

    // ==========================================
    // LOGIN — exchange email + password for a session
    // POST /auth/v1/token?grant_type=password
    // ==========================================


    @POST("token")
    Call<AuthResponse> login(
            @Query("grant_type") String grantType,
            @Body LoginRequest request
    );

    // FORGOT PASSWORD — send reset email
    @POST("recover")
    Call<Void> recoverPassword(@Body java.util.Map<String, String> body);

    // ==========================================
    // REFRESH TOKEN
    // POST /auth/v1/token?grant_type=refresh_token
    // ==========================================
    @POST("token")
    Call<AuthResponse> refreshToken(
            @Query("grant_type") String grantType,
            @Body java.util.Map<String, String> body
    );

    // ==========================================
    // LOGOUT — invalidate the current session
    // POST /auth/v1/logout
    // ==========================================

    @retrofit2.http.PUT("user")
    Call<AuthResponse> updateUser(
            @retrofit2.http.Header("Authorization") String bearerToken,
            @retrofit2.http.Body java.util.Map<String, String> body
    );
    @POST("logout")
    Call<Void> logout(@Header("Authorization") String bearerToken);
}
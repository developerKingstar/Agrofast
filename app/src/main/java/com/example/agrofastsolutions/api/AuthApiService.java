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

    // ==========================================
    // LOGOUT — invalidate the current session
    // POST /auth/v1/logout
    // ==========================================
    @POST("logout")
    Call<Void> logout(@Header("Authorization") String bearerToken);
}
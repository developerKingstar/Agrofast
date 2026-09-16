package com.example.agrofastsolutions.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.agrofastsolutions.api.AuthApiService;
import com.example.agrofastsolutions.api.AuthClient;
import com.example.agrofastsolutions.util.DevLogger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * High-level manager for Supabase Auth.
 *
 * Activities call: signup(), login(), logout(), isLoggedIn()
 * This class handles the API calls and stores the session.
 */
public class SupabaseAuthManager {

    private static final String TAG = "SupabaseAuthManager";

    // SharedPreferences file name
    private static final String PREFS_NAME = "agrofast_prefs";

    // Keys we store in SharedPreferences
    private static final String KEY_USER_ID       = "user_id";
    private static final String KEY_USER_EMAIL    = "user_email";
    private static final String KEY_USER_NAME     = "user_name";
    private static final String KEY_ACCESS_TOKEN  = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private final AuthApiService authApi;
    private final SharedPreferences prefs;

    public SupabaseAuthManager(Context context) {
        authApi = AuthClient.getClient().create(AuthApiService.class);
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ==========================================
    // CALLBACK INTERFACE
    // ==========================================
    public interface AuthCallback {
        void onSuccess(String userId);
        void onError(String error);
    }

    // ==========================================
    // SIGN UP
    // ==========================================
    public void signUp(String email,
                       String password,
                       String name,
                       String phone,
                       String location,
                       AuthCallback callback) {

        Log.d(TAG, "signUp: " + email);

        SignUpRequest request = new SignUpRequest(email, password, name, phone, location);

        authApi.signUp(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse body = response.body();

                    // Supabase may return user info at top-level id OR under body.user.id
                    String userId = null;
                    String email = null;
                    String name = null;
                    String accessToken = null;
                    String refreshToken = null;

                    if (body.getUser() != null && body.getUser().id != null) {
                        userId = body.getUser().id;
                        email = body.getUser().email;
                        if (body.getUser().userMetadata != null) {
                            name = body.getUser().userMetadata.name;
                        }
                    } else if (body.getId() != null) {
                        userId = body.getId();
                    }

                    accessToken = body.getAccessToken();
                    refreshToken = body.getRefreshToken();

                    if (userId == null) {
                        Log.e(TAG, "signUp succeeded but no user id returned");
                        callback.onError("Signup succeeded but user ID missing");
                        return;
                    }

                    // Save session
                    saveSession(userId, email, name, accessToken, refreshToken);

                    Log.d(TAG, "signUp OK: user_id=" + userId);
                    callback.onSuccess(userId);
                } else {
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) errorBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    DevLogger.logError("Supabase signup", errorBody, null);
                    callback.onError(DevLogger.toUserMessage(errorBody));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                DevLogger.logError("Supabase auth network", t.getMessage(), t);
                callback.onError(DevLogger.toUserMessage("network"));
            }
        });
    }

    // ==========================================
    // LOGIN
    // ==========================================
    public void login(String email, String password, AuthCallback callback) {

        Log.d(TAG, "login: " + email);

        LoginRequest request = new LoginRequest(email, password);

        authApi.login("password", request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse body = response.body();

                    String userId = null;
                    String userEmail = null;
                    String userName = null;

                    if (body.getUser() != null) {
                        userId = body.getUser().id;
                        userEmail = body.getUser().email;
                        if (body.getUser().userMetadata != null) {
                            userName = body.getUser().userMetadata.name;
                        }
                    }

                    if (userId == null) {
                        Log.e(TAG, "login succeeded but no user id returned");
                        callback.onError("Login succeeded but user ID missing");
                        return;
                    }

                    saveSession(userId, userEmail, userName,
                            body.getAccessToken(), body.getRefreshToken());

                    Log.d(TAG, "login OK: user_id=" + userId);
                    callback.onSuccess(userId);
                } else {
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) errorBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    DevLogger.logError("Supabase login", errorBody, null);
                    callback.onError(DevLogger.toUserMessage(errorBody));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                DevLogger.logError("Supabase auth network", t.getMessage(), t);
                callback.onError(DevLogger.toUserMessage("network"));
            }
        });
    }

    // ==========================================
    // LOGOUT
    // ==========================================
    public void logout() {
        Log.d(TAG, "logout");
        // Clear local session first — don't wait for network
        prefs.edit().clear().apply();
    }

    // ==========================================
    // SESSION HELPERS
    // ==========================================

    private void saveSession(String userId, String email, String name,
                             String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_ID, userId);
        if (email != null)        editor.putString(KEY_USER_EMAIL, email);
        if (name != null) {
            editor.putString(KEY_USER_NAME, name);
            editor.putString("first_name", name);   // ✅ backward compat with old Dashboard
        }
        if (accessToken != null)  editor.putString(KEY_ACCESS_TOKEN, accessToken);
        if (refreshToken != null) editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    /** Is a user currently logged in? */
    public boolean isLoggedIn() {
        String userId = prefs.getString(KEY_USER_ID, "");
        return userId != null && !userId.isEmpty();
    }

    /** Current logged-in user id, or "" if none. */
    public String getCurrentUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    public String getCurrentUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getCurrentUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    // ==========================================
    // ERROR TRANSLATION
    // ==========================================
    private String friendlyError(String errorBody) {
        if (errorBody == null) return "Unknown error";
        if (errorBody.contains("User already registered")) return "This email is already registered.";
        if (errorBody.contains("Invalid login credentials")) return "Wrong email or password.";
        if (errorBody.contains("Password should be")) return "Password must be at least 6 characters.";
        if (errorBody.contains("Unable to validate email")) return "Please enter a valid email.";
        return "Something went wrong. Please try again.";
    }
}
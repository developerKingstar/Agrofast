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

// ✅ Fetch fresh profile from public.users so Dashboard reads real name + photo
//    (auth metadata can be stale if user edited profile after signup)
                    refreshProfileIntoPrefs(userId, callback);

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

    public void sendPasswordResetEmail(String email, AuthCallback callback) {

        Log.d(TAG, "sendPasswordResetEmail: " + email);

        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("email", email);

        authApi.recoverPassword(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("email_sent");
                } else {
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) errorBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    DevLogger.logError("sendPasswordResetEmail", errorBody, null);

                    if (response.code() == 429) {
                        callback.onError("Too many reset emails. Please wait an hour and try again.");
                    } else {
                        callback.onError(DevLogger.toUserMessage(errorBody));
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                DevLogger.logError("sendPasswordResetEmail", t.getMessage(), t);
                callback.onError("Network: " + t.getMessage());
            }
        });
    }

    // ==========================================
    // UPDATE PASSWORD WITH TOKEN (used by Reset Password flow)
    // ==========================================
    public void updatePasswordWithToken(String accessToken,
                                        String newPassword,
                                        AuthCallback callback) {

        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("password", newPassword);

        authApi.updateUser("Bearer " + accessToken, body)
                .enqueue(new retrofit2.Callback<AuthResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<AuthResponse> call,
                                           retrofit2.Response<AuthResponse> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "password reset successful");
                            callback.onSuccess("password_reset");
                        } else {
                            String errorBody = "Unknown error";
                            try {
                                if (response.errorBody() != null) errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("updatePasswordWithToken", errorBody, null);
                            callback.onError(DevLogger.toUserMessage(errorBody));
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<AuthResponse> call, Throwable t) {
                        DevLogger.logError("updatePasswordWithToken", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    // ==========================================
    // CHANGE PASSWORD (logged-in user, knows current password)
    //   1. Re-login with current password (fresh token)
    //   2. Update password
    // ==========================================
    public void changePassword(String currentPassword,
                               String newPassword,
                               AuthCallback callback) {

        String email = prefs.getString("user_email", "");
        if (email.isEmpty()) {
            callback.onError("You're not logged in.");
            return;
        }

        Log.d(TAG, "changePassword: re-authenticating " + email);

        LoginRequest reLogin = new LoginRequest(email, currentPassword);

        authApi.login("password", reLogin).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) errorBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    DevLogger.logError("changePassword (re-auth)", errorBody, null);
                    callback.onError("Current password is incorrect.");
                    return;
                }

                String freshToken = response.body().getAccessToken();
                if (freshToken == null || freshToken.isEmpty()) {
                    callback.onError("Could not verify your session. Please log in again.");
                    return;
                }

                // Use the reusable method we already built for reset
                updatePasswordWithToken(freshToken, newPassword, callback);
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                DevLogger.logError("changePassword (re-auth)", t.getMessage(), t);
                callback.onError("Network error. Please try again.");
            }
        });
    }

    // ==========================================
    // GET FRESH ACCESS TOKEN
    //   - If current token is still valid → return it
    //   - If expired → refresh it via refresh_token
    //   - If refresh fails → return null (caller should route to login)
    // ==========================================
    public void getFreshAccessToken(TokenCallback callback) {

        String currentToken = prefs.getString(KEY_ACCESS_TOKEN, "");
        String refreshToken = prefs.getString(KEY_REFRESH_TOKEN, "");

        // If no tokens at all → not logged in
        if (refreshToken == null || refreshToken.isEmpty()) {
            callback.onResult(null);
            return;
        }

        // Check if current token is still valid (with 5-min buffer)
        if (isTokenValid(currentToken)) {
            callback.onResult(currentToken);
            return;
        }

        // Token expired → refresh it
        Log.d(TAG, "Access token expired — refreshing...");

        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("refresh_token", refreshToken);

        authApi.refreshToken("refresh_token", body)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse r = response.body();

                            // Save new tokens
                            SharedPreferences.Editor editor = prefs.edit();
                            if (r.getAccessToken() != null) editor.putString(KEY_ACCESS_TOKEN, r.getAccessToken());
                            if (r.getRefreshToken() != null) editor.putString(KEY_REFRESH_TOKEN, r.getRefreshToken());
                            editor.apply();

                            Log.d(TAG, "Token refreshed successfully");
                            callback.onResult(r.getAccessToken());
                        } else {
                            Log.e(TAG, "Token refresh failed: HTTP " + response.code());
                            // Refresh failed → session is dead
                            prefs.edit().clear().apply();
                            callback.onResult(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        Log.e(TAG, "Token refresh network failure", t);
                        callback.onResult(null);
                    }
                });
    }

    /**
     * Decode JWT and check if it's still valid (with 5-min buffer).
     */
    private boolean isTokenValid(String token) {
        if (token == null || token.isEmpty()) return false;

        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return false;

            // Base64-decode the payload (2nd part)
            String payload = new String(android.util.Base64.decode(
                    parts[1], android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP));

            // Look for "exp":1234567890
            int expIdx = payload.indexOf("\"exp\":");
            if (expIdx < 0) return false;

            int start = expIdx + 6;
            int end = payload.indexOf(",", start);
            if (end < 0) end = payload.indexOf("}", start);

            long exp = Long.parseLong(payload.substring(start, end).trim());
            long now = System.currentTimeMillis() / 1000;

            // Valid if exp is at least 5 minutes in the future
            return exp > (now + 300);

        } catch (Exception e) {
            Log.e(TAG, "Could not parse token", e);
            return false;
        }
    }

    // Callback for token retrieval
    public interface TokenCallback {
        void onResult(String accessToken);   // null if not available
    }

    // ==========================================
    // LOGOUT
    // ==========================================
    public void logout() {
        Log.d(TAG, "logout");
        // Remove ONLY auth/session keys.
        // Keeps non-auth keys (like nothing, in our case) safe.
        // (profile_photo_url is removed here too — Login will re-write it fresh.)
        prefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_NAME)
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove("first_name")
                .remove("profile_photo_url")
                .remove("phone")
                .remove("location")
                .remove("email")
                .remove("is_logged_in")
                .apply();
    }

    // ==========================================
// REFRESH PROFILE INTO PREFS
//   Pulls name/phone/location/photo from public.users
//   and stores them so Dashboard + BottomSheet read fresh data.
// ==========================================
    private void refreshProfileIntoPrefs(String userId, AuthCallback callback) {

        com.example.agrofastsolutions.api.ApiService profileApi =
                com.example.agrofastsolutions.api.ApiClient.getClient()
                        .create(com.example.agrofastsolutions.api.ApiService.class);

        profileApi.getUserById("eq." + userId)
                .enqueue(new Callback<java.util.List<com.example.agrofastsolutions.NewUser>>() {
                    @Override
                    public void onResponse(Call<java.util.List<com.example.agrofastsolutions.NewUser>> call,
                                           Response<java.util.List<com.example.agrofastsolutions.NewUser>> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {

                            com.example.agrofastsolutions.NewUser u = response.body().get(0);

                            SharedPreferences.Editor e = prefs.edit();

                            if (u.getName() != null && !u.getName().isEmpty()) {
                                e.putString(KEY_USER_NAME, u.getName());
                                // first word for Dashboard typewriter
                                String first = u.getName().trim().split("\\s+")[0];
                                e.putString("first_name", first);
                            }
                            if (u.getEmail() != null)          e.putString(KEY_USER_EMAIL, u.getEmail());
                            if (u.getPhone() != null)          e.putString("phone", u.getPhone());
                            if (u.getLocation() != null)       e.putString("location", u.getLocation());
                            if (u.getProfilePhotoUrl() != null)
                                e.putString("profile_photo_url", u.getProfilePhotoUrl());

                            e.apply();

                            Log.d(TAG, "profile refreshed into prefs for " + userId);
                        } else {
                            Log.w(TAG, "profile refresh empty: HTTP " + response.code());
                        }

                        // Whatever happened, let login continue
                        callback.onSuccess(userId);
                    }

                    @Override
                    public void onFailure(Call<java.util.List<com.example.agrofastsolutions.NewUser>> call,
                                          Throwable t) {
                        Log.e(TAG, "profile refresh network fail", t);
                        // Don't block login — just continue
                        callback.onSuccess(userId);
                    }
                });
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
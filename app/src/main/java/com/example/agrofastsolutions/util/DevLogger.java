package com.example.agrofastsolutions.util;

import android.util.Log;

/**
 * DevLogger — the bridge between technical errors and human-friendly messages.
 *
 * USER sees: friendly string (via toUserMessage)
 * DEV sees:  full technical detail in Logcat, tag AGROFAST_DEV
 */
public class DevLogger {

    // Special tag so you can filter Logcat with: tag:AGROFAST_DEV
    public static final String TAG = "AGROFAST_DEV";

    // Toggle off for release builds to hide technical logs
    private static final boolean IS_DEV_MODE = true;

    // ==========================================
    // LOG A TECHNICAL ERROR (for the developer)
    // ==========================================
    public static void logError(String context, String technicalError, Throwable t) {
        if (!IS_DEV_MODE) return;

        Log.e(TAG, "══════════════════════════════════════════");
        Log.e(TAG, "❌ ERROR in: " + context);
        Log.e(TAG, "Message: " + technicalError);
        if (t != null) {
            Log.e(TAG, "Exception: " + t.getClass().getSimpleName());
            Log.e(TAG, "Stack:", t);
        }
        Log.e(TAG, "Time: " + new java.util.Date());
        Log.e(TAG, "══════════════════════════════════════════");
    }

    // ==========================================
    // CONVERT TECHNICAL ERROR → FRIENDLY MESSAGE
    // ==========================================
    public static String toUserMessage(String technicalError) {
        if (technicalError == null || technicalError.isEmpty()) {
            return "Something went wrong. Please try again.";
        }

        String lower = technicalError.toLowerCase();

        // Network issues
        if (lower.contains("network") || lower.contains("timeout")
                || lower.contains("unable to resolve host")
                || lower.contains("failed to connect")) {
            return "No internet connection. Please check your network and try again.";
        }

        // Auth issues
        if (lower.contains("401") || lower.contains("unauthorized")
                || lower.contains("jwt expired")) {
            return "Your session has expired. Please log in again.";
        }

        if (lower.contains("403") || lower.contains("forbidden")
                || lower.contains("permission denied")) {
            return "You don't have permission to do that.";
        }

        // Not found
        if (lower.contains("404") || lower.contains("not found")
                || lower.contains("does not exist")) {
            return "We couldn't find that. It may have been removed.";
        }

        // Bad request — usually our fault, not the user's
        if (lower.contains("400") || lower.contains("bad request")) {
            return "Could not complete that request. Please try again.";
        }

        // Duplicate — usually already exists
        if (lower.contains("409") || lower.contains("duplicate")
                || lower.contains("already exists")) {
            return "That already exists.";
        }

        // Rate limits
        if (lower.contains("429") || lower.contains("rate limit")) {
            return "Too many attempts. Please wait a moment and try again.";
        }

        // Server issues
        if (lower.contains("500") || lower.contains("502")
                || lower.contains("503") || lower.contains("server error")) {
            return "Our servers are busy right now. Please try again shortly.";
        }

        // Signup / login specific (Supabase Auth messages)
        if (lower.contains("user already registered")) {
            return "This email is already registered. Try logging in.";
        }
        if (lower.contains("invalid login credentials")) {
            return "Wrong email or password.";
        }
        if (lower.contains("password should be at least")) {
            return "Password must be at least 6 characters.";
        }
        if (lower.contains("unable to validate email")) {
            return "Please enter a valid email address.";
        }
        if (lower.contains("email not confirmed")) {
            return "Please confirm your email before logging in.";
        }

        // Fallback
        return "Something went wrong. Please try again.";
    }
}
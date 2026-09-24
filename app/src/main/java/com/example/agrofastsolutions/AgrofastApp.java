package com.example.agrofastsolutions;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Application class — applies the saved theme mode at app startup,
 * before any Activity is created.
 *
 * Registered in AndroidManifest.xml via android:name=".AgrofastApp"
 */
public class AgrofastApp extends Application {

    private static final String PREFS_NAME = "CustomizeSettings";
    private static final String KEY_THEME  = "theme_mode";

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String mode = prefs.getString(KEY_THEME, "light");

        if ("dark".equalsIgnoreCase(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
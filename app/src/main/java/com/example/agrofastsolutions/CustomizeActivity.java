package com.example.agrofastsolutions;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class CustomizeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "CustomizeSettings";
    private static final String KEY_THEME  = "theme_mode";   // "light" | "dark"

    private Spinner themeSpinner;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        themeSpinner = findViewById(R.id.SpinnerTheme);

        String[] modes = {"Light", "Dark"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                modes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(adapter);

        String saved = preferences.getString(KEY_THEME, "light");
        themeSpinner.setSelection("dark".equalsIgnoreCase(saved) ? 1 : 0);

        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (position == 1) ? "dark" : "light";

                String current = preferences.getString(KEY_THEME, "light");
                if (selected.equals(current)) return;

                preferences.edit().putString(KEY_THEME, selected).apply();

                if ("dark".equals(selected)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
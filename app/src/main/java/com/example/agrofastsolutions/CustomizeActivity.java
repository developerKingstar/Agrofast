package com.example.agrofastsolutions;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class CustomizeActivity extends AppCompatActivity {

    Spinner colorSpinner, languageSpinner;
    LinearLayout mainLayout;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadLanguage();

        setContentView(R.layout.activity_customize);

        colorSpinner = findViewById(R.id.SpinnerColor);
        languageSpinner = findViewById(R.id.SpinnerLanguage);
        mainLayout = findViewById(R.id.mainLayout);

        preferences = getSharedPreferences(
                "CustomizeSettings",
                MODE_PRIVATE
        );

        String[] colors = {"Blue", "Green", "Red", "Purple"};

        ArrayAdapter<String> colorAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colors);

        colorAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        colorSpinner.setAdapter(colorAdapter);

        String savedColor =
                preferences.getString("color", "Blue");

        int colorPosition = 0;

        for (int i = 0; i < colors.length; i++) {
            if (colors[i].equals(savedColor)) {
                colorPosition = i;
                break;
            }
        }

        colorSpinner.setSelection(colorPosition);

        colorSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        String selectedColor =
                                parent.getItemAtPosition(position)
                                        .toString();

                        preferences.edit()
                                .putString("color", selectedColor)
                                .apply();

                        changeAppColor(selectedColor);
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );

        String[] languages = {"English", "Swahili"};

        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);

        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        languageSpinner.setAdapter(languageAdapter);

        String savedLanguage = preferences.getString("language", "English");

        int languagePosition = 0;

        for (int i = 0; i < languages.length; i++) {
            if (languages[i].equals(savedLanguage)) {
                languagePosition = i;
                break;
            }
        }

        languageSpinner.setSelection(languagePosition);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                                      @Override
                                                      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                                          String selectedLanguage = parent.getItemAtPosition(position)
                                                                  .toString();

                                                          preferences.edit()
                                                                  .putString("language", selectedLanguage)
                                                                  .apply();

                                                          changeLanguage(selectedLanguage);
                                                      }

                                                      @Override
                                                      public void onNothingSelected(
                                                              AdapterView<?> parent) {
                                                      }
                                                  }
        );
    }

    private void changeAppColor(String color) {

        if (mainLayout == null) {
            return;
        }

        if (color.equals("Blue")) {

            mainLayout.setBackgroundColor(
                    Color.rgb(135, 195, 235)
            );

        } else if (color.equals("Green")) {

            mainLayout.setBackgroundColor(
                    Color.rgb(144, 238, 144)
            );

        } else if (color.equals("Red")) {

            mainLayout.setBackgroundColor(
                    Color.rgb(255, 150, 150)
            );

        } else if (color.equals("Purple")) {

            mainLayout.setBackgroundColor(
                    Color.rgb(200, 160, 240)
            );
        }
    }

    private void changeLanguage(String language) {

        if (language.equals("Swahili")) {
            setLocale("sw");
        } else {
            setLocale("en");
        }
    }

    private void setLocale(String languageCode) {

        Locale locale = new Locale(languageCode);

        Locale.setDefault(locale);

        Configuration configuration =
                new Configuration();

        configuration.setLocale(locale);

        getResources().updateConfiguration(
                configuration,
                getResources().getDisplayMetrics()
        );
    }

    private void loadLanguage() {

        SharedPreferences prefs =
                getSharedPreferences("CustomizeSettings", MODE_PRIVATE);

        String language =
                prefs.getString("language", "English");

        String languageCode;

        if (language.equals("Swahili")) {
            languageCode = "sw";
        } else {
            languageCode = "en";
        }

        Locale locale =
                new Locale(languageCode);

        Locale.setDefault(locale);

        Configuration configuration =
                new Configuration();

        configuration.setLocale(locale);

        getResources().updateConfiguration(
                configuration,
                getResources().getDisplayMetrics()
        );
    }
}

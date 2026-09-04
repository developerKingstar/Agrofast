package com.example.agrofastsolutions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.Calendar;

/**
 * DASHBOARD SCREEN -- the home screen after login.
 * Shows the custom hamburger menu, profile icon, a looping typewriter
 * greeting, the price ticker, and the news feed.
 */
public class DashBoardActivity extends AppCompatActivity {

    // Same preference file + key that SignUpActivity should write the
    // first name into when the account is created. See note below.
    private static final String PREFS_NAME = "agrofast_prefs";
    private static final String KEY_FIRST_NAME = "first_name";

    private DrawerLayout drawerLayout;
    private TextView tvTicker;
    private TextView tvGreeting;

    private final Handler typewriterHandler = new Handler(Looper.getMainLooper());

    private static final int TYPING_SPEED_MS = 90;
    private static final int DELETING_SPEED_MS = 45;
    private static final int PAUSE_AFTER_TYPING_MS = 1300;
    private static final int PAUSE_AFTER_DELETING_MS = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupDrawerItem(R.id.nav_buy,       () -> {  Intent intent = new Intent(this, BuyActivity.class);
            startActivity(intent);
        });

        setupDrawerItem(R.id.nav_sell,      () -> { Intent intent = new Intent(this, SellActivity.class);
            startActivity(intent);
        });

        setupDrawerItem(R.id.nav_market,    () -> { /* TODO: open MarketActivity */ });
        setupDrawerItem(R.id.nav_myspace,   () -> { /* TODO: open MySpaceActivity */ });
        setupDrawerItem(R.id.nav_customize, () -> { Intent intent = new Intent(this, CustomizeActivity.class);
            startActivity(intent);
        });

        setupDrawerItem(R.id.nav_guide,     () -> { /* TODO: open UserGuideActivity */ });
        setupDrawerItem(R.id.nav_logout,    () -> {  Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        });

        findViewById(R.id.iv_profile).setOnClickListener(v ->
                new ProfileBottomSheet().show(getSupportFragmentManager(), "profile_sheet"));


        tvTicker = findViewById(R.id.tv_ticker);
        tvTicker.setText("Maize: high demand, 850 TZS/kg (Dodoma)  |  Rice: 1200 TZS/kg  |  World coffee: $2.10/lb");
        tvTicker.setSelected(true);

        // --- Typewriter greeting, now looping and using the real signed-up name ---
        tvGreeting = findViewById(R.id.tv_greeting);
        String firstName = loadFirstName();
        runGreetingCycle(tvGreeting, firstName);

        // TODO: set up rv_news (RecyclerView) with a NewsAdapter once the news API call is wired up
    }

    private void setupDrawerItem(int viewId, Runnable action) {
        View row = findViewById(viewId);
        row.setOnClickListener(v -> {
            action.run();
            drawerLayout.closeDrawer(GravityCompat.START);
        });
    }

    /**
     * Reads the first name saved during Sign Up.
     *
     * IMPORTANT -- for this to actually show a real name, your SignUpActivity
     * needs to save it when the account is created, e.g.:
     *
     *   SharedPreferences prefs = getSharedPreferences("agrofast_prefs", MODE_PRIVATE);
     *   prefs.edit().putString("first_name", firstNameFromInputField).apply();
     *
     * Until that's wired up, this falls back to "there" so the greeting
     * still reads naturally ("Good Afternoon there").
     */
    private String loadFirstName() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String fullOrFirstName = prefs.getString(KEY_FIRST_NAME, null);

        if (fullOrFirstName == null || fullOrFirstName.trim().isEmpty()) {
            return "Kingstar";
        }

        // In case a full name ever gets saved by mistake, only use the first word.
        String[] parts = fullOrFirstName.trim().split("\\s+");
        return parts[0];
    }

    // ============================================================
    //  TYPEWRITER GREETING LOGIC (now loops forever)
    //  Flow: type greeting -> pause -> delete -> pause ->
    //        type question -> pause -> delete -> pause -> repeat
    // ============================================================

    private void runGreetingCycle(TextView tv, String name) {
        String greeting = getTimeBasedGreeting() + " " + name;
        String question = "What shall we do today?";

        typeText(tv, greeting, () ->
                typewriterHandler.postDelayed(() ->
                                deleteText(tv, () ->
                                        typewriterHandler.postDelayed(() ->
                                                        typeText(tv, question, () ->
                                                                typewriterHandler.postDelayed(() ->
                                                                                deleteText(tv, () ->
                                                                                        typewriterHandler.postDelayed(() ->
                                                                                                        runGreetingCycle(tv, name), // loop back to the start
                                                                                                PAUSE_AFTER_DELETING_MS)
                                                                                ),
                                                                        PAUSE_AFTER_TYPING_MS)
                                                        ),
                                                PAUSE_AFTER_DELETING_MS)
                                ),
                        PAUSE_AFTER_TYPING_MS)
        );
    }

    private void typeText(TextView tv, String fullText, Runnable onDone) {
        tv.setText("");
        final int[] index = {0};
        Runnable typer = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= fullText.length()) {
                    tv.setText(fullText.substring(0, index[0]));
                    index[0]++;
                    typewriterHandler.postDelayed(this, TYPING_SPEED_MS);
                } else if (onDone != null) {
                    onDone.run();
                }
            }
        };
        typewriterHandler.post(typer);
    }

    private void deleteText(TextView tv, Runnable onDone) {
        final String current = tv.getText().toString();
        final int[] index = {current.length()};
        Runnable deleter = new Runnable() {
            @Override
            public void run() {
                if (index[0] >= 0) {
                    tv.setText(current.substring(0, index[0]));
                    index[0]--;
                    typewriterHandler.postDelayed(this, DELETING_SPEED_MS);
                } else if (onDone != null) {
                    onDone.run();
                }
            }
        };
        typewriterHandler.post(deleter);
    }

    private String getTimeBasedGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            return "Good Morning,";
        } else if (hour < 17) {
            return "Good Afternoon,";
        } else {
            return "Good Evening,";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stops the loop for good when the user leaves this screen --
        // otherwise it would keep typing forever in the background.
        typewriterHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
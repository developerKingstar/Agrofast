package com.example.agrofastsolutions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agrofastsolutions.auth.SupabaseAuthManager;
import com.example.agrofastsolutions.news.NewsAdapter;
import com.example.agrofastsolutions.news.NewsDataResponse;
import com.example.agrofastsolutions.news.NewsRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * DASHBOARD SCREEN -- the home screen after login.
 * Shows the custom hamburger menu, profile icon, a looping typewriter
 * greeting, the price ticker, and the agricultural news feed.
 */
public class DashBoardActivity extends AppCompatActivity {

    private String currentDisplayName = null;
    private static final String PREFS_NAME = "agrofast_prefs";
    private static final String KEY_FIRST_NAME = "first_name";

    private DrawerLayout drawerLayout;
    private TextView tvTicker;
    private TextView tvGreeting;

    // ---------- News ----------
    private NewsRepository newsRepo;
    private NewsAdapter newsAdapter;
    private final List<NewsDataResponse.NewsArticle> newsArticles = new ArrayList<>();
    private boolean newsLoaded = false;

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

        setupDrawerItem(R.id.nav_buy, () -> {
            Intent intent = new Intent(this, BuyActivity.class);
            startActivity(intent);
        });

        setupDrawerItem(R.id.nav_sell, () -> {
            Intent intent = new Intent(this, Create_listing.class);
            startActivity(intent);
        });

        setupDrawerItem(R.id.nav_market, () -> {
            Intent intent = new Intent(this, MarketActivity.class);
            startActivity(intent);
        });

        setupDrawerItem(R.id.nav_myspace, () -> {
            Intent intent = new Intent(this, Show_order.class);
            startActivity(intent);
        });

        setupDrawerItem(R.id.nav_favourites, () -> {
            Intent intent = new Intent(this, FavouritesActivity.class);
            startActivity(intent);
        });

        setupDrawerItem(R.id.nav_customize, () -> {
            Intent intent = new Intent(this, CustomizeActivity.class);
            startActivity(intent);
        });

        setupDrawerItem(R.id.nav_guide, () -> {
            Intent intent = new Intent(this, UserGuideActivity.class);
            startActivity(intent);
        });

        setupDrawerItem(R.id.nav_logout, this::performLogout);

        findViewById(R.id.iv_profile).setOnClickListener(v ->
                new ProfileBottomSheet().show(getSupportFragmentManager(), "profile_sheet"));

        tvTicker = findViewById(R.id.tv_ticker);
        tvTicker.setText("Maize: high demand, 850 TZS/kg (Dodoma)  |  Rice: 1200 TZS/kg  |  World coffee: $2.10/lb");
        tvTicker.setSelected(true);

        // --- Typewriter greeting: start is handled in onResume() ---
        tvGreeting = findViewById(R.id.tv_greeting);

        // ---------- News section ----------
        setupNewsSection();
    }

    // ==========================================
    // NEWS SECTION — RecyclerView + NewsRepository
    // ==========================================
    private void setupNewsSection() {
        RecyclerView rvNews = findViewById(R.id.rv_news);

        // Horizontal scrolling — fits the green card
        rvNews.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        newsAdapter = new NewsAdapter(newsArticles);
        rvNews.setAdapter(newsAdapter);

        newsRepo = new NewsRepository();
        loadAgriculturalNews();
    }

    /**
     * Fetch agricultural news.
     * Strategy: try Tanzania first; if empty/error, fall back to global.
     */
    private void loadAgriculturalNews() {
        newsRepo.fetchAgricultureNews("tz", new NewsRepository.NewsCallback() {
            @Override
            public void onSuccess(List<NewsDataResponse.NewsArticle> result) {
                if (isFinishing() || isDestroyed()) return;

                if (result == null || result.isEmpty()) {
                    // Tanzania returned nothing — fall back to global
                    loadGlobalAgriculturalNews();
                    return;
                }

                newsArticles.clear();
                newsArticles.addAll(result);
                newsAdapter.notifyDataSetChanged();
                newsLoaded = true;
            }

            @Override
            public void onError(String error) {
                if (isFinishing() || isDestroyed()) return;
                // Fall back to global agriculture news
                loadGlobalAgriculturalNews();
            }
        });
    }

    private void loadGlobalAgriculturalNews() {
        newsRepo.fetchAgricultureNews(null, new NewsRepository.NewsCallback() {
            @Override
            public void onSuccess(List<NewsDataResponse.NewsArticle> result) {
                if (isFinishing() || isDestroyed()) return;

                if (result == null || result.isEmpty()) {
                    Toast.makeText(DashBoardActivity.this,
                            "No agriculture news right now",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                newsArticles.clear();
                newsArticles.addAll(result);
                newsAdapter.notifyDataSetChanged();
                newsLoaded = true;
            }

            @Override
            public void onError(String error) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(DashBoardActivity.this,
                        "News unavailable: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==========================================
    // LIFECYCLE
    // ==========================================
    @Override
    protected void onResume() {
        super.onResume();

        // Check if the stored name changed (e.g., user edited profile)
        String latestName = loadFirstName();

        if (currentDisplayName == null) {
            currentDisplayName = latestName;
            runGreetingCycle(tvGreeting, currentDisplayName);
        } else if (!currentDisplayName.equals(latestName)) {
            currentDisplayName = latestName;
            typewriterHandler.removeCallbacksAndMessages(null);
            runGreetingCycle(tvGreeting, currentDisplayName);
        }

        // ✅ Load (or refresh) the user's profile photo in the toolbar
        loadDashboardAvatar();

        // Refresh news if it failed to load the first time
        if (!newsLoaded && newsRepo != null) {
            loadAgriculturalNews();
        }
    }

    // ==========================================
    // DASHBOARD AVATAR
    // ==========================================
    private void loadDashboardAvatar() {
        ImageView ivProfile = findViewById(R.id.iv_profile);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String photoUrl = prefs.getString("profile_photo_url", "");

        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_account)
                    .circleCrop()
                    .into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.ic_account);
        }
    }

    // ==========================================
    // LOGOUT
    // ==========================================
    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log out?")
                .setMessage("You'll need to log in again to use Agrofast.")
                .setPositiveButton("Log out", (dialog, which) -> {

                    SupabaseAuthManager auth = new SupabaseAuthManager(this);
                    auth.logout();

                    getSharedPreferences("agrofast_prefs", MODE_PRIVATE)
                            .edit()
                            .remove("is_logged_in")
                            .remove("first_name")
                            .remove("email")
                            .remove("phone")
                            .remove("location")
                            .apply();

                    Intent intent = new Intent(this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finishAffinity();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupDrawerItem(int viewId, Runnable action) {
        View row = findViewById(viewId);
        row.setOnClickListener(v -> {
            action.run();
            drawerLayout.closeDrawer(GravityCompat.START);
        });
    }

    // ==========================================
    // FIRST NAME
    // ==========================================
    private String loadFirstName() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String fullOrFirstName = prefs.getString(KEY_FIRST_NAME, null);

        if (fullOrFirstName == null || fullOrFirstName.trim().isEmpty()) {
            return "user";
        }

        String[] parts = fullOrFirstName.trim().split("\\s+");
        return parts[0];
    }

    // ============================================================
    //  TYPEWRITER GREETING LOGIC
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
                                                                                                        runGreetingCycle(tv, name),
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
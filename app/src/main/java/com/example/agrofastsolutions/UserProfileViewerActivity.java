package com.example.agrofastsolutions;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.agrofastsolutions.repository.AgrofastRepository;
import com.example.agrofastsolutions.util.DevLogger;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Read-only profile viewer.
 * Shows another user's photo, name, location, rating, review count,
 * and their received reviews.
 *
 * Also has a toggle button: "Add to Favourites" ↔ "Remove from Favourites".
 */
public class UserProfileViewerActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "extra_user_id";

    private static final String TAG = "UserProfileViewer";

    private ImageView ivPhoto;
    private TextView tvName, tvLocation, tvRating, tvReviewCount, tvMemberSince;
    private TextView tvEmptyReviews;
    private MaterialButton btnToggleFavourite;
    private ProgressBar progressBar;
    private RecyclerView rvReviews;

    private ReviewAdapter reviewAdapter;
    private final List<Review> reviews = new ArrayList<>();

    private AgrofastRepository repository;

    private String targetUserId;
    private boolean isFavourited = false;
    private boolean isSelf = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_viewer);

        repository = new AgrofastRepository(this);

        targetUserId = getIntent().getStringExtra(EXTRA_USER_ID);
        if (targetUserId == null || targetUserId.isEmpty()) {
            Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if this is me
        android.content.SharedPreferences prefs =
                getSharedPreferences("agrofast_prefs", MODE_PRIVATE);
        String myId = prefs.getString("user_id", "");
        isSelf = myId != null && myId.equals(targetUserId);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarUserProfile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Bind views
        ivPhoto             = findViewById(R.id.ivUserPhoto);
        tvName              = findViewById(R.id.tvUserName);
        tvLocation          = findViewById(R.id.tvUserLocation);
        tvRating            = findViewById(R.id.tvUserRating);
        tvReviewCount       = findViewById(R.id.tvUserReviewCount);
        tvMemberSince       = findViewById(R.id.tvUserMemberSince);
        tvEmptyReviews      = findViewById(R.id.tvEmptyReviews);
        btnToggleFavourite  = findViewById(R.id.btnToggleFavourite);
        progressBar         = findViewById(R.id.progressUserProfile);
        rvReviews           = findViewById(R.id.rvUserReviews);

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviews);
        rvReviews.setAdapter(reviewAdapter);

        // Hide the favourite button if viewing my own profile
        if (isSelf) {
            btnToggleFavourite.setVisibility(View.GONE);
        }

        btnToggleFavourite.setOnClickListener(v -> toggleFavourite());

        loadUserProfile();
        loadReviews();
        if (!isSelf) checkIfFavourited();
    }

    // ==========================================
    // LOAD PROFILE
    // ==========================================
    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);

        repository.getUserById(targetUserId, new AgrofastRepository.DataCallback<NewUser>() {
            @Override
            public void onSuccess(NewUser user) {
                progressBar.setVisibility(View.GONE);
                populate(user);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                DevLogger.logError(TAG + " load", error, null);
                Toast.makeText(UserProfileViewerActivity.this,
                        DevLogger.toUserMessage(error), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populate(NewUser user) {
        if (user == null) return;

        tvName.setText((user.getName() == null || user.getName().isEmpty())
                ? "User" : user.getName());
        tvLocation.setText((user.getLocation() == null || user.getLocation().isEmpty())
                ? "—" : user.getLocation());

        tvRating.setText(String.format(Locale.US, "★ %.1f", user.getRatingAvg()));

        int count = user.getReviewCount();
        tvReviewCount.setText("(" + count + " review" + (count == 1 ? "" : "s") + ")");

        tvMemberSince.setText("Member since " + formatDate(user.getCreatedAt()));

        String photo = user.getProfilePhotoUrl();
        if (photo != null && !photo.isEmpty()) {
            Glide.with(this)
                    .load(photo)
                    .placeholder(R.drawable.ic_account)
                    .circleCrop()
                    .into(ivPhoto);
        } else {
            ivPhoto.setImageResource(R.drawable.ic_account);
        }
    }

    // ==========================================
    // LOAD REVIEWS
    // ==========================================
    private void loadReviews() {
        repository.getReviewsForUser(targetUserId,
                new AgrofastRepository.DataCallback<List<Review>>() {
                    @Override
                    public void onSuccess(List<Review> list) {
                        reviews.clear();
                        if (list != null) reviews.addAll(list);
                        reviewAdapter.notifyDataSetChanged();

                        tvEmptyReviews.setVisibility(
                                reviews.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onError(String error) {
                        DevLogger.logError(TAG + " reviews", error, null);
                        tvEmptyReviews.setVisibility(View.VISIBLE);
                        tvEmptyReviews.setText("Could not load reviews");
                    }
                });
    }

    // ==========================================
    // FAVOURITE TOGGLE
    // ==========================================
    private void checkIfFavourited() {
        repository.checkFavourite(targetUserId,
                new AgrofastRepository.DataCallback<List<Favourite>>() {
                    @Override
                    public void onSuccess(List<Favourite> list) {
                        isFavourited = list != null && !list.isEmpty();
                        updateFavouriteButton();
                    }

                    @Override
                    public void onError(String error) {
                        // Non-fatal; just default to "Add"
                        isFavourited = false;
                        updateFavouriteButton();
                    }
                });
    }

    private void updateFavouriteButton() {

        // colorOnPrimary: defined by Material 3 theme
        android.util.TypedValue onPrimaryTv = new android.util.TypedValue();
        getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorOnPrimary,
                onPrimaryTv,
                true
        );

        // colorPrimary: defined by AppCompat (backported from framework)
        android.util.TypedValue primaryTv = new android.util.TypedValue();
        getTheme().resolveAttribute(
                androidx.appcompat.R.attr.colorPrimary,
                primaryTv,
                true
        );

        int bgColor   = onPrimaryTv.data;   // white (light) / black (dark)
        int textColor = primaryTv.data;     // green (light) / yellowish-green (dark)

        if (isFavourited) {
            btnToggleFavourite.setText("♥ Remove from Favourites");
        } else {
            btnToggleFavourite.setText("♡ Add to Favourites");
        }

        btnToggleFavourite.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(bgColor));
        btnToggleFavourite.setTextColor(textColor);
    }

    private void toggleFavourite() {
        btnToggleFavourite.setEnabled(false);

        if (isFavourited) {
            repository.removeFavourite(targetUserId,
                    new AgrofastRepository.DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            btnToggleFavourite.setEnabled(true);
                            isFavourited = false;
                            updateFavouriteButton();
                            Toast.makeText(UserProfileViewerActivity.this,
                                    "Removed from favourites", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            btnToggleFavourite.setEnabled(true);
                            DevLogger.logError(TAG + " remove fav", error, null);
                            Toast.makeText(UserProfileViewerActivity.this,
                                    DevLogger.toUserMessage(error), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            repository.addFavourite(targetUserId,
                    new AgrofastRepository.DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            btnToggleFavourite.setEnabled(true);
                            isFavourited = true;
                            updateFavouriteButton();
                            Toast.makeText(UserProfileViewerActivity.this,
                                    "Added to favourites ❤️", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            btnToggleFavourite.setEnabled(true);
                            DevLogger.logError(TAG + " add fav", error, null);
                            Toast.makeText(UserProfileViewerActivity.this,
                                    DevLogger.toUserMessage(error), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    // ==========================================
    // HELPERS
    // ==========================================
    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "—";
        try {
            java.text.SimpleDateFormat input =
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            java.util.Date date = input.parse(isoDate.substring(0, 19));
            if (date == null) return "—";
            java.text.SimpleDateFormat output =
                    new java.text.SimpleDateFormat("MMM yyyy", Locale.US);
            return output.format(date);
        } catch (Exception e) {
            return "—";
        }
    }
}
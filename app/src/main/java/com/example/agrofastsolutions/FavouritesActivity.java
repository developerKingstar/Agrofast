package com.example.agrofastsolutions;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agrofastsolutions.repository.AgrofastRepository;
import com.example.agrofastsolutions.util.DevLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Favourites screen.
 * Two modes:
 *   - Default: lists my favourites
 *   - Search:  when the user types in the search bar, shows matching users
 */
public class FavouritesActivity extends AppCompatActivity {

    private static final String TAG = "FavouritesActivity";

    private RecyclerView rv;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvSectionLabel;
    private EditText etSearch;

    private FavouriteAdapter favAdapter;
    private SearchUserAdapter searchAdapter;

    private final List<Favourite> favourites = new ArrayList<>();
    private final List<NewUser> searchResults = new ArrayList<>();

    private AgrofastRepository repository;

    private boolean showingSearch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        repository = new AgrofastRepository(this);

        Toolbar toolbar = findViewById(R.id.toolbarFavourites);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rv             = findViewById(R.id.rvFavourites);
        progressBar    = findViewById(R.id.progressFavourites);
        tvEmpty        = findViewById(R.id.tvEmptyFavourites);
        tvSectionLabel = findViewById(R.id.tvSectionLabel);
        etSearch       = findViewById(R.id.etSearchUsers);

        rv.setLayoutManager(new LinearLayoutManager(this));

        // Two adapters — swap when mode toggles
        favAdapter = new FavouriteAdapter(favourites, new FavouriteAdapter.OnFavouriteClickListener() {
            @Override
            public void onFavouriteClick(Favourite favourite) {
                openUserProfile(favourite.getFavoritedUserId());
            }

            @Override
            public void onRemoveClick(Favourite favourite) {
                confirmRemove(favourite);
            }
        });

        searchAdapter = new SearchUserAdapter(searchResults, user ->
                openUserProfile(user.getUserId()));

        // Start in favourites mode
        rv.setAdapter(favAdapter);

        // Search listener — debounce simple (fire on each change; Retrofit is fast enough)
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s.toString().trim();
                if (q.isEmpty()) {
                    switchToFavouritesMode();
                } else {
                    switchToSearchMode(q);
                }
            }
        });

        loadFavourites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh favourites when returning from profile viewer
        if (!showingSearch) loadFavourites();
    }

    // ==========================================
    // FAVOURITES MODE
    // ==========================================
    private void switchToFavouritesMode() {
        showingSearch = false;
        rv.setAdapter(favAdapter);
        tvSectionLabel.setText("My Favourites");
        updateEmptyState();
    }

    private void loadFavourites() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        repository.getFavourites(new AgrofastRepository.DataCallback<List<Favourite>>() {
            @Override
            public void onSuccess(List<Favourite> list) {
                progressBar.setVisibility(View.GONE);
                favourites.clear();
                if (list != null) favourites.addAll(list);
                favAdapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                DevLogger.logError(TAG + " load", error, null);
                Toast.makeText(FavouritesActivity.this,
                        DevLogger.toUserMessage(error), Toast.LENGTH_LONG).show();
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        boolean empty = showingSearch
                ? searchResults.isEmpty()
                : favourites.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);

        if (showingSearch && searchResults.isEmpty()) {
            tvEmpty.setText("No users found");
        } else if (empty) {
            tvEmpty.setText("No favourites yet.\nSearch above to add people.");
        }
    }

    // ==========================================
    // SEARCH MODE
    // ==========================================
    private void switchToSearchMode(String query) {
        showingSearch = true;
        rv.setAdapter(searchAdapter);
        tvSectionLabel.setText("Search Results");
        tvEmpty.setVisibility(View.GONE);

        repository.searchUsers(query, new AgrofastRepository.DataCallback<List<NewUser>>() {
            @Override
            public void onSuccess(List<NewUser> list) {
                searchResults.clear();
                if (list != null) searchResults.addAll(list);
                searchAdapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onError(String error) {
                DevLogger.logError(TAG + " search", error, null);
                tvEmpty.setText(DevLogger.toUserMessage(error));
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    // ==========================================
    // REMOVE FAVOURITE — confirmation
    // ==========================================
    private void confirmRemove(Favourite favourite) {
        String name = favourite.getFavoriteName() != null
                ? favourite.getFavoriteName() : "this user";

        new AlertDialog.Builder(this)
                .setTitle("Remove favourite?")
                .setMessage("Remove " + name + " from your favourites?")
                .setPositiveButton("Remove", (d, w) -> doRemove(favourite))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void doRemove(Favourite favourite) {
        progressBar.setVisibility(View.VISIBLE);

        repository.removeFavourite(favourite.getFavoritedUserId(),
                new AgrofastRepository.DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressBar.setVisibility(View.GONE);
                        favourites.remove(favourite);
                        favAdapter.notifyDataSetChanged();
                        Toast.makeText(FavouritesActivity.this,
                                "Removed", Toast.LENGTH_SHORT).show();
                        updateEmptyState();
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        DevLogger.logError(TAG + " remove", error, null);
                        Toast.makeText(FavouritesActivity.this,
                                DevLogger.toUserMessage(error), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ==========================================
    // OPEN PROFILE VIEWER
    // ==========================================
    private void openUserProfile(String userId) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Unknown user", Toast.LENGTH_SHORT).show();
            return;
        }
        android.content.Intent i = new android.content.Intent(this, UserProfileViewerActivity.class);
        i.putExtra(UserProfileViewerActivity.EXTRA_USER_ID, userId);
        startActivity(i);
    }
}
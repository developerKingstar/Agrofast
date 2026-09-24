package com.example.agrofastsolutions;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.agrofastsolutions.repository.AgrofastRepository;
import com.example.agrofastsolutions.util.DevLogger;

import java.util.ArrayList;
import java.util.List;

public class MarketActivity extends AppCompatActivity {

    private static final String TAG = "MarketActivity";

    private RecyclerView recyclerView;
    private MarketPriceAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private EditText etSearch;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private AgrofastRepository repository;

    // Debounce search so we don't hammer Supabase on every keystroke
    private final android.os.Handler searchHandler =
            new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        repository = new AgrofastRepository(this);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarMarket);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Bind views
        recyclerView  = findViewById(R.id.recyclerView);
        swipeRefresh  = findViewById(R.id.swipeRefreshLayout);
        etSearch      = findViewById(R.id.etSearch);
        progressBar   = findViewById(R.id.progressMarket);
        tvEmpty       = findViewById(R.id.tvEmptyMarket);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Resolve ?attr/colorOnPrimary from the current theme
        // (white in light mode on green card, black in dark mode on yellowish-green card)

        android.util.TypedValue tv = new android.util.TypedValue();
        getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorOnPrimary,
                tv,
                true
        );
        int onPrimaryColor = tv.data;

        adapter = new MarketPriceAdapter(new ArrayList<>(), onPrimaryColor);

        recyclerView.setAdapter(adapter);

        swipeRefresh.setColorSchemeColors(
                ContextCompat.getColor(this, android.R.color.holo_green_dark));
        swipeRefresh.setOnRefreshListener(this::loadPrices);

        // Debounced search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                String q = s.toString().trim();
                searchRunnable = () -> runSearch(q);
                searchHandler.postDelayed(searchRunnable, 350);  // 350ms debounce
            }
        });

        loadPrices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
    }

    // ==========================================
    // LOAD — all prices
    // ==========================================
    private void loadPrices() {
        swipeRefresh.setRefreshing(true);
        tvEmpty.setVisibility(View.GONE);

        repository.getMarketPrices(new AgrofastRepository.DataCallback<List<MarketPrice>>() {
            @Override
            public void onSuccess(List<MarketPrice> list) {
                swipeRefresh.setRefreshing(false);
                adapter.setData(list);
                updateEmptyState(list);
            }

            @Override
            public void onError(String error) {
                swipeRefresh.setRefreshing(false);
                DevLogger.logError(TAG + " load", error, null);
                Toast.makeText(MarketActivity.this,
                        DevLogger.toUserMessage(error), Toast.LENGTH_LONG).show();
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText(DevLogger.toUserMessage(error));
            }
        });
    }

    // ==========================================
    // SEARCH — debounced
    // ==========================================
    private void runSearch(String query) {
        if (query.isEmpty()) {
            loadPrices();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        repository.searchMarketPrices(query,
                new AgrofastRepository.DataCallback<List<MarketPrice>>() {
                    @Override
                    public void onSuccess(List<MarketPrice> list) {
                        progressBar.setVisibility(View.GONE);
                        adapter.setData(list);
                        updateEmptyState(list);
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        DevLogger.logError(TAG + " search", error, null);
                        Toast.makeText(MarketActivity.this,
                                DevLogger.toUserMessage(error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateEmptyState(List<MarketPrice> list) {
        boolean empty = list == null || list.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty) tvEmpty.setText("No market prices found");
    }
}
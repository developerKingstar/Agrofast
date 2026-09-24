package com.example.agrofastsolutions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agrofastsolutions.repository.AgrofastRepository;
import com.example.agrofastsolutions.util.DevLogger;

import java.util.ArrayList;
import java.util.List;

public class ListingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView tvTitle;
    private ImageButton btnBack;

    private ListingAdapter adapter;
    private final List<Listing> listingList = new ArrayList<>();
    private AgrofastRepository repository;

    private String cropQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listings);

        if (getIntent() != null) {
            cropQuery = getIntent().getStringExtra("CROP");
            if (cropQuery == null) cropQuery = "";
        }

        recyclerView = findViewById(R.id.recyclerViewListings);
        progressBar = findViewById(R.id.progressBarListings);
        tvEmpty = findViewById(R.id.tvEmptyListings);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        if (!cropQuery.trim().isEmpty()) {
            tvTitle.setText("Results for \"" + cropQuery.trim() + "\"");
        } else {
            tvTitle.setText("Available Listings");
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ListingAdapter(listingList, this::openListingDetails);
        recyclerView.setAdapter(adapter);

        repository = new AgrofastRepository(this);

        loadListings();
    }

    private void loadListings() {
        showLoading();

        AgrofastRepository.DataCallback<List<Listing>> callback =
                new AgrofastRepository.DataCallback<List<Listing>>() {
                    @Override
                    public void onSuccess(List<Listing> data) {
                        hideLoading();
                        listingList.clear();
                        if (data != null) listingList.addAll(data);
                        adapter.notifyDataSetChanged();

                        if (listingList.isEmpty()) {
                            showEmpty("No listings found matching \"" + cropQuery + "\"");
                        } else {
                            hideEmpty();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        hideLoading();

                        DevLogger.logError("ListingsActivity load", error, null);

                        String friendly = DevLogger.toUserMessage(error);
                        showEmpty(friendly);
                        Toast.makeText(ListingsActivity.this, friendly, Toast.LENGTH_SHORT).show();
                    }
                };

        if (cropQuery.trim().isEmpty()) {
            repository.getAllListings(callback);
        } else {
            repository.searchListings(cropQuery, callback);
        }
    }

    private void openListingDetails(Listing listing) {
        Intent intent = new Intent(ListingsActivity.this, ListingDetailsActivity.class);
        intent.putExtra("LISTING_ID",   listing.getListingId());
        intent.putExtra("CROP",         listing.getCropType());
        intent.putExtra("SELLER_ID",    listing.getSellerId());
        intent.putExtra("SELLER_NAME",  listing.getSellerName());
        intent.putExtra("QUANTITY",     listing.getQuantityAvailable());
        intent.putExtra("UNIT",         listing.getUnit());
        intent.putExtra("PRICE",        listing.getAskingPrice());
        intent.putExtra("LOCATION",     listing.getLocation());

        // ✅ NEW — pass the first photo URL (or empty string)
        String photoUrl = listing.getFirstPhotoUrl();
        intent.putExtra("PHOTO_URL", photoUrl != null ? photoUrl : "");
        startActivity(intent);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmpty(String message) {
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmpty() {
        tvEmpty.setVisibility(View.GONE);
    }
}
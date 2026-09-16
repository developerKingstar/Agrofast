package com.example.agrofastsolutions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class MySell_Listings extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private MyListingAdapter adapter;
    private final List<Listing> listingList = new ArrayList<>();
    private AgrofastRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_sell_listings);

        recyclerView = findViewById(R.id.recyclerViewMyListings);
        progressBar  = findViewById(R.id.progressBarMyListings);
        tvEmpty      = findViewById(R.id.tvEmptyMyListings);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyListingAdapter(listingList, this::openListingDetails);
        recyclerView.setAdapter(adapter);

        repository = new AgrofastRepository(this);

        loadMyListings();
    }

    private void loadMyListings() {
        showLoading();

        repository.getMyListings(new AgrofastRepository.DataCallback<List<Listing>>() {
            @Override
            public void onSuccess(List<Listing> data) {
                hideLoading();
                listingList.clear();
                if (data != null) listingList.addAll(data);
                adapter.notifyDataSetChanged();

                if (listingList.isEmpty()) {
                    showEmpty("You haven't published any listings yet");
                } else {
                    hideEmpty();
                }
            }

            @Override
            public void onError(String error) {
                hideLoading();

                DevLogger.logError("MySell_Listings load", error, null);

                String friendly = DevLogger.toUserMessage(error);
                showEmpty(friendly);
                Toast.makeText(MySell_Listings.this, friendly, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openListingDetails(Listing listing) {
        Intent intent = new Intent(MySell_Listings.this, Edit_and_suspend.class);
        intent.putExtra("LISTING_ID", listing.getListingId());
        intent.putExtra("CROP", listing.getCropType());
        intent.putExtra("QUANTITY", listing.getQuantityAvailable());
        intent.putExtra("PRICE", listing.getAskingPrice());
        intent.putExtra("LOCATION", listing.getLocation());
        intent.putExtra("STATUS", listing.getStatus());
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
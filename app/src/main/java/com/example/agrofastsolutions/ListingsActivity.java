package com.example.agrofastsolutions;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;

public class ListingsActivity extends AppCompatActivity {
    Button btnViewListing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listings);
        btnViewListing = findViewById(R.id.btnViewListing);
        btnViewListing.setOnClickListener(v -> {
            Intent intent = new Intent(ListingsActivity.this,ListingDetailsActivity.class);
            startActivity(intent);
        });
    }
}
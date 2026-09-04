package com.example.agrofastsolutions;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;

public class ListingDetailsActivity extends AppCompatActivity {
    Button btnMakeOffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_details);
        btnMakeOffer = findViewById(R.id.btnMakeOffer);
        btnMakeOffer.setOnClickListener(v -> {
            Intent intent = new Intent(ListingDetailsActivity.this,MakeOfferActivity.class);
            startActivity(intent);
        });
    }
}
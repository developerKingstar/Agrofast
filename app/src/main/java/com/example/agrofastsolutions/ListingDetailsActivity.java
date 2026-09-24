package com.example.agrofastsolutions;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ListingDetailsActivity extends AppCompatActivity {

    // Views
    private ImageView imgDetailPhoto;
    private TextView tvDetailCrop, tvDetailSeller, tvDetailQuantity,
            tvDetailLocation, tvDetailPrice;
    private Button btnMakeOffer;

    // Data from previous screen
    private String listingId, cropType, sellerId, sellerName, unit, location, photoUrl;
    private double quantityAvailable, price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_details);

        // 1. Read extras from ListingsActivity
        listingId  = safe(getIntent().getStringExtra("LISTING_ID"));
        cropType   = safe(getIntent().getStringExtra("CROP"));
        sellerId   = safe(getIntent().getStringExtra("SELLER_ID"));
        sellerName = safe(getIntent().getStringExtra("SELLER_NAME"));
        location   = safe(getIntent().getStringExtra("LOCATION"));
        unit       = safe(getIntent().getStringExtra("UNIT"));
        photoUrl   = safe(getIntent().getStringExtra("PHOTO_URL"));   // ✅ NEW
        if (unit.isEmpty()) unit = "kg";

        quantityAvailable = getIntent().getDoubleExtra("QUANTITY", 0);
        price             = getIntent().getDoubleExtra("PRICE", 0);

        // 2. Find views
        imgDetailPhoto   = findViewById(R.id.imgDetailPhoto);          // ✅ NEW
        tvDetailCrop     = findViewById(R.id.tvDetailCrop);
        tvDetailSeller   = findViewById(R.id.tvDetailSeller);
        tvDetailQuantity = findViewById(R.id.tvDetailQuantity);
        tvDetailLocation = findViewById(R.id.tvDetailLocation);
        tvDetailPrice    = findViewById(R.id.tvDetailPrice);
        btnMakeOffer     = findViewById(R.id.btnMakeOffer);

        // 3. Populate with real data
        tvDetailCrop.setText(cropType.isEmpty() ? "Crop" : cropType);
        tvDetailSeller.setText("Farmer: " + (sellerName.isEmpty() ? "Unknown" : sellerName));
        tvDetailQuantity.setText("Available Quantity: " +
                formatNumber(quantityAvailable) + " " + unit);
        tvDetailLocation.setText("📍 " + (location.isEmpty() ? "Unknown" : location));
        tvDetailPrice.setText("Price: TSh " + formatNumber(price) + " per " + unit);

        // 3b. Load the listing photo via Glide
        if (!photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(imgDetailPhoto);
        } else {
            imgDetailPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // 4. Send Offer button → forward to MakeOfferActivity
        btnMakeOffer.setOnClickListener(v -> {
            Intent intent = new Intent(ListingDetailsActivity.this, MakeOfferActivity.class);

            intent.putExtra("LISTING_ID", listingId);
            intent.putExtra("SELLER_ID", sellerId);
            intent.putExtra("CROP", cropType);
            intent.putExtra("SELLER_NAME", sellerName);

            intent.putExtra("ASKING_PRICE", price);
            intent.putExtra("AVAILABLE_QTY", quantityAvailable);
            intent.putExtra("UNIT", unit);

            startActivity(intent);
        });
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String formatNumber(double value) {
        if (value == (long) value) return String.valueOf((long) value);
        return String.valueOf(value);
    }
}
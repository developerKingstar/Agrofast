package com.example.agrofastsolutions;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * Farmer's own published listings ("View listing" from the Sell screen).
 * Static demo cards for now -- same placeholder situation as
 * ListingsActivity on the Buy side, until real listing data exists.
 * Tapping a card opens Edit_and_suspend to view/edit/suspend that listing.
 */
public class MySell_Listings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_sell_listings);

        CardView cardListing1 = findViewById(R.id.cardListing1);
        CardView cardListing2 = findViewById(R.id.cardListing2);

        cardListing1.setOnClickListener(v ->
                startActivity(new Intent(this, Edit_and_suspend.class)));

        cardListing2.setOnClickListener(v ->
                startActivity(new Intent(this, Edit_and_suspend.class)));
    }
}
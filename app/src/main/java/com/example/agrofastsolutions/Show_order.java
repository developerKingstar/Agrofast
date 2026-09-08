package com.example.agrofastsolutions;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

/**
 * My Space -- the full 4-category spec: Offers received, Offers turned
 * into orders (Pending), Completed orders, and Declined orders.
 */
public class Show_order extends AppCompatActivity {

    private MaterialButton btnReceivingOffers;
    private MaterialButton btnPendingOrders;
    private MaterialButton btnCompletedOrders;
    private MaterialButton btnDeclinedOrders;

    private final int activeGreen = Color.parseColor("#1B4D3E");
    private final int inactiveGray = Color.parseColor("#757575");

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_order);

        btnReceivingOffers = findViewById(R.id.btnReceivingOffers);
        btnPendingOrders = findViewById(R.id.btnPendingOrders);
        btnCompletedOrders = findViewById(R.id.btnCompletedOrders);
        btnDeclinedOrders = findViewById(R.id.btnDeclinedOrders);

        // Default tab on first launch: Pending Orders (in-progress orders)
        if (savedInstanceState == null) {
            loadFragment(new Pending());
            setActiveTab(btnPendingOrders);
        }

        btnReceivingOffers.setOnClickListener(v -> {
            loadFragment(new Receiving());
            setActiveTab(btnReceivingOffers);
        });

        btnPendingOrders.setOnClickListener(v -> {
            loadFragment(new Pending());
            setActiveTab(btnPendingOrders);
        });

        btnCompletedOrders.setOnClickListener(v -> {
            loadFragment(new Completed());
            setActiveTab(btnCompletedOrders);
        });

        btnDeclinedOrders.setOnClickListener(v -> {
            loadFragment(new Declined());
            setActiveTab(btnDeclinedOrders);
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    /** Highlights whichever of the 4 tab buttons is currently active. */
    private void setActiveTab(MaterialButton active) {
        MaterialButton[] all = { btnReceivingOffers, btnPendingOrders, btnCompletedOrders, btnDeclinedOrders };
        for (MaterialButton button : all) {
            int color = (button == active) ? activeGreen : inactiveGray;
            button.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }
}
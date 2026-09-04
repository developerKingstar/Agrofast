package com.example.agrofastsolutions;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class Show_order extends AppCompatActivity {
    CardView cardView;

    private MaterialButton btnPendingOrders;
    private MaterialButton btnReceivingOffers;

    // Define color codes
    private final int activeGreen = Color.parseColor("#1B4D3E");
    private final int inactiveGray = Color.parseColor("#757575");

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_order);

        btnPendingOrders = findViewById(R.id.btnPendingOrders);
        btnReceivingOffers = findViewById(R.id.btnReceivingOffers);

        // Load Pending Orders by default on initial launch
        if (savedInstanceState == null) {
            loadFragment(new Pending());
            setButtonState(true);
        }


        // Click Listener for Pending Button
        btnPendingOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new Pending());
                setButtonState(true);
            }
        });


        // Click Listener for Receiving Button
        btnReceivingOffers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new Receiving());
                setButtonState(false);
            }
        });




    }

    // Helper method to swap fragments inside FrameLayout
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    // Helper method to change background colors dynamically
    private void setButtonState(boolean isPendingActive) {
        if (isPendingActive) {
            btnPendingOrders.setBackgroundTintList(ColorStateList.valueOf(activeGreen));
            btnReceivingOffers.setBackgroundTintList(ColorStateList.valueOf(inactiveGray));
        } else {
            btnPendingOrders.setBackgroundTintList(ColorStateList.valueOf(inactiveGray));
            btnReceivingOffers.setBackgroundTintList(ColorStateList.valueOf(activeGreen));
        }

    }
}
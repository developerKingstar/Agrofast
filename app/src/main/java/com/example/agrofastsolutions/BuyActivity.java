package com.example.agrofastsolutions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class BuyActivity extends AppCompatActivity {

    EditText etCrop,etQuantity;
    Button btnSearch;
    ImageButton btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);
        etCrop = findViewById(R.id.etCrop);
        etQuantity = findViewById(R.id.etQuantity);
        btnSearch = findViewById(R.id.btnSearch);
        btnBack = findViewById(R.id.btnBack);

        // Back button
        btnBack.setOnClickListener(v -> finish());
        // Search listings
        btnSearch.setOnClickListener(v -> {
            String crop = etCrop.getText().toString().trim();
            String quantity = etQuantity.getText().toString().trim();
            Intent intent = new Intent(BuyActivity.this,ListingsActivity.class);

            intent.putExtra("CROP",crop);
            intent.putExtra("QUANTITY",quantity);

            startActivity(intent);

        });
    }
}
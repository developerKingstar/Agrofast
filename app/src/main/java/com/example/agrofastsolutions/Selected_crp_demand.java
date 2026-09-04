package com.example.agrofastsolutions;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.RangeSlider;

import java.util.List;

public class Selected_crp_demand extends AppCompatActivity {

    private TextView tvHeaderTitle, tvMinPrice, tvMaxPrice, tvVolumeValue;
    private RangeSlider priceRangeSlider;
    private MaterialButton btnSubmitDemand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_crp_demand);

        // 1. Initialize Views from XML
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvMinPrice = findViewById(R.id.tvMinPrice);
        tvMaxPrice = findViewById(R.id.tvMaxPrice);
        tvVolumeValue = findViewById(R.id.tvVolumeValue);
        priceRangeSlider = findViewById(R.id.priceRangeSlider);
        btnSubmitDemand = findViewById(R.id.btnSubmitDemand);

        // 2. Configure RangeSlider limits and step sizes (Price in Tsh/kg)
        float minPriceLimit = 1000.0f;
        float maxPriceLimit = 5000.0f;
        float defaultMinPrice = 2300.0f;
        float defaultMaxPrice = 2600.0f;

        priceRangeSlider.setValueFrom(minPriceLimit);
        priceRangeSlider.setValueTo(maxPriceLimit);
        priceRangeSlider.setStepSize(100.0f); // Adjusts by 100 Tsh per step
        priceRangeSlider.setValues(defaultMinPrice, defaultMaxPrice);

        // Format tooltips during sliding
        priceRangeSlider.setLabelFormatter(value -> "Tsh " + String.format("%,.0f", value));

        // 3. Update Min and Max price TextViews continuously as thumbs move
        priceRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> selectedValues = slider.getValues();
            float selectedMin = selectedValues.get(0);
            float selectedMax = selectedValues.get(1);

            tvMinPrice.setText(String.format("Tsh%,.0f/kg", selectedMin));
            tvMaxPrice.setText(String.format("Tsh%,.0f/kg", selectedMax));
        });

        // 4. Handle "Place Order" button click
        btnSubmitDemand.setOnClickListener(v -> {
            List<Float> currentValues = priceRangeSlider.getValues();
            float finalMinPrice = currentValues.get(0);
            float finalMaxPrice = currentValues.get(1);

            Intent intent = new Intent(Selected_crp_demand.this, Selecting_listing.class);
            startActivity(intent);

            String cropName = tvHeaderTitle.getText().toString();
            String message = String.format("Order placed for %s!\nPrice range: Tsh %,.0f - Tsh %,.0f/kg",
                    cropName, finalMinPrice, finalMaxPrice);

            Toast.makeText(Selected_crp_demand.this, message, Toast.LENGTH_LONG).show();
        });
    }
}
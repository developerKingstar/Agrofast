package com.example.agrofastsolutions;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_guide);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });

        // Find views
        LinearLayout guideBuy = findViewById(R.id.guideBuy);
        LinearLayout guideSell = findViewById(R.id.guideSell);
        LinearLayout guideFee = findViewById(R.id.guideFee);
        LinearLayout guideRatings = findViewById(R.id.guideRatings);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Buy guide
        if (guideBuy != null) {
            guideBuy.setOnClickListener(v -> openGuide("buy"));
        }

        // Sell guide
        if (guideSell != null) {
            guideSell.setOnClickListener(v -> openGuide("sell"));
        }

        // Nuisance fee guide
        if (guideFee != null) {
            guideFee.setOnClickListener(v -> openGuide("fee"));
        }

        // Ratings and reviews guide
        if (guideRatings != null) {
            guideRatings.setOnClickListener(v -> openGuide("ratings"));
        }
    }

    private void openGuide(String guideType) {
        Intent intent = new Intent(
                UserGuideActivity.this,
                GuideDetailActivity.class
        );
        intent.putExtra("guide_type", guideType);
        startActivity(intent);
    }
}
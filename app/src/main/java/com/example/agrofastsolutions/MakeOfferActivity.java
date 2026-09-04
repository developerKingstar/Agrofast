package com.example.agrofastsolutions;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MakeOfferActivity extends AppCompatActivity {
    EditText etOfferQuantity,etOfferPrice;
    Button btnSendOffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_make_offer);
        etOfferQuantity = findViewById(R.id.etOfferQuantity);
        etOfferPrice = findViewById(R.id.etOfferPrice);
        btnSendOffer = findViewById(R.id.btnSendOffer);

        btnSendOffer.setOnClickListener(v -> {
            String quantity = etOfferQuantity.getText().toString().trim();
            String price = etOfferPrice.getText().toString().trim();
            if (quantity.isEmpty()) {
                Toast.makeText(this, "Please enter quantity",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(this,"Offer send successfully!", Toast.LENGTH_LONG)
                        .show();
                Intent intent = new Intent(MakeOfferActivity.this,OfferStatusActivity.class);
                startActivity(intent);
          }
        });
    }
}
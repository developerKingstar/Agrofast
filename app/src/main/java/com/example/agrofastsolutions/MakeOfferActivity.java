package com.example.agrofastsolutions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agrofastsolutions.repository.AgrofastRepository;
import com.example.agrofastsolutions.util.DevLogger;

public class MakeOfferActivity extends AppCompatActivity {

    private EditText etOfferQuantity, etOfferPrice;
    private Button btnSendOffer, btnCancelOffer;
    private ProgressBar progressBar;
    private TextView tvOfferContext;

    private String listingId, sellerId, cropType, sellerName, unit;
    private double askingPrice, availableQty;

    private AgrofastRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_offer);

        listingId     = safe(getIntent().getStringExtra("LISTING_ID"));
        sellerId      = safe(getIntent().getStringExtra("SELLER_ID"));
        cropType      = safe(getIntent().getStringExtra("CROP"));
        sellerName    = safe(getIntent().getStringExtra("SELLER_NAME"));
        unit          = safe(getIntent().getStringExtra("UNIT"));
        if (unit.isEmpty()) unit = "kg";

        askingPrice   = getIntent().getDoubleExtra("ASKING_PRICE", 0);
        availableQty  = getIntent().getDoubleExtra("AVAILABLE_QTY", 0);

        etOfferQuantity = findViewById(R.id.etOfferQuantity);
        etOfferPrice    = findViewById(R.id.etOfferPrice);
        btnSendOffer    = findViewById(R.id.btnSendOffer);
        btnCancelOffer  = findViewById(R.id.btnCancelOffer);
        progressBar     = findViewById(R.id.progressBarOffer);
        tvOfferContext  = findViewById(R.id.tvOfferContext);

        String context = "For " + (cropType.isEmpty() ? "Crop" : cropType) +
                " from " + (sellerName.isEmpty() ? "Seller" : sellerName) +
                "\nAsking: TSh " + formatNumber(askingPrice) + "/" + unit +
                "   •   Available: " + formatNumber(availableQty) + " " + unit;
        tvOfferContext.setText(context);

        repository = new AgrofastRepository(this);

        btnSendOffer.setOnClickListener(v -> attemptSendOffer());
        btnCancelOffer.setOnClickListener(v -> finish());
    }

    private void attemptSendOffer() {
        String qtyStr = etOfferQuantity.getText().toString().trim();
        String priceStr = etOfferPrice.getText().toString().trim();

        if (qtyStr.isEmpty()) {
            etOfferQuantity.setError("Please enter a quantity");
            etOfferQuantity.requestFocus();
            return;
        }

        double qty;
        try {
            qty = Double.parseDouble(qtyStr);
        } catch (NumberFormatException e) {
            etOfferQuantity.setError("Please enter a valid number");
            return;
        }

        if (qty <= 0) {
            etOfferQuantity.setError("Quantity must be greater than 0");
            return;
        }

        if (qty > availableQty && availableQty > 0) {
            etOfferQuantity.setError("Only " + formatNumber(availableQty) + " " + unit + " available");
            return;
        }

        double offeredPrice = 0;
        if (!priceStr.isEmpty()) {
            try {
                offeredPrice = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                etOfferPrice.setError("Please enter a valid number");
                return;
            }
            if (offeredPrice <= 0) {
                etOfferPrice.setError("Price must be greater than 0");
                return;
            }
        } else {
            offeredPrice = askingPrice;
        }

        showLoading(true);

        final double finalQty = qty;
        final double finalPrice = offeredPrice;

        repository.sendOffer(listingId, sellerId, finalQty, finalPrice,
                new AgrofastRepository.DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showLoading(false);

                        Toast.makeText(MakeOfferActivity.this,
                                "Offer sent!", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(MakeOfferActivity.this, OfferStatusActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);

                        DevLogger.logError("MakeOfferActivity send", error, null);

                        Toast.makeText(MakeOfferActivity.this,
                                DevLogger.toUserMessage(error), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSendOffer.setEnabled(!loading);
        btnCancelOffer.setEnabled(!loading);
        btnSendOffer.setText(loading ? "Sending..." : "SEND OFFER");
    }

    private String safe(String s) { return s == null ? "" : s; }

    private String formatNumber(double value) {
        if (value == (long) value) return String.valueOf((long) value);
        return String.valueOf(value);
    }
}
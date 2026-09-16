package com.example.agrofastsolutions;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agrofastsolutions.repository.AgrofastRepository;
import com.example.agrofastsolutions.util.DevLogger;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class Edit_and_suspend extends AppCompatActivity {

    private TextInputEditText etCropType, etQuantity, etPrice, etLocation, etHarvestDate;
    private MaterialButton btnEditSave, btnDecline;

    private boolean isEditMode = false;
    private String listingId;

    private AgrofastRepository repository;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_and_suspend);

        repository = new AgrofastRepository(this);

        listingId = safe(getIntent().getStringExtra("LISTING_ID"));
        String crop = safe(getIntent().getStringExtra("CROP"));
        double qty = getIntent().getDoubleExtra("QUANTITY", 0);
        double price = getIntent().getDoubleExtra("PRICE", 0);
        String loc = safe(getIntent().getStringExtra("LOCATION"));

        etCropType    = findViewById(R.id.etCropType);
        etQuantity    = findViewById(R.id.etQuantity);
        etPrice       = findViewById(R.id.etPrice);
        etLocation    = findViewById(R.id.etLocation);
        etHarvestDate = findViewById(R.id.etHarvestDate);
        btnEditSave   = findViewById(R.id.btnAccept);
        btnDecline    = findViewById(R.id.btnDecline);

        etCropType.setText(crop.isEmpty() ? "Crop" : crop);
        etQuantity.setText(formatNumber(qty));
        etPrice.setText(formatNumber(price));
        etLocation.setText(loc);

        etHarvestDate.setFocusable(false);
        etHarvestDate.setOnClickListener(v -> {
            if (isEditMode) showDatePicker();
        });

        btnEditSave.setOnClickListener(v -> {
            if (!isEditMode) {
                enableEditing(true);
            } else {
                if (validateInputs()) saveChanges();
            }
        });

        btnDecline.setOnClickListener(v -> confirmDeactivate());
    }

    private void enableEditing(boolean enable) {
        isEditMode = enable;
        etQuantity.setEnabled(enable);
        etPrice.setEnabled(enable);
        etLocation.setEnabled(enable);
        etCropType.setEnabled(false);
        etHarvestDate.setEnabled(enable);
        etHarvestDate.setFocusable(false);

        if (enable) {
            btnEditSave.setText("Save");
            btnEditSave.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            etQuantity.requestFocus();
        } else {
            btnEditSave.setText("Edit");
            btnEditSave.setBackgroundColor(android.graphics.Color.parseColor("#1B4D3E"));
        }
    }

    private boolean validateInputs() {
        if (etQuantity.getText() == null || etQuantity.getText().toString().trim().isEmpty()) {
            etQuantity.setError("Quantity required");
            return false;
        }
        if (etPrice.getText() == null || etPrice.getText().toString().trim().isEmpty()) {
            etPrice.setError("Price required");
            return false;
        }
        if (etLocation.getText() == null || etLocation.getText().toString().trim().isEmpty()) {
            etLocation.setError("Location required");
            return false;
        }
        return true;
    }

    private void saveChanges() {
        double newQty, newPrice;
        String newLocation = etLocation.getText().toString().trim();

        try {
            newQty = Double.parseDouble(etQuantity.getText().toString().trim());
            newPrice = Double.parseDouble(etPrice.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newQty <= 0 || newPrice <= 0) {
            Toast.makeText(this, "Quantity and price must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEditSave.setEnabled(false);
        btnEditSave.setText("Saving...");

        repository.updateListing(listingId, newQty, newPrice, newLocation,
                new AgrofastRepository.DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        btnEditSave.setEnabled(true);
                        enableEditing(false);
                        Toast.makeText(Edit_and_suspend.this,
                                "Listing updated!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        btnEditSave.setEnabled(true);
                        btnEditSave.setText("Save");

                        DevLogger.logError("Edit_and_suspend save", error, null);

                        Toast.makeText(Edit_and_suspend.this,
                                DevLogger.toUserMessage(error), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void confirmDeactivate() {
        new AlertDialog.Builder(this)
                .setTitle("Deactivate listing?")
                .setMessage("This listing will no longer appear in Buy search. You can recreate it later.")
                .setPositiveButton("Deactivate", (dialog, which) -> doDeactivate())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void doDeactivate() {
        btnDecline.setEnabled(false);
        btnDecline.setText("Deactivating...");

        repository.deactivateListing(listingId, new AgrofastRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(Edit_and_suspend.this,
                        "Listing deactivated.", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                btnDecline.setEnabled(true);
                btnDecline.setText("Deactivate");

                DevLogger.logError("Edit_and_suspend deactivate", error, null);

                Toast.makeText(Edit_and_suspend.this,
                        DevLogger.toUserMessage(error), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, y, m, d) -> {
                    String selectedDate = d + "/" + (m + 1) + "/" + y;
                    etHarvestDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String formatNumber(double value) {
        if (value == (long) value) return String.valueOf((long) value);
        return String.valueOf(value);
    }
}
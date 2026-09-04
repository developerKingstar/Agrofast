package com.example.agrofastsolutions;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class Edit_and_suspend extends AppCompatActivity {

    private TextInputEditText etCropType, etQuantity, etPrice, etLocation, etHarvestDate;
    private MaterialButton btnEditSave, btnDecline;
    private boolean isEditMode = false;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_and_suspend);

        // Initialize views
        etCropType = findViewById(R.id.etCropType);
        etQuantity = findViewById(R.id.etQuantity);
        etPrice = findViewById(R.id.etPrice);
        etLocation = findViewById(R.id.etLocation);
        etHarvestDate = findViewById(R.id.etHarvestDate);

        btnEditSave = findViewById(R.id.btnAccept);
        btnDecline = findViewById(R.id.btnDecline);

        // Setup DatePicker for harvest date when clicked in edit mode
        etHarvestDate.setOnClickListener(v -> {
            if (isEditMode) {
                showDatePicker();
            }
        });

        // Toggle edit/save mode
        btnEditSave.setOnClickListener(v -> {
            if (!isEditMode) {
                enableEditing(true);
            } else {
                if (validateInputs()) {
                    saveChanges();
                    enableEditing(false);
                }
            }
        });

        btnDecline.setOnClickListener(v -> finish());
    }

    private void enableEditing(boolean enable) {
        isEditMode = enable;

        etCropType.setEnabled(enable);
        etQuantity.setEnabled(enable);
        etPrice.setEnabled(enable);
        etLocation.setEnabled(enable);

        // Keep focus off direct typing for date to enforce DatePicker selection
        etHarvestDate.setEnabled(enable);
        etHarvestDate.setFocusable(false);

        if (enable) {
            btnEditSave.setText("Save");
            btnEditSave.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            etCropType.requestFocus();
        } else {
            btnEditSave.setText("Edit");
            btnEditSave.setBackgroundColor(android.graphics.Color.parseColor("#1B4D3E"));
        }
    }

    private boolean validateInputs() {
        if (etCropType.getText().toString().trim().isEmpty()) {
            etCropType.setError("Crop type required");
            return false;
        }
        if (etQuantity.getText().toString().trim().isEmpty()) {
            etQuantity.setError("Quantity required");
            return false;
        }
        if (etPrice.getText().toString().trim().isEmpty()) {
            etPrice.setError("Price required");
            return false;
        }
        return true;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    etHarvestDate.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveChanges() {
        // Collect updated values for database or API submission
        String updatedCrop = etCropType.getText().toString().trim();
        String updatedQuantity = etQuantity.getText().toString().trim();
        String updatedPrice = etPrice.getText().toString().trim();
        String updatedLocation = etLocation.getText().toString().trim();
        String updatedDate = etHarvestDate.getText().toString().trim();

        Toast.makeText(this, "Details updated successfully!", Toast.LENGTH_SHORT).show();

    }
}

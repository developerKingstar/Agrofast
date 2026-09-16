package com.example.agrofastsolutions;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.example.agrofastsolutions.repository.AgrofastRepository;
import com.example.agrofastsolutions.util.DevLogger;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Create_listing extends AppCompatActivity {

    private MaterialButton btnViewListings, btnSubmit;
    private LinearLayout layoutUploadPlaceholder;
    private ImageView imgCropPicture;
    private CardView cardUploadPicture;

    private TextInputEditText etCropName, etQuantity, etPrice, etLocation, etDescription;

    private Uri cameraImageUri;
    private AgrofastRepository repository;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    displaySelectedImage(uri);
                    Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraImageUri != null) {
                    displaySelectedImage(cameraImageUri);
                    Toast.makeText(this, "Photo captured", Toast.LENGTH_SHORT).show();
                }
            });

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_listing);

        repository = new AgrofastRepository(this);

        layoutUploadPlaceholder = findViewById(R.id.layoutUploadPlaceholder);
        imgCropPicture          = findViewById(R.id.imgPreview);
        cardUploadPicture       = findViewById(R.id.cardUploadPicture);
        cardUploadPicture.setOnClickListener(v -> showImagePickerDialog());

        etCropName    = findViewById(R.id.etCropName);
        etQuantity    = findViewById(R.id.etQuantity);
        etPrice       = findViewById(R.id.etPrice);
        etLocation    = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);

        btnSubmit       = findViewById(R.id.btnSubmitListing);
        btnViewListings = findViewById(R.id.btnvw_Listing);

        btnSubmit.setOnClickListener(v -> attemptSubmitListing());

        btnViewListings.setOnClickListener(v -> {
            startActivity(new Intent(Create_listing.this, MySell_Listings.class));
        });
    }

    private void attemptSubmitListing() {
        String crop      = textOf(etCropName);
        String qtyStr    = textOf(etQuantity);
        String priceStr  = textOf(etPrice);
        String location  = textOf(etLocation);

        if (crop.isEmpty()) { etCropName.setError("Please enter a crop name"); etCropName.requestFocus(); return; }
        if (qtyStr.isEmpty()) { etQuantity.setError("Please enter a quantity"); etQuantity.requestFocus(); return; }

        double quantity;
        try { quantity = Double.parseDouble(qtyStr); }
        catch (NumberFormatException e) { etQuantity.setError("Please enter a valid number"); return; }
        if (quantity <= 0) { etQuantity.setError("Quantity must be greater than 0"); return; }

        if (priceStr.isEmpty()) { etPrice.setError("Please enter a price"); etPrice.requestFocus(); return; }

        double price;
        try { price = Double.parseDouble(priceStr); }
        catch (NumberFormatException e) { etPrice.setError("Please enter a valid number"); return; }
        if (price <= 0) { etPrice.setError("Price must be greater than 0"); return; }

        if (location.isEmpty()) { etLocation.setError("Please enter a location"); etLocation.requestFocus(); return; }

        showLoading(true);

        repository.createListing(crop, quantity, price, location,
                new AgrofastRepository.DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showLoading(false);
                        Toast.makeText(Create_listing.this,
                                "Listing published!", Toast.LENGTH_SHORT).show();
                        clearForm();
                        startActivity(new Intent(Create_listing.this, MySell_Listings.class));
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);

                        DevLogger.logError("Create_listing submit", error, null);

                        Toast.makeText(Create_listing.this,
                                DevLogger.toUserMessage(error), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String textOf(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void clearForm() {
        etCropName.setText("");
        etQuantity.setText("");
        etPrice.setText("");
        etLocation.setText("");
        etDescription.setText("");
    }

    private void showLoading(boolean loading) {
        btnSubmit.setEnabled(!loading);
        btnSubmit.setText(loading ? "Publishing..." : "Publish Listing");
    }

    private void displaySelectedImage(Uri uri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                if (inputStream != null) inputStream.close();
            }

            if (bitmap != null) {
                imgCropPicture.setImageBitmap(bitmap);
                layoutUploadPlaceholder.setVisibility(View.GONE);
                imgCropPicture.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Unsupported image format", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            DevLogger.logError("Create_listing image decode", e.getMessage(), e);
            Toast.makeText(this, "Could not load that image.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImagePickerDialog() {
        String[] options = {"Choose from Gallery", "Take a Photo"};
        new AlertDialog.Builder(this)
                .setTitle("Select Crop Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openGallery();
                    else if (which == 1) openCamera();
                })
                .show();
    }

    private void openGallery() { galleryLauncher.launch("image/*"); }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            DevLogger.logError("Create_listing camera", e.getMessage(), e);
            Toast.makeText(this, "Could not open camera.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
}
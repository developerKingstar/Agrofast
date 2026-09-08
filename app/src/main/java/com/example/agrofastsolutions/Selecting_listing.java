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
import android.provider.MediaStore;
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

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Selecting_listing extends AppCompatActivity {

    private MaterialButton materialButton,submitbtn;
    private LinearLayout layoutUploadPlaceholder;
    private ImageView imgCropPicture;
    private CardView cardUploadPicture;

    private Uri cameraImageUri;

    // 1. Gallery Launcher - handles image/* (PNG, JPEG, WEBP, HEIC, etc.)
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    displaySelectedImage(uri);
                    Toast.makeText(this, "Image selected from Gallery", Toast.LENGTH_SHORT).show();
                }
            });

    // 2. Camera Launcher
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraImageUri != null) {
                    displaySelectedImage(cameraImageUri);
                    Toast.makeText(this, "Photo captured successfully", Toast.LENGTH_SHORT).show();
                }
            });

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecting_listing);


        layoutUploadPlaceholder = findViewById(R.id.layoutUploadPlaceholder);
        imgCropPicture = findViewById(R.id.imgPreview);
        cardUploadPicture = findViewById(R.id.cardUploadPicture);

        // Attach click listener to the outer card container so tapping anywhere opens selector
        cardUploadPicture.setOnClickListener(v -> showImagePickerDialog());

        submitbtn = findViewById(R.id.btnSubmitListing);
        submitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Selecting_listing.this, "Request sent successfully", Toast.LENGTH_SHORT).show();

                //Intent intent = new Intent(Selecting_listing.this, Show_order.class);
                //startActivity(intent);
            }
        });



        materialButton = findViewById(R.id.btnvw_Listing);
        materialButton.setOnClickListener(v -> {
            Intent intent = new Intent(Selecting_listing.this, MySell_Listings.class);
            startActivity(intent);
        });
    }

    /**
     * Decodes and displays any image URI format (PNG, JPG, WEBP, etc.),
     * hides the placeholder, and turns on ImageView visibility.
     */
    private void displaySelectedImage(Uri uri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                if (inputStream != null) {
                    inputStream.close();
                }
            }

            if (bitmap != null) {
                imgCropPicture.setImageBitmap(bitmap);
                // Toggle visibility: Hide placeholder, show image view
                layoutUploadPlaceholder.setVisibility(View.GONE);
                imgCropPicture.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Unsupported image format", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImagePickerDialog() {
        String[] options = {"Choose from Gallery", "Take a Photo"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Crop Photo");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openGallery();
            } else if (which == 1) {
                openCamera();
            }
        });
        builder.show();
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );
            cameraLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            Toast.makeText(this, "Error creating file for camera", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
}
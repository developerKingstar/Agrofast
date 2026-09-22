package com.example.agrofastsolutions;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.agrofastsolutions.auth.SupabaseAuthManager;
import com.example.agrofastsolutions.repository.AgrofastRepository;
import com.example.agrofastsolutions.util.DevLogger;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // Views
    private ImageButton btnBack;
    private ImageView ivProfilePhoto;
    private TextView tvName, tvRating, tvReviewCount, tvStatus,
            tvEmail, tvPhone, tvLocation, tvMemberSince;
    private MaterialButton btnEditProfile, btnChangePhoto;
    private ProgressBar progressBar;

    private AgrofastRepository repository;
    private SupabaseAuthManager authManager;

    // For camera capture
    private Uri cameraImageUri;

    // ==========================================
    // GALLERY PICKER
    // ==========================================
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    Log.d(TAG, "Gallery image selected: " + uri);
                    uploadImageFromUri(uri);
                }
            });

    // ==========================================
    // CAMERA CAPTURE
    // ==========================================
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraImageUri != null) {
                    Log.d(TAG, "Camera photo captured: " + cameraImageUri);
                    uploadImageFromUri(cameraImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        repository = new AgrofastRepository(this);
        authManager = new SupabaseAuthManager(this);

        // Bind views
        btnBack        = findViewById(R.id.btnBackProfile);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvName         = findViewById(R.id.tvProfileName);
        tvRating       = findViewById(R.id.tvProfileRating);
        tvReviewCount  = findViewById(R.id.tvProfileReviewCount);
        tvStatus       = findViewById(R.id.tvProfileStatus);
        tvEmail        = findViewById(R.id.tvProfileEmail);
        tvPhone        = findViewById(R.id.tvProfilePhone);
        tvLocation     = findViewById(R.id.tvProfileLocation);
        tvMemberSince  = findViewById(R.id.tvProfileMemberSince);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        progressBar    = findViewById(R.id.progressBarProfile);

        // Listeners
        btnBack.setOnClickListener(v -> finish());
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        btnChangePhoto.setOnClickListener(v -> showPhotoSourceDialog());

        // Optional: tap the avatar itself to change photo too
        ivProfilePhoto.setOnClickListener(v -> showPhotoSourceDialog());

        // Load user profile
        loadProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    // ==========================================
    // PROFILE LOADING
    // ==========================================
    private void loadProfile() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmail.setText(authManager.getCurrentUserEmail());

        repository.getCurrentUserProfile(new AgrofastRepository.DataCallback<NewUser>() {
            @Override
            public void onSuccess(NewUser user) {
                progressBar.setVisibility(View.GONE);
                populate(user);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                DevLogger.logError("ProfileActivity load", error, null);
                Toast.makeText(ProfileActivity.this,
                        DevLogger.toUserMessage(error),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populate(NewUser user) {
        if (user == null) return;

        tvName.setText((user.getName() == null || user.getName().isEmpty())
                ? "User" : user.getName());
        tvEmail.setText((user.getEmail() == null || user.getEmail().isEmpty())
                ? "—" : user.getEmail());
        tvPhone.setText((user.getPhone() == null || user.getPhone().isEmpty())
                ? "—" : user.getPhone());
        tvLocation.setText((user.getLocation() == null || user.getLocation().isEmpty())
                ? "—" : user.getLocation());

        double rating = user.getRatingAvg();
        tvRating.setText(String.format(Locale.US, "★ %.1f", rating));

        int reviews = user.getReviewCount();
        tvReviewCount.setText("(" + reviews + " review" + (reviews == 1 ? "" : "s") + ")");

        if (user.getStatus() != null) {
            tvStatus.setText(user.getStatus().toUpperCase());
        }

        tvMemberSince.setText(formatDate(user.getCreatedAt()));

        // Photo
        String photoUrl = user.getProfilePhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.bg_circle_placeholder)
                    .circleCrop()
                    .into(ivProfilePhoto);
        } else {
            ivProfilePhoto.setImageResource(android.R.drawable.ic_menu_myplaces);
        }
    }

    // ==========================================
    // PHOTO UPLOAD
    // ==========================================

    private void showPhotoSourceDialog() {
        String[] options = {"Choose from Gallery", "Take a Photo"};
        new AlertDialog.Builder(this)
                .setTitle("Change Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openGallery();
                    else if (which == 1) openCamera();
                })
                .show();
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
            DevLogger.logError("ProfileActivity camera", e.getMessage(), e);
            Toast.makeText(this, "Could not open camera.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Read image bytes from URI, upload to Supabase Storage, save URL.
     */
    private void uploadImageFromUri(Uri uri) {
        try {
            byte[] bytes = readBytesFromUri(uri);
            if (bytes == null || bytes.length == 0) {
                Toast.makeText(this, "Could not read that image.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Determine extension (default to jpg)
            String extension = "jpg";
            String mime = getContentResolver().getType(uri);
            if (mime != null && mime.contains("/")) {
                String sub = mime.substring(mime.indexOf("/") + 1).toLowerCase();
                if (sub.equals("jpeg")) sub = "jpg";
                if (sub.equals("png") || sub.equals("jpg") || sub.equals("webp")) {
                    extension = sub;
                }
            }

            // Show loading
            showUploading(true);

            final String finalExt = extension;
            repository.uploadProfilePhoto(bytes, finalExt,
                    new AgrofastRepository.DataCallback<String>() {
                        @Override
                        public void onSuccess(String newPhotoUrl) {
                            showUploading(false);
                            Log.d(TAG, "Profile photo updated: " + newPhotoUrl);

                            // ✅ Cache the URL locally so Dashboard avatar can read it
                            getSharedPreferences("agrofast_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("profile_photo_url", newPhotoUrl)
                                    .apply();

                            Toast.makeText(ProfileActivity.this,
                                    "Profile photo updated!", Toast.LENGTH_SHORT).show();

                            // Refresh the profile to display new photo
                            loadProfile();
                        }

                        @Override
                        public void onError(String error) {
                            showUploading(false);
                            DevLogger.logError("ProfileActivity upload", error, null);
                            Toast.makeText(ProfileActivity.this,
                                    DevLogger.toUserMessage(error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (Exception e) {
            DevLogger.logError("ProfileActivity upload", e.getMessage(), e);
            Toast.makeText(this, "Could not process image.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Convert a URI to a byte array (works with gallery and camera URIs).
     */
    private byte[] readBytesFromUri(Uri uri) throws IOException {
        // For newer Android versions, use ImageDecoder for better format support
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                Bitmap bitmap = ImageDecoder.decodeBitmap(source);
                // Compress to JPEG to keep size reasonable
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos);
                return bos.toByteArray();
            } catch (Exception e) {
                Log.w(TAG, "ImageDecoder failed, falling back to stream read", e);
            }
        }

        // Fallback: read the raw stream
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) return null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        }
    }

    private void showUploading(boolean uploading) {
        progressBar.setVisibility(uploading ? View.VISIBLE : View.GONE);
        btnChangePhoto.setEnabled(!uploading);
        btnEditProfile.setEnabled(!uploading);
        btnChangePhoto.setText(uploading ? "Uploading..." : "Change Photo");
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "PROFILE_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // ==========================================
    // HELPERS
    // ==========================================
    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "—";
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date date = input.parse(isoDate.substring(0, 19));
            if (date == null) return "—";
            SimpleDateFormat output = new SimpleDateFormat("MMM yyyy", Locale.US);
            return output.format(date);
        } catch (Exception e) {
            return "—";
        }
    }
}
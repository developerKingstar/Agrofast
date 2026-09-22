package com.example.agrofastsolutions;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agrofastsolutions.auth.SupabaseAuthManager;
import com.example.agrofastsolutions.repository.AgrofastRepository;
import com.example.agrofastsolutions.util.DevLogger;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private ImageButton btnBack;
    private TextView tvEmail;
    private TextInputEditText etName, etPhone, etLocation;
    private MaterialButton btnSave, btnCancel;
    private ProgressBar progressBar;

    private AgrofastRepository repository;
    private SupabaseAuthManager authManager;

    // Cache current values so we can restore on cancel
    private String originalName = "";
    private String originalPhone = "";
    private String originalLocation = "";

    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        repository = new AgrofastRepository(this);
        authManager = new SupabaseAuthManager(this);

        // Bind views
        btnBack      = findViewById(R.id.btnBackEdit);
        tvEmail      = findViewById(R.id.tvEditEmail);
        etName       = findViewById(R.id.etEditName);
        etPhone      = findViewById(R.id.etEditPhone);
        etLocation   = findViewById(R.id.etEditLocation);
        btnSave      = findViewById(R.id.btnSaveProfile);
        btnCancel    = findViewById(R.id.btnCancelEdit);
        progressBar  = findViewById(R.id.progressBarEdit);

        // Show email (read-only from prefs)
        tvEmail.setText(authManager.getCurrentUserEmail());

        // Listeners
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> attemptSave());

        // Load current profile
        loadProfile();
    }

    private void loadProfile() {
        progressBar.setVisibility(View.VISIBLE);

        repository.getCurrentUserProfile(new AgrofastRepository.DataCallback<NewUser>() {
            @Override
            public void onSuccess(NewUser user) {
                progressBar.setVisibility(View.GONE);
                if (user == null) return;

                originalName     = user.getName() == null ? "" : user.getName();
                originalPhone    = user.getPhone() == null ? "" : user.getPhone();
                originalLocation = user.getLocation() == null ? "" : user.getLocation();

                etName.setText(originalName);
                etPhone.setText(originalPhone);
                etLocation.setText(originalLocation);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                DevLogger.logError("EditProfileActivity load", error, null);
                Toast.makeText(EditProfileActivity.this,
                        DevLogger.toUserMessage(error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptSave() {
        if (isSubmitting) return;

        String name     = textOf(etName);
        String phone    = textOf(etPhone);
        String location = textOf(etLocation);

        // Validation
        if (name.isEmpty()) {
            etName.setError("Please enter your name");
            etName.requestFocus();
            return;
        }

        if (name.length() < 2) {
            etName.setError("Name is too short");
            return;
        }

        // Phone is optional but if entered, sanity-check length
        if (!phone.isEmpty() && phone.length() < 7) {
            etPhone.setError("Please enter a valid phone number");
            return;
        }

        // Location optional

        // If nothing changed, just exit
        if (name.equals(originalName)
                && phone.equals(originalPhone)
                && location.equals(originalLocation)) {
            Toast.makeText(this, "No changes to save.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Submit
        setSubmitting(true);

        repository.updateProfile(name, phone, location, new AgrofastRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                setSubmitting(false);

                // Update cached name — first word only for the greeting
                String firstName = name.trim().split("\\s+")[0];
                getSharedPreferences("agrofast_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("user_name", name)         // full name
                        .putString("first_name", firstName)   // first name only
                        .apply();

                Toast.makeText(EditProfileActivity.this,
                        "Profile updated!", Toast.LENGTH_SHORT).show();

                finish();
            }

            @Override
            public void onError(String error) {
                setSubmitting(false);

                DevLogger.logError("EditProfileActivity save", error, null);

                Toast.makeText(EditProfileActivity.this,
                        DevLogger.toUserMessage(error), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ==========================================
    // Helpers
    // ==========================================
    private String textOf(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void setSubmitting(boolean submitting) {
        isSubmitting = submitting;
        btnSave.setEnabled(!submitting);
        btnCancel.setEnabled(!submitting);
        btnSave.setText(submitting ? "Saving..." : "Save Changes");
        progressBar.setVisibility(submitting ? View.VISIBLE : View.GONE);
    }
}
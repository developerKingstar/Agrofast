package com.example.agrofastsolutions;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.agrofastsolutions.auth.SupabaseAuthManager;
import com.example.agrofastsolutions.repository.AgrofastRepository;
import com.example.agrofastsolutions.util.DevLogger;

public class Signup extends AppCompatActivity {

    private static final String TAG = "Signup";

    // Views
    private EditText edtUsername, edtPassword, edtEmail, edtphone, edtLocation;
    private TextView btnLogin, txtSignup;

    // Managers
    private SupabaseAuthManager authManager;
    private AgrofastRepository repository;

    private boolean isSubmitting = false;   // prevent double taps

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        // Fix edge-to-edge padding
        View root = findViewById(R.id.signup_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Managers
        authManager = new SupabaseAuthManager(this);
        repository  = new AgrofastRepository(this);

        // Bind views
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtEmail    = findViewById(R.id.edtEmail);
        edtphone    = findViewById(R.id.edtphone);
        edtLocation = findViewById(R.id.edtLocation);
        btnLogin    = findViewById(R.id.btnLogin);
        txtSignup   = findViewById(R.id.txtSignup);

        // Link back to Login
        txtSignup.setOnClickListener(v -> {
            startActivity(new Intent(Signup.this, Login.class));
            finish();
        });

        // Submit
        btnLogin.setOnClickListener(v -> attemptSignup());
    }

    // ==========================================
    // SIGNUP FLOW
    // ==========================================
    private void attemptSignup() {

        if (isSubmitting) return;   // already in progress

        String username = textOf(edtUsername);
        String password = textOf(edtPassword);
        String email    = textOf(edtEmail);
        String phone    = textOf(edtphone);
        String location = textOf(edtLocation);

        // ===== Validation =====
        if (username.isEmpty()) {
            edtUsername.setError("Please enter a username");
            edtUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Please enter a password");
            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError("Password must be at least 6 characters");
            edtPassword.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            edtEmail.setError("Please enter an email");
            edtEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Please enter a valid email");
            edtEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            edtphone.setError("Please enter a phone number");
            edtphone.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            edtLocation.setError("Please enter a location");
            edtLocation.requestFocus();
            return;
        }

        // ===== Submit =====
        setSubmitting(true);

        Log.d(TAG, "Signing up: " + email);

        authManager.signUp(email, password, username, phone, location,
                new SupabaseAuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String userId) {
                        Log.d(TAG, "Auth signup OK, user_id: " + userId);

                        // Step 2: create the public.users row
                        repository.createPublicUser(userId, email, phone, username, location,
                                new AgrofastRepository.DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        setSubmitting(false);
                                        Toast.makeText(Signup.this,
                                                "Account created!", Toast.LENGTH_SHORT).show();

                                        // Navigate to Dashboard
                                        Intent intent = new Intent(Signup.this, DashBoardActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        setSubmitting(false);

                                        DevLogger.logError("Signup: createPublicUser", error, null);
                                        Toast.makeText(Signup.this,
                                                DevLogger.toUserMessage(error),
                                                Toast.LENGTH_LONG).show();
                                        // Send them to Login so they can retry
                                        startActivity(new Intent(Signup.this, Login.class));
                                        finish();

                                    }
                                });
                    }

                    @Override
                    public void onError(String error) {
                        setSubmitting(false);
                        DevLogger.logError("Signup screen", error, null);
                        Toast.makeText(Signup.this, error, Toast.LENGTH_LONG).show();

                        android.util.Log.d("TestDevLogger",
                                DevLogger.toUserMessage("HTTP 401: Invalid login credentials"));
                    }
                });
    }

    // ==========================================
    // UI HELPERS
    // ==========================================
    private void setSubmitting(boolean submitting) {
        isSubmitting = submitting;
        btnLogin.setEnabled(!submitting);
        btnLogin.setText(submitting ? "Creating account..." : "Sign Up");
    }

    private String textOf(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

}
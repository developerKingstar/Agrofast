package com.example.agrofastsolutions;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agrofastsolutions.auth.SupabaseAuthManager;
import com.example.agrofastsolutions.util.DevLogger;
import com.google.android.material.button.MaterialButton;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private MaterialButton btnSend;
    private ProgressBar progressBar;
    private TextView tvStatus;

    private SupabaseAuthManager authManager;
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        authManager = new SupabaseAuthManager(this);

        etEmail     = findViewById(R.id.etForgotEmail);
        btnSend     = findViewById(R.id.btnSendReset);
        progressBar = findViewById(R.id.progressBarReset);
        tvStatus    = findViewById(R.id.tvResetStatus);
        TextView tvBack = findViewById(R.id.tvBackToLogin);

        btnSend.setOnClickListener(v -> attemptSend());
        tvBack.setOnClickListener(v -> finish());
    }

    private void attemptSend() {
        if (isSubmitting) return;

        String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();

        if (email.isEmpty()) { etEmail.setError("Please enter your email"); etEmail.requestFocus(); return; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email"); etEmail.requestFocus(); return;
        }

        setSubmitting(true);
        tvStatus.setVisibility(View.GONE);

        authManager.sendPasswordResetEmail(email, new SupabaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String ignored) {
                setSubmitting(false);
                tvStatus.setText("Reset link sent! Check your inbox (and spam folder). " +
                        "The link opens in a browser.");
                // Success
                tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(
                        ForgotPasswordActivity.this,
                        com.google.android.material.R.color.design_default_color_primary));

                // Or simpler — just remove the setTextColor lines entirely.
                // The theme's default text color is already readable.
                tvStatus.setVisibility(View.VISIBLE);
                etEmail.setText("");
            }

            @Override
            public void onError(String error) {
                setSubmitting(false);
                tvStatus.setText(error);
                // Success
                tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(
                        ForgotPasswordActivity.this,
                        com.google.android.material.R.color.design_default_color_primary));
                // Or simpler — just remove the setTextColor lines entirely.
                // The theme's default text color is already readable.
                tvStatus.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setSubmitting(boolean submitting) {
        isSubmitting = submitting;
        btnSend.setEnabled(!submitting);
        btnSend.setText(submitting ? "Sending..." : "Send Reset Link");
        progressBar.setVisibility(submitting ? View.VISIBLE : View.GONE);
    }
}
package com.example.agrofastsolutions;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agrofastsolutions.auth.SupabaseAuthManager;
import com.example.agrofastsolutions.util.DevLogger;
import com.google.android.material.button.MaterialButton;

public class ResetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ResetPasswordActivity";

    private EditText etNewPass, etConfirmPass;
    private MaterialButton btnSave;
    private ProgressBar progressBar;
    private TextView tvMessage;

    private String accessToken = null;
    private String refreshToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etNewPass     = findViewById(R.id.etNewPassword);
        etConfirmPass = findViewById(R.id.etConfirmPassword);
        btnSave       = findViewById(R.id.btnSaveNewPassword);
        progressBar   = findViewById(R.id.progressBarResetPwd);
        tvMessage     = findViewById(R.id.tvResetPwdMessage);

        btnSave.setOnClickListener(v -> attemptSave());

        // Parse the incoming deep link
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getData() == null) {
            Log.d(TAG, "No deep link data — showing placeholder message");
            tvMessage.setText("Open the reset link from your email to continue.");
            tvMessage.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
            return;
        }

        Uri data = intent.getData();
        Log.d(TAG, "Deep link received: " + data);

        // Supabase appends tokens in the URL fragment (after the #).
        // Uri.getFragment() gives us everything after the #.
        String fragment = data.getFragment();
        if (fragment == null || fragment.isEmpty()) {
            tvMessage.setText("Invalid reset link.");
            tvMessage.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
            return;
        }

        // Parse "access_token=xxx&refresh_token=yyy&type=recovery"
        String[] pairs = fragment.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length != 2) continue;
            String key = kv[0];
            String value = kv[1];
            if ("access_token".equals(key)) accessToken = value;
            else if ("refresh_token".equals(key)) refreshToken = value;
        }

        if (accessToken == null || accessToken.isEmpty()) {
            tvMessage.setText("Reset token missing. Please request a new email.");
            tvMessage.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
            return;
        }

        Log.d(TAG, "Token received successfully");
        tvMessage.setText("Enter your new password below.");
        tvMessage.setVisibility(View.VISIBLE);
    }

    private void attemptSave() {
        String pass1 = etNewPass.getText() == null ? "" : etNewPass.getText().toString();
        String pass2 = etConfirmPass.getText() == null ? "" : etConfirmPass.getText().toString();

        if (pass1.isEmpty()) { etNewPass.setError("Enter a new password"); return; }
        if (pass1.length() < 6) { etNewPass.setError("At least 6 characters"); return; }
        if (!pass1.equals(pass2)) { etConfirmPass.setError("Passwords don't match"); return; }
        if (accessToken == null) {
            Toast.makeText(this, "Reset link missing token", Toast.LENGTH_LONG).show();
            return;
        }

        setSubmitting(true);

        SupabaseAuthManager auth = new SupabaseAuthManager(this);
        auth.updatePasswordWithToken(accessToken, pass1, new SupabaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String ignored) {
                setSubmitting(false);

                Toast.makeText(ResetPasswordActivity.this,
                        "Password updated! Please log in.", Toast.LENGTH_LONG).show();

                // Route to Login
                Intent i = new Intent(ResetPasswordActivity.this, Login.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }

            @Override
            public void onError(String error) {
                setSubmitting(false);
                DevLogger.logError("ResetPasswordActivity", error, null);
                tvMessage.setText(DevLogger.toUserMessage(error));
                tvMessage.setTextColor(0xFFB71C1C);
                tvMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setSubmitting(boolean submitting) {
        btnSave.setEnabled(!submitting);
        btnSave.setText(submitting ? "Saving..." : "Save New Password");
        progressBar.setVisibility(submitting ? View.VISIBLE : View.GONE);
    }
}
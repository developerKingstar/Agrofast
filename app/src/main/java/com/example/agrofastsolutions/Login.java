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
import com.example.agrofastsolutions.util.DevLogger;

public class Login extends AppCompatActivity {

    private static final String TAG = "Login";

    // Views
    private EditText edtEmail, edtPassword;
    private TextView btnLogin, txtSignup, txtForgotPassword;

    // Auth
    private SupabaseAuthManager authManager;

    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Status bar padding fix
        View root = findViewById(R.id.login_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Auth manager
        authManager = new SupabaseAuthManager(this);

        // Bind views
        edtEmail          = findViewById(R.id.edtEmail);
        edtPassword       = findViewById(R.id.edtPassword);
        btnLogin          = findViewById(R.id.btnLogin);
        txtSignup         = findViewById(R.id.txtSignup);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        // Go to Signup
        txtSignup.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Signup.class));
        });

        txtForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(Login.this, ForgotPasswordActivity.class)));

        // Login button
        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    // ==========================================
    // LOGIN FLOW
    // ==========================================
    private void attemptLogin() {

        if (isSubmitting) return;

        String email    = textOf(edtEmail);
        String password = textOf(edtPassword);

        // ===== Validation =====
        if (email.isEmpty()) {
            edtEmail.setError("Please enter your email");
            edtEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Please enter a valid email");
            edtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Please enter your password");
            edtPassword.requestFocus();
            return;
        }

        // ===== Submit =====
        setSubmitting(true);
        Log.d(TAG, "Logging in: " + email);

        authManager.login(email, password, new SupabaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                setSubmitting(false);
                Log.d(TAG, "Login OK, user_id: " + userId);

                Toast.makeText(Login.this,
                        "Welcome back!", Toast.LENGTH_SHORT).show();

                // Go to Dashboard
                Intent intent = new Intent(Login.this, DashBoardActivity.class);
                startActivity(intent);
                finish();
            }
            @Override
            public void onError(String error) {
                setSubmitting(false);
                DevLogger.logError("Login screen", error, null);

                Toast.makeText(Login.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ==========================================
    // UI HELPERS
    // ==========================================
    private void setSubmitting(boolean submitting) {
        isSubmitting = submitting;
        btnLogin.setEnabled(!submitting);
        btnLogin.setText(submitting ? "Logging in..." : "Log in");
    }

    private String textOf(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
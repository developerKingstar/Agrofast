package com.example.agrofastsolutions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Signup extends AppCompatActivity {

    EditText edtUsername, edtPassword, edtEmail, edtphone, edtLocation;
    TextView btnLogin;   // the "Sign Up" submit button (his XML reuses this id from Login.xml)
    TextView txtSignup;  // "Already have an account? Log in" link

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        // Fix: EdgeToEdge draws content behind the status bar by default,
        // which was overlapping the top of this screen. This pushes the
        // root layout down (and up above the nav bar) by exactly the
        // system bars' size, instead of letting content sit under them.
        View root = findViewById(R.id.signup_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtphone = findViewById(R.id.edtphone);
        edtLocation = findViewById(R.id.edtLocation);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignup = findViewById(R.id.txtSignup);

        // "Already have an account? Log in" -> back to Log in screen
        txtSignup.setOnClickListener(v -> {
            Intent intent = new Intent(Signup.this, Login.class);
            startActivity(intent);
            finish(); // don't stack Signup under Login
        });

        // Submit button
        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phone = edtphone.getText().toString().trim();
            String location = edtLocation.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()
                    || phone.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: replace this block with a real backend/database signup call
            // (this is exactly the auth + database work flagged for later).
            // For now, just save locally so the rest of the app has something
            // to work with -- e.g. Dashboard's greeting reads "first_name" from here.
            SharedPreferences prefs = getSharedPreferences("agrofast_prefs", MODE_PRIVATE);
            prefs.edit()
                    .putString("first_name", username)
                    .putString("email", email)
                    .putString("phone", phone)
                    .putString("location", location)
                    .putBoolean("is_logged_in", true)
                    .apply();

            Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Signup.this, DashBoardActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
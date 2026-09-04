package com.example.agrofastsolutions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Login extends AppCompatActivity {
    TextView tvsignup;
    TextView tvDevSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Same status-bar-overlap fix as Signup -- pushes content below
        // the status bar instead of letting it draw underneath.
        View root = findViewById(R.id.login_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvsignup = findViewById(R.id.txtSignup);

        tvsignup.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Signup.class);
            startActivity(intent);
        });

        // DEV ONLY: skips real login entirely, saves a fake session, and
        // jumps straight to Dashboard. Delete this whole block (and the
        // txtDevSkip TextView in activity_login.xml) once real login is
        // wired up to your database -- don't ship this to real users.
        tvDevSkip = findViewById(R.id.txtDevSkip);
        tvDevSkip.setOnClickListener(view -> {
            SharedPreferences prefs = getSharedPreferences("agrofast_prefs", MODE_PRIVATE);
            prefs.edit()
                    .putString("first_name", "Kingstar")
                    .putBoolean("is_logged_in", true)
                    .apply();

            Intent intent = new Intent(Login.this, DashBoardActivity.class);
            startActivity(intent);
            finish();
        });

        // TODO: btnLogin has no click behavior yet -- wire this up together
        // with authentication/database work, as agreed.
    }
}
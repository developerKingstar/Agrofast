package com.example.agrofastsolutions;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.agrofastsolutions.auth.SupabaseAuthManager;

/**
 * LAUNCHER SCREEN -- first thing the user sees.
 * Plays the Lottie intro animation, then decides where to go next:
 * straight to Dashboard if already logged in, otherwise to Login.
 */
public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        LottieAnimationView lottieView = findViewById(R.id.lottie_launcher);

        // Move to the next screen the instant the animation finishes --
        // this works correctly no matter what speed you set below.
        lottieView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(@NonNull Animator animation) { }
            @Override public void onAnimationEnd(@NonNull Animator animation) { goToNextScreen(); }
            @Override public void onAnimationCancel(@NonNull Animator animation) { }
            @Override public void onAnimationRepeat(@NonNull Animator animation) { }
        });

        // The raw file is about 15 seconds -- a bit long for a launcher screen.
        // Uncomment to speed it up (1.5x brings it down to ~10s) without
        // needing to re-export anything from the Lottie editor:
        // lottieView.setSpeed(1.5f);

        lottieView.enableMergePathsForKitKatAndAbove(true);
        lottieView.playAnimation();
    }

    private void goToNextScreen() {
        // ✅ Use SupabaseAuthManager — checks the real session
        SupabaseAuthManager auth = new SupabaseAuthManager(this);
        boolean isLoggedIn = auth.isLoggedIn();

        Intent intent = isLoggedIn
                ? new Intent(this, DashBoardActivity.class)
                : new Intent(this, Login.class);

        startActivity(intent);
        finish(); // remove Launcher from the back stack
    }
}
package com.example.digitalwellbeinganalyzer.views;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalwellbeinganalyzer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        // =====================================================
        // HOME UI
        // =====================================================

        TextView welcomeText = findViewById(R.id.welcomeText);

        TextView screenTimeText = findViewById(R.id.screenTimeText);

        TextView wellbeingScoreText =
                findViewById(R.id.wellbeingScoreText);

        TextView wellbeingStatusText =
                findViewById(R.id.wellbeingStatusText);

        TextView longestSessionText =
                findViewById(R.id.longestSessionText);

        TextView lastSessionText =
                findViewById(R.id.lastSessionText);

        TextView riskLevelText =
                findViewById(R.id.riskLevelText);

        TextView riskDescriptionText =
                findViewById(R.id.riskDescriptionText);

        // =====================================================
        // CURRENT USER
        // =====================================================

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {

            String email = currentUser.getEmail();

            if (email != null) {
                welcomeText.setText(email);
            }
        }

        // =====================================================
        // DEFAULT HOME VALUES
        // =====================================================

        wellbeingScoreText.setText("82");
        wellbeingStatusText.setText("Healthy");

        screenTimeText.setText("0h 0m");

        longestSessionText.setText("0h 0m");

        lastSessionText.setText("--:--");

        riskLevelText.setText("Low");

        riskDescriptionText.setText(
                "Your current usage pattern looks healthy."
        );

        // =====================================================
        // SETTINGS
        // =====================================================

        ImageView settingsButton =
                findViewById(R.id.settingsButton);

        settingsButton.setOnClickListener(v -> {

            // Open Android Usage Access settings
            Intent intent =
                    new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);

            startActivity(intent);
        });

        // =====================================================
        // BOTTOM NAVIGATION
        // =====================================================

        LinearLayout navHome =
                findViewById(R.id.navHome);

        LinearLayout navAnalytics =
                findViewById(R.id.navAnalytics);

        LinearLayout navGuides =
                findViewById(R.id.navGuides);

        LinearLayout navProfile =
                findViewById(R.id.navProfile);

        // Home
        navHome.setOnClickListener(v -> {
            // Already on Home
        });

        // Analytics
        navAnalytics.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    BehaviorAnalysisActivity.class
            );

            startActivity(intent);
        });

        // Guides
        navGuides.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    GuideActivity.class
            );

            startActivity(intent);
        });

        // Profile
        navProfile.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    ProfileActivity.class
            );

            startActivity(intent);
        });

        // =====================================================
        // USAGE ACCESS PERMISSION
        // =====================================================

        if (!hasUsageAccessPermission()) {

            Intent intent =
                    new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);

            startActivity(intent);
        }
    }

    // =========================================================
    // CHECK USAGE ACCESS PERMISSION
    // =========================================================

    private boolean hasUsageAccessPermission() {

        AppOpsManager appOps =
                (AppOpsManager) getSystemService(
                        Context.APP_OPS_SERVICE
                );

        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                getPackageName()
        );

        return mode == AppOpsManager.MODE_ALLOWED;
    }
}
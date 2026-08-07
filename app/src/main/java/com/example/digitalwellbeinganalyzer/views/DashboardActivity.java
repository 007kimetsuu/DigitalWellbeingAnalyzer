package com.example.digitalwellbeinganalyzer.views;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import java.util.Calendar;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalwellbeinganalyzer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.content.Intent;
public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private TextView screenTimeText;

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

        screenTimeText = findViewById(R.id.screenTimeText);

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

            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            if (displayName != null && !displayName.trim().isEmpty()) {
                welcomeText.setText(displayName + " 👋");
            } else if (email != null && !email.trim().isEmpty()) {
                String username = email.substring(0, email.indexOf("@"));
                welcomeText.setText(username + " 👋");
            }

            loadTodayScreenTime();
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
                    M2AnalyticsActivity.class
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

    private void loadTodayScreenTime() {

        if (!hasUsageAccessPermission()) {
            screenTimeText.setText("--");
            return;
        }

        UsageStatsManager usageStatsManager =
                (UsageStatsManager) getSystemService(
                        Context.USAGE_STATS_SERVICE
                );

        if (usageStatsManager == null) {
            screenTimeText.setText("0h 0m");
            return;
        }

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startTime = calendar.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        List<UsageStats> usageStatsList =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        startTime,
                        endTime
                );

        long totalUsageMillis = 0;

        if (usageStatsList != null) {

            for (UsageStats usageStats : usageStatsList) {

                totalUsageMillis +=
                        usageStats.getTotalTimeInForeground();
            }
        }

        long totalMinutes =
                totalUsageMillis / (1000 * 60);

        long hours =
                totalMinutes / 60;

        long minutes =
                totalMinutes % 60;

        screenTimeText.setText(
                hours + "h " + minutes + "m"
        );
    }
}
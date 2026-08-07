package com.example.digitalwellbeinganalyzer.views;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalwellbeinganalyzer.R;

import java.util.Calendar;
import java.util.List;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView averageSessionText;
    private TextView averageSessionChangeText;
    private TextView mindfulStatusText;
    private TextView mindfulDescriptionText;
    private TextView weeklyScreenTimeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_analytics);

        connectViews();

        loadAnalyticsData();

        setupRiskButton();

        setupBottomNavigation();
    }

    // =========================================================
    // CONNECT VIEWS
    // =========================================================

    private void connectViews() {

        averageSessionText =
                findViewById(R.id.averageSessionText);

        averageSessionChangeText =
                findViewById(R.id.averageSessionChangeText);

        mindfulStatusText =
                findViewById(R.id.mindfulStatusText);

        mindfulDescriptionText =
                findViewById(R.id.mindfulDescriptionText);

        weeklyScreenTimeText =
                findViewById(R.id.weeklyScreenTimeText);
    }

    // =========================================================
    // RISK ANALYSIS BUTTON
    // =========================================================

    private void setupRiskButton() {

        View riskButton =
                findViewById(R.id.riskAnalysisButton);

        if (riskButton != null) {

            riskButton.setOnClickListener(v -> {

                Intent intent = new Intent(
                        AnalyticsActivity.this,
                        activity_analytics_risk.class
                );

                startActivity(intent);
            });
        }
    }

    // =========================================================
    // LOAD ANALYTICS DATA
    // =========================================================

    private void loadAnalyticsData() {

        if (!hasUsageAccessPermission()) {

            averageSessionText.setText("--");

            averageSessionChangeText.setText(
                    "Usage Access permission required"
            );

            mindfulStatusText.setText("Waiting");

            mindfulDescriptionText.setText(
                    "Enable Usage Access so MindTrack can analyse your device usage."
            );

            weeklyScreenTimeText.setText("--");

            return;
        }

        UsageStatsManager usageStatsManager =
                (UsageStatsManager)
                        getSystemService(Context.USAGE_STATS_SERVICE);

        if (usageStatsManager == null) {
            return;
        }

        long endTime =
                System.currentTimeMillis();

        Calendar calendar =
                Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startTime =
                calendar.getTimeInMillis();

        List<UsageStats> usageStatsList =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        startTime,
                        endTime
                );

        if (usageStatsList == null ||
                usageStatsList.isEmpty()) {

            showNoDataState();

            return;
        }

        long totalUsageMillis = 0;

        int activeApps = 0;

        for (UsageStats usageStats :
                usageStatsList) {

            long usageTime =
                    usageStats.getTotalTimeInForeground();

            if (usageTime > 0) {

                totalUsageMillis += usageTime;

                activeApps++;
            }
        }

        long totalMinutes =
                totalUsageMillis / (1000 * 60);

        long hours =
                totalMinutes / 60;

        long minutes =
                totalMinutes % 60;

        // =====================================================
        // DAILY SCREEN TIME
        // =====================================================

        weeklyScreenTimeText.setText(
                hours + "h " + minutes + "m Daily Avg"
        );

        // =====================================================
        // AVERAGE USAGE
        // =====================================================

        if (activeApps > 0) {

            long averageMinutes =
                    totalMinutes / activeApps;

            averageSessionText.setText(
                    averageMinutes + "m"
            );

        } else {

            averageSessionText.setText("0m");
        }

        // =====================================================
        // MINDFULNESS STATUS
        // =====================================================

        if (totalMinutes < 120) {

            mindfulStatusText.setText("Healthy");

            mindfulDescriptionText.setText(
                    "Your current screen time is relatively low today."
            );

        } else if (totalMinutes < 300) {

            mindfulStatusText.setText("Moderate");

            mindfulDescriptionText.setText(
                    "Your device usage is moderate today. Consider taking regular breaks."
            );

        } else {

            mindfulStatusText.setText("Needs Attention");

            mindfulDescriptionText.setText(
                    "Your screen time is relatively high today. Consider reducing prolonged usage."
            );
        }

        // =====================================================
        // SUMMARY
        // =====================================================

        averageSessionChangeText.setText(
                activeApps + " active apps today"
        );
    }

    // =========================================================
    // NO DATA STATE
    // =========================================================

    private void showNoDataState() {

        averageSessionText.setText("0m");

        averageSessionChangeText.setText(
                "No usage data yet"
        );

        mindfulStatusText.setText(
                "Waiting"
        );

        mindfulDescriptionText.setText(
                "Use your device normally and return later to view your analytics."
        );

        weeklyScreenTimeText.setText(
                "0h 0m Daily Avg"
        );
    }

    // =========================================================
    // USAGE ACCESS PERMISSION
    // =========================================================

    private boolean hasUsageAccessPermission() {

        AppOpsManager appOps =
                (AppOpsManager)
                        getSystemService(
                                Context.APP_OPS_SERVICE
                        );

        if (appOps == null) {
            return false;
        }

        int mode =
                appOps.checkOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        Process.myUid(),
                        getPackageName()
                );

        return mode ==
                AppOpsManager.MODE_ALLOWED;
    }

    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private void setupBottomNavigation() {

        View navHome =
                findViewById(R.id.navHome);

        View navAnalytics =
                findViewById(R.id.navAnalytics);

        View navGuides =
                findViewById(R.id.navGuides);

        View navProfile =
                findViewById(R.id.navProfile);


        // HOME
        if (navHome != null) {

            navHome.setOnClickListener(v -> {

                Intent intent = new Intent(
                        AnalyticsActivity.this,
                        DashboardActivity.class
                );

                startActivity(intent);
                finish();
            });
        }


        // ANALYTICS
        if (navAnalytics != null) {

            navAnalytics.setOnClickListener(v -> {
                // Already on Analytics
            });
        }


        // GUIDES
        if (navGuides != null) {

            navGuides.setOnClickListener(v -> {

                Intent intent = new Intent(
                        AnalyticsActivity.this,
                        GuideActivity.class
                );

                startActivity(intent);
                finish();
            });
        }


        // PROFILE
        if (navProfile != null) {

            navProfile.setOnClickListener(v -> {

                Intent intent = new Intent(
                        AnalyticsActivity.this,
                        ProfileActivity.class
                );

                startActivity(intent);
                finish();
            });
        }
    }
}
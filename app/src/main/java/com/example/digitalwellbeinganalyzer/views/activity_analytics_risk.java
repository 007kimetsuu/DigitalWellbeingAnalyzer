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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.digitalwellbeinganalyzer.R;

import java.util.Calendar;
import java.util.List;

public class activity_analytics_risk extends AppCompatActivity {

    private TextView confidenceText;
    private TextView riskStatusText;
    private TextView averageSessionText;
    private TextView criticalPeaksText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_analytics_risk);

        connectViews();

        loadRiskData();

        setupBottomNavigation();

        View mainView =
                findViewById(R.id.main);

        if (mainView != null) {

            ViewCompat.setOnApplyWindowInsetsListener(
                    mainView,
                    (v, insets) -> {

                        Insets systemBars =
                                insets.getInsets(
                                        WindowInsetsCompat.Type.systemBars()
                                );

                        v.setPadding(
                                systemBars.left,
                                systemBars.top,
                                systemBars.right,
                                systemBars.bottom
                        );

                        return insets;
                    }
            );
        }
    }

    // =========================================================
    // CONNECT VIEWS
    // =========================================================

    private void connectViews() {

        confidenceText =
                findViewById(R.id.confidenceText);

        riskStatusText =
                findViewById(R.id.riskStatusText);

        averageSessionText =
                findViewById(R.id.averageSessionText);

        criticalPeaksText =
                findViewById(R.id.criticalPeaksText);
    }

    // =========================================================
    // LOAD RISK DATA
    // =========================================================

    private void loadRiskData() {

        if (!hasUsageAccessPermission()) {

            confidenceText.setText("--");

            riskStatusText.setText(
                    "Usage Access Required"
            );

            averageSessionText.setText("--");

            criticalPeaksText.setText("--");

            return;
        }

        UsageStatsManager usageStatsManager =
                (UsageStatsManager)
                        getSystemService(
                                Context.USAGE_STATS_SERVICE
                        );

        if (usageStatsManager == null) {
            return;
        }

        long endTime =
                System.currentTimeMillis();

        Calendar calendar =
                Calendar.getInstance();

        calendar.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        calendar.set(
                Calendar.MINUTE,
                0
        );

        calendar.set(
                Calendar.SECOND,
                0
        );

        calendar.set(
                Calendar.MILLISECOND,
                0
        );

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

            showNoData();

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
                totalUsageMillis /
                        (1000 * 60);

        // =====================================================
        // AVERAGE SESSION
        // =====================================================

        long averageMinutes = 0;

        if (activeApps > 0) {

            averageMinutes =
                    totalMinutes / activeApps;
        }

        averageSessionText.setText(
                averageMinutes + "m"
        );

        // =====================================================
        // BASIC RISK CALCULATION
        // =====================================================

        if (totalMinutes < 120) {

            confidenceText.setText("85%");

            riskStatusText.setText(
                    "Low Risk"
            );

        } else if (totalMinutes < 300) {

            confidenceText.setText("75%");

            riskStatusText.setText(
                    "Moderate Risk Identified"
            );

        } else {

            confidenceText.setText("90%");

            riskStatusText.setText(
                    "High Risk Identified"
            );
        }

        // =====================================================
        // TEMPORARY CRITICAL PEAK INDICATOR
        // =====================================================

        criticalPeaksText.setText(
                String.valueOf(activeApps)
        );
    }

    // =========================================================
    // NO DATA
    // =========================================================

    private void showNoData() {

        confidenceText.setText("--");

        riskStatusText.setText(
                "No Data Available"
        );

        averageSessionText.setText(
                "0m"
        );

        criticalPeaksText.setText(
                "0"
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
                        activity_analytics_risk.this,
                        DashboardActivity.class
                );

                startActivity(intent);
                finish();
            });
        }


        // ANALYTICS
        if (navAnalytics != null) {

            navAnalytics.setOnClickListener(v -> {

                Intent intent = new Intent(
                        activity_analytics_risk.this,
                        M2AnalyticsActivity.class
                );

                startActivity(intent);
                finish();
            });
        }


        // GUIDES
        if (navGuides != null) {

            navGuides.setOnClickListener(v -> {

                Intent intent = new Intent(
                        activity_analytics_risk.this,
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
                        activity_analytics_risk.this,
                        ProfileActivity.class
                );

                startActivity(intent);
                finish();
            });
        }
    }
}
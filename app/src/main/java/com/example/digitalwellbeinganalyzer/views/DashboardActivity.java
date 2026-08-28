package com.example.digitalwellbeinganalyzer.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.digitalwellbeinganalyzer.AppCategoryClassifier;
import com.example.digitalwellbeinganalyzer.InterventionManager;
import com.example.digitalwellbeinganalyzer.R;
import com.example.digitalwellbeinganalyzer.RecommendationResult;
import com.example.digitalwellbeinganalyzer.RiskDataRepository;
import com.example.digitalwellbeinganalyzer.RiskEngineResult;
import com.example.digitalwellbeinganalyzer.UsageStatsHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    private static final int
            NOTIFICATION_PERMISSION_REQUEST_CODE = 2001;

    private FirebaseAuth firebaseAuth;
    private RiskDataRepository repository;

    private final ExecutorService usageExecutor =
            Executors.newSingleThreadExecutor();

    private TextView screenTimeText;
    private TextView screenTimeChangeText;

    private TextView wellbeingScoreText;
    private TextView wellbeingStatusText;

    private TextView longestSessionText;
    private TextView lastSessionText;

    private TextView riskLevelText;
    private TextView riskDescriptionText;

    private TextView dailyBreakdownText;

    private TextView recommendationTitleText;
    private TextView recommendationDescriptionText;

    private ProgressBar wellbeingScoreProgress;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_dashboard
        );


        // =====================================================
        // FIREBASE + BACKEND
        // =====================================================

        firebaseAuth =
                FirebaseAuth.getInstance();


        repository =
                new RiskDataRepository(
                        getApplicationContext()
                );


        InterventionManager
                .createNotificationChannel(
                        this
                );


        requestNotificationPermissionIfNeeded();


        // =====================================================
        // CONNECT UI
        // =====================================================

        TextView welcomeText =
                findViewById(
                        R.id.welcomeText
                );


        screenTimeText =
                findViewById(
                        R.id.screenTimeText
                );


        screenTimeChangeText =
                findViewById(
                        R.id.screenTimeChangeText
                );


        wellbeingScoreText =
                findViewById(
                        R.id.wellbeingScoreText
                );


        wellbeingStatusText =
                findViewById(
                        R.id.wellbeingStatusText
                );


        wellbeingScoreProgress =
                findViewById(
                        R.id.wellbeingScoreProgress
                );


        longestSessionText =
                findViewById(
                        R.id.longestSessionText
                );


        lastSessionText =
                findViewById(
                        R.id.lastSessionText
                );


        riskLevelText =
                findViewById(
                        R.id.riskLevelText
                );


        riskDescriptionText =
                findViewById(
                        R.id.riskDescriptionText
                );


        dailyBreakdownText =
                findViewById(
                        R.id.dailyBreakdownText
                );


        recommendationTitleText =
                findViewById(
                        R.id.recommendationTitleText
                );


        recommendationDescriptionText =
                findViewById(
                        R.id.recommendationDescriptionText
                );


        // =====================================================
        // FIREBASE USER
        // =====================================================

        FirebaseUser currentUser =
                firebaseAuth.getCurrentUser();


        if (currentUser != null) {

            String displayName =
                    currentUser.getDisplayName();


            String email =
                    currentUser.getEmail();


            if (displayName != null &&
                    !displayName.trim().isEmpty()) {

                welcomeText.setText(
                        displayName + " 👋"
                );

            } else if (
                    email != null &&
                            !email.trim().isEmpty()
            ) {

                int atIndex =
                        email.indexOf("@");


                String username;


                if (atIndex > 0) {

                    username =
                            email.substring(
                                    0,
                                    atIndex
                            );

                } else {

                    username =
                            email;
                }


                welcomeText.setText(
                        username + " 👋"
                );
            }
        }


        // =====================================================
        // INITIAL UI
        // =====================================================

        wellbeingScoreText.setText(
                "--"
        );


        wellbeingStatusText.setText(
                "Calculating"
        );


        wellbeingScoreProgress.setProgress(
                0
        );


        screenTimeText.setText(
                "--"
        );


        screenTimeChangeText.setText(
                "Waiting for usage data"
        );


        longestSessionText.setText(
                "--"
        );


        lastSessionText.setText(
                "--:--"
        );


        riskLevelText.setText(
                "--"
        );


        riskDescriptionText.setText(
                "Analyzing your current usage pattern."
        );


        dailyBreakdownText.setText(
                "Waiting for usage data"
        );


        recommendationTitleText.setText(
                "Analyzing your usage"
        );


        recommendationDescriptionText.setText(
                "A personalized recommendation will appear here."
        );


        // =====================================================
        // SETTINGS
        // =====================================================

        ImageView settingsButton =
                findViewById(
                        R.id.settingsButton
                );


        settingsButton.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            Settings.ACTION_USAGE_ACCESS_SETTINGS
                    );


            startActivity(
                    intent
            );
        });


        // =====================================================
        // NAVIGATION
        // =====================================================

        LinearLayout navHome =
                findViewById(
                        R.id.navHome
                );


        LinearLayout navAnalytics =
                findViewById(
                        R.id.navAnalytics
                );


        LinearLayout navGuides =
                findViewById(
                        R.id.navGuides
                );


        LinearLayout navProfile =
                findViewById(
                        R.id.navProfile
                );


        navHome.setOnClickListener(v -> {
            // Already on Dashboard.
        });


        navAnalytics.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            DashboardActivity.this,
                            M2AnalyticsActivity.class
                    );


            startActivity(
                    intent
            );
        });


        navGuides.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            DashboardActivity.this,
                            GuideActivity.class
                    );


            startActivity(
                    intent
            );
        });


        navProfile.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            DashboardActivity.this,
                            ProfileActivity.class
                    );


            startActivity(
                    intent
            );
        });


        // =====================================================
        // FIRST-TIME USAGE ACCESS
        // =====================================================

        if (!UsageStatsHelper
                .hasUsageStatsPermission(this)) {

            Intent intent =
                    new Intent(
                            Settings.ACTION_USAGE_ACCESS_SETTINGS
                    );


            startActivity(
                    intent
            );
        }
    }


    // =========================================================
    // REFRESH
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();


        loadUsageData();

        loadFinalRisk();

        loadSmartRecommendation();
    }


    // =========================================================
    // NOTIFICATION PERMISSION
    // =========================================================

    private void requestNotificationPermissionIfNeeded() {

        if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.TIRAMISU) {

            return;
        }


        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED) {

            return;
        }


        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.POST_NOTIFICATIONS
                },
                NOTIFICATION_PERMISSION_REQUEST_CODE
        );
    }


    // =========================================================
    // REAL USAGE DATA
    // =========================================================

    private void loadUsageData() {

        if (!UsageStatsHelper
                .hasUsageStatsPermission(this)) {

            screenTimeText.setText(
                    "--"
            );


            longestSessionText.setText(
                    "--"
            );


            lastSessionText.setText(
                    "--:--"
            );


            screenTimeChangeText.setText(
                    "Usage Access permission required"
            );


            dailyBreakdownText.setText(
                    "Usage Access permission required"
            );


            return;
        }


        usageExecutor.execute(() -> {

            // -------------------------------------------------
            // TODAY'S SCREEN TIME
            // -------------------------------------------------

            long todayUsage =
                    UsageStatsHelper
                            .getTotalSessionUsageToday(
                                    getApplicationContext()
                            );


            // -------------------------------------------------
            // LONGEST SESSION
            // -------------------------------------------------

            long longestSession =
                    UsageStatsHelper
                            .getLongestSessionDurationToday(
                                    getApplicationContext()
                            );


            // -------------------------------------------------
            // LAST COMPLETED SESSION
            // -------------------------------------------------

            long lastSessionEnd =
                    UsageStatsHelper
                            .getLastSessionEndToday(
                                    getApplicationContext()
                            );


            // -------------------------------------------------
            // YESTERDAY
            // -------------------------------------------------

            Calendar todayStart =
                    Calendar.getInstance();


            todayStart.set(
                    Calendar.HOUR_OF_DAY,
                    0
            );


            todayStart.set(
                    Calendar.MINUTE,
                    0
            );


            todayStart.set(
                    Calendar.SECOND,
                    0
            );


            todayStart.set(
                    Calendar.MILLISECOND,
                    0
            );


            Calendar yesterdayStart =
                    (Calendar)
                            todayStart.clone();


            yesterdayStart.add(
                    Calendar.DAY_OF_YEAR,
                    -1
            );


            long yesterdayUsage =
                    UsageStatsHelper
                            .getTotalSessionUsageBetween(

                                    getApplicationContext(),

                                    yesterdayStart
                                            .getTimeInMillis(),

                                    todayStart
                                            .getTimeInMillis()
                            );


            // -------------------------------------------------
            // CATEGORY BREAKDOWN
            // -------------------------------------------------

            Map<String, Long> categoryTotals =
                    AppCategoryClassifier
                            .getCategoryUsageToday(
                                    getApplicationContext()
                            );


            runOnUiThread(() -> {

                if (!isActivityUsable()) {

                    return;
                }


                screenTimeText.setText(
                        formatDuration(
                                todayUsage
                        )
                );


                longestSessionText.setText(
                        formatDuration(
                                longestSession
                        )
                );


                lastSessionText.setText(
                        formatSessionEnd(
                                lastSessionEnd
                        )
                );


                screenTimeChangeText.setText(
                        buildUsageComparison(
                                todayUsage,
                                yesterdayUsage
                        )
                );


                dailyBreakdownText.setText(
                        buildCategoryBreakdown(
                                categoryTotals
                        )
                );
            });
        });
    }


    // =========================================================
    // FINAL COMBINED RISK + WELLBEING SCORE
    // =========================================================

    private void loadFinalRisk() {

        repository.getFinalRiskToday(

                getApplicationContext(),

                result -> runOnUiThread(() -> {

                    if (!isActivityUsable()) {

                        return;
                    }


                    if (result == null) {

                        riskLevelText.setText(
                                "No Data"
                        );


                        riskDescriptionText.setText(
                                "Risk analysis is not available yet."
                        );


                        wellbeingScoreText.setText(
                                "--"
                        );


                        wellbeingStatusText.setText(
                                "Waiting for data"
                        );


                        wellbeingScoreProgress.setProgress(
                                0
                        );


                        return;
                    }


                    updateFinalRiskUI(
                            result
                    );


                    updateWellbeingFromRisk(
                            result
                    );
                })
        );
    }


    // =========================================================
    // WELLBEING FROM COMBINED RISK
    // =========================================================

    private void updateWellbeingFromRisk(
            RiskEngineResult result
    ) {

        int safeRiskScore =
                Math.max(
                        0,
                        Math.min(
                                100,
                                result.finalRiskScore
                        )
                );


        /*
         * Risk score:
         * 0   = lowest risk
         * 100 = highest risk
         *
         * Wellbeing score is the inverse:
         * 100 = strongest wellbeing
         * 0   = weakest wellbeing
         */
        int wellbeingScore =
                100 -
                        safeRiskScore;


        wellbeingScoreText.setText(
                String.valueOf(
                        wellbeingScore
                )
        );


        wellbeingScoreProgress.setProgress(
                wellbeingScore
        );


        if (wellbeingScore >= 80) {

            wellbeingStatusText.setText(
                    "Healthy"
            );

        } else if (
                wellbeingScore >= 50
        ) {

            wellbeingStatusText.setText(
                    "Moderate"
            );

        } else {

            wellbeingStatusText.setText(
                    "Needs Attention"
            );
        }
    }


    // =========================================================
    // FINAL RISK UI
    // =========================================================

    private void updateFinalRiskUI(
            RiskEngineResult result
    ) {

        switch (result.finalRiskLevel) {

            case RiskEngineResult.LEVEL_HIGH:

                riskLevelText.setText(
                        "High"
                );


                riskDescriptionText.setText(
                        "Your current combined usage and behavioral analysis shows elevated risk."
                );

                break;


            case RiskEngineResult.LEVEL_MEDIUM:

                riskLevelText.setText(
                        "Moderate"
                );


                riskDescriptionText.setText(
                        "Some current usage patterns may need attention."
                );

                break;


            case RiskEngineResult.LEVEL_LOW:

            default:

                riskLevelText.setText(
                        "Low"
                );


                riskDescriptionText.setText(
                        "Your current combined usage pattern is within the lower-risk range."
                );

                break;
        }
    }


    // =========================================================
    // SMART RECOMMENDATION
    // =========================================================

    private void loadSmartRecommendation() {

        repository.getSmartRecommendationToday(

                getApplicationContext(),

                recommendation ->
                        runOnUiThread(() -> {

                            if (!isActivityUsable()) {

                                return;
                            }


                            if (recommendation == null) {

                                recommendationTitleText.setText(
                                        "Build a healthier pattern"
                                );


                                recommendationDescriptionText.setText(
                                        "Keep using MindTrack to receive personalized guidance."
                                );


                                return;
                            }


                            displayRecommendation(
                                    recommendation
                            );
                        })
        );
    }


    private void displayRecommendation(
            RecommendationResult recommendation
    ) {

        if (recommendation.title != null &&
                !recommendation.title
                        .trim()
                        .isEmpty()) {

            recommendationTitleText.setText(
                    recommendation.title
            );

        } else {

            recommendationTitleText.setText(
                    "Today's Recommendation"
            );
        }


        if (recommendation.message != null &&
                !recommendation.message
                        .trim()
                        .isEmpty()) {

            recommendationDescriptionText.setText(
                    recommendation.message
            );

        } else {

            recommendationDescriptionText.setText(
                    "Continue monitoring your digital habits."
            );
        }
    }


    // =========================================================
    // CATEGORY BREAKDOWN
    // =========================================================

    private String buildCategoryBreakdown(
            Map<String, Long> totals
    ) {

        if (totals == null ||
                totals.isEmpty()) {

            return "No usage data yet";
        }


        long education =
                getCategoryValue(
                        totals,
                        AppCategoryClassifier
                                .CATEGORY_EDUCATION
                );


        long productivity =
                getCategoryValue(
                        totals,
                        AppCategoryClassifier
                                .CATEGORY_PRODUCTIVITY
                );


        long social =
                getCategoryValue(
                        totals,
                        AppCategoryClassifier
                                .CATEGORY_SOCIAL
                );


        long communication =
                getCategoryValue(
                        totals,
                        AppCategoryClassifier
                                .CATEGORY_COMMUNICATION
                );


        long entertainment =
                getCategoryValue(
                        totals,
                        AppCategoryClassifier
                                .CATEGORY_ENTERTAINMENT
                );


        long gaming =
                getCategoryValue(
                        totals,
                        AppCategoryClassifier
                                .CATEGORY_GAMING
                );


        long utilities =
                getCategoryValue(
                        totals,
                        AppCategoryClassifier
                                .CATEGORY_UTILITIES
                );


        long other =
                getCategoryValue(
                        totals,
                        AppCategoryClassifier
                                .CATEGORY_OTHER
                );


        long total =
                education
                        + productivity
                        + social
                        + communication
                        + entertainment
                        + gaming
                        + utilities
                        + other;


        if (total <= 0) {

            return "No usage data yet";
        }


        return
                "● Education             "
                        + calculatePercent(
                        education,
                        total
                )
                        + "%\n"

                        + "● Productivity          "
                        + calculatePercent(
                        productivity,
                        total
                )
                        + "%\n"

                        + "● Social                "
                        + calculatePercent(
                        social,
                        total
                )
                        + "%\n"

                        + "● Communication         "
                        + calculatePercent(
                        communication,
                        total
                )
                        + "%\n"

                        + "● Entertainment         "
                        + calculatePercent(
                        entertainment,
                        total
                )
                        + "%\n"

                        + "● Gaming                "
                        + calculatePercent(
                        gaming,
                        total
                )
                        + "%\n"

                        + "● Utilities             "
                        + calculatePercent(
                        utilities,
                        total
                )
                        + "%\n"

                        + "● Other                 "
                        + calculatePercent(
                        other,
                        total
                )
                        + "%";
    }


    private long getCategoryValue(
            Map<String, Long> totals,
            String category
    ) {

        Long value =
                totals.get(
                        category
                );


        return value != null
                ? value
                : 0L;
    }


    private int calculatePercent(
            long value,
            long total
    ) {

        if (total <= 0) {

            return 0;
        }


        return (int) Math.round(
                (value * 100.0)
                        / total
        );
    }


    // =========================================================
    // USAGE COMPARISON
    // =========================================================

    private String buildUsageComparison(
            long todayUsage,
            long yesterdayUsage
    ) {

        if (yesterdayUsage <= 0) {

            return "No comparison data from yesterday";
        }


        double difference =
                todayUsage
                        - yesterdayUsage;


        int percentage =
                (int) Math.round(
                        Math.abs(
                                difference /
                                        (double)
                                                yesterdayUsage
                        ) * 100
                );


        if (difference < 0) {

            return "↓ "
                    + percentage
                    + "% from yesterday";
        }


        if (difference > 0) {

            return "↑ "
                    + percentage
                    + "% from yesterday";
        }


        return "Same as yesterday";
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private boolean isActivityUsable() {

        return !isFinishing()
                && !isDestroyed();
    }


    private String formatDuration(
            long milliseconds
    ) {

        if (milliseconds <= 0) {

            return "0h 0m";
        }


        long totalMinutes =
                milliseconds /
                        (1000L * 60L);


        long hours =
                totalMinutes /
                        60L;


        long minutes =
                totalMinutes %
                        60L;


        return hours
                + "h "
                + minutes
                + "m";
    }


    private String formatSessionEnd(
            long timestamp
    ) {

        if (timestamp <= 0) {

            return "--:--";
        }


        SimpleDateFormat format =
                new SimpleDateFormat(
                        "h:mm a",
                        Locale.getDefault()
                );


        return format.format(
                new Date(
                        timestamp
                )
        );
    }


    // =========================================================
    // CLEANUP
    // =========================================================

    @Override
    protected void onDestroy() {

        usageExecutor.shutdownNow();


        if (repository != null) {

            repository.shutdown();
        }


        super.onDestroy();
    }
}
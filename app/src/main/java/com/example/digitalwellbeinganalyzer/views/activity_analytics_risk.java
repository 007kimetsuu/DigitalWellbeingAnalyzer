package com.example.digitalwellbeinganalyzer.views;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.digitalwellbeinganalyzer.BehaviorMetrics;
import com.example.digitalwellbeinganalyzer.R;
import com.example.digitalwellbeinganalyzer.RecommendationResult;
import com.example.digitalwellbeinganalyzer.RiskDataRepository;
import com.example.digitalwellbeinganalyzer.RiskEngineResult;
import com.example.digitalwellbeinganalyzer.RiskReading;

import java.util.Calendar;
import java.util.List;

public class activity_analytics_risk extends AppCompatActivity {

    private TextView confidenceText;
    private TextView riskStatusText;
    private TextView averageSessionText;
    private TextView criticalPeaksText;

    private View riskFactorRow1;
    private View riskFactorRow2;
    private View riskFactorRow3;
    private View riskFactorRow4;

    private TextView riskFactorTitle1;
    private TextView riskFactorTitle2;
    private TextView riskFactorTitle3;
    private TextView riskFactorTitle4;

    private TextView riskFactorDescription1;
    private TextView riskFactorDescription2;
    private TextView riskFactorDescription3;
    private TextView riskFactorDescription4;

    private TextView riskFactorSeverity1;
    private TextView riskFactorSeverity2;
    private TextView riskFactorSeverity3;
    private TextView riskFactorSeverity4;

    private TextView recommendationText;
    private Button enableFocusShieldButton;

    private RiskDataRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_analytics_risk);

        repository =
                new RiskDataRepository(
                        getApplicationContext()
                );

        connectViews();

        setupInsets();

        setupBottomNavigation();

        setupRecommendationButton();

        loadRiskData();
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


        riskFactorRow1 =
                findViewById(R.id.riskFactorRow1);

        riskFactorRow2 =
                findViewById(R.id.riskFactorRow2);

        riskFactorRow3 =
                findViewById(R.id.riskFactorRow3);

        riskFactorRow4 =
                findViewById(R.id.riskFactorRow4);


        riskFactorTitle1 =
                findViewById(R.id.riskFactorTitle1);

        riskFactorTitle2 =
                findViewById(R.id.riskFactorTitle2);

        riskFactorTitle3 =
                findViewById(R.id.riskFactorTitle3);

        riskFactorTitle4 =
                findViewById(R.id.riskFactorTitle4);


        riskFactorDescription1 =
                findViewById(R.id.riskFactorDescription1);

        riskFactorDescription2 =
                findViewById(R.id.riskFactorDescription2);

        riskFactorDescription3 =
                findViewById(R.id.riskFactorDescription3);

        riskFactorDescription4 =
                findViewById(R.id.riskFactorDescription4);


        riskFactorSeverity1 =
                findViewById(R.id.riskFactorSeverity1);

        riskFactorSeverity2 =
                findViewById(R.id.riskFactorSeverity2);

        riskFactorSeverity3 =
                findViewById(R.id.riskFactorSeverity3);

        riskFactorSeverity4 =
                findViewById(R.id.riskFactorSeverity4);


        recommendationText =
                findViewById(R.id.recommendationText);

        enableFocusShieldButton =
                findViewById(R.id.enableFocusShieldButton);
    }

    // =========================================================
    // LOAD ALL REAL RISK DATA
    // =========================================================

    private void loadRiskData() {

        if (!hasUsageAccessPermission()) {

            showPermissionRequired();

            return;
        }

        loadFinalRisk();

        loadBehaviorMetrics();

        loadCriticalPeaks();

        loadSmartRecommendation();
    }

    // =========================================================
    // FINAL RISK + ML CONFIDENCE + CRITICAL FACTORS
    // =========================================================

    private void loadFinalRisk() {

        repository.getFinalRiskToday(

                getApplicationContext(),

                result -> runOnUiThread(() -> {

                    if (!isActivityUsable()) {
                        return;
                    }

                    if (result == null) {

                        showRiskUnavailable();

                        return;
                    }

                    displayRiskResult(result);
                })
        );
    }

    private void displayRiskResult(
            RiskEngineResult result
    ) {

        if (result.mlAvailable) {

            int confidence =
                    (int) Math.round(
                            result.mlConfidencePercent
                    );

            confidenceText.setText(
                    confidence + "%"
            );

        } else {

            confidenceText.setText("--");
        }


        switch (result.finalRiskLevel) {

            case RiskEngineResult.LEVEL_HIGH:

                riskStatusText.setText(
                        "High Risk Identified"
                );

                break;


            case RiskEngineResult.LEVEL_MEDIUM:

                riskStatusText.setText(
                        "Moderate Risk Identified"
                );

                break;


            case RiskEngineResult.LEVEL_LOW:

            default:

                riskStatusText.setText(
                        "Low Risk"
                );

                break;
        }


        displayCriticalFactors(
                result.criticalFactors,
                result.finalRiskLevel
        );
    }

    // =========================================================
    // REAL SESSION METRICS
    // =========================================================

    private void loadBehaviorMetrics() {

        repository.getBehaviorMetricsToday(

                getApplicationContext(),

                metrics -> runOnUiThread(() -> {

                    if (!isActivityUsable()) {
                        return;
                    }

                    if (metrics == null) {

                        averageSessionText.setText("0m");

                        return;
                    }

                    displayBehaviorMetrics(metrics);
                })
        );
    }

    private void displayBehaviorMetrics(
            BehaviorMetrics metrics
    ) {

        averageSessionText.setText(
                formatDuration(
                        metrics.averageSessionDurationMs
                )
        );
    }

    // =========================================================
    // CRITICAL PEAKS
    // =========================================================

    private void loadCriticalPeaks() {

        repository.getAllHistory(

                readings -> {

                    int criticalPeaks =
                            countTodayHighRiskReadings(
                                    readings
                            );

                    runOnUiThread(() -> {

                        if (!isActivityUsable()) {
                            return;
                        }

                        criticalPeaksText.setText(
                                String.valueOf(
                                        criticalPeaks
                                )
                        );
                    });
                }
        );
    }

    private int countTodayHighRiskReadings(
            List<RiskReading> readings
    ) {

        if (readings == null ||
                readings.isEmpty()) {

            return 0;
        }

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

        long startOfToday =
                calendar.getTimeInMillis();

        long now =
                System.currentTimeMillis();

        int count = 0;

        for (RiskReading reading : readings) {

            if (reading == null) {
                continue;
            }

            if (reading.timestamp >= startOfToday &&
                    reading.timestamp <= now &&
                    reading.riskLevel ==
                            RiskEngineResult.LEVEL_HIGH) {

                count++;
            }
        }

        return count;
    }

    // =========================================================
    // CRITICAL FACTORS
    // =========================================================

    private void displayCriticalFactors(
            List<String> factors,
            int riskLevel
    ) {

        View[] rows = {
                riskFactorRow1,
                riskFactorRow2,
                riskFactorRow3,
                riskFactorRow4
        };

        TextView[] titles = {
                riskFactorTitle1,
                riskFactorTitle2,
                riskFactorTitle3,
                riskFactorTitle4
        };

        TextView[] descriptions = {
                riskFactorDescription1,
                riskFactorDescription2,
                riskFactorDescription3,
                riskFactorDescription4
        };

        TextView[] severities = {
                riskFactorSeverity1,
                riskFactorSeverity2,
                riskFactorSeverity3,
                riskFactorSeverity4
        };


        for (View row : rows) {

            if (row != null) {

                row.setVisibility(
                        View.GONE
                );
            }
        }


        if (factors == null ||
                factors.isEmpty()) {

            riskFactorRow1.setVisibility(
                    View.VISIBLE
            );

            riskFactorTitle1.setText(
                    "No critical factors detected"
            );

            riskFactorDescription1.setText(
                    "Today's behavioral patterns are currently within the lower-risk range."
            );

            riskFactorSeverity1.setText(
                    "Low"
            );

            return;
        }


        int numberToShow =
                Math.min(
                        factors.size(),
                        rows.length
                );


        for (int i = 0;
             i < numberToShow;
             i++) {

            rows[i].setVisibility(
                    View.VISIBLE
            );

            titles[i].setText(
                    factors.get(i)
            );

            descriptions[i].setText(
                    "Detected from today's usage and behavioral analysis."
            );

            severities[i].setText(
                    getSeverityText(
                            riskLevel
                    )
            );
        }
    }

    private String getSeverityText(
            int riskLevel
    ) {

        switch (riskLevel) {

            case RiskEngineResult.LEVEL_HIGH:
                return "High";

            case RiskEngineResult.LEVEL_MEDIUM:
                return "Moderate";

            default:
                return "Low";
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

                            displayRecommendation(
                                    recommendation
                            );
                        })
        );
    }

    private void displayRecommendation(
            RecommendationResult recommendation
    ) {

        if (recommendation == null) {

            recommendationText.setText(
                    "Not enough usage data is available yet to generate a personalized recommendation."
            );

            return;
        }

        if (recommendation.message == null ||
                recommendation.message
                        .trim()
                        .isEmpty()) {

            recommendationText.setText(
                    "Continue monitoring your digital habits."
            );

            return;
        }

        recommendationText.setText(
                recommendation.message
        );
    }

    // =========================================================
    // RECOMMENDATION BUTTON
    // =========================================================

    private void setupRecommendationButton() {

        if (enableFocusShieldButton == null) {
            return;
        }

        enableFocusShieldButton.setText(
                "View Guides"
        );

        enableFocusShieldButton.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    activity_analytics_risk.this,
                                    GuideActivity.class
                            );

                    startActivity(intent);
                }
        );
    }

    // =========================================================
    // PERMISSION / EMPTY STATES
    // =========================================================

    private void showPermissionRequired() {

        confidenceText.setText("--");

        riskStatusText.setText(
                "Usage Access Required"
        );

        averageSessionText.setText("--");

        criticalPeaksText.setText("--");

        recommendationText.setText(
                "Enable Usage Access to allow MindTrack to analyze your digital behavior."
        );

        hideAllRiskFactors();
    }

    private void showRiskUnavailable() {

        confidenceText.setText("--");

        riskStatusText.setText(
                "Not Enough Data"
        );

        hideAllRiskFactors();
    }

    private void hideAllRiskFactors() {

        if (riskFactorRow1 != null) {
            riskFactorRow1.setVisibility(View.GONE);
        }

        if (riskFactorRow2 != null) {
            riskFactorRow2.setVisibility(View.GONE);
        }

        if (riskFactorRow3 != null) {
            riskFactorRow3.setVisibility(View.GONE);
        }

        if (riskFactorRow4 != null) {
            riskFactorRow4.setVisibility(View.GONE);
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private boolean isActivityUsable() {

        return !isFinishing() &&
                !isDestroyed();
    }

    private String formatDuration(
            long milliseconds
    ) {

        if (milliseconds <= 0) {
            return "0m";
        }

        long totalMinutes =
                milliseconds /
                        (1000L * 60L);

        long hours =
                totalMinutes / 60L;

        long minutes =
                totalMinutes % 60L;


        if (hours > 0) {

            return hours +
                    "h " +
                    minutes +
                    "m";
        }

        return minutes + "m";
    }

    // =========================================================
    // USAGE ACCESS
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
    // WINDOW INSETS
    // =========================================================

    private void setupInsets() {

        View mainView =
                findViewById(R.id.main);

        if (mainView == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(

                mainView,

                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat
                                            .Type
                                            .systemBars()
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


        if (navHome != null) {

            navHome.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                activity_analytics_risk.this,
                                DashboardActivity.class
                        );

                startActivity(intent);
                finish();
            });
        }


        if (navAnalytics != null) {

            navAnalytics.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                activity_analytics_risk.this,
                                M2AnalyticsActivity.class
                        );

                startActivity(intent);
                finish();
            });
        }


        if (navGuides != null) {

            navGuides.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                activity_analytics_risk.this,
                                GuideActivity.class
                        );

                startActivity(intent);
                finish();
            });
        }


        if (navProfile != null) {

            navProfile.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                activity_analytics_risk.this,
                                ProfileActivity.class
                        );

                startActivity(intent);
                finish();
            });
        }
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    @Override
    protected void onDestroy() {

        if (repository != null) {
            repository.shutdown();
        }

        super.onDestroy();
    }
}
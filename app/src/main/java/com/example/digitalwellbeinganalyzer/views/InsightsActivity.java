package com.example.digitalwellbeinganalyzer.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalwellbeinganalyzer.InsightsResult;
import com.example.digitalwellbeinganalyzer.R;
import com.example.digitalwellbeinganalyzer.RiskDataRepository;

public class InsightsActivity extends AppCompatActivity {

    private TextView insightTitleText;
    private TextView insightDescriptionText;

    private TextView weeklyIncreaseText;
    private TextView weeklyBehaviorDescription;

    private TextView deepFocusText;
    private TextView pickupsText;

    private TextView consistencyValueText;
    private TextView consistencyTitleText;
    private TextView consistencyDescriptionText;

    private View premiumAnalysisCard;

    private Button setBoundaryButton;
    private Button dismissButton;

    private RiskDataRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_insights);

        repository =
                new RiskDataRepository(
                        getApplicationContext()
                );

        connectViews();

        setupButtons();

        setupBottomNavigation();

        loadInsightData();
    }

    // =========================================================
    // CONNECT VIEWS
    // =========================================================

    private void connectViews() {

        premiumAnalysisCard =
                findViewById(R.id.premiumAnalysisCard);

        insightTitleText =
                findViewById(R.id.insightTitleText);

        insightDescriptionText =
                findViewById(R.id.insightDescriptionText);

        weeklyIncreaseText =
                findViewById(R.id.weeklyIncreaseText);

        weeklyBehaviorDescription =
                findViewById(R.id.weeklyBehaviorDescription);

        deepFocusText =
                findViewById(R.id.deepFocusText);

        pickupsText =
                findViewById(R.id.pickupsText);

        consistencyValueText =
                findViewById(R.id.consistencyValueText);

        consistencyTitleText =
                findViewById(R.id.consistencyTitleText);

        consistencyDescriptionText =
                findViewById(R.id.consistencyDescriptionText);

        setBoundaryButton =
                findViewById(R.id.setBoundaryButton);

        dismissButton =
                findViewById(R.id.dismissButton);
    }

    // =========================================================
    // LOAD REAL INSIGHT DATA
    // =========================================================

    private void loadInsightData() {

        showLoadingState();

        repository.getInsightsToday(

                getApplicationContext(),

                insights ->
                        runOnUiThread(() -> {

                            if (!isActivityUsable()) {
                                return;
                            }

                            displayInsights(
                                    insights
                            );
                        })
        );
    }

    // =========================================================
    // DISPLAY INSIGHTS
    // =========================================================

    private void displayInsights(
            InsightsResult insights
    ) {

        if (insights == null) {

            showNoData();

            return;
        }

        // =====================================================
        // PRIMARY INSIGHT
        // =====================================================

        if (insights.primaryTitle != null &&
                !insights.primaryTitle.trim().isEmpty()) {

            insightTitleText.setText(
                    insights.primaryTitle
            );

        } else {

            insightTitleText.setText(
                    "Not Enough Data"
            );
        }


        if (insights.primaryDescription != null &&
                !insights.primaryDescription.trim().isEmpty()) {

            insightDescriptionText.setText(
                    insights.primaryDescription
            );

        } else {

            insightDescriptionText.setText(
                    "Keep using your device normally so MindTrack can identify meaningful behavioral patterns."
            );
        }


        // =====================================================
        // WEEKLY COMPARISON
        // =====================================================

        if (insights.weeklyComparisonAvailable) {

            double change =
                    insights.weeklyChangePercent;

            long roundedChange =
                    Math.round(
                            Math.abs(change)
                    );


            if (change > 0) {

                weeklyIncreaseText.setText(
                        roundedChange + "% increase"
                );

            } else if (change < 0) {

                weeklyIncreaseText.setText(
                        roundedChange + "% decrease"
                );

            } else {

                weeklyIncreaseText.setText(
                        "No change"
                );
            }

        } else {

            weeklyIncreaseText.setText(
                    "Comparison unavailable"
            );
        }


        if (insights.weeklyDescription != null &&
                !insights.weeklyDescription.trim().isEmpty()) {

            weeklyBehaviorDescription.setText(
                    insights.weeklyDescription
            );

        } else {

            weeklyBehaviorDescription.setText(
                    "More usage history is needed before a reliable weekly comparison can be calculated."
            );
        }


        // =====================================================
        // DEEP FOCUS
        // =====================================================

        deepFocusText.setText(
                formatDuration(
                        insights.deepFocusMs
                )
        );


        // =====================================================
        // PICKUPS
        // =====================================================

        /*
         * Pickup tracking is intentionally shown as unavailable
         * until we have a reliable persisted pickup source.
         *
         * SCREEN_ON alone is not treated as a true physical pickup.
         */

        if (insights.pickupDataAvailable &&
                insights.pickupCount >= 0) {

            pickupsText.setText(
                    String.valueOf(
                            insights.pickupCount
                    )
            );

        } else {

            pickupsText.setText("--");
        }


        // =====================================================
        // CONSISTENCY STREAK
        // =====================================================

        int streakDays =
                Math.max(
                        0,
                        insights.consistencyStreakDays
                );


        consistencyValueText.setText(
                streakDays + "d"
        );


        if (insights.consistencyTitle != null &&
                !insights.consistencyTitle.trim().isEmpty()) {

            consistencyTitleText.setText(
                    insights.consistencyTitle
            );

        } else {

            consistencyTitleText.setText(
                    "Consistency Streak"
            );
        }


        if (insights.consistencyDescription != null &&
                !insights.consistencyDescription
                        .trim()
                        .isEmpty()) {

            consistencyDescriptionText.setText(
                    insights.consistencyDescription
            );

        } else {

            consistencyDescriptionText.setText(
                    "No consistency pattern is available yet."
            );
        }
    }

    // =========================================================
    // LOADING STATE
    // =========================================================

    private void showLoadingState() {

        insightTitleText.setText(
                "Analyzing your usage..."
        );

        insightDescriptionText.setText(
                "MindTrack is evaluating today's behavioral patterns."
        );

        weeklyIncreaseText.setText("--");

        weeklyBehaviorDescription.setText(
                "Calculating weekly comparison..."
        );

        deepFocusText.setText("--");

        pickupsText.setText("--");

        consistencyValueText.setText("--");

        consistencyTitleText.setText(
                "Consistency Streak"
        );

        consistencyDescriptionText.setText(
                "Calculating consistency..."
        );
    }

    // =========================================================
    // NO DATA
    // =========================================================

    private void showNoData() {

        insightTitleText.setText(
                "Not Enough Data"
        );

        insightDescriptionText.setText(
                "Use your device normally for a while so MindTrack can identify useful behavioral patterns."
        );

        weeklyIncreaseText.setText(
                "Comparison unavailable"
        );

        weeklyBehaviorDescription.setText(
                "More usage history is required."
        );

        deepFocusText.setText("0m");

        pickupsText.setText("--");

        consistencyValueText.setText("0d");

        consistencyTitleText.setText(
                "Consistency Streak"
        );

        consistencyDescriptionText.setText(
                "More data is needed to calculate a consistency streak."
        );
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
    // BUTTONS
    // =========================================================

    private void setupButtons() {

        if (setBoundaryButton != null) {

            setBoundaryButton.setOnClickListener(
                    v -> {

                        Intent intent =
                                new Intent(
                                        InsightsActivity.this,
                                        GuideActivity.class
                                );

                        startActivity(intent);
                    }
            );
        }


        if (dismissButton != null) {

            dismissButton.setOnClickListener(
                    v -> {

                        if (premiumAnalysisCard != null) {

                            premiumAnalysisCard.setVisibility(
                                    View.GONE
                            );
                        }
                    }
            );
        }
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
                                InsightsActivity.this,
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
                                InsightsActivity.this,
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
                                InsightsActivity.this,
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
                                InsightsActivity.this,
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
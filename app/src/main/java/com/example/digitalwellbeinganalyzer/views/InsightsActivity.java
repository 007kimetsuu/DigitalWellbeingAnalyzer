package com.example.digitalwellbeinganalyzer.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalwellbeinganalyzer.R;

public class InsightsActivity extends AppCompatActivity {

    private TextView insightTitleText;
    private TextView insightDescriptionText;
    private TextView weeklyIncreaseText;
    private TextView weeklyBehaviorDescription;
    private TextView deepFocusText;
    private TextView pickupsText;
    private TextView consistencyTitleText;
    private TextView consistencyDescriptionText;

    private Button setBoundaryButton;
    private Button dismissButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_insights);

        connectViews();

        loadInsightData();

        setupButtons();

        setupBottomNavigation();
    }

    // =========================================================
    // CONNECT VIEWS
    // =========================================================

    private void connectViews() {

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
    // INSIGHT DATA
    // =========================================================

    private void loadInsightData() {

        // Temporary values.
        // Later these can come from the behavioural
        // analysis engine.

        insightTitleText.setText(
                "You typically use entertainment apps before bedtime."
        );

        insightDescriptionText.setText(
                "This behavior pattern is linked to a 15% decrease in deep sleep quality. Try switching to a reading app 30 minutes earlier."
        );

        weeklyIncreaseText.setText(
                "42% increase"
        );

        weeklyBehaviorDescription.setText(
                "You spent more time on social media after 7 PM compared to your morning average."
        );

        deepFocusText.setText(
                "3.2h"
        );

        pickupsText.setText(
                "84"
        );

        consistencyTitleText.setText(
                "Consistency Streak"
        );

        consistencyDescriptionText.setText(
                "Your morning screen-free goal was met on 5 of 7 days this week."
        );
    }

    // =========================================================
    // BUTTONS
    // =========================================================

    private void setupButtons() {

        if (setBoundaryButton != null) {

            setBoundaryButton.setOnClickListener(v -> {

                Toast.makeText(
                        InsightsActivity.this,
                        "Focus boundary feature coming soon",
                        Toast.LENGTH_SHORT
                ).show();

            });
        }

        if (dismissButton != null) {

            dismissButton.setOnClickListener(v -> {

                Toast.makeText(
                        InsightsActivity.this,
                        "Insight dismissed",
                        Toast.LENGTH_SHORT
                ).show();

            });
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


        // =====================================================
        // HOME
        // =====================================================

        if (navHome != null) {

            navHome.setOnClickListener(v -> {

                Intent intent = new Intent(
                        InsightsActivity.this,
                        DashboardActivity.class
                );

                startActivity(intent);
                finish();
            });
        }


        // =====================================================
        // ANALYTICS
        // =====================================================

        if (navAnalytics != null) {

            navAnalytics.setOnClickListener(v -> {

                Intent intent = new Intent(
                        InsightsActivity.this,
                        M2AnalyticsActivity.class
                );

                startActivity(intent);
                finish();
            });
        }


        // =====================================================
        // GUIDES
        // =====================================================

        if (navGuides != null) {

            navGuides.setOnClickListener(v -> {

                Intent intent = new Intent(
                        InsightsActivity.this,
                        GuideActivity.class
                );

                startActivity(intent);
                finish();
            });
        }


        // =====================================================
        // PROFILE
        // =====================================================

        if (navProfile != null) {

            navProfile.setOnClickListener(v -> {

                Intent intent = new Intent(
                        InsightsActivity.this,
                        ProfileActivity.class
                );

                startActivity(intent);
                finish();
            });
        }
    }
}
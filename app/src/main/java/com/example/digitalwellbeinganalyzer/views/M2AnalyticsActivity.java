package com.example.digitalwellbeinganalyzer.views;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.digitalwellbeinganalyzer.R;

public class M2AnalyticsActivity extends AppCompatActivity {

    private CardView mindfulStatusCard;
    private CardView insightsCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_m2_analytics);

        connectViews();
        setupNavigation();
    }

    private void connectViews() {

        mindfulStatusCard =
                findViewById(R.id.mindfulStatusCard);

        insightsCard =
                findViewById(R.id.insightsCard);
    }

    private void setupNavigation() {

        // =========================================
        // RISK ANALYSIS
        // =========================================

        mindfulStatusCard.setOnClickListener(v -> {

            Intent intent = new Intent(
                    M2AnalyticsActivity.this,
                    activity_analytics_risk.class
            );

            startActivity(intent);
        });


        // =========================================
        // INSIGHTS
        // =========================================

        insightsCard.setOnClickListener(v -> {

            Intent intent = new Intent(
                    M2AnalyticsActivity.this,
                    InsightsActivity.class
            );

            startActivity(intent);
        });


        // =========================================
        // BOTTOM NAVIGATION
        // =========================================

        findViewById(R.id.navHome).setOnClickListener(v -> {

            Intent intent = new Intent(
                    M2AnalyticsActivity.this,
                    DashboardActivity.class
            );

            startActivity(intent);
            finish();
        });


        findViewById(R.id.navAnalytics).setOnClickListener(v -> {
            // Already on Analytics.
        });


        findViewById(R.id.navGuides).setOnClickListener(v -> {

            Intent intent = new Intent(
                    M2AnalyticsActivity.this,
                    GuideActivity.class
            );

            startActivity(intent);
            finish();
        });


        findViewById(R.id.navProfile).setOnClickListener(v -> {

            Intent intent = new Intent(
                    M2AnalyticsActivity.this,
                    ProfileActivity.class
            );

            startActivity(intent);
            finish();
        });
    }
}
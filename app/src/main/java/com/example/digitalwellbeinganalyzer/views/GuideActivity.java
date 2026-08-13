package com.example.digitalwellbeinganalyzer.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalwellbeinganalyzer.R;

public class GuideActivity extends AppCompatActivity {

    private CheckBox breakTaskCheckBox;
    private CheckBox nightTaskCheckBox;
    private CheckBox productivityTaskCheckBox;

    private TextView tasksLeftText;
    private TextView mindfulnessStreakText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_guide);

        connectViews();
        setupBottomNavigation();
        setupGuideCards();
        setupTaskCheckboxes();
    }


    // =========================================================
    // CONNECT VIEWS
    // =========================================================

    private void connectViews() {

        breakTaskCheckBox =
                findViewById(R.id.breakTaskCheckBox);

        nightTaskCheckBox =
                findViewById(R.id.nightTaskCheckBox);

        productivityTaskCheckBox =
                findViewById(R.id.productivityTaskCheckBox);

        tasksLeftText =
                findViewById(R.id.tasksLeftText);

        mindfulnessStreakText =
                findViewById(R.id.mindfulnessStreakText);
    }


    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private void setupBottomNavigation() {

        findViewById(R.id.navHome).setOnClickListener(v -> {

            Intent intent = new Intent(
                    GuideActivity.this,
                    DashboardActivity.class
            );

            startActivity(intent);
            finish();
        });


        findViewById(R.id.navAnalytics).setOnClickListener(v -> {

            Intent intent = new Intent(
                    GuideActivity.this,
                    M2AnalyticsActivity.class
            );

            startActivity(intent);
            finish();
        });


        // Already on Guides
        findViewById(R.id.navGuides).setOnClickListener(v -> {
            // Do nothing
        });


        findViewById(R.id.navProfile).setOnClickListener(v -> {

            Intent intent = new Intent(
                    GuideActivity.this,
                    ProfileActivity.class
            );

            startActivity(intent);
            finish();
        });
    }


    // =========================================================
    // GUIDE CARDS
    // =========================================================

    private void setupGuideCards() {

        findViewById(R.id.dailyFocusCard).setOnClickListener(v -> {

            Toast.makeText(
                    GuideActivity.this,
                    "Daily Focus recommendations are based on your usage.",
                    Toast.LENGTH_SHORT
            ).show();
        });


        findViewById(R.id.takeBreaksCard).setOnClickListener(v -> {

            Toast.makeText(
                    GuideActivity.this,
                    "Try taking a short break every hour.",
                    Toast.LENGTH_SHORT
            ).show();
        });


        findViewById(R.id.lateNightUsageCard).setOnClickListener(v -> {

            Toast.makeText(
                    GuideActivity.this,
                    "Try reducing screen usage after 11 PM.",
                    Toast.LENGTH_SHORT
            ).show();
        });


        findViewById(R.id.manageScreenTimeCard).setOnClickListener(v -> {

            Toast.makeText(
                    GuideActivity.this,
                    "Consider moving some screen time toward productive apps.",
                    Toast.LENGTH_SHORT
            ).show();
        });


        findViewById(R.id.weeklyGoalCard).setOnClickListener(v -> {

            Toast.makeText(
                    GuideActivity.this,
                    "Your weekly mindfulness progress is being tracked.",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }


    // =========================================================
    // TASK CHECKBOXES
    // =========================================================

    private void setupTaskCheckboxes() {

        breakTaskCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> updateTaskProgress()
        );

        nightTaskCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> updateTaskProgress()
        );

        productivityTaskCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> updateTaskProgress()
        );
    }


    // =========================================================
    // UPDATE TASK PROGRESS
    // =========================================================

    private void updateTaskProgress() {

        int completedTasks = 0;

        if (breakTaskCheckBox.isChecked()) {
            completedTasks++;
        }

        if (nightTaskCheckBox.isChecked()) {
            completedTasks++;
        }

        if (productivityTaskCheckBox.isChecked()) {
            completedTasks++;
        }

        int remainingTasks = 3 - completedTasks;

        if (remainingTasks == 0) {

            tasksLeftText.setText("ALL TASKS DONE");

            mindfulnessStreakText.setText(
                    "Mindfulness Streak: 6 Days"
            );

        } else {

            tasksLeftText.setText(
                    remainingTasks + " TASKS LEFT"
            );

            mindfulnessStreakText.setText(
                    "Mindfulness Streak: 5 Days"
            );
        }
    }
}
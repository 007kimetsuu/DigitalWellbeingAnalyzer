package com.example.digitalwellbeinganalyzer.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalwellbeinganalyzer.BehaviorMetrics;
import com.example.digitalwellbeinganalyzer.BehaviorMetricsCalculator;
import com.example.digitalwellbeinganalyzer.ContextualAnalysisCalculator;
import com.example.digitalwellbeinganalyzer.ContextualUsageMetrics;
import com.example.digitalwellbeinganalyzer.InsightsEngine;
import com.example.digitalwellbeinganalyzer.InsightsResult;
import com.example.digitalwellbeinganalyzer.R;
import com.example.digitalwellbeinganalyzer.UsageStatsHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GuideActivity extends AppCompatActivity {

    private static final String PREFS_NAME =
            "guide_daily_tasks";


    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();


    private CheckBox breakTaskCheckBox;
    private CheckBox nightTaskCheckBox;
    private CheckBox productivityTaskCheckBox;

    private TextView guidesWelcomeText;

    private TextView tasksLeftText;

    private TextView dailyFocusDescriptionText;
    private TextView dailyFocusGoalText;

    private TextView breakSuggestionDescriptionText;
    private TextView lateNightSuggestionDescriptionText;
    private TextView productivitySuggestionDescriptionText;

    private TextView mindfulnessStreakText;
    private TextView weeklyGoalDescriptionText;

    private TextView averageFocusText;
    private TextView digitalBloomLevelText;

    private ProgressBar mindfulnessProgress;

    private SharedPreferences preferences;

    private String todayKey;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_guide
        );


        preferences =
                getSharedPreferences(
                        PREFS_NAME,
                        MODE_PRIVATE
                );


        todayKey =
                new SimpleDateFormat(
                        "yyyyMMdd",
                        Locale.US
                ).format(
                        new Date()
                );


        connectViews();

        setupWelcomeText();

        setupSettings();

        setupBottomNavigation();

        restoreTaskState();

        setupTaskCheckboxes();

        setupGuideCards();

        showLoadingState();
    }


    // =========================================================
    // RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        loadRealGuideData();
    }


    // =========================================================
    // CONNECT VIEWS
    // =========================================================

    private void connectViews() {

        guidesWelcomeText =
                findViewById(
                        R.id.guidesWelcomeText
                );


        breakTaskCheckBox =
                findViewById(
                        R.id.breakTaskCheckBox
                );


        nightTaskCheckBox =
                findViewById(
                        R.id.nightTaskCheckBox
                );


        productivityTaskCheckBox =
                findViewById(
                        R.id.productivityTaskCheckBox
                );


        tasksLeftText =
                findViewById(
                        R.id.tasksLeftText
                );


        dailyFocusDescriptionText =
                findViewById(
                        R.id.dailyFocusDescriptionText
                );


        dailyFocusGoalText =
                findViewById(
                        R.id.dailyFocusGoalText
                );


        breakSuggestionDescriptionText =
                findViewById(
                        R.id.breakSuggestionDescriptionText
                );


        lateNightSuggestionDescriptionText =
                findViewById(
                        R.id.lateNightSuggestionDescriptionText
                );


        productivitySuggestionDescriptionText =
                findViewById(
                        R.id.productivitySuggestionDescriptionText
                );


        mindfulnessStreakText =
                findViewById(
                        R.id.mindfulnessStreakText
                );


        weeklyGoalDescriptionText =
                findViewById(
                        R.id.weeklyGoalDescriptionText
                );


        mindfulnessProgress =
                findViewById(
                        R.id.mindfulnessProgress
                );


        averageFocusText =
                findViewById(
                        R.id.averageFocusText
                );


        digitalBloomLevelText =
                findViewById(
                        R.id.digitalBloomLevelText
                );
    }


    // =========================================================
    // HEADER
    // =========================================================

    private void setupWelcomeText() {

        FirebaseUser user =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();


        String name =
                "Student";


        if (user != null) {

            if (user.getDisplayName() != null &&
                    !user.getDisplayName()
                            .trim()
                            .isEmpty()) {

                name =
                        user.getDisplayName();

            } else if (
                    user.getEmail() != null &&
                            !user.getEmail()
                                    .trim()
                                    .isEmpty()
            ) {

                String email =
                        user.getEmail();


                int atIndex =
                        email.indexOf("@");


                if (atIndex > 0) {

                    name =
                            email.substring(
                                    0,
                                    atIndex
                            );
                }
            }
        }


        int hour =
                java.util.Calendar
                        .getInstance()
                        .get(
                                java.util.Calendar.HOUR_OF_DAY
                        );


        String greeting;


        if (hour < 12) {

            greeting =
                    "Good morning";

        } else if (hour < 17) {

            greeting =
                    "Good afternoon";

        } else {

            greeting =
                    "Good evening";
        }


        guidesWelcomeText.setText(
                greeting
                        + ", "
                        + name
        );
    }


    // =========================================================
    // SETTINGS
    // =========================================================

    private void setupSettings() {

        findViewById(
                R.id.settingsIcon
        ).setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            Settings.ACTION_USAGE_ACCESS_SETTINGS
                    );


            startActivity(
                    intent
            );
        });
    }


    // =========================================================
    // REAL GUIDE DATA
    // =========================================================

    private void loadRealGuideData() {

        if (!UsageStatsHelper
                .hasUsageStatsPermission(this)) {

            showPermissionRequiredState();

            return;
        }


        executor.execute(() -> {

            BehaviorMetrics behavior =
                    BehaviorMetricsCalculator
                            .calculateToday(
                                    getApplicationContext()
                            );


            ContextualUsageMetrics contextual =
                    ContextualAnalysisCalculator
                            .calculateToday(
                                    getApplicationContext()
                            );


            InsightsResult insights =
                    InsightsEngine
                            .generateToday(
                                    getApplicationContext()
                            );


            runOnUiThread(() -> {

                if (!isActivityUsable()) {

                    return;
                }


                updateGuideData(
                        behavior,
                        contextual,
                        insights
                );
            });
        });
    }


    // =========================================================
    // UPDATE GUIDE UI
    // =========================================================

    private void updateGuideData(
            BehaviorMetrics behavior,
            ContextualUsageMetrics contextual,
            InsightsResult insights
    ) {

        if (behavior == null ||
                contextual == null ||
                insights == null) {

            showLoadingState();

            return;
        }


        // =====================================================
        // DAILY FOCUS
        // =====================================================

        dailyFocusDescriptionText.setText(
                buildDailyFocusDescription(
                        behavior
                )
        );


        dailyFocusGoalText.setText(
                buildDailyGoal(
                        behavior,
                        contextual
                )
        );


        // =====================================================
        // BREAK SUGGESTION
        // =====================================================

        if (behavior.longestSessionMs > 0) {

            breakSuggestionDescriptionText.setText(
                    "Your longest session today was "
                            + formatDurationReadable(
                            behavior.longestSessionMs
                    )
                            + ". Take short breaks during longer sessions to reduce continuous screen exposure."
            );

        } else {

            breakSuggestionDescriptionText.setText(
                    "No meaningful app session has been recorded yet today. Short breaks during longer sessions can help maintain focus."
            );
        }


        // =====================================================
        // LATE NIGHT
        // =====================================================

        if (contextual.lateNightUsageMs > 0) {

            lateNightSuggestionDescriptionText.setText(
                    "MindTrack recorded "
                            + formatDurationReadable(
                            contextual.lateNightUsageMs
                    )
                            + " of late-night usage today. Consider reducing screen activity between 11 PM and 5 AM."
            );

        } else {

            lateNightSuggestionDescriptionText.setText(
                    "No late-night usage has been detected today. Keeping screen use lower between 11 PM and 5 AM can support a more consistent nighttime routine."
            );
        }


        // =====================================================
        // PRODUCTIVITY
        // =====================================================

        if (behavior.totalScreenTimeMs > 0) {

            int productivityPercent =
                    (int) Math.round(
                            behavior.productivityPercent
                    );


            productivitySuggestionDescriptionText.setText(
                    productivityPercent
                            + "% of today's recorded screen time was classified as Education or Productivity. Use this as a guide when balancing your app usage."
            );

        } else {

            productivitySuggestionDescriptionText.setText(
                    "MindTrack needs more usage data before it can estimate today's productive app usage."
            );
        }


        // =====================================================
        // REAL CONSISTENCY STREAK
        // =====================================================

        int streak =
                Math.max(
                        0,
                        insights.consistencyStreakDays
                );


        if (streak == 1) {

            mindfulnessStreakText.setText(
                    "Productive Streak: 1 Day"
            );

        } else {

            mindfulnessStreakText.setText(
                    "Productive Streak: "
                            + streak
                            + " Days"
            );
        }


        int progress =
                (int) Math.round(
                        (Math.min(
                                streak,
                                7
                        ) / 7.0)
                                * 100.0
                );


        mindfulnessProgress.setProgress(
                progress
        );


        if (streak >= 7) {

            weeklyGoalDescriptionText.setText(
                    "You reached the 7-day productive-usage consistency goal."
            );

        } else if (streak > 0) {

            weeklyGoalDescriptionText.setText(
                    streak
                            + " of 7 consecutive days currently meet the productive-usage target."
            );

        } else {

            weeklyGoalDescriptionText.setText(
                    "Reach at least 30 minutes of Education or Productivity usage to begin a consistency streak."
            );
        }


        // =====================================================
        // DEEP FOCUS
        // =====================================================

        averageFocusText.setText(
                formatMinutes(
                        insights.deepFocusMs
                )
        );


        // =====================================================
        // DAILY TASK COMPLETION
        // =====================================================

        updateTaskProgress();
    }


    // =========================================================
    // DAILY FOCUS HELPERS
    // =========================================================

    private String buildDailyFocusDescription(
            BehaviorMetrics behavior
    ) {

        if (behavior.totalScreenTimeMs <= 0) {

            return "MindTrack is waiting for enough usage data to build today's guidance.";
        }


        return "You've recorded "
                + formatDurationReadable(
                behavior.totalScreenTimeMs
        )
                + " of meaningful app usage today across "
                + behavior.uniqueAppsUsed
                + (
                behavior.uniqueAppsUsed == 1
                        ? " app."
                        : " apps."
        );
    }


    private String buildDailyGoal(
            BehaviorMetrics behavior,
            ContextualUsageMetrics contextual
    ) {

        if (contextual.lateNightUsageMs > 0) {

            return "Today's goal: Reduce late-night screen use";
        }


        if (behavior.longestSessionMs >=
                60L * 60L * 1000L) {

            return "Today's goal: Take a break after long sessions";
        }


        if (behavior.totalScreenTimeMs > 0 &&
                behavior.productivityPercent < 30.0) {

            return "Today's goal: Build a more balanced app mix";
        }


        return "Today's goal: Maintain intentional screen habits";
    }


    // =========================================================
    // LOADING / PERMISSION STATES
    // =========================================================

    private void showLoadingState() {

        dailyFocusDescriptionText.setText(
                "Analyzing today's usage patterns."
        );


        dailyFocusGoalText.setText(
                "Today's goal: Building your guidance"
        );


        breakSuggestionDescriptionText.setText(
                "Checking today's session lengths."
        );


        lateNightSuggestionDescriptionText.setText(
                "Checking today's late-night usage."
        );


        productivitySuggestionDescriptionText.setText(
                "Checking today's productive app usage."
        );


        mindfulnessStreakText.setText(
                "Productive Streak: --"
        );


        weeklyGoalDescriptionText.setText(
                "Checking recent productive-usage consistency."
        );


        mindfulnessProgress.setProgress(
                0
        );


        averageFocusText.setText(
                "--"
        );


        updateTaskProgress();
    }


    private void showPermissionRequiredState() {

        dailyFocusDescriptionText.setText(
                "Usage Access is required before MindTrack can generate guidance from your app activity."
        );


        dailyFocusGoalText.setText(
                "Today's goal: Enable Usage Access"
        );


        breakSuggestionDescriptionText.setText(
                "Enable Usage Access to analyze session lengths."
        );


        lateNightSuggestionDescriptionText.setText(
                "Enable Usage Access to analyze late-night usage."
        );


        productivitySuggestionDescriptionText.setText(
                "Enable Usage Access to analyze productive app usage."
        );


        mindfulnessStreakText.setText(
                "Productive Streak: --"
        );


        weeklyGoalDescriptionText.setText(
                "Usage history is unavailable until Usage Access is enabled."
        );


        mindfulnessProgress.setProgress(
                0
        );


        averageFocusText.setText(
                "--"
        );
    }


    // =========================================================
    // GUIDE CARD CLICKS
    // =========================================================

    private void setupGuideCards() {

        findViewById(
                R.id.dailyFocusCard
        ).setOnClickListener(v -> {

            Toast.makeText(
                    GuideActivity.this,
                    dailyFocusGoalText.getText(),
                    Toast.LENGTH_SHORT
            ).show();
        });


        findViewById(
                R.id.takeBreaksCard
        ).setOnClickListener(v -> {

            Toast.makeText(
                    GuideActivity.this,
                    "Short breaks can help interrupt long continuous screen sessions.",
                    Toast.LENGTH_SHORT
            ).show();
        });


        findViewById(
                R.id.lateNightUsageCard
        ).setOnClickListener(v -> {

            Toast.makeText(
                    GuideActivity.this,
                    "MindTrack treats 11 PM to 5 AM as the current late-night analysis window.",
                    Toast.LENGTH_SHORT
            ).show();
        });


        findViewById(
                R.id.manageScreenTimeCard
        ).setOnClickListener(v -> {

            Toast.makeText(
                    GuideActivity.this,
                    "Education and Productivity apps contribute to MindTrack's productive-usage metric.",
                    Toast.LENGTH_SHORT
            ).show();
        });


        findViewById(
                R.id.weeklyGoalCard
        ).setOnClickListener(v -> {

            Toast.makeText(
                    GuideActivity.this,
                    "A day counts toward the streak after at least 30 minutes of productive usage.",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }


    // =========================================================
    // DAILY TASK STORAGE
    // =========================================================

    private void restoreTaskState() {

        breakTaskCheckBox.setChecked(
                preferences.getBoolean(
                        todayKey + "_break",
                        false
                )
        );


        nightTaskCheckBox.setChecked(
                preferences.getBoolean(
                        todayKey + "_night",
                        false
                )
        );


        productivityTaskCheckBox.setChecked(
                preferences.getBoolean(
                        todayKey + "_productivity",
                        false
                )
        );


        updateTaskProgress();
    }


    private void setupTaskCheckboxes() {

        breakTaskCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    preferences
                            .edit()
                            .putBoolean(
                                    todayKey + "_break",
                                    isChecked
                            )
                            .apply();


                    updateTaskProgress();
                }
        );


        nightTaskCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    preferences
                            .edit()
                            .putBoolean(
                                    todayKey + "_night",
                                    isChecked
                            )
                            .apply();


                    updateTaskProgress();
                }
        );


        productivityTaskCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    preferences
                            .edit()
                            .putBoolean(
                                    todayKey + "_productivity",
                                    isChecked
                            )
                            .apply();


                    updateTaskProgress();
                }
        );
    }


    // =========================================================
    // TASK PROGRESS
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


        int remainingTasks =
                3 -
                        completedTasks;


        if (remainingTasks == 0) {

            tasksLeftText.setText(
                    "ALL TASKS DONE"
            );

        } else if (remainingTasks == 1) {

            tasksLeftText.setText(
                    "1 TASK LEFT"
            );

        } else {

            tasksLeftText.setText(
                    remainingTasks
                            + " TASKS LEFT"
            );
        }


        digitalBloomLevelText.setText(
                completedTasks
                        + " / 3"
        );
    }


    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private void setupBottomNavigation() {

        findViewById(
                R.id.navHome
        ).setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            GuideActivity.this,
                            DashboardActivity.class
                    );


            startActivity(
                    intent
            );


            finish();
        });


        findViewById(
                R.id.navAnalytics
        ).setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            GuideActivity.this,
                            M2AnalyticsActivity.class
                    );


            startActivity(
                    intent
            );


            finish();
        });


        findViewById(
                R.id.navGuides
        ).setOnClickListener(v -> {
            // Already on Guides.
        });


        findViewById(
                R.id.navProfile
        ).setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            GuideActivity.this,
                            ProfileActivity.class
                    );


            startActivity(
                    intent
            );


            finish();
        });
    }


    // =========================================================
    // FORMAT HELPERS
    // =========================================================

    private String formatMinutes(
            long milliseconds
    ) {

        if (milliseconds <= 0) {

            return "0m";
        }


        long minutes =
                milliseconds /
                        (60L * 1000L);


        return minutes
                + "m";
    }


    private String formatDurationReadable(
            long milliseconds
    ) {

        if (milliseconds <= 0) {

            return "0 minutes";
        }


        long totalMinutes =
                milliseconds /
                        (60L * 1000L);


        if (totalMinutes <= 0) {

            return "less than 1 minute";
        }


        long hours =
                totalMinutes /
                        60L;


        long minutes =
                totalMinutes %
                        60L;


        if (hours <= 0) {

            return totalMinutes
                    + (
                    totalMinutes == 1
                            ? " minute"
                            : " minutes"
            );
        }


        if (minutes <= 0) {

            return hours
                    + (
                    hours == 1
                            ? " hour"
                            : " hours"
            );
        }


        return hours
                + "h "
                + minutes
                + "m";
    }


    // =========================================================
    // LIFECYCLE
    // =========================================================

    private boolean isActivityUsable() {

        return !isFinishing()
                && !isDestroyed();
    }


    @Override
    protected void onDestroy() {

        executor.shutdownNow();

        super.onDestroy();
    }
}
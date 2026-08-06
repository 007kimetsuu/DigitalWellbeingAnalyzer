package com.example.digitalwellbeinganalyzer.views;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalwellbeinganalyzer.R;

import java.util.Calendar;
import java.util.List;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView analyticsScreenTimeText;
    private TextView sessionCountText;
    private TextView averageSessionText;
    private TextView trendSummaryText;
    private TextView behaviourInsightText;
    private TextView patternText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        connectViews();

        loadAnalyticsData();
        findViewById(R.id.backButton).setOnClickListener(v -> {
            finish();
        });
    }

    private void connectViews() {

        analyticsScreenTimeText =
                findViewById(R.id.analyticsScreenTimeText);

        sessionCountText =
                findViewById(R.id.sessionCountText);

        averageSessionText =
                findViewById(R.id.averageSessionText);

        trendSummaryText =
                findViewById(R.id.trendSummaryText);

        behaviourInsightText =
                findViewById(R.id.behaviourInsightText);

        patternText =
                findViewById(R.id.patternText);
    }

    private void loadAnalyticsData() {

        if (!hasUsageAccessPermission()) {

            analyticsScreenTimeText.setText("--");
            sessionCountText.setText("--");
            averageSessionText.setText("--");

            trendSummaryText.setText(
                    "Usage Access permission is required to calculate your analytics."
            );

            behaviourInsightText.setText(
                    "Enable Usage Access from Android Settings to begin analysing your usage."
            );

            patternText.setText(
                    "No usage pattern is available yet."
            );

            return;
        }

        UsageStatsManager usageStatsManager =
                (UsageStatsManager)
                        getSystemService(Context.USAGE_STATS_SERVICE);

        if (usageStatsManager == null) {
            return;
        }

        long endTime = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startTime = calendar.getTimeInMillis();

        List<UsageStats> usageStatsList =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        startTime,
                        endTime
                );

        if (usageStatsList == null || usageStatsList.isEmpty()) {

            analyticsScreenTimeText.setText("0h 0m");
            sessionCountText.setText("0");
            averageSessionText.setText("0m");

            trendSummaryText.setText(
                    "No usage data has been recorded today."
            );

            behaviourInsightText.setText(
                    "Use your device normally and return later to see insights."
            );

            patternText.setText(
                    "Your usage pattern will appear here."
            );

            return;
        }

        long totalUsageMillis = 0;
        int appsWithUsage = 0;

        for (UsageStats usageStats : usageStatsList) {

            long usageTime =
                    usageStats.getTotalTimeInForeground();

            if (usageTime > 0) {

                totalUsageMillis += usageTime;
                appsWithUsage++;
            }
        }

        long totalMinutes =
                totalUsageMillis / (1000 * 60);

        long hours =
                totalMinutes / 60;

        long minutes =
                totalMinutes % 60;

        analyticsScreenTimeText.setText(
                hours + "h " + minutes + "m"
        );

        /*
         * At this stage we use the number of apps with
         * recorded foreground activity as a temporary
         * activity indicator.
         *
         * M1's session tracking will replace this with
         * actual session data later.
         */
        sessionCountText.setText(
                String.valueOf(appsWithUsage)
        );

        if (appsWithUsage > 0) {

            long averageMinutes =
                    totalMinutes / appsWithUsage;

            averageSessionText.setText(
                    averageMinutes + "m"
            );

        } else {

            averageSessionText.setText("0m");
        }

        generateBasicInsights(
                totalMinutes,
                appsWithUsage
        );
    }

    private void generateBasicInsights(
            long totalMinutes,
            int activeApps
    ) {

        if (totalMinutes == 0) {

            trendSummaryText.setText(
                    "There is not enough usage data to identify a trend yet."
            );

            behaviourInsightText.setText(
                    "Keep using your device normally so MindTrack can build a usage profile."
            );

            patternText.setText(
                    "No significant pattern detected yet."
            );

            return;
        }

        if (totalMinutes < 120) {

            trendSummaryText.setText(
                    "Your screen time is currently below 2 hours today."
            );

        } else if (totalMinutes < 300) {

            trendSummaryText.setText(
                    "You have accumulated a moderate amount of screen time today."
            );

        } else {

            trendSummaryText.setText(
                    "You have spent more than 5 hours on your device today."
            );
        }

        if (activeApps >= 8) {

            behaviourInsightText.setText(
                    "Your usage is spread across several applications today."
            );

        } else if (activeApps >= 3) {

            behaviourInsightText.setText(
                    "Your usage is currently concentrated across a few applications."
            );

        } else {

            behaviourInsightText.setText(
                    "Your usage is currently concentrated in a small number of applications."
            );
        }

        patternText.setText(
                "Detailed behavioural patterns will become available "
                        + "after interaction tracking is implemented."
        );
    }

    private boolean hasUsageAccessPermission() {

        AppOpsManager appOps =
                (AppOpsManager)
                        getSystemService(Context.APP_OPS_SERVICE);

        if (appOps == null) {
            return false;
        }

        int mode =
                appOps.checkOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        Process.myUid(),
                        getPackageName()
                );

        return mode == AppOpsManager.MODE_ALLOWED;
    }
}
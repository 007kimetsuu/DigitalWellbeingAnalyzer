package com.example.digitalwellbeinganalyzer.views;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalwellbeinganalyzer.R;

import java.util.Calendar;
import java.util.List;

public class AppUsageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("APP_USAGE", "AppUsageActivity started");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_app_usage);

        // Connect UI elements
        TextView totalScreenTimeText =
                findViewById(R.id.totalScreenTimeText);

        LinearLayout appUsageContainer =
                findViewById(R.id.appUsageContainer);

        // Android PackageManager
        PackageManager packageManager =
                getPackageManager();

        // Android UsageStatsManager
        UsageStatsManager usageStatsManager =
                (UsageStatsManager)
                        getSystemService(USAGE_STATS_SERVICE);

        // Current time
        long endTime =
                System.currentTimeMillis();

        // Start of today: 12:00 AM
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

        // Get today's usage statistics
        List<UsageStats> usageStatsList =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        startTime,
                        endTime
                );

        if (usageStatsList == null) {

            Log.d(
                    "APP_USAGE",
                    "Usage stats list is NULL"
            );

        } else {

            Log.d(
                    "APP_USAGE",
                    "Usage stats list size: "
                            + usageStatsList.size()
            );

            long totalUsageTime = 0;

            // Loop through all usage records
            for (UsageStats usageStats :
                    usageStatsList) {

                long totalTime =
                        usageStats
                                .getTotalTimeInForeground();

                // Add to total screen time
                totalUsageTime += totalTime;

                // Convert this app's usage to minutes
                long appMinutes =
                        totalTime / (1000 * 60);

                // Package name
                String packageName =
                        usageStats.getPackageName();

                ApplicationInfo applicationInfo;

                // Get application information
                try {

                    applicationInfo =
                            packageManager
                                    .getApplicationInfo(
                                            packageName,
                                            0
                                    );

                } catch (
                        PackageManager
                                .NameNotFoundException e
                ) {

                    continue;
                }

                // Check whether it is a system app
                boolean isSystemApp =
                        (applicationInfo.flags
                                & ApplicationInfo.FLAG_SYSTEM)
                                != 0;

                // Check whether it is an updated system app
                boolean isUpdatedSystemApp =
                        (applicationInfo.flags
                                & ApplicationInfo
                                .FLAG_UPDATED_SYSTEM_APP)
                                != 0;

                // Debug information
                Log.d(
                        "APP_FILTER",
                        "Checking: "
                                + packageName
                                + " | Minutes: "
                                + appMinutes
                                + " | System: "
                                + isSystemApp
                                + " | UpdatedSystem: "
                                + isUpdatedSystemApp
                );

                // Ignore genuine system apps
                // but allow updated system apps


                // Ignore our own Digital Wellbeing app
                if (packageName.equals(
                        getPackageName())) {

                    continue;
                }

                // Ignore apps used for less than 1 minute
                if (appMinutes <= 0) {

                    continue;
                }

                // Get readable app name
                String appName;

                try {

                    appName =
                            packageManager
                                    .getApplicationLabel(
                                            applicationInfo
                                    )
                                    .toString();

                } catch (Exception e) {

                    appName = packageName;
                }

                // Convert usage to hours/minutes
                long appHours =
                        appMinutes / 60;

                long remainingMinutes =
                        appMinutes % 60;

                // Log final app information
                Log.d(
                        "APP_USAGE",
                        "App: "
                                + appName
                                + " | Package: "
                                + packageName
                                + " | Time: "
                                + appHours
                                + "h "
                                + remainingMinutes
                                + "m"
                );

                // Create TextView
                TextView appText =
                        new TextView(this);

                appText.setText(
                        appName
                                + "    "
                                + appHours
                                + "h "
                                + remainingMinutes
                                + "m"
                );

                appText.setTextSize(16);

                appText.setTextColor(
                        getResources()
                                .getColor(
                                        R.color.text_primary
                                )
                );

                appText.setPadding(
                        0,
                        12,
                        0,
                        12
                );

                // Add app to UI
                appUsageContainer.addView(
                        appText
                );
            }

            // Convert total usage to minutes
            long totalMinutes =
                    totalUsageTime / (1000 * 60);

            long hours =
                    totalMinutes / 60;

            long minutes =
                    totalMinutes % 60;

            // Log total usage
            Log.d(
                    "APP_USAGE",
                    "TOTAL USAGE: "
                            + hours
                            + "h "
                            + minutes
                            + "m"
            );

            // Display today's total
            totalScreenTimeText.setText(
                    hours
                            + "h "
                            + minutes
                            + "m"
            );
        }

        // Back button
        Button backButton =
                findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            AppUsageActivity.this,
                            DashboardActivity.class
                    );

            startActivity(intent);
            finish();
        });
    }
}
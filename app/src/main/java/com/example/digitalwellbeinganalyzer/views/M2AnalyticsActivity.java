package com.example.digitalwellbeinganalyzer.views;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.digitalwellbeinganalyzer.R;
import com.example.digitalwellbeinganalyzer.RiskDataRepository;
import com.example.digitalwellbeinganalyzer.RiskEngineResult;
import com.example.digitalwellbeinganalyzer.RiskReading;
import com.example.digitalwellbeinganalyzer.UsageStatsHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class M2AnalyticsActivity extends AppCompatActivity {

    private CardView mindfulStatusCard;
    private CardView insightsCard;

    private TextView dailyAverageText;
    private TextView riskStatusText;
    private TextView criticalPeaksText;
    private TextView averageSessionText;

    private View[] weeklyBars;
    private TextView[] weeklyDayLabels;

    private View[] categoryBars;
    private TextView[] categoryLabels;

    private GridLayout hourlyHeatmapGrid;

    private TextView appName1;
    private TextView appName2;
    private TextView appName3;

    private TextView appSubtitle1;
    private TextView appSubtitle2;
    private TextView appSubtitle3;

    private TextView appUsage1;
    private TextView appUsage2;
    private TextView appUsage3;

    private RiskDataRepository repository;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_m2_analytics
        );


        repository =
                new RiskDataRepository(this);


        connectViews();

        setupNavigation();

        loadAnalyticsData();
    }


    // =========================================================
    // CONNECT VIEWS
    // =========================================================

    private void connectViews() {

        mindfulStatusCard =
                findViewById(
                        R.id.mindfulStatusCard
                );


        insightsCard =
                findViewById(
                        R.id.insightsCard
                );


        dailyAverageText =
                findViewById(
                        R.id.dailyAverageText
                );


        riskStatusText =
                findViewById(
                        R.id.riskStatusText
                );


        criticalPeaksText =
                findViewById(
                        R.id.criticalPeaksText
                );


        averageSessionText =
                findViewById(
                        R.id.averageSessionText
                );


        weeklyBars =
                new View[]{

                        findViewById(
                                R.id.weekBar1
                        ),

                        findViewById(
                                R.id.weekBar2
                        ),

                        findViewById(
                                R.id.weekBar3
                        ),

                        findViewById(
                                R.id.weekBar4
                        ),

                        findViewById(
                                R.id.weekBar5
                        ),

                        findViewById(
                                R.id.weekBar6
                        ),

                        findViewById(
                                R.id.weekBar7
                        )
                };


        weeklyDayLabels =
                new TextView[]{

                        findViewById(
                                R.id.weekDay1
                        ),

                        findViewById(
                                R.id.weekDay2
                        ),

                        findViewById(
                                R.id.weekDay3
                        ),

                        findViewById(
                                R.id.weekDay4
                        ),

                        findViewById(
                                R.id.weekDay5
                        ),

                        findViewById(
                                R.id.weekDay6
                        ),

                        findViewById(
                                R.id.weekDay7
                        )
                };


        categoryBars =
                new View[]{

                        findViewById(
                                R.id.categoryBar1
                        ),

                        findViewById(
                                R.id.categoryBar2
                        ),

                        findViewById(
                                R.id.categoryBar3
                        ),

                        findViewById(
                                R.id.categoryBar4
                        )
                };


        categoryLabels =
                new TextView[]{

                        findViewById(
                                R.id.categoryLabel1
                        ),

                        findViewById(
                                R.id.categoryLabel2
                        ),

                        findViewById(
                                R.id.categoryLabel3
                        ),

                        findViewById(
                                R.id.categoryLabel4
                        )
                };


        hourlyHeatmapGrid =
                findViewById(
                        R.id.hourlyHeatmapGrid
                );


        appName1 =
                findViewById(
                        R.id.appName1
                );


        appName2 =
                findViewById(
                        R.id.appName2
                );


        appName3 =
                findViewById(
                        R.id.appName3
                );


        appSubtitle1 =
                findViewById(
                        R.id.appSubtitle1
                );


        appSubtitle2 =
                findViewById(
                        R.id.appSubtitle2
                );


        appSubtitle3 =
                findViewById(
                        R.id.appSubtitle3
                );


        appUsage1 =
                findViewById(
                        R.id.appUsage1
                );


        appUsage2 =
                findViewById(
                        R.id.appUsage2
                );


        appUsage3 =
                findViewById(
                        R.id.appUsage3
                );
    }


    // =========================================================
    // LOAD ALL ANALYTICS
    // =========================================================

    private void loadAnalyticsData() {

        if (!UsageStatsHelper
                .hasUsageStatsPermission(this)) {

            showUsagePermissionState();

            return;
        }


        loadWeeklyScreenTime();

        loadFinalRisk();

        loadCriticalReadings();

        loadAverageSession();

        loadCategoryBreakdown();

        loadHourlyActivity();

        loadMostUsedApps();
    }


    // =========================================================
    // WEEKLY SCREEN TIME
    // =========================================================

    private void loadWeeklyScreenTime() {

        executor.execute(() -> {

            List<UsageStatsHelper.DailyUsageSummary> days =
                    UsageStatsHelper
                            .getLast7DaysUsage(
                                    getApplicationContext()
                            );


            long average =
                    UsageStatsHelper
                            .getAverageDailyUsageLast7Days(
                                    getApplicationContext()
                            );


            runOnUiThread(() -> {

                if (!isActivityUsable()) {
                    return;
                }


                dailyAverageText.setText(
                        "Daily average • "
                                + formatDuration(
                                average
                        )
                );


                displayWeeklyBars(
                        days
                );
            });
        });
    }


    private void displayWeeklyBars(
            List<UsageStatsHelper.DailyUsageSummary> days
    ) {

        if (days == null ||
                days.isEmpty()) {

            return;
        }


        long maxUsage =
                0L;


        for (UsageStatsHelper.DailyUsageSummary day :
                days) {

            maxUsage =
                    Math.max(
                            maxUsage,
                            day.getTotalUsageMs()
                    );
        }


        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "EEE",
                        Locale.getDefault()
                );


        int count =
                Math.min(
                        days.size(),
                        weeklyBars.length
                );


        for (int i = 0;
             i < count;
             i++) {

            UsageStatsHelper.DailyUsageSummary day =
                    days.get(i);


            long usage =
                    day.getTotalUsageMs();


            int heightDp;


            if (usage <= 0 ||
                    maxUsage <= 0) {

                heightDp =
                        6;

            } else {

                double ratio =
                        (double) usage /
                                (double) maxUsage;


                heightDp =
                        12 +
                                (int) Math.round(
                                        ratio * 100
                                );
            }


            setViewHeightDp(
                    weeklyBars[i],
                    heightDp
            );


            weeklyDayLabels[i].setText(

                    formatter.format(
                            new Date(
                                    day.getDayStartTime()
                            )
                    )
            );
        }
    }


    // =========================================================
    // FINAL RISK
    // =========================================================

    private void loadFinalRisk() {

        repository.getFinalRiskToday(

                this,

                result -> runOnUiThread(() -> {

                    if (!isActivityUsable()) {
                        return;
                    }


                    if (result == null) {

                        riskStatusText.setText(
                                "Not enough risk data yet"
                        );

                        return;
                    }


                    displayRiskStatus(
                            result
                    );
                })
        );
    }


    private void displayRiskStatus(
            RiskEngineResult result
    ) {

        String status;


        switch (result.finalRiskLevel) {

            case RiskEngineResult.LEVEL_HIGH:

                status =
                        "High behavioral risk • Score "
                                + result.finalRiskScore;

                break;


            case RiskEngineResult.LEVEL_MEDIUM:

                status =
                        "Moderate behavioral risk • Score "
                                + result.finalRiskScore;

                break;


            case RiskEngineResult.LEVEL_LOW:

            default:

                status =
                        "Low behavioral risk • Score "
                                + result.finalRiskScore;

                break;
        }


        riskStatusText.setText(
                status
        );
    }


    // =========================================================
    // HIGH-RISK READINGS - LAST 7 DAYS
    // =========================================================

    private void loadCriticalReadings() {

        repository.getAllHistory(

                readings -> {

                    int count =
                            countHighRiskReadingsLast7Days(
                                    readings
                            );


                    runOnUiThread(() -> {

                        if (!isActivityUsable()) {
                            return;
                        }


                        criticalPeaksText.setText(
                                String.valueOf(
                                        count
                                )
                        );
                    });
                }
        );
    }


    private int countHighRiskReadingsLast7Days(
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


        calendar.add(
                Calendar.DAY_OF_YEAR,
                -6
        );


        long startTime =
                calendar.getTimeInMillis();


        int count =
                0;


        for (RiskReading reading :
                readings) {

            if (reading != null &&
                    reading.timestamp >= startTime &&
                    reading.riskLevel ==
                            RiskEngineResult.LEVEL_HIGH) {

                count++;
            }
        }


        return count;
    }


    // =========================================================
    // AVERAGE SESSION - LAST 7 DAYS
    // =========================================================

    private void loadAverageSession() {

        executor.execute(() -> {

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


            calendar.add(
                    Calendar.DAY_OF_YEAR,
                    -6
            );


            List<UsageStatsHelper.AppSession> sessions =
                    UsageStatsHelper
                            .getAppSessionsBetween(

                                    getApplicationContext(),

                                    calendar.getTimeInMillis(),

                                    System.currentTimeMillis()
                            );


            long totalDuration =
                    0L;


            int validSessions =
                    0;


            for (UsageStatsHelper.AppSession session :
                    sessions) {

                if (session == null) {
                    continue;
                }


                long duration =
                        session.getDurationMs();


                if (duration <= 0) {
                    continue;
                }


                totalDuration +=
                        duration;

                validSessions++;
            }


            long average =
                    validSessions == 0
                            ? 0L
                            : totalDuration
                              / validSessions;


            runOnUiThread(() -> {

                if (!isActivityUsable()) {
                    return;
                }


                averageSessionText.setText(
                        formatDuration(
                                average
                        )
                );
            });
        });
    }


    // =========================================================
    // CATEGORY BREAKDOWN
    // =========================================================

    private void loadCategoryBreakdown() {

        repository
                .getCategoryBreakdownToday(

                        this,

                        totals -> runOnUiThread(() -> {

                            if (!isActivityUsable()) {
                                return;
                            }


                            displayCategoryBreakdown(
                                    totals
                            );
                        })
                );
    }


    private void displayCategoryBreakdown(
            Map<String, Long> totals
    ) {

        if (totals == null ||
                totals.isEmpty()) {

            clearCategoryBars();

            return;
        }


        List<Map.Entry<String, Long>> entries =
                new ArrayList<>(
                        totals.entrySet()
                );


        Collections.sort(

                entries,

                new Comparator<Map.Entry<String, Long>>() {

                    @Override
                    public int compare(
                            Map.Entry<String, Long> first,
                            Map.Entry<String, Long> second
                    ) {

                        return Long.compare(
                                second.getValue(),
                                first.getValue()
                        );
                    }
                }
        );


        long max =
                entries
                        .get(0)
                        .getValue();


        for (int i = 0;
             i < categoryBars.length;
             i++) {

            if (i >= entries.size()) {

                categoryLabels[i]
                        .setText("--");


                setViewHeightDp(
                        categoryBars[i],
                        6
                );


                continue;
            }


            Map.Entry<String, Long> entry =
                    entries.get(i);


            categoryLabels[i].setText(
                    shortenCategoryName(
                            entry.getKey()
                    )
            );


            long value =
                    entry.getValue();


            int heightDp;


            if (max <= 0 ||
                    value <= 0) {

                heightDp =
                        6;

            } else {

                double ratio =
                        (double) value /
                                (double) max;


                heightDp =
                        10 +
                                (int) Math.round(
                                        ratio * 78
                                );
            }


            setViewHeightDp(
                    categoryBars[i],
                    heightDp
            );
        }
    }


    private void clearCategoryBars() {

        for (int i = 0;
             i < categoryBars.length;
             i++) {

            categoryLabels[i]
                    .setText("--");


            setViewHeightDp(
                    categoryBars[i],
                    6
            );
        }
    }


    private String shortenCategoryName(
            String name
    ) {

        if (name == null ||
                name.trim().isEmpty()) {

            return "Other";
        }


        if (name.length() <= 10) {

            return name;
        }


        return name.substring(
                0,
                9
        ) + "…";
    }


    // =========================================================
    // HOURLY ACTIVITY
    // =========================================================

    private void loadHourlyActivity() {

        executor.execute(() -> {

            List<UsageStatsHelper.HourlyUsageSummary> hourly =
                    UsageStatsHelper
                            .getHourlyUsageLast7Days(
                                    getApplicationContext()
                            );


            runOnUiThread(() -> {

                if (!isActivityUsable()) {
                    return;
                }


                displayHourlyHeatmap(
                        hourly
                );
            });
        });
    }


    private void displayHourlyHeatmap(
            List<UsageStatsHelper.HourlyUsageSummary> hourly
    ) {

        if (hourlyHeatmapGrid == null) {

            return;
        }


        hourlyHeatmapGrid
                .removeAllViews();


        long maxUsage =
                0L;


        if (hourly != null) {

            for (UsageStatsHelper.HourlyUsageSummary item :
                    hourly) {

                maxUsage =
                        Math.max(
                                maxUsage,
                                item.getTotalUsageMs()
                        );
            }
        }


        for (int hour = 0;
             hour < 24;
             hour++) {

            long usage =
                    0L;


            if (hourly != null &&
                    hour < hourly.size()) {

                usage =
                        hourly
                                .get(hour)
                                .getTotalUsageMs();
            }


            View cell =
                    new View(this);


            GridLayout.LayoutParams params =
                    new GridLayout.LayoutParams();


            params.width =
                    0;


            params.height =
                    dpToPx(25);


            params.columnSpec =
                    GridLayout.spec(
                            hour % 12,
                            1f
                    );


            params.rowSpec =
                    GridLayout.spec(
                            hour / 12
                    );


            int margin =
                    dpToPx(2);


            params.setMargins(
                    margin,
                    margin,
                    margin,
                    margin
            );


            cell.setLayoutParams(
                    params
            );


            cell.setBackground(
                    createHeatmapBackground(
                            usage,
                            maxUsage
                    )
            );


            hourlyHeatmapGrid
                    .addView(
                            cell
                    );
        }
    }


    private GradientDrawable createHeatmapBackground(
            long usage,
            long maxUsage
    ) {

        GradientDrawable drawable =
                new GradientDrawable();


        drawable.setCornerRadius(
                dpToPx(3)
        );


        double ratio =
                maxUsage <= 0
                        ? 0
                        : (double) usage /
                          (double) maxUsage;


        if (ratio <= 0.10) {

            drawable.setColor(
                    Color.parseColor(
                            "#EEEFFC"
                    )
            );

        } else if (ratio <= 0.30) {

            drawable.setColor(
                    Color.parseColor(
                            "#D8D5F8"
                    )
            );

        } else if (ratio <= 0.55) {

            drawable.setColor(
                    Color.parseColor(
                            "#B8B0FF"
                    )
            );

        } else if (ratio <= 0.80) {

            drawable.setColor(
                    Color.parseColor(
                            "#8E83EC"
                    )
            );

        } else {

            drawable.setColor(
                    Color.parseColor(
                            "#5144DD"
                    )
            );
        }


        return drawable;
    }


    // =========================================================
    // MOST USED APPS
    // =========================================================

    private void loadMostUsedApps() {

        executor.execute(() -> {

            List<UsageStatsHelper.AppUsageItem> apps =
                    UsageStatsHelper
                            .getTopUsedAppsToday(

                                    getApplicationContext(),

                                    3
                            );


            runOnUiThread(() -> {

                if (!isActivityUsable()) {
                    return;
                }


                displayMostUsedApps(
                        apps
                );
            });
        });
    }


    private void displayMostUsedApps(
            List<UsageStatsHelper.AppUsageItem> apps
    ) {

        TextView[] nameViews =
                {

                        appName1,
                        appName2,
                        appName3
                };


        TextView[] subtitleViews =
                {

                        appSubtitle1,
                        appSubtitle2,
                        appSubtitle3
                };


        TextView[] usageViews =
                {

                        appUsage1,
                        appUsage2,
                        appUsage3
                };


        for (int i = 0;
             i < 3;
             i++) {

            if (apps != null &&
                    i < apps.size()) {

                UsageStatsHelper.AppUsageItem item =
                        apps.get(i);


                nameViews[i].setText(
                        item.getAppName()
                );


                subtitleViews[i].setText(
                        "Used today"
                );


                usageViews[i].setText(
                        formatDuration(
                                item.getTotalUsageMs()
                        )
                );

            } else {

                nameViews[i].setText(
                        "No app data"
                );


                subtitleViews[i].setText(
                        "Usage unavailable"
                );


                usageViews[i].setText(
                        "--"
                );
            }
        }
    }


    // =========================================================
    // PERMISSION STATE
    // =========================================================

    private void showUsagePermissionState() {

        dailyAverageText.setText(
                "Usage Access required"
        );


        riskStatusText.setText(
                "Enable Usage Access to analyze behavior"
        );


        criticalPeaksText.setText(
                "--"
        );


        averageSessionText.setText(
                "--"
        );


        clearCategoryBars();


        appName1.setText(
                "Usage Access required"
        );


        appName2.setText(
                "Usage Access required"
        );


        appName3.setText(
                "Usage Access required"
        );


        appSubtitle1.setText(
                "Enable permission in Settings"
        );


        appSubtitle2.setText(
                "Enable permission in Settings"
        );


        appSubtitle3.setText(
                "Enable permission in Settings"
        );


        appUsage1.setText(
                "--"
        );


        appUsage2.setText(
                "--"
        );


        appUsage3.setText(
                "--"
        );
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

            return "0m";
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


        if (hours > 0) {

            return hours
                    + "h "
                    + minutes
                    + "m";
        }


        return minutes
                + "m";
    }


    private void setViewHeightDp(
            View view,
            int heightDp
    ) {

        if (view == null) {

            return;
        }


        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams)
                        view.getLayoutParams();


        params.height =
                dpToPx(
                        heightDp
                );


        view.setLayoutParams(
                params
        );
    }


    private int dpToPx(
            int dp
    ) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;


        return Math.round(
                dp * density
        );
    }


    // =========================================================
    // NAVIGATION
    // =========================================================

    private void setupNavigation() {

        if (mindfulStatusCard != null) {

            mindfulStatusCard
                    .setOnClickListener(v -> {

                        Intent intent =
                                new Intent(
                                        M2AnalyticsActivity.this,
                                        activity_analytics_risk.class
                                );


                        startActivity(
                                intent
                        );
                    });
        }


        if (insightsCard != null) {

            insightsCard
                    .setOnClickListener(v -> {

                        Intent intent =
                                new Intent(
                                        M2AnalyticsActivity.this,
                                        InsightsActivity.class
                                );


                        startActivity(
                                intent
                        );
                    });
        }


        View navHome =
                findViewById(
                        R.id.navHome
                );


        View navAnalytics =
                findViewById(
                        R.id.navAnalytics
                );


        View navGuides =
                findViewById(
                        R.id.navGuides
                );


        View navProfile =
                findViewById(
                        R.id.navProfile
                );


        if (navHome != null) {

            navHome
                    .setOnClickListener(v -> {

                        Intent intent =
                                new Intent(
                                        M2AnalyticsActivity.this,
                                        DashboardActivity.class
                                );


                        startActivity(
                                intent
                        );

                        finish();
                    });
        }


        if (navAnalytics != null) {

            navAnalytics
                    .setOnClickListener(v -> {
                        // Already on Analytics.
                    });
        }


        if (navGuides != null) {

            navGuides
                    .setOnClickListener(v -> {

                        Intent intent =
                                new Intent(
                                        M2AnalyticsActivity.this,
                                        GuideActivity.class
                                );


                        startActivity(
                                intent
                        );

                        finish();
                    });
        }


        if (navProfile != null) {

            navProfile
                    .setOnClickListener(v -> {

                        Intent intent =
                                new Intent(
                                        M2AnalyticsActivity.this,
                                        ProfileActivity.class
                                );


                        startActivity(
                                intent
                        );

                        finish();
                    });
        }
    }


    // =========================================================
    // CLEANUP
    // =========================================================

    @Override
    protected void onDestroy() {

        executor.shutdownNow();


        if (repository != null) {

            repository.shutdown();
        }


        super.onDestroy();
    }
}
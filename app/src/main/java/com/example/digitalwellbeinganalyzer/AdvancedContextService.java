package com.example.digitalwellbeinganalyzer;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvancedContextService
        extends AccessibilityService {

    private static final String TAG =
            "ContextBrain";


    private RiskDataRepository riskRepository;


    private long lastSmartRecommendationCheck =
            0L;


    private static final long
            SMART_RECOMMENDATION_CHECK_INTERVAL_MS =
            15L * 60L * 1000L;


    private ScreenStateReceiver screenStateReceiver;


    private String currentPackageName =
            "";


    private int scrollCountWindow =
            0;


    private long lastScrollTimestamp =
            0L;


    private long windowStartTime =
            System.currentTimeMillis();


    // =========================================================
    // DATABASE
    // =========================================================

    private final ExecutorService dbExecutor =
            Executors.newSingleThreadExecutor();


    private AppDatabase database;


    private long lastInsertTimestamp =
            0L;


    private int lastInsertedRiskLevel =
            -1;


    private static final long INSERT_INTERVAL_MS =
            5000L;


    // Keep raw risk snapshots for approximately 90 days.
    private static final long RAW_READING_RETENTION_MS =
            90L
                    * 24L
                    * 60L
                    * 60L
                    * 1000L;


    // At most one cleanup attempt per day
    // during one service lifetime.
    private static final long DATABASE_CLEANUP_INTERVAL_MS =
            24L
                    * 60L
                    * 60L
                    * 1000L;


    private long lastDatabaseCleanupTimestamp =
            0L;


    // =========================================================
    // SESSION TRACKING
    // =========================================================

    private long sessionStartTime =
            0L;


    private String sessionPackageName =
            "";


    private static final long SESSION_GAP_THRESHOLD_MS =
            30000L;


    // =========================================================
    // DETECTION RULES
    // =========================================================

    private static final Pattern URL_PATTERN =
            Pattern.compile(

                    "(https?://|www\\.)[\\w\\-.]+\\.[a-z]{2,}(/\\S*)?",

                    Pattern.CASE_INSENSITIVE
            );


    private static final Set<String> HIGH_RISK_APPS =
            new HashSet<>(
                    Arrays.asList(

                            "com.instagram.android",
                            "com.zhiliaoapp.musically",
                            "com.twitter.android",
                            "com.google.android.youtube",
                            "com.snapchat.android",
                            "com.reddit.frontpage"
                    )
            );


    private static final Set<String> PRODUCTIVE_APPS =
            new HashSet<>(
                    Arrays.asList(

                            "com.google.android.apps.docs",
                            "com.microsoft.office.word",
                            "com.android.chrome",
                            "com.google.android.gm"
                    )
            );


    private static final String[] STRESS_KEYWORDS =
            {

                    "breaking",
                    "urgent",
                    "crisis",
                    "disaster",
                    "shocking",
                    "fatal",
                    "warning",
                    "death",
                    "outbreak",
                    "scam",
                    "tragedy",
                    "viral",
                    "fail"
            };


    // =========================================================
    // SERVICE CONNECTION
    // =========================================================

    @Override
    public void onServiceConnected() {

        super.onServiceConnected();


        database =
                AppDatabase
                        .getInstance(
                                this
                        );


        cleanupOldRiskReadingsIfNeeded();


        riskRepository =
                new RiskDataRepository(
                        this
                );


        InterventionManager
                .createNotificationChannel(
                        this
                );


        screenStateReceiver =
                new ScreenStateReceiver();


        IntentFilter filter =
                new IntentFilter(
                        Intent.ACTION_SCREEN_ON
                );


        registerReceiver(
                screenStateReceiver,
                filter
        );


        ScreenStateReceiver
                .setListener(timestamp -> {

                    Log.i(
                            TAG,
                            "Pickup event received in service at: "
                                    + timestamp
                    );

                    // Screen-on is not a guaranteed
                    // physical pickup measurement.
                });
    }


    // =========================================================
    // ACCESSIBILITY EVENTS
    // =========================================================

    @Override
    public void onAccessibilityEvent(
            AccessibilityEvent event
    ) {

        if (event == null) {

            return;
        }


        int eventType =
                event.getEventType();


        long currentTime =
                System.currentTimeMillis();


        if (currentTime
                - windowStartTime
                > 10000L) {

            scrollCountWindow =
                    0;


            windowStartTime =
                    currentTime;
        }


        if (eventType ==
                AccessibilityEvent
                        .TYPE_WINDOW_STATE_CHANGED) {

            if (event.getPackageName()
                    != null) {

                currentPackageName =
                        event.getPackageName()
                                .toString();


                updateSessionContinuity(
                        currentPackageName,
                        currentTime
                );
            }
        }


        if (eventType ==
                AccessibilityEvent
                        .TYPE_VIEW_SCROLLED) {

            scrollCountWindow++;


            lastScrollTimestamp =
                    currentTime;
        }


        AccessibilityNodeInfo rootNode =
                getRootInActiveWindow();


        if (rootNode != null) {

            StringBuilder textBuffer =
                    new StringBuilder();


            extractNodeText(
                    rootNode,
                    textBuffer
            );


            rootNode.recycle();


            String screenText =
                    textBuffer
                            .toString()
                            .toLowerCase();


            processAndClassifyActivity(

                    currentPackageName,

                    screenText,

                    scrollCountWindow
            );
        }
    }


    // =========================================================
    // SESSION CONTINUITY
    // =========================================================

    private void updateSessionContinuity(
            String packageName,
            long currentTime
    ) {

        boolean isSameApp =
                packageName.equals(
                        sessionPackageName
                );


        if (!isSameApp ||
                sessionStartTime == 0L) {

            sessionStartTime =
                    currentTime;


            sessionPackageName =
                    packageName;


            Log.i(
                    TAG,
                    "New session started: "
                            + packageName
            );
        }
    }


    // =========================================================
    // SCREEN TEXT EXTRACTION
    // =========================================================

    private void extractNodeText(
            AccessibilityNodeInfo node,
            StringBuilder builder
    ) {

        if (node == null) {

            return;
        }


        if (node.getText() != null &&
                node.getText().length() > 0) {

            builder
                    .append(
                            node.getText()
                    )
                    .append(" ");
        }


        for (int i = 0;
             i < node.getChildCount();
             i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);


            extractNodeText(
                    child,
                    builder
            );


            if (child != null) {

                child.recycle();
            }
        }
    }


    // =========================================================
    // CLASSIFICATION
    // =========================================================

    private void processAndClassifyActivity(
            String packageName,
            String text,
            int scrollFreq
    ) {

        double appCategoryScore =
                0.0;


        if (HIGH_RISK_APPS
                .contains(packageName)) {

            appCategoryScore =
                    1.0;

        } else if (
                PRODUCTIVE_APPS
                        .contains(packageName)
        ) {

            appCategoryScore =
                    -1.0;
        }


        double keywordMatches =
                0.0;


        for (String keyword :
                STRESS_KEYWORDS) {

            if (text.contains(
                    keyword
            )) {

                keywordMatches +=
                        1.0;
            }
        }


        double scrollRateMetric =
                (double) scrollFreq;


        double isHighRiskApp =
                HIGH_RISK_APPS
                        .contains(packageName)
                        ? 1.0
                        : 0.0;


        long timeSinceLastScroll =
                System.currentTimeMillis()
                        - lastScrollTimestamp;


        double rapidScrollFlag =
                timeSinceLastScroll < 1500L
                        && scrollFreq > 3
                        ? 1.0
                        : 0.0;


        Calendar calendar =
                Calendar.getInstance();


        int hour =
                calendar.get(
                        Calendar.HOUR_OF_DAY
                );


        double isNightTime =
                hour >= 23 ||
                        hour < 5
                        ? 1.0
                        : 0.0;


        Matcher urlMatcher =
                URL_PATTERN.matcher(
                        text
                );


        double hasUrlDetected =
                urlMatcher.find()
                        ? 1.0
                        : 0.0;


        double hasVideoTitleDetected =
                detectVideoTitle(
                        text
                )
                        ? 1.0
                        : 0.0;


        double[] featureVector =
                new double[]{

                        appCategoryScore,
                        keywordMatches,
                        scrollRateMetric,
                        isHighRiskApp,
                        rapidScrollFlag,
                        isNightTime,
                        hasUrlDetected,
                        hasVideoTitleDetected
                };


        try {

            double[] predictionScores =
                    WellbeingClassifier
                            .score(
                                    featureVector
                            );


            int predictedClass =
                    getArgMax(
                            predictionScores
                    );


            String riskLevelLabel =
                    predictedClass == 0
                            ? "LOW RISK"

                            : predictedClass == 1
                              ? "MEDIUM RISK"

                              : "HIGH RISK";


            Log.i(
                    TAG,
                    "--------------------------------------------------"
            );


            Log.i(
                    TAG,
                    "APP: "
                            + packageName
                            + " | CAT SCORE: "
                            + appCategoryScore
            );


            Log.i(
                    TAG,
                    "KEYWORDS: "
                            + keywordMatches
                            + " | SCROLL FREQ: "
                            + scrollFreq
            );


            Log.i(
                    TAG,
                    "NIGHT: "
                            + isNightTime
                            + " | URL: "
                            + hasUrlDetected
                            + " | TITLE: "
                            + hasVideoTitleDetected
            );


            Log.i(
                    TAG,
                    ">>> PREDICTED RISK LEVEL: ["
                            + riskLevelLabel
                            + "] <<<"
            );


            Log.i(
                    TAG,
                    "--------------------------------------------------"
            );


            long now =
                    System.currentTimeMillis();


            boolean intervalPassed =
                    now
                            - lastInsertTimestamp
                            >= INSERT_INTERVAL_MS;


            boolean riskChanged =
                    predictedClass
                            != lastInsertedRiskLevel;


            long sessionDurationMs =
                    sessionStartTime <= 0L
                            ? 0L
                            : now - sessionStartTime;


            if (database != null &&
                    (intervalPassed ||
                            riskChanged)) {

                RiskReading reading =
                        new RiskReading(

                                now,

                                packageName,

                                predictedClass,

                                appCategoryScore,

                                keywordMatches,

                                scrollRateMetric,

                                isHighRiskApp,

                                rapidScrollFlag,

                                isNightTime,

                                hasUrlDetected,

                                hasVideoTitleDetected,

                                sessionDurationMs
                        );


                // =================================================
                // STORE RISK READING
                // =================================================

                dbExecutor.execute(() -> {

                    database
                            .riskReadingDao()
                            .insert(
                                    reading
                            );
                });


                cleanupOldRiskReadingsIfNeeded();


                // =================================================
                // REAL-TIME INTERVENTIONS
                // =================================================

                InterventionManager
                        .evaluateReading(

                                AdvancedContextService.this,

                                reading
                        );


                checkSmartRecommendation();


                lastInsertTimestamp =
                        now;


                lastInsertedRiskLevel =
                        predictedClass;
            }

        } catch (Exception exception) {

            Log.e(
                    TAG,
                    "Inference Error: "
                            + exception.getMessage()
            );
        }
    }


    // =========================================================
    // DATABASE RETENTION
    // =========================================================

    private void cleanupOldRiskReadingsIfNeeded() {

        if (database == null) {

            return;
        }


        long now =
                System.currentTimeMillis();


        if ((now
                - lastDatabaseCleanupTimestamp)
                < DATABASE_CLEANUP_INTERVAL_MS) {

            return;
        }


        lastDatabaseCleanupTimestamp =
                now;


        long cutoffTimestamp =
                now
                        - RAW_READING_RETENTION_MS;


        dbExecutor.execute(() -> {

            try {

                int deletedRows =
                        database
                                .riskReadingDao()
                                .deleteOlderThan(
                                        cutoffTimestamp
                                );


                Log.i(
                        TAG,
                        "Database cleanup removed "
                                + deletedRows
                                + " old risk readings."
                );

            } catch (Exception exception) {

                Log.e(
                        TAG,
                        "Database cleanup failed: "
                                + exception.getMessage()
                );
            }
        });
    }


    // =========================================================
    // SMART RECOMMENDATION
    // =========================================================

    private void checkSmartRecommendation() {

        if (riskRepository == null) {

            return;
        }


        long now =
                System.currentTimeMillis();


        if ((now
                - lastSmartRecommendationCheck)
                < SMART_RECOMMENDATION_CHECK_INTERVAL_MS) {

            return;
        }


        lastSmartRecommendationCheck =
                now;


        riskRepository
                .getSmartRecommendationToday(

                        AdvancedContextService.this,

                        recommendation ->

                                InterventionManager
                                        .showSmartRecommendation(

                                                AdvancedContextService.this,

                                                recommendation
                                        )
                );
    }


    // =========================================================
    // VIDEO TITLE HEURISTIC
    // =========================================================

    private boolean detectVideoTitle(
            String text
    ) {

        if (text == null ||
                text.isEmpty()) {

            return false;
        }


        String[] lines =
                text.split(
                        "\\n|\\s{2,}"
                );


        for (String line :
                lines) {

            String trimmed =
                    line.trim();


            if (trimmed.length() >= 15 &&
                    trimmed.length() <= 150 &&
                    !URL_PATTERN
                            .matcher(trimmed)
                            .find() &&
                    trimmed.matches(
                            ".*[a-zA-Z]{4,}.*"
                    )) {

                return true;
            }
        }


        return false;
    }


    // =========================================================
    // ARGMAX
    // =========================================================

    private int getArgMax(
            double[] array
    ) {

        int maxIndex =
                0;


        for (int i = 1;
             i < array.length;
             i++) {

            if (array[i]
                    > array[maxIndex]) {

                maxIndex =
                        i;
            }
        }


        return maxIndex;
    }


    // =========================================================
    // SERVICE CLEANUP
    // =========================================================

    @Override
    public void onDestroy() {

        ScreenStateReceiver
                .setListener(null);


        if (screenStateReceiver != null) {

            try {

                unregisterReceiver(
                        screenStateReceiver
                );

            } catch (IllegalArgumentException ignored) {

                // Receiver was already unregistered.
            }
        }


        dbExecutor.shutdownNow();


        if (riskRepository != null) {

            riskRepository.shutdown();
        }


        super.onDestroy();
    }


    // =========================================================
    // ACCESSIBILITY INTERRUPTION
    // =========================================================

    @Override
    public void onInterrupt() {

        Log.w(
                TAG,
                "Accessibility Service Interrupted"
        );
    }
}
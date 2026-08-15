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

public class AdvancedContextService extends AccessibilityService {

    private static final String TAG = "ContextBrain";

    private ScreenStateReceiver screenStateReceiver;

    private String currentPackageName = "";
    private int scrollCountWindow = 0;
    private long lastScrollTimestamp = 0;
    private long windowStartTime = System.currentTimeMillis();

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private AppDatabase database;

    private long lastInsertTimestamp = 0;
    private int lastInsertedRiskLevel = -1;
    private static final long INSERT_INTERVAL_MS = 5000;

    private long sessionStartTime = 0;
    private String sessionPackageName = "";
    private static final long SESSION_GAP_THRESHOLD_MS = 30000; // 30 seconds

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://|www\\.)[\\w\\-.]+\\.[a-z]{2,}(/\\S*)?", Pattern.CASE_INSENSITIVE);

    private static final Set<String> HIGH_RISK_APPS = new HashSet<>(Arrays.asList(
            "com.instagram.android", "com.zhiliaoapp.musically", "com.twitter.android",
            "com.google.android.youtube", "com.snapchat.android", "com.reddit.frontpage"
    ));

    private static final Set<String> PRODUCTIVE_APPS = new HashSet<>(Arrays.asList(
            "com.google.android.apps.docs", "com.microsoft.office.word",
            "com.android.chrome", "com.google.android.gm"
    ));

    private static final String[] STRESS_KEYWORDS = {
            "breaking", "urgent", "crisis", "disaster", "shocking", "fatal",
            "warning", "death", "outbreak", "scam", "tragedy", "viral", "fail"
    };

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        database = AppDatabase.getInstance(this);

        screenStateReceiver = new ScreenStateReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenStateReceiver, filter);

        ScreenStateReceiver.setListener(timestamp -> {
            Log.i(TAG, "Pickup event received in service at: " + timestamp);
            // We'll wire this into a PickupEvent table shortly
        });
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        int eventType = event.getEventType();
        long currentTime = System.currentTimeMillis();

        if (currentTime - windowStartTime > 10000) {
            scrollCountWindow = 0;
            windowStartTime = currentTime;
        }

        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null) {
                currentPackageName = event.getPackageName().toString();
                updateSessionContinuity(currentPackageName, currentTime);
            }
        }

        if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            scrollCountWindow++;
            lastScrollTimestamp = currentTime;
        }

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            StringBuilder textBuffer = new StringBuilder();
            extractNodeText(rootNode, textBuffer);
            rootNode.recycle();

            String screenText = textBuffer.toString().toLowerCase();
            processAndClassifyActivity(currentPackageName, screenText, scrollCountWindow);
        }
    }

    private void updateSessionContinuity(String packageName, long currentTime) {
        boolean isSameApp = packageName.equals(sessionPackageName);

        if (!isSameApp || (sessionStartTime == 0)) {
            sessionStartTime = currentTime;
            sessionPackageName = packageName;
            Log.i(TAG, "New session started: " + packageName);
        }
    }

    private void extractNodeText(AccessibilityNodeInfo node, StringBuilder builder) {
        if (node == null) return;

        if (node.getText() != null && node.getText().length() > 0) {
            builder.append(node.getText()).append(" ");
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            extractNodeText(child, builder);
            if (child != null) child.recycle();
        }
    }

    private void processAndClassifyActivity(String packageName, String text, int scrollFreq) {
        double appCategoryScore = 0.0;
        if (HIGH_RISK_APPS.contains(packageName)) {
            appCategoryScore = 1.0;
        } else if (PRODUCTIVE_APPS.contains(packageName)) {
            appCategoryScore = -1.0;
        }

        double keywordMatches = 0.0;
        for (String kw : STRESS_KEYWORDS) {
            if (text.contains(kw)) {
                keywordMatches += 1.0;
            }
        }

        double scrollRateMetric = (double) scrollFreq;
        double isHighRiskApp = HIGH_RISK_APPS.contains(packageName) ? 1.0 : 0.0;
        long timeSinceLastScroll = System.currentTimeMillis() - lastScrollTimestamp;
        double rapidScrollFlag = (timeSinceLastScroll < 1500 && scrollFreq > 3) ? 1.0 : 0.0;

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        double isNightTime = (hour >= 23 || hour < 5) ? 1.0 : 0.0;

        Matcher urlMatcher = URL_PATTERN.matcher(text);
        double hasUrlDetected = urlMatcher.find() ? 1.0 : 0.0;

        double hasVideoTitleDetected = detectVideoTitle(text) ? 1.0 : 0.0;

        double[] featureVector = new double[] {
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
            double[] predictionScores = WellbeingClassifier.score(featureVector);
            int predictedClass = getArgMax(predictionScores);

            String riskLevelLabel = (predictedClass == 0) ? "LOW RISK" :
                    (predictedClass == 1) ? "MEDIUM RISK" : "HIGH RISK";

            Log.i(TAG, "--------------------------------------------------");
            Log.i(TAG, "APP: " + packageName + " | CAT SCORE: " + appCategoryScore);
            Log.i(TAG, "KEYWORDS: " + keywordMatches + " | SCROLL FREQ: " + scrollFreq);
            Log.i(TAG, "NIGHT: " + isNightTime + " | URL: " + hasUrlDetected + " | TITLE: " + hasVideoTitleDetected);
            Log.i(TAG, ">>> PREDICTED RISK LEVEL: [" + riskLevelLabel + "] <<<");
            Log.i(TAG, "--------------------------------------------------");

            long now = System.currentTimeMillis();
            boolean intervalPassed = (now - lastInsertTimestamp) >= INSERT_INTERVAL_MS;
            boolean riskChanged = predictedClass != lastInsertedRiskLevel;
            long sessionDurationMs = now - sessionStartTime;

            if (database != null && (intervalPassed || riskChanged)) {
                RiskReading reading = new RiskReading(
                        now, packageName, predictedClass,
                        appCategoryScore, keywordMatches, scrollRateMetric,
                        isHighRiskApp, rapidScrollFlag, isNightTime,
                        hasUrlDetected, hasVideoTitleDetected, sessionDurationMs
                );
                dbExecutor.execute(() -> database.riskReadingDao().insert(reading));
                lastInsertTimestamp = now;
                lastInsertedRiskLevel = predictedClass;
            }

        } catch (Exception e) {
            Log.e(TAG, "Inference Error: " + e.getMessage());
        }
    }

    private boolean detectVideoTitle(String text) {
        if (text == null || text.isEmpty()) return false;
        String[] lines = text.split("\\n|\\s{2,}");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() >= 15 && trimmed.length() <= 150
                    && !URL_PATTERN.matcher(trimmed).find()
                    && trimmed.matches(".*[a-zA-Z]{4,}.*")) {
                return true;
            }
        }
        return false;
    }

    private int getArgMax(double[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (screenStateReceiver != null) {
            unregisterReceiver(screenStateReceiver);
        }
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "Accessibility Service Interrupted");
    }
}
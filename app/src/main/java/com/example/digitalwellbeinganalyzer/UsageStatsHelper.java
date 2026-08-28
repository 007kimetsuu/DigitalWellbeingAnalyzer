package com.example.digitalwellbeinganalyzer;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Process;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class UsageStatsHelper {

    /*
     * Tiny same-app gaps can occur when one Activity inside
     * an app switches to another Activity.
     */
    private static final long SESSION_MERGE_GAP_MS =
            2000L;

    /*
     * Look before the requested range so a session that began
     * before midnight can still be reconstructed correctly.
     */
    private static final long SESSION_LOOKBACK_MS =
            24L * 60L * 60L * 1000L;


    // =========================================================
    // SESSION MODEL
    // =========================================================

    public static class AppSession {

        private final String packageName;
        private final long startTime;
        private final long endTime;
        private final boolean completed;

        public AppSession(
                String packageName,
                long startTime,
                long endTime,
                boolean completed
        ) {

            this.packageName =
                    packageName;

            this.startTime =
                    startTime;

            this.endTime =
                    endTime;

            this.completed =
                    completed;
        }


        public String getPackageName() {
            return packageName;
        }


        public long getStartTime() {
            return startTime;
        }


        public long getEndTime() {
            return endTime;
        }


        public boolean isCompleted() {
            return completed;
        }


        public long getDurationMs() {

            return Math.max(
                    0L,
                    endTime - startTime
            );
        }
    }


    public static class DailyUsageSummary {

        private final long dayStartTime;
        private final long totalUsageMs;

        public DailyUsageSummary(
                long dayStartTime,
                long totalUsageMs
        ) {

            this.dayStartTime =
                    dayStartTime;

            this.totalUsageMs =
                    totalUsageMs;
        }


        public long getDayStartTime() {
            return dayStartTime;
        }


        public long getTotalUsageMs() {
            return totalUsageMs;
        }
    }


    public static class HourlyUsageSummary {

        private final int hour;
        private final long totalUsageMs;

        public HourlyUsageSummary(
                int hour,
                long totalUsageMs
        ) {

            this.hour =
                    hour;

            this.totalUsageMs =
                    totalUsageMs;
        }


        public int getHour() {
            return hour;
        }


        public long getTotalUsageMs() {
            return totalUsageMs;
        }
    }


    public static class AppUsageItem {

        private final String packageName;
        private final String appName;
        private final long totalUsageMs;

        public AppUsageItem(
                String packageName,
                String appName,
                long totalUsageMs
        ) {

            this.packageName =
                    packageName;

            this.appName =
                    appName;

            this.totalUsageMs =
                    totalUsageMs;
        }


        public String getPackageName() {
            return packageName;
        }


        public String getAppName() {
            return appName;
        }


        public long getTotalUsageMs() {
            return totalUsageMs;
        }
    }


    // =========================================================
    // PERMISSION
    // =========================================================

    public static boolean hasUsageStatsPermission(
            Context context
    ) {

        if (context == null) {
            return false;
        }


        AppOpsManager appOps =
                (AppOpsManager)
                        context.getSystemService(
                                Context.APP_OPS_SERVICE
                        );


        if (appOps == null) {
            return false;
        }


        int mode =
                appOps.checkOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        Process.myUid(),
                        context.getPackageName()
                );


        return mode ==
                AppOpsManager.MODE_ALLOWED;
    }


    // =========================================================
    // TIME HELPERS
    // =========================================================

    public static long getStartOfToday() {

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


        return calendar.getTimeInMillis();
    }


    // =========================================================
    // PACKAGE FILTERING
    // =========================================================

    /*
     * These packages are not meaningful user-app consumption
     * for MindTrack's digital wellbeing calculations.
     *
     * We intentionally do NOT exclude every Android system app.
     * Apps such as Settings, Chrome, YouTube, etc. can still
     * represent genuine user interaction.
     */
    private static Set<String> buildExcludedPackages(
            Context context
    ) {

        Set<String> excludedPackages =
                new HashSet<>();


        if (context == null) {
            return excludedPackages;
        }


        // MindTrack itself should never inflate screen time.
        excludedPackages.add(
                context.getPackageName()
        );


        // Android System UI.
        excludedPackages.add(
                "com.android.systemui"
        );


        // Android / Google permission UI.
        excludedPackages.add(
                "com.android.permissioncontroller"
        );

        excludedPackages.add(
                "com.google.android.permissioncontroller"
        );


        // Dynamically identify the device's launcher.
        try {

            Intent homeIntent =
                    new Intent(
                            Intent.ACTION_MAIN
                    );

            homeIntent.addCategory(
                    Intent.CATEGORY_HOME
            );


            ResolveInfo resolveInfo =
                    context
                            .getPackageManager()
                            .resolveActivity(
                                    homeIntent,
                                    PackageManager.MATCH_DEFAULT_ONLY
                            );


            if (resolveInfo != null &&
                    resolveInfo.activityInfo != null &&
                    resolveInfo.activityInfo.packageName != null) {

                excludedPackages.add(
                        resolveInfo
                                .activityInfo
                                .packageName
                );
            }

        } catch (Exception ignored) {
        }


        return excludedPackages;
    }


    private static boolean shouldExcludePackage(
            String packageName,
            Set<String> excludedPackages
    ) {

        if (packageName == null ||
                packageName.trim().isEmpty()) {

            return true;
        }


        return excludedPackages != null &&
                excludedPackages.contains(
                        packageName
                );
    }


    // =========================================================
    // FOREGROUND USAGE - SINGLE APP
    // =========================================================

    public static long getForegroundTimeBetween(
            Context context,
            String packageName,
            long startTime,
            long endTime
    ) {

        if (context == null ||
                packageName == null ||
                packageName.isEmpty() ||
                startTime >= endTime) {

            return 0L;
        }


        Set<String> excludedPackages =
                buildExcludedPackages(
                        context
                );


        if (shouldExcludePackage(
                packageName,
                excludedPackages
        )) {

            return 0L;
        }


        UsageStatsManager usageStatsManager =
                (UsageStatsManager)
                        context.getSystemService(
                                Context.USAGE_STATS_SERVICE
                        );


        if (usageStatsManager == null) {
            return 0L;
        }


        List<UsageStats> statsList =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        startTime,
                        endTime
                );


        long totalTime =
                0L;


        if (statsList != null) {

            for (UsageStats stats :
                    statsList) {

                if (packageName.equals(
                        stats.getPackageName()
                )) {

                    totalTime +=
                            stats.getTotalTimeInForeground();
                }
            }
        }


        return totalTime;
    }


    public static long getTodayForegroundTime(
            Context context,
            String packageName
    ) {

        return getForegroundTimeBetween(
                context,
                packageName,
                getStartOfToday(),
                System.currentTimeMillis()
        );
    }


    // =========================================================
    // FOREGROUND USAGE - ALL APPS
    // =========================================================

    public static Map<String, Long> getAllAppUsageBetween(
            Context context,
            long startTime,
            long endTime
    ) {

        Map<String, Long> usageMap =
                new TreeMap<>();


        if (context == null ||
                startTime >= endTime) {

            return usageMap;
        }


        Set<String> excludedPackages =
                buildExcludedPackages(
                        context
                );


        UsageStatsManager usageStatsManager =
                (UsageStatsManager)
                        context.getSystemService(
                                Context.USAGE_STATS_SERVICE
                        );


        if (usageStatsManager == null) {
            return usageMap;
        }


        List<UsageStats> statsList =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        startTime,
                        endTime
                );


        if (statsList != null) {

            for (UsageStats stats :
                    statsList) {

                String packageName =
                        stats.getPackageName();


                if (shouldExcludePackage(
                        packageName,
                        excludedPackages
                )) {

                    continue;
                }


                long foregroundTime =
                        stats.getTotalTimeInForeground();


                if (foregroundTime > 0) {

                    usageMap.merge(
                            packageName,
                            foregroundTime,
                            Long::sum
                    );
                }
            }
        }


        return usageMap;
    }


    public static Map<String, Long> getAllAppUsageToday(
            Context context
    ) {

        return getAllAppUsageBetween(
                context,
                getStartOfToday(),
                System.currentTimeMillis()
        );
    }


    public static long getTotalUsageToday(
            Context context
    ) {

        Map<String, Long> usageMap =
                getAllAppUsageToday(
                        context
                );


        long totalUsage =
                0L;


        for (long usageTime :
                usageMap.values()) {

            totalUsage +=
                    usageTime;
        }


        return totalUsage;
    }


    // =========================================================
    // SESSION TRACKING
    // =========================================================

    /**
     * Builds approximate app-level foreground sessions using
     * Android UsageEvents.
     *
     * Important rule:
     *
     * When another app enters the foreground, any previously
     * active app is closed at that timestamp.
     *
     * This prevents stale/zombie sessions from lasting for
     * tens of minutes or hours.
     */
    public static List<AppSession> getAppSessionsBetween(
            Context context,
            long startTime,
            long endTime
    ) {

        List<AppSession> sessions =
                new ArrayList<>();


        if (context == null ||
                startTime >= endTime) {

            return sessions;
        }


        Context appContext =
                context.getApplicationContext();


        Set<String> excludedPackages =
                buildExcludedPackages(
                        appContext
                );


        UsageStatsManager usageStatsManager =
                (UsageStatsManager)
                        appContext.getSystemService(
                                Context.USAGE_STATS_SERVICE
                        );


        if (usageStatsManager == null) {
            return sessions;
        }


        long queryStart =
                Math.max(
                        0L,
                        startTime -
                                SESSION_LOOKBACK_MS
                );


        UsageEvents usageEvents =
                usageStatsManager.queryEvents(
                        queryStart,
                        endTime
                );


        if (usageEvents == null) {
            return sessions;
        }


        /*
         * packageName -> timestamp app entered foreground
         */
        Map<String, Long> activeSessions =
                new HashMap<>();


        UsageEvents.Event event =
                new UsageEvents.Event();


        while (usageEvents.hasNextEvent()) {

            usageEvents.getNextEvent(
                    event
            );


            String packageName =
                    event.getPackageName();


            int eventType =
                    event.getEventType();


            long timestamp =
                    event.getTimeStamp();


            // =================================================
            // APP ENTERED FOREGROUND
            // =================================================

            if (isForegroundEvent(
                    eventType
            )) {

                if (packageName == null ||
                        packageName.isEmpty()) {

                    continue;
                }


                /*
                 * A normal phone UI has one meaningful foreground
                 * application at a time.
                 *
                 * If another package becomes foreground, close
                 * stale sessions from previous packages here.
                 *
                 * This is the main fix for cases such as Pixel
                 * Launcher appearing active for an hour.
                 */
                closeOtherActiveSessions(
                        sessions,
                        activeSessions,
                        packageName,
                        timestamp,
                        startTime,
                        endTime
                );


                /*
                 * Launcher, MindTrack itself, System UI etc.
                 * can terminate another real session, but we do
                 * not create usage sessions for them.
                 */
                if (shouldExcludePackage(
                        packageName,
                        excludedPackages
                )) {

                    activeSessions.remove(
                            packageName
                    );

                    continue;
                }


                /*
                 * Duplicate foreground events can happen.
                 * Preserve the earliest start.
                 */
                if (!activeSessions.containsKey(
                        packageName
                )) {

                    activeSessions.put(
                            packageName,
                            timestamp
                    );
                }
            }


            // =================================================
            // APP LEFT FOREGROUND
            // =================================================

            else if (isBackgroundEvent(
                    eventType
            )) {

                if (packageName == null ||
                        packageName.isEmpty()) {

                    continue;
                }


                Long sessionStart =
                        activeSessions.remove(
                                packageName
                        );


                if (sessionStart != null) {

                    addClippedSession(
                            sessions,
                            packageName,
                            sessionStart,
                            timestamp,
                            startTime,
                            endTime,
                            true
                    );
                }
            }


            // =================================================
            // SCREEN TURNED NON-INTERACTIVE
            // =================================================

            else if (eventType ==
                    UsageEvents.Event.SCREEN_NON_INTERACTIVE) {

                closeAllActiveSessions(
                        sessions,
                        activeSessions,
                        timestamp,
                        startTime,
                        endTime
                );
            }
        }


        // =====================================================
        // CURRENTLY ACTIVE SESSION
        // =====================================================

        for (Map.Entry<String, Long> entry :
                activeSessions.entrySet()) {

            if (shouldExcludePackage(
                    entry.getKey(),
                    excludedPackages
            )) {

                continue;
            }


            addClippedSession(
                    sessions,
                    entry.getKey(),
                    entry.getValue(),
                    endTime,
                    startTime,
                    endTime,
                    false
            );
        }


        return mergeAdjacentSessions(
                sessions
        );
    }


    public static List<AppSession> getTodaySessions(
            Context context
    ) {

        return getAppSessionsBetween(
                context,
                getStartOfToday(),
                System.currentTimeMillis()
        );
    }


    // =========================================================
    // LONGEST SESSION
    // =========================================================

    public static AppSession getLongestSessionToday(
            Context context
    ) {

        List<AppSession> sessions =
                getTodaySessions(
                        context
                );


        AppSession longest =
                null;


        for (AppSession session :
                sessions) {

            if (longest == null ||
                    session.getDurationMs() >
                            longest.getDurationMs()) {

                longest =
                        session;
            }
        }


        return longest;
    }


    public static long getLongestSessionDurationToday(
            Context context
    ) {

        AppSession session =
                getLongestSessionToday(
                        context
                );


        if (session == null) {
            return 0L;
        }


        return session.getDurationMs();
    }


    // =========================================================
    // LAST COMPLETED SESSION
    // =========================================================

    public static AppSession getLastCompletedSessionToday(
            Context context
    ) {

        List<AppSession> sessions =
                getTodaySessions(
                        context
                );


        AppSession latest =
                null;


        for (AppSession session :
                sessions) {

            if (!session.isCompleted()) {
                continue;
            }


            if (latest == null ||
                    session.getEndTime() >
                            latest.getEndTime()) {

                latest =
                        session;
            }
        }


        return latest;
    }


    public static long getLastSessionEndToday(
            Context context
    ) {

        AppSession session =
                getLastCompletedSessionToday(
                        context
                );


        if (session == null) {
            return 0L;
        }


        return session.getEndTime();
    }


    // =========================================================
    // SESSION COUNT
    // =========================================================

    public static int getSessionCountToday(
            Context context
    ) {

        return getTodaySessions(
                context
        ).size();
    }


    // =========================================================
    // HOURLY USAGE
    // =========================================================

    public static List<HourlyUsageSummary> getHourlyUsageBetween(
            Context context,
            long startTime,
            long endTime
    ) {

        long[] hourlyTotals =
                new long[24];


        List<AppSession> sessions =
                getAppSessionsBetween(
                        context,
                        startTime,
                        endTime
                );


        List<long[]> intervals =
                getNonOverlappingIntervals(
                        sessions
                );


        for (long[] interval :
                intervals) {

            long current =
                    Math.max(
                            interval[0],
                            startTime
                    );


            long intervalEnd =
                    Math.min(
                            interval[1],
                            endTime
                    );


            while (current < intervalEnd) {

                Calendar calendar =
                        Calendar.getInstance();


                calendar.setTimeInMillis(
                        current
                );


                int hour =
                        calendar.get(
                                Calendar.HOUR_OF_DAY
                        );


                Calendar nextHour =
                        (Calendar)
                                calendar.clone();


                nextHour.set(
                        Calendar.MINUTE,
                        0
                );

                nextHour.set(
                        Calendar.SECOND,
                        0
                );

                nextHour.set(
                        Calendar.MILLISECOND,
                        0
                );


                nextHour.add(
                        Calendar.HOUR_OF_DAY,
                        1
                );


                long segmentEnd =
                        Math.min(
                                intervalEnd,
                                nextHour.getTimeInMillis()
                        );


                hourlyTotals[hour] +=
                        segmentEnd -
                                current;


                current =
                        segmentEnd;
            }
        }


        List<HourlyUsageSummary> result =
                new ArrayList<>();


        for (int hour = 0;
             hour < 24;
             hour++) {

            result.add(
                    new HourlyUsageSummary(
                            hour,
                            hourlyTotals[hour]
                    )
            );
        }


        return result;
    }


    public static List<HourlyUsageSummary> getHourlyUsageLast7Days(
            Context context
    ) {

        Calendar start =
                Calendar.getInstance();


        start.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        start.set(
                Calendar.MINUTE,
                0
        );

        start.set(
                Calendar.SECOND,
                0
        );

        start.set(
                Calendar.MILLISECOND,
                0
        );


        start.add(
                Calendar.DAY_OF_YEAR,
                -6
        );


        return getHourlyUsageBetween(
                context,
                start.getTimeInMillis(),
                System.currentTimeMillis()
        );
    }


    // =========================================================
    // MOST USED APPS
    // =========================================================

    public static List<AppUsageItem> getMostUsedAppsBetween(
            Context context,
            long startTime,
            long endTime
    ) {

        List<AppUsageItem> result =
                new ArrayList<>();


        if (context == null ||
                startTime >= endTime) {

            return result;
        }


        Set<String> excludedPackages =
                buildExcludedPackages(
                        context
                );


        List<AppSession> sessions =
                getAppSessionsBetween(
                        context,
                        startTime,
                        endTime
                );


        Map<String, Long> packageTotals =
                new HashMap<>();


        for (AppSession session :
                sessions) {

            String packageName =
                    session.getPackageName();


            long duration =
                    session.getDurationMs();


            if (packageName == null ||
                    packageName.isEmpty() ||
                    duration <= 0L ||
                    shouldExcludePackage(
                            packageName,
                            excludedPackages
                    )) {

                continue;
            }


            packageTotals.merge(
                    packageName,
                    duration,
                    Long::sum
            );
        }


        PackageManager packageManager =
                context.getPackageManager();


        for (Map.Entry<String, Long> entry :
                packageTotals.entrySet()) {

            String packageName =
                    entry.getKey();


            String appName =
                    getApplicationName(
                            packageManager,
                            packageName
                    );


            result.add(
                    new AppUsageItem(
                            packageName,
                            appName,
                            entry.getValue()
                    )
            );
        }


        Collections.sort(
                result,
                new Comparator<AppUsageItem>() {

                    @Override
                    public int compare(
                            AppUsageItem first,
                            AppUsageItem second
                    ) {

                        return Long.compare(
                                second.getTotalUsageMs(),
                                first.getTotalUsageMs()
                        );
                    }
                }
        );


        return result;
    }


    public static List<AppUsageItem> getMostUsedAppsToday(
            Context context
    ) {

        return getMostUsedAppsBetween(
                context,
                getStartOfToday(),
                System.currentTimeMillis()
        );
    }


    public static List<AppUsageItem> getTopUsedAppsToday(
            Context context,
            int limit
    ) {

        List<AppUsageItem> apps =
                getMostUsedAppsToday(
                        context
                );


        if (limit <= 0) {

            return new ArrayList<>();
        }


        if (apps.size() <= limit) {

            return apps;
        }


        return new ArrayList<>(
                apps.subList(
                        0,
                        limit
                )
        );
    }


    // =========================================================
    // SESSION-BASED USAGE TOTALS
    // =========================================================

    public static long getTotalSessionUsageBetween(
            Context context,
            long startTime,
            long endTime
    ) {

        List<AppSession> sessions =
                getAppSessionsBetween(
                        context,
                        startTime,
                        endTime
                );


        List<long[]> intervals =
                getNonOverlappingIntervals(
                        sessions
                );


        long totalUsage =
                0L;


        for (long[] interval :
                intervals) {

            totalUsage +=
                    interval[1] -
                            interval[0];
        }


        return totalUsage;
    }


    public static long getTotalSessionUsageToday(
            Context context
    ) {

        return getTotalSessionUsageBetween(
                context,
                getStartOfToday(),
                System.currentTimeMillis()
        );
    }


    // =========================================================
    // LAST 7 CALENDAR DAYS
    // =========================================================

    public static List<DailyUsageSummary> getLast7DaysUsage(
            Context context
    ) {

        List<DailyUsageSummary> result =
                new ArrayList<>();


        long now =
                System.currentTimeMillis();


        Calendar today =
                Calendar.getInstance();


        today.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        today.set(
                Calendar.MINUTE,
                0
        );

        today.set(
                Calendar.SECOND,
                0
        );

        today.set(
                Calendar.MILLISECOND,
                0
        );


        for (int daysAgo = 6;
             daysAgo >= 0;
             daysAgo--) {

            Calendar dayCalendar =
                    (Calendar)
                            today.clone();


            dayCalendar.add(
                    Calendar.DAY_OF_YEAR,
                    -daysAgo
            );


            long dayStart =
                    dayCalendar.getTimeInMillis();


            Calendar nextDayCalendar =
                    (Calendar)
                            dayCalendar.clone();


            nextDayCalendar.add(
                    Calendar.DAY_OF_YEAR,
                    1
            );


            long dayEnd =
                    nextDayCalendar.getTimeInMillis();


            if (dayEnd > now) {

                dayEnd =
                        now;
            }


            long totalUsage =
                    getTotalSessionUsageBetween(
                            context,
                            dayStart,
                            dayEnd
                    );


            result.add(
                    new DailyUsageSummary(
                            dayStart,
                            totalUsage
                    )
            );
        }


        return result;
    }


    public static long getTotalUsageLast7Days(
            Context context
    ) {

        List<DailyUsageSummary> dailyUsage =
                getLast7DaysUsage(
                        context
                );


        long total =
                0L;


        for (DailyUsageSummary day :
                dailyUsage) {

            total +=
                    day.getTotalUsageMs();
        }


        return total;
    }


    public static long getAverageDailyUsageLast7Days(
            Context context
    ) {

        List<DailyUsageSummary> dailyUsage =
                getLast7DaysUsage(
                        context
                );


        if (dailyUsage.isEmpty()) {

            return 0L;
        }


        long total =
                0L;


        for (DailyUsageSummary day :
                dailyUsage) {

            total +=
                    day.getTotalUsageMs();
        }


        return total /
                dailyUsage.size();
    }


    // =========================================================
    // PRIVATE HELPERS
    // =========================================================

    private static String getApplicationName(
            PackageManager packageManager,
            String packageName
    ) {

        try {

            ApplicationInfo applicationInfo =
                    packageManager.getApplicationInfo(
                            packageName,
                            0
                    );


            CharSequence label =
                    packageManager.getApplicationLabel(
                            applicationInfo
                    );


            if (label != null &&
                    label.length() > 0) {

                return label.toString();
            }

        } catch (
                PackageManager.NameNotFoundException ignored
        ) {
        }


        return packageName;
    }


    @SuppressWarnings("deprecation")
    private static boolean isForegroundEvent(
            int eventType
    ) {

        return eventType ==
                UsageEvents.Event.MOVE_TO_FOREGROUND;
    }


    @SuppressWarnings("deprecation")
    private static boolean isBackgroundEvent(
            int eventType
    ) {

        return eventType ==
                UsageEvents.Event.MOVE_TO_BACKGROUND;
    }


    /**
     * When a new package enters foreground, close sessions for
     * any other package still incorrectly marked as active.
     */
    private static void closeOtherActiveSessions(
            List<AppSession> sessions,
            Map<String, Long> activeSessions,
            String incomingPackage,
            long timestamp,
            long requestedStart,
            long requestedEnd
    ) {

        List<String> packagesToClose =
                new ArrayList<>();


        for (String activePackage :
                activeSessions.keySet()) {

            if (!activePackage.equals(
                    incomingPackage
            )) {

                packagesToClose.add(
                        activePackage
                );
            }
        }


        for (String packageName :
                packagesToClose) {

            Long sessionStart =
                    activeSessions.remove(
                            packageName
                    );


            if (sessionStart == null) {
                continue;
            }


            addClippedSession(
                    sessions,
                    packageName,
                    sessionStart,
                    timestamp,
                    requestedStart,
                    requestedEnd,
                    true
            );
        }
    }


    private static void addClippedSession(
            List<AppSession> sessions,
            String packageName,
            long sessionStart,
            long sessionEnd,
            long requestedStart,
            long requestedEnd,
            boolean completed
    ) {

        long clippedStart =
                Math.max(
                        sessionStart,
                        requestedStart
                );


        long clippedEnd =
                Math.min(
                        sessionEnd,
                        requestedEnd
                );


        if (clippedEnd <=
                clippedStart) {

            return;
        }


        sessions.add(
                new AppSession(
                        packageName,
                        clippedStart,
                        clippedEnd,
                        completed
                )
        );
    }


    private static void closeAllActiveSessions(
            List<AppSession> sessions,
            Map<String, Long> activeSessions,
            long timestamp,
            long requestedStart,
            long requestedEnd
    ) {

        for (Map.Entry<String, Long> entry :
                activeSessions.entrySet()) {

            addClippedSession(
                    sessions,
                    entry.getKey(),
                    entry.getValue(),
                    timestamp,
                    requestedStart,
                    requestedEnd,
                    true
            );
        }


        activeSessions.clear();
    }


    /**
     * Merges tiny same-package gaps caused by Activity changes
     * inside one application.
     */
    private static List<AppSession> mergeAdjacentSessions(
            List<AppSession> sessions
    ) {

        if (sessions.isEmpty()) {
            return sessions;
        }


        Collections.sort(
                sessions,
                new Comparator<AppSession>() {

                    @Override
                    public int compare(
                            AppSession first,
                            AppSession second
                    ) {

                        return Long.compare(
                                first.getStartTime(),
                                second.getStartTime()
                        );
                    }
                }
        );


        List<AppSession> merged =
                new ArrayList<>();


        for (AppSession current :
                sessions) {

            if (merged.isEmpty()) {

                merged.add(
                        current
                );

                continue;
            }


            AppSession previous =
                    merged.get(
                            merged.size() - 1
                    );


            boolean samePackage =
                    previous
                            .getPackageName()
                            .equals(
                                    current
                                            .getPackageName()
                            );


            boolean smallGap =
                    current.getStartTime()
                            <= previous.getEndTime()
                            + SESSION_MERGE_GAP_MS;


            if (samePackage &&
                    smallGap) {

                long mergedEnd =
                        Math.max(
                                previous.getEndTime(),
                                current.getEndTime()
                        );


                AppSession combined =
                        new AppSession(
                                previous.getPackageName(),
                                previous.getStartTime(),
                                mergedEnd,
                                previous.isCompleted()
                                        && current.isCompleted()
                        );


                merged.set(
                        merged.size() - 1,
                        combined
                );

            } else {

                merged.add(
                        current
                );
            }
        }


        return merged;
    }


    /**
     * Combines overlapping intervals so usage time is never
     * double-counted.
     */
    private static List<long[]> getNonOverlappingIntervals(
            List<AppSession> sessions
    ) {

        List<long[]> result =
                new ArrayList<>();


        if (sessions == null ||
                sessions.isEmpty()) {

            return result;
        }


        List<AppSession> sorted =
                new ArrayList<>(
                        sessions
                );


        Collections.sort(
                sorted,
                new Comparator<AppSession>() {

                    @Override
                    public int compare(
                            AppSession first,
                            AppSession second
                    ) {

                        return Long.compare(
                                first.getStartTime(),
                                second.getStartTime()
                        );
                    }
                }
        );


        for (AppSession session :
                sorted) {

            long start =
                    session.getStartTime();


            long end =
                    session.getEndTime();


            if (end <= start) {
                continue;
            }


            if (result.isEmpty()) {

                result.add(
                        new long[]{
                                start,
                                end
                        }
                );

                continue;
            }


            long[] previous =
                    result.get(
                            result.size() - 1
                    );


            if (start <=
                    previous[1]) {

                previous[1] =
                        Math.max(
                                previous[1],
                                end
                        );

            } else {

                result.add(
                        new long[]{
                                start,
                                end
                        }
                );
            }
        }


        return result;
    }
}
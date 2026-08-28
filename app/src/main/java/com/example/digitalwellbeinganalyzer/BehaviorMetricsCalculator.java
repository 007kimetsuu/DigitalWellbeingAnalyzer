package com.example.digitalwellbeinganalyzer;

import android.content.Context;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BehaviorMetricsCalculator {

    /*
     * Sessions <= 2 minutes are considered short sessions.
     *
     * This is a project heuristic used to describe fragmented
     * usage. It is NOT a medically validated threshold.
     */
    private static final long SHORT_SESSION_THRESHOLD_MS =
            2L * 60L * 1000L;


    private BehaviorMetricsCalculator() {
        // Utility class
    }


    // =========================================================
    // TODAY'S BEHAVIOR METRICS
    // =========================================================

    public static BehaviorMetrics calculateToday(
            Context context
    ) {

        if (context == null) {

            return createEmptyMetrics();
        }


        List<UsageStatsHelper.AppSession> sessions =
                UsageStatsHelper.getTodaySessions(
                        context
                );

        List<UsageStatsHelper.AppUsageItem> appUsage =
                UsageStatsHelper.getMostUsedAppsToday(
                        context
                );

        long totalScreenTimeMs = 0L;
        long longestSessionMs = 0L;

        for (UsageStatsHelper.AppSession session : sessions) {

            long duration =
                    session.getDurationMs();

            totalScreenTimeMs +=
                    duration;

            if (duration > longestSessionMs) {

                longestSessionMs =
                        duration;
            }
        }

        int sessionCount =
                sessions.size();

        long averageSessionDurationMs =
                sessionCount > 0
                        ? totalScreenTimeMs / sessionCount
                        : 0L;


        // =====================================================
        // FRAGMENTATION
        // =====================================================

        int shortSessionCount = 0;

        for (UsageStatsHelper.AppSession session :
                sessions) {

            if (session.getDurationMs()
                    <= SHORT_SESSION_THRESHOLD_MS) {

                shortSessionCount++;
            }
        }


        double fragmentationPercent =
                sessionCount > 0
                        ? (shortSessionCount * 100.0)
                          / sessionCount
                        : 0.0;


        // =====================================================
        // APP SWITCHING
        // =====================================================

        int appSwitchCount = 0;

        for (int i = 1;
             i < sessions.size();
             i++) {

            UsageStatsHelper.AppSession previous =
                    sessions.get(i - 1);

            UsageStatsHelper.AppSession current =
                    sessions.get(i);


            if (!previous
                    .getPackageName()
                    .equals(
                            current.getPackageName()
                    )) {

                appSwitchCount++;
            }
        }


        // =====================================================
        // REPEATED APP OPENINGS
        // =====================================================

        Map<String, Integer> openingCounts =
                new HashMap<>();


        Set<String> uniquePackages =
                new HashSet<>();


        for (UsageStatsHelper.AppSession session :
                sessions) {

            String packageName =
                    session.getPackageName();

            if (packageName == null ||
                    packageName.isEmpty()) {

                continue;
            }


            uniquePackages.add(
                    packageName
            );


            openingCounts.merge(
                    packageName,
                    1,
                    Integer::sum
            );
        }


        int repeatedAppOpenings = 0;


        for (int count :
                openingCounts.values()) {

            if (count > 1) {

                repeatedAppOpenings +=
                        count - 1;
            }
        }


        int uniqueAppsUsed =
                uniquePackages.size();


        // =====================================================
        // LATE-NIGHT USAGE
        // =====================================================

        long lateNightUsageMs = 0L;

        int lateNightSessionCount = 0;


        for (UsageStatsHelper.AppSession session :
                sessions) {

            long overlap =
                    getLateNightOverlapToday(
                            session.getStartTime(),
                            session.getEndTime()
                    );


            if (overlap > 0) {

                lateNightUsageMs +=
                        overlap;

                lateNightSessionCount++;
            }
        }


        // =====================================================
        // PRODUCTIVITY / DISTRACTION
        // =====================================================

        long productiveUsageMs = 0L;
        long distractingUsageMs = 0L;


        for (UsageStatsHelper.AppUsageItem item :
                appUsage) {

            String category =
                    AppCategoryClassifier.getCategory(
                            context,
                            item.getPackageName()
                    );


            long duration =
                    item.getTotalUsageMs();


            if (AppCategoryClassifier
                    .CATEGORY_EDUCATION
                    .equals(category)
                    ||
                    AppCategoryClassifier
                            .CATEGORY_PRODUCTIVITY
                            .equals(category)) {

                productiveUsageMs +=
                        duration;
            }


            if (AppCategoryClassifier
                    .CATEGORY_SOCIAL
                    .equals(category)
                    ||
                    AppCategoryClassifier
                            .CATEGORY_ENTERTAINMENT
                            .equals(category)
                    ||
                    AppCategoryClassifier
                            .CATEGORY_GAMING
                            .equals(category)) {

                distractingUsageMs +=
                        duration;
            }
        }


        double productivityPercent =
                totalScreenTimeMs > 0
                        ? (productiveUsageMs * 100.0)
                          / totalScreenTimeMs
                        : 0.0;


        // =====================================================
        // RESULT
        // =====================================================

        return new BehaviorMetrics(

                totalScreenTimeMs,

                sessionCount,
                averageSessionDurationMs,
                longestSessionMs,

                shortSessionCount,
                fragmentationPercent,

                appSwitchCount,
                repeatedAppOpenings,
                uniqueAppsUsed,

                lateNightUsageMs,
                lateNightSessionCount,

                productiveUsageMs,
                distractingUsageMs,
                productivityPercent
        );
    }


    // =========================================================
    // LATE NIGHT OVERLAP
    // =========================================================

    /**
     * MindTrack currently defines late-night usage as:
     *
     * 11:00 PM -> 5:00 AM
     *
     * This matches the existing AdvancedContextService rule.
     */
    private static long getLateNightOverlapToday(
            long sessionStart,
            long sessionEnd
    ) {

        if (sessionEnd <= sessionStart) {
            return 0L;
        }


        Calendar midnight =
                Calendar.getInstance();

        midnight.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        midnight.set(
                Calendar.MINUTE,
                0
        );

        midnight.set(
                Calendar.SECOND,
                0
        );

        midnight.set(
                Calendar.MILLISECOND,
                0
        );


        long dayStart =
                midnight.getTimeInMillis();


        Calendar fiveAm =
                (Calendar) midnight.clone();

        fiveAm.set(
                Calendar.HOUR_OF_DAY,
                5
        );


        Calendar elevenPm =
                (Calendar) midnight.clone();

        elevenPm.set(
                Calendar.HOUR_OF_DAY,
                23
        );


        Calendar nextMidnight =
                (Calendar) midnight.clone();

        nextMidnight.add(
                Calendar.DAY_OF_YEAR,
                1
        );


        long overlapEarlyMorning =
                calculateOverlap(
                        sessionStart,
                        sessionEnd,
                        dayStart,
                        fiveAm.getTimeInMillis()
                );


        long overlapLateEvening =
                calculateOverlap(
                        sessionStart,
                        sessionEnd,
                        elevenPm.getTimeInMillis(),
                        nextMidnight.getTimeInMillis()
                );


        return overlapEarlyMorning +
                overlapLateEvening;
    }


    private static long calculateOverlap(
            long startA,
            long endA,
            long startB,
            long endB
    ) {

        long start =
                Math.max(
                        startA,
                        startB
                );


        long end =
                Math.min(
                        endA,
                        endB
                );


        return Math.max(
                0L,
                end - start
        );
    }


    // =========================================================
    // EMPTY RESULT
    // =========================================================

    private static BehaviorMetrics createEmptyMetrics() {

        return new BehaviorMetrics(

                0L,

                0,
                0L,
                0L,

                0,
                0.0,

                0,
                0,
                0,

                0L,
                0,

                0L,
                0L,
                0.0
        );
    }
}
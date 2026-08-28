package com.example.digitalwellbeinganalyzer;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextualAnalysisCalculator {

    private ContextualAnalysisCalculator() {
        // Utility class
    }


    // =========================================================
    // TODAY'S CONTEXTUAL ANALYSIS
    // =========================================================

    public static ContextualUsageMetrics calculateToday(
            Context context
    ) {

        if (context == null) {
            return createEmptyMetrics();
        }


        List<UsageStatsHelper.AppSession> rawSessions =
                UsageStatsHelper.getTodaySessions(
                        context
                );


        List<ContextualUsageMetrics.ContextualSession>
                contextualSessions =
                new ArrayList<>();


        // =====================================================
        // TIME CONTEXT TOTALS
        // =====================================================

        long earlyMorningUsageMs = 0L;
        long daytimeUsageMs = 0L;
        long eveningUsageMs = 0L;
        long lateNightUsageMs = 0L;


        // =====================================================
        // CATEGORY CONTEXT TOTALS
        // =====================================================

        long productiveUsageMs = 0L;
        long distractingUsageMs = 0L;
        long neutralUsageMs = 0L;


        Map<String, Long> categoryTotals =
                new HashMap<>();


        // =====================================================
        // TODAY TYPE
        // =====================================================

        Calendar today =
                Calendar.getInstance();

        int todayDayOfWeek =
                today.get(
                        Calendar.DAY_OF_WEEK
                );


        boolean todayIsWeekend =
                todayDayOfWeek == Calendar.SATURDAY
                        ||
                        todayDayOfWeek == Calendar.SUNDAY;


        // =====================================================
        // PROCESS EACH SESSION
        // =====================================================

        for (UsageStatsHelper.AppSession session :
                rawSessions) {

            if (session == null) {
                continue;
            }


            String packageName =
                    session.getPackageName();


            long startTime =
                    session.getStartTime();

            long endTime =
                    session.getEndTime();

            long durationMs =
                    session.getDurationMs();


            if (packageName == null ||
                    packageName.isEmpty() ||
                    durationMs <= 0) {

                continue;
            }


            // -------------------------------------------------
            // CATEGORY
            // -------------------------------------------------

            String category =
                    AppCategoryClassifier.getCategory(
                            context,
                            packageName
                    );


            categoryTotals.merge(
                    category,
                    durationMs,
                    Long::sum
            );


            // -------------------------------------------------
            // START TIME CONTEXT
            // -------------------------------------------------

            Calendar startCalendar =
                    Calendar.getInstance();

            startCalendar.setTimeInMillis(
                    startTime
            );


            int startHour =
                    startCalendar.get(
                            Calendar.HOUR_OF_DAY
                    );


            int dayOfWeek =
                    startCalendar.get(
                            Calendar.DAY_OF_WEEK
                    );


            boolean weekend =
                    dayOfWeek == Calendar.SATURDAY
                            ||
                            dayOfWeek == Calendar.SUNDAY;


            // -------------------------------------------------
            // LATE NIGHT
            // -------------------------------------------------

            boolean lateNight =
                    hasLateNightOverlap(
                            startTime,
                            endTime
                    );


            // -------------------------------------------------
            // CATEGORY BEHAVIOR CONTEXT
            // -------------------------------------------------

            boolean productive =
                    isProductiveCategory(
                            category
                    );


            boolean distracting =
                    isDistractingCategory(
                            category
                    );


            if (productive) {

                productiveUsageMs +=
                        durationMs;

            } else if (distracting) {

                distractingUsageMs +=
                        durationMs;

            } else {

                neutralUsageMs +=
                        durationMs;
            }


            // -------------------------------------------------
            // TIME-OF-DAY DURATIONS
            // -------------------------------------------------

            earlyMorningUsageMs +=
                    getTimeRangeOverlapToday(
                            startTime,
                            endTime,
                            5,
                            8
                    );


            daytimeUsageMs +=
                    getTimeRangeOverlapToday(
                            startTime,
                            endTime,
                            8,
                            17
                    );


            eveningUsageMs +=
                    getTimeRangeOverlapToday(
                            startTime,
                            endTime,
                            17,
                            23
                    );


            lateNightUsageMs +=
                    getLateNightOverlapToday(
                            startTime,
                            endTime
                    );


            // -------------------------------------------------
            // CONTEXTUAL SESSION
            // -------------------------------------------------

            ContextualUsageMetrics.ContextualSession
                    contextualSession =
                    new ContextualUsageMetrics.ContextualSession(

                            packageName,
                            category,

                            startTime,
                            endTime,
                            durationMs,

                            startHour,
                            dayOfWeek,

                            weekend,
                            lateNight,

                            productive,
                            distracting
                    );


            contextualSessions.add(
                    contextualSession
            );
        }


        // =====================================================
        // DOMINANT CATEGORY
        // =====================================================

        String dominantCategory =
                findDominantCategory(
                        categoryTotals
                );


        // =====================================================
        // PEAK USAGE HOUR
        // =====================================================

        int peakUsageHour =
                findPeakUsageHour(
                        context
                );


        // =====================================================
        // RESULT
        // =====================================================

        return new ContextualUsageMetrics(

                contextualSessions,

                earlyMorningUsageMs,
                daytimeUsageMs,
                eveningUsageMs,
                lateNightUsageMs,

                peakUsageHour,

                todayIsWeekend,

                dominantCategory,

                productiveUsageMs,
                distractingUsageMs,
                neutralUsageMs
        );
    }


    // =========================================================
    // PRODUCTIVITY CLASSIFICATION
    // =========================================================

    private static boolean isProductiveCategory(
            String category
    ) {

        return AppCategoryClassifier
                .CATEGORY_EDUCATION
                .equals(category)

                ||

                AppCategoryClassifier
                        .CATEGORY_PRODUCTIVITY
                        .equals(category);
    }


    // =========================================================
    // DISTRACTING CLASSIFICATION
    // =========================================================

    private static boolean isDistractingCategory(
            String category
    ) {

        return AppCategoryClassifier
                .CATEGORY_SOCIAL
                .equals(category)

                ||

                AppCategoryClassifier
                        .CATEGORY_ENTERTAINMENT
                        .equals(category)

                ||

                AppCategoryClassifier
                        .CATEGORY_GAMING
                        .equals(category);
    }


    // =========================================================
    // DOMINANT CATEGORY
    // =========================================================

    private static String findDominantCategory(
            Map<String, Long> totals
    ) {

        if (totals == null ||
                totals.isEmpty()) {

            return AppCategoryClassifier
                    .CATEGORY_OTHER;
        }


        String dominant =
                AppCategoryClassifier
                        .CATEGORY_OTHER;


        long highestUsage =
                0L;


        for (Map.Entry<String, Long> entry :
                totals.entrySet()) {

            long value =
                    entry.getValue() != null
                            ? entry.getValue()
                            : 0L;


            if (value > highestUsage) {

                highestUsage =
                        value;

                dominant =
                        entry.getKey();
            }
        }


        return dominant;
    }


    // =========================================================
    // PEAK USAGE HOUR
    // =========================================================

    private static int findPeakUsageHour(
            Context context
    ) {

        List<UsageStatsHelper.HourlyUsageSummary>
                hourlyUsage =
                UsageStatsHelper
                        .getHourlyUsageBetween(

                                context,

                                UsageStatsHelper
                                        .getStartOfToday(),

                                System.currentTimeMillis()
                        );


        int peakHour = -1;
        long peakDuration = 0L;


        for (UsageStatsHelper.HourlyUsageSummary hour :
                hourlyUsage) {

            if (hour.getTotalUsageMs()
                    > peakDuration) {

                peakDuration =
                        hour.getTotalUsageMs();

                peakHour =
                        hour.getHour();
            }
        }


        return peakHour;
    }


    // =========================================================
    // LATE NIGHT DETECTION
    // =========================================================

    private static boolean hasLateNightOverlap(
            long startTime,
            long endTime
    ) {

        return getLateNightOverlapToday(
                startTime,
                endTime
        ) > 0;
    }


    /**
     * Late-night definition matches
     * AdvancedContextService:
     *
     * 11 PM -> 5 AM
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


        long earlyOverlap =
                calculateOverlap(
                        sessionStart,
                        sessionEnd,

                        dayStart,
                        fiveAm.getTimeInMillis()
                );


        long lateOverlap =
                calculateOverlap(
                        sessionStart,
                        sessionEnd,

                        elevenPm.getTimeInMillis(),
                        nextMidnight.getTimeInMillis()
                );


        return earlyOverlap +
                lateOverlap;
    }


    // =========================================================
    // GENERIC TIME CONTEXT
    // =========================================================

    private static long getTimeRangeOverlapToday(
            long sessionStart,
            long sessionEnd,
            int startHour,
            int endHour
    ) {

        Calendar dayStart =
                Calendar.getInstance();

        dayStart.set(
                Calendar.HOUR_OF_DAY,
                startHour
        );

        dayStart.set(
                Calendar.MINUTE,
                0
        );

        dayStart.set(
                Calendar.SECOND,
                0
        );

        dayStart.set(
                Calendar.MILLISECOND,
                0
        );


        Calendar dayEnd =
                Calendar.getInstance();

        dayEnd.set(
                Calendar.HOUR_OF_DAY,
                endHour
        );

        dayEnd.set(
                Calendar.MINUTE,
                0
        );

        dayEnd.set(
                Calendar.SECOND,
                0
        );

        dayEnd.set(
                Calendar.MILLISECOND,
                0
        );


        return calculateOverlap(
                sessionStart,
                sessionEnd,

                dayStart.getTimeInMillis(),
                dayEnd.getTimeInMillis()
        );
    }


    // =========================================================
    // INTERVAL OVERLAP
    // =========================================================

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

    private static ContextualUsageMetrics createEmptyMetrics() {

        return new ContextualUsageMetrics(

                new ArrayList<>(),

                0L,
                0L,
                0L,
                0L,

                -1,

                false,

                AppCategoryClassifier
                        .CATEGORY_OTHER,

                0L,
                0L,
                0L
        );
    }
}
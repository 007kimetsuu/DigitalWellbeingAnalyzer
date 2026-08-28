package com.example.digitalwellbeinganalyzer;

import android.content.Context;

import java.util.Calendar;
import java.util.List;

public class InsightsEngine {

    /*
     * Project heuristic:
     *
     * A productive session lasting at least 25 minutes
     * contributes to "deep focus".
     *
     * This is inspired by common focus-session patterns,
     * but is NOT a scientifically validated attention metric.
     */
    private static final long DEEP_FOCUS_THRESHOLD_MS =
            25L * 60L * 1000L;


    /*
     * Minimum productive usage per day for that day
     * to count toward the consistency streak.
     */
    private static final long CONSISTENCY_THRESHOLD_MS =
            30L * 60L * 1000L;

    /*
     * Weekly percentage comparisons become misleading when
     * the previous period contains only a tiny amount of usage.
     *
     * Require at least 30 minutes of recorded usage in the
     * previous comparison period before calculating a percentage.
     *
     * This is a project data-quality threshold, not a clinical rule.
     */
    private static final long
            MIN_WEEKLY_COMPARISON_BASELINE_MS =
            30L * 60L * 1000L;

    private InsightsEngine() {
        // Utility class
    }


    // =========================================================
    // MAIN INSIGHT GENERATION
    // =========================================================

    public static InsightsResult generateToday(
            Context context
    ) {

        if (context == null) {
            return createEmptyResult();
        }


        BehaviorMetrics behavior =
                BehaviorMetricsCalculator
                        .calculateToday(
                                context
                        );


        ContextualUsageMetrics contextual =
                ContextualAnalysisCalculator
                        .calculateToday(
                                context
                        );


        BehaviorInferenceResult inference =
                BehaviorRuleEngine.analyze(
                        behavior,
                        contextual
                );


        // =====================================================
        // PRIMARY INSIGHT
        // =====================================================

        String primaryTitle;
        String primaryDescription;


        if (inference.lateNightPattern &&
                inference.distractionHeavy) {

            primaryTitle =
                    "Late-Night Distraction Pattern";

            primaryDescription =
                    "A meaningful portion of your recent usage occurred late at night and was concentrated in social, entertainment, or gaming activity.";

        } else if (inference.fragmentedUsage &&
                inference.frequentAppSwitching) {

            primaryTitle =
                    "Fragmented Usage Pattern";

            primaryDescription =
                    "Your day contained many short sessions and frequent app switching, suggesting low-continuity phone usage.";

        } else if (inference.prolongedSessions) {

            primaryTitle =
                    "Long Continuous Session";

            primaryDescription =
                    "At least one extended app session was detected today. Long uninterrupted sessions can be useful for focused work but may also indicate prolonged passive use.";

        } else if (inference.distractionHeavy) {

            primaryTitle =
                    "Distraction-Heavy Usage";

            primaryDescription =
                    "Most classified usage today was concentrated in social, entertainment, or gaming categories.";

        } else if (behavior.productivityPercent >= 50.0 &&
                behavior.totalScreenTimeMs > 0) {

            primaryTitle =
                    "Productive Usage Pattern";

            primaryDescription =
                    "A large share of your classified phone usage today was spent in education or productivity applications.";

        } else {

            primaryTitle =
                    "Balanced Usage Pattern";

            primaryDescription =
                    "No strong behavioral warning pattern was detected from the usage data currently available.";
        }


        // =====================================================
        // WEEKLY COMPARISON
        // =====================================================

        WeeklyComparison weeklyComparison =
                calculateWeeklyComparison(
                        context
                );


        // =====================================================
        // DEEP FOCUS
        // =====================================================

        long deepFocusMs =
                calculateDeepFocusToday(
                        context
                );


        // =====================================================
        // PICKUPS
        // =====================================================

        /*
         * IMPORTANT:
         *
         * Current ScreenStateReceiver only observes SCREEN_ON,
         * which is not a guaranteed physical pickup.
         *
         * It also does not persist those events.
         *
         * Therefore pickup count remains unavailable.
         */
        int pickupCount = -1;
        boolean pickupDataAvailable = false;


        // =====================================================
        // CONSISTENCY
        // =====================================================

        int consistencyStreak =
                calculateProductiveStreak(
                        context
                );


        String consistencyTitle;

        String consistencyDescription;


        if (consistencyStreak >= 5) {

            consistencyTitle =
                    consistencyStreak +
                            " Day Consistency Streak";

            consistencyDescription =
                    "You have maintained at least 30 minutes of productive usage across several consecutive days.";

        } else if (consistencyStreak >= 2) {

            consistencyTitle =
                    consistencyStreak +
                            " Day Streak";

            consistencyDescription =
                    "Your recent productive usage has been relatively consistent. Continue building the pattern.";

        } else if (consistencyStreak == 1) {

            consistencyTitle =
                    "Streak Started";

            consistencyDescription =
                    "You reached today's productive-usage target. Repeat it tomorrow to build consistency.";

        } else {

            consistencyTitle =
                    "Build Consistency";

            consistencyDescription =
                    "No current productive-usage streak was detected from the available recent usage data.";
        }


        return new InsightsResult(

                primaryTitle,
                primaryDescription,

                weeklyComparison.changePercent,
                weeklyComparison.available,
                weeklyComparison.description,

                deepFocusMs,

                pickupCount,
                pickupDataAvailable,

                consistencyStreak,
                consistencyTitle,
                consistencyDescription
        );
    }


    // =========================================================
    // WEEKLY COMPARISON
    // =========================================================

    private static WeeklyComparison calculateWeeklyComparison(
            Context context
    ) {

        Calendar currentStart =
                Calendar.getInstance();

        currentStart.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        currentStart.set(
                Calendar.MINUTE,
                0
        );

        currentStart.set(
                Calendar.SECOND,
                0
        );

        currentStart.set(
                Calendar.MILLISECOND,
                0
        );

        currentStart.add(
                Calendar.DAY_OF_YEAR,
                -6
        );


        Calendar previousStart =
                (Calendar) currentStart.clone();

        previousStart.add(
                Calendar.DAY_OF_YEAR,
                -7
        );


        long now =
                System.currentTimeMillis();


        long currentUsage =
                UsageStatsHelper
                        .getTotalSessionUsageBetween(
                                context,
                                currentStart.getTimeInMillis(),
                                now
                        );


        long previousUsage =
                UsageStatsHelper
                        .getTotalSessionUsageBetween(
                                context,
                                previousStart.getTimeInMillis(),
                                currentStart.getTimeInMillis()
                        );


        if (previousUsage <
                MIN_WEEKLY_COMPARISON_BASELINE_MS) {

            return new WeeklyComparison(

                    0.0,
                    false,

                    "Not enough previous-week usage data is available for a reliable comparison."
            );
        }


        double change =
                ((currentUsage - previousUsage)
                        * 100.0)
                        / previousUsage;


        String description;


        if (change > 5.0) {

            description =
                    "Your recorded usage is higher than the previous comparable period.";

        } else if (change < -5.0) {

            description =
                    "Your recorded usage is lower than the previous comparable period.";

        } else {

            description =
                    "Your recorded usage is similar to the previous comparable period.";
        }


        return new WeeklyComparison(

                change,
                true,
                description
        );
    }


    // =========================================================
    // DEEP FOCUS
    // =========================================================

    private static long calculateDeepFocusToday(
            Context context
    ) {

        List<UsageStatsHelper.AppSession> sessions =
                UsageStatsHelper
                        .getTodaySessions(
                                context
                        );


        long totalDeepFocus = 0L;


        for (UsageStatsHelper.AppSession session :
                sessions) {

            if (session == null) {
                continue;
            }


            String category =
                    AppCategoryClassifier
                            .getCategory(
                                    context,
                                    session.getPackageName()
                            );


            boolean productive =
                    AppCategoryClassifier
                            .CATEGORY_PRODUCTIVITY
                            .equals(category)

                            ||

                            AppCategoryClassifier
                                    .CATEGORY_EDUCATION
                                    .equals(category);


            if (productive &&
                    session.getDurationMs()
                            >= DEEP_FOCUS_THRESHOLD_MS) {

                totalDeepFocus +=
                        session.getDurationMs();
            }
        }


        return totalDeepFocus;
    }


    // =========================================================
    // PRODUCTIVE CONSISTENCY STREAK
    // =========================================================

    private static int calculateProductiveStreak(
            Context context
    ) {

        int streak = 0;


        Calendar dayStart =
                Calendar.getInstance();

        dayStart.set(
                Calendar.HOUR_OF_DAY,
                0
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


        long now =
                System.currentTimeMillis();


        for (int daysAgo = 0;
             daysAgo < 7;
             daysAgo++) {

            Calendar currentDay =
                    (Calendar) dayStart.clone();

            currentDay.add(
                    Calendar.DAY_OF_YEAR,
                    -daysAgo
            );


            Calendar nextDay =
                    (Calendar) currentDay.clone();

            nextDay.add(
                    Calendar.DAY_OF_YEAR,
                    1
            );


            long rangeStart =
                    currentDay.getTimeInMillis();


            long rangeEnd =
                    Math.min(
                            nextDay.getTimeInMillis(),
                            now
                    );


            long productiveUsage =
                    calculateProductiveUsageBetween(
                            context,
                            rangeStart,
                            rangeEnd
                    );


            if (productiveUsage
                    >= CONSISTENCY_THRESHOLD_MS) {

                streak++;

            } else {

                /*
                 * Streak is consecutive from today backwards.
                 */
                break;
            }
        }


        return streak;
    }


    private static long calculateProductiveUsageBetween(
            Context context,
            long startTime,
            long endTime
    ) {

        List<UsageStatsHelper.AppSession> sessions =
                UsageStatsHelper
                        .getAppSessionsBetween(
                                context,
                                startTime,
                                endTime
                        );


        long productiveUsage = 0L;


        for (UsageStatsHelper.AppSession session :
                sessions) {

            if (session == null) {
                continue;
            }


            String category =
                    AppCategoryClassifier
                            .getCategory(
                                    context,
                                    session.getPackageName()
                            );


            if (AppCategoryClassifier
                    .CATEGORY_EDUCATION
                    .equals(category)

                    ||

                    AppCategoryClassifier
                            .CATEGORY_PRODUCTIVITY
                            .equals(category)) {

                productiveUsage +=
                        session.getDurationMs();
            }
        }


        return productiveUsage;
    }


    // =========================================================
    // EMPTY RESULT
    // =========================================================

    private static InsightsResult createEmptyResult() {

        return new InsightsResult(

                "Not Enough Data",

                "MindTrack needs usage data before behavioral insights can be generated.",

                0.0,
                false,

                "Weekly comparison is not available.",

                0L,

                -1,
                false,

                0,

                "Build Consistency",

                "No consistency data is available yet."
        );
    }


    // =========================================================
    // INTERNAL WEEKLY MODEL
    // =========================================================

    private static class WeeklyComparison {

        final double changePercent;
        final boolean available;
        final String description;


        WeeklyComparison(
                double changePercent,
                boolean available,
                String description
        ) {

            this.changePercent =
                    changePercent;

            this.available =
                    available;

            this.description =
                    description;
        }
    }
}
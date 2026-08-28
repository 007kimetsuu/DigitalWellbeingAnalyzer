package com.example.digitalwellbeinganalyzer;

import java.util.ArrayList;
import java.util.List;

public class BehaviorRuleEngine {

    /*
     * IMPORTANT:
     *
     * These thresholds are project heuristics.
     * They are NOT medically or psychologically validated.
     *
     * They exist so MindTrack can generate transparent,
     * explainable behavioral interpretations.
     */

    private static final double FRAGMENTATION_THRESHOLD_PERCENT =
            60.0;

    private static final int HIGH_SWITCH_COUNT =
            20;

    private static final long LONG_SESSION_THRESHOLD_MS =
            45L * 60L * 1000L;

    private static final long SIGNIFICANT_LATE_NIGHT_USAGE_MS =
            30L * 60L * 1000L;

    private static final double DISTRACTION_RATIO_THRESHOLD =
            60.0;


    private BehaviorRuleEngine() {
        // Utility class
    }


    // =========================================================
    // MAIN ANALYSIS
    // =========================================================

    public static BehaviorInferenceResult analyze(
            BehaviorMetrics behavior,
            ContextualUsageMetrics context
    ) {

        if (behavior == null || context == null) {

            return createEmptyResult();
        }


        List<String> patterns =
                new ArrayList<>();


        int score = 0;


        // =====================================================
        // 1. FRAGMENTED USAGE
        // =====================================================

        boolean fragmentedUsage =
                behavior.sessionCount >= 5
                        &&
                        behavior.fragmentationPercent
                                >= FRAGMENTATION_THRESHOLD_PERCENT;


        if (fragmentedUsage) {

            score += 20;

            patterns.add(
                    "Frequent short sessions suggest fragmented usage."
            );
        }


        // =====================================================
        // 2. FREQUENT APP SWITCHING
        // =====================================================

        boolean frequentAppSwitching =
                behavior.appSwitchCount
                        >= HIGH_SWITCH_COUNT;


        if (frequentAppSwitching) {

            score += 15;

            patterns.add(
                    "Frequent app switching may indicate interrupted or low-continuity usage."
            );
        }


        // =====================================================
        // 3. PROLONGED SESSION
        // =====================================================

        boolean prolongedSessions =
                behavior.longestSessionMs
                        >= LONG_SESSION_THRESHOLD_MS;


        if (prolongedSessions) {

            score += 20;

            patterns.add(
                    "A prolonged continuous session was detected."
            );
        }


        // =====================================================
        // 4. LATE-NIGHT USAGE
        // =====================================================

        boolean lateNightPattern =
                context.lateNightUsageMs
                        >= SIGNIFICANT_LATE_NIGHT_USAGE_MS;


        if (lateNightPattern) {

            score += 20;

            patterns.add(
                    "Significant phone usage occurred during late-night hours."
            );
        }


        // =====================================================
        // 5. DISTRACTION RATIO
        // =====================================================

        long classifiedUsage =
                context.productiveUsageMs
                        +
                        context.distractingUsageMs;


        double distractionPercent = 0.0;


        if (classifiedUsage > 0) {

            distractionPercent =
                    context.distractingUsageMs
                            * 100.0
                            / classifiedUsage;
        }


        boolean distractionHeavy =
                classifiedUsage > 0
                        &&
                        distractionPercent
                                >= DISTRACTION_RATIO_THRESHOLD;


        if (distractionHeavy) {

            score += 15;

            patterns.add(
                    "Most classified usage was concentrated in social, entertainment, or gaming categories."
            );
        }


        // =====================================================
        // 6. COMBINED CONTEXT RULES
        // =====================================================

        if (lateNightPattern &&
                distractionHeavy) {

            score += 10;

            patterns.add(
                    "Late-night usage was also dominated by potentially distracting activity."
            );
        }


        if (fragmentedUsage &&
                frequentAppSwitching) {

            score += 10;

            patterns.add(
                    "Short sessions combined with frequent switching indicate highly fragmented usage."
            );
        }


        // =====================================================
        // LIMIT SCORE
        // =====================================================

        score =
                Math.max(
                        0,
                        Math.min(
                                100,
                                score
                        )
                );


        // =====================================================
        // OVERALL LEVEL
        // =====================================================

        String overallLevel;


        if (score >= 60) {

            overallLevel =
                    "High";

        } else if (score >= 30) {

            overallLevel =
                    "Moderate";

        } else {

            overallLevel =
                    "Low";
        }


        // =====================================================
        // HEALTHY / NO-PATTERN FALLBACK
        // =====================================================

        if (patterns.isEmpty()) {

            patterns.add(
                    "No strong behavioral warning patterns were detected from today's available usage data."
            );
        }


        return new BehaviorInferenceResult(

                score,

                overallLevel,

                patterns,

                fragmentedUsage,
                prolongedSessions,
                lateNightPattern,
                distractionHeavy,
                frequentAppSwitching
        );
    }


    // =========================================================
    // EMPTY RESULT
    // =========================================================

    private static BehaviorInferenceResult createEmptyResult() {

        List<String> patterns =
                new ArrayList<>();

        patterns.add(
                "Not enough behavioral data is available for rule-based analysis."
        );


        return new BehaviorInferenceResult(

                0,

                "Low",

                patterns,

                false,
                false,
                false,
                false,
                false
        );
    }
}
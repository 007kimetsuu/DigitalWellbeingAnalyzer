package com.example.digitalwellbeinganalyzer;

public class RecommendationEngine {

    private RecommendationEngine() {
        // Utility class
    }


    // =========================================================
    // NEW SMART RECOMMENDATION ENGINE
    // =========================================================

    public static RecommendationResult generate(
            RiskEngineResult risk,
            BehaviorInferenceResult behavior,
            ContextualUsageMetrics context,
            InsightsResult insights
    ) {

        // =====================================================
        // 1. LATE-NIGHT + ELEVATED RISK
        // =====================================================

        if (behavior != null &&
                behavior.lateNightPattern &&
                risk != null &&
                risk.finalRiskLevel >=
                        RiskEngineResult.LEVEL_MEDIUM) {

            return new RecommendationResult(

                    "Reduce Late-Night Usage",

                    "Your recent usage shows meaningful activity during late-night hours. Consider setting a wind-down boundary before 11 PM.",

                    RecommendationResult.GUIDE_LATE_NIGHT,

                    "Late-night usage combined with an elevated behavioral risk level.",

                    3
            );
        }


        // =====================================================
        // 2. PROLONGED SESSION
        // =====================================================

        if (behavior != null &&
                behavior.prolongedSessions) {

            return new RecommendationResult(

                    "Take a Recovery Break",

                    "A long continuous phone session was detected. Step away for 10–15 minutes before continuing.",

                    RecommendationResult.GUIDE_BREAKS,

                    "At least one prolonged continuous usage session was detected.",

                    3
            );
        }


        // =====================================================
        // 3. FRAGMENTED + SWITCHING
        // =====================================================

        if (behavior != null &&
                behavior.fragmentedUsage &&
                behavior.frequentAppSwitching) {

            return new RecommendationResult(

                    "Reduce App Switching",

                    "Your usage is highly fragmented. Try staying with one intentional task for a focused period instead of repeatedly switching apps.",

                    RecommendationResult.GUIDE_PATTERNS,

                    "Frequent short sessions and repeated app switching were detected.",

                    3
            );
        }


        // =====================================================
        // 4. DISTRACTION-HEAVY USAGE
        // =====================================================

        if (behavior != null &&
                behavior.distractionHeavy) {

            return new RecommendationResult(

                    "Set a Screen-Time Boundary",

                    "A large share of your classified usage is concentrated in social, entertainment, or gaming apps. Consider setting a clear usage limit for your most-used distracting category.",

                    RecommendationResult.GUIDE_SCREEN_TIME,

                    "Distracting categories account for a high share of classified usage.",

                    2
            );
        }


        // =====================================================
        // 5. MODERATE / HIGH FINAL RISK
        // =====================================================

        if (risk != null &&
                risk.finalRiskLevel ==
                        RiskEngineResult.LEVEL_HIGH) {

            return new RecommendationResult(

                    "Review Your Usage Pattern",

                    "Several signals contributed to a high MindTrack risk score today. Review your recent sessions and take a deliberate break before continuing.",

                    RecommendationResult.GUIDE_PATTERNS,

                    "Combined ML and behavioral risk signals produced a high risk level.",

                    3
            );
        }


        if (risk != null &&
                risk.finalRiskLevel ==
                        RiskEngineResult.LEVEL_MEDIUM) {

            return new RecommendationResult(

                    "Reset Your Usage",

                    "Your recent usage shows some patterns worth monitoring. A short break and a more intentional next session may help.",

                    RecommendationResult.GUIDE_BREAKS,

                    "The combined MindTrack risk engine produced a moderate risk level.",

                    2
            );
        }


        // =====================================================
        // 6. LOW PRODUCTIVITY
        // =====================================================

        if (context != null) {

            long classifiedUsage =
                    context.productiveUsageMs +
                            context.distractingUsageMs;


            if (classifiedUsage > 0) {

                double productivePercent =
                        context.productiveUsageMs *
                                100.0 /
                                classifiedUsage;


                if (productivePercent < 25.0) {

                    return new RecommendationResult(

                            "Plan an Intentional Session",

                            "Productive activity forms a relatively small share of your classified usage today. Try dedicating your next session to one study or productivity task.",

                            RecommendationResult.GUIDE_PATTERNS,

                            "Productive usage represents less than 25% of classified activity.",

                            2
                    );
                }
            }
        }


        // =====================================================
        // 7. POSITIVE / LOW RISK
        // =====================================================

        if (risk != null &&
                risk.finalRiskLevel ==
                        RiskEngineResult.LEVEL_LOW) {

            return new RecommendationResult(

                    "Maintain Your Current Pattern",

                    "No strong risk pattern is currently dominating your usage. Continue using your phone intentionally and take regular breaks.",

                    RecommendationResult.GUIDE_PATTERNS,

                    "Current combined risk level is low.",

                    1
            );
        }


        // =====================================================
        // 8. NOT ENOUGH DATA
        // =====================================================

        return new RecommendationResult(

                "Keep Using MindTrack",

                "More usage data is needed before MindTrack can generate a meaningful personalized recommendation.",

                RecommendationResult.GUIDE_PATTERNS,

                "Insufficient behavioral or risk information is currently available.",

                1
        );
    }


    // =========================================================
    // OLD SURYA API — PRESERVED
    // =========================================================

    /**
     * Legacy recommendation method.
     *
     * Kept so existing Activities using
     * getRecommendation(RiskReading) do not break.
     */
    public static String getRecommendation(
            RiskReading latest
    ) {

        if (latest == null) {

            return "Start using your phone normally so we can build your profile.";
        }


        if (latest.isNightTime > 0 &&
                latest.riskLevel >= 1) {

            return "Late-night usage detected. Consider reducing phone use before bedtime.";
        }


        if (latest.rapidScrollFlag > 0 &&
                latest.scrollRate > 8) {

            return "Rapid scrolling detected. Consider taking a short break before continuing.";
        }


        if (latest.isHighRiskApp > 0 &&
                latest.riskLevel == 2) {

            return "Elevated risk was detected while using a high-risk app category. Consider switching to a more intentional activity.";
        }


        if (latest.hasVideoTitleDetected > 0 &&
                latest.riskLevel >= 1) {

            return "Possible passive video consumption was detected. Consider taking a break or switching to an intentional task.";
        }


        if (latest.riskLevel == 0) {

            return "Your latest recorded usage shows a low risk pattern.";
        }


        return "Moderate usage risk was detected. Consider taking a short break and reviewing your current activity.";
    }
}
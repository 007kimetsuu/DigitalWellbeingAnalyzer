package com.example.digitalwellbeinganalyzer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RiskEngine {

    /*
     * Project-level fusion weights.
     *
     * ML remains the primary signal because it is the trained model.
     * Transparent behavioral rules provide additional context.
     *
     * These weights are NOT clinically validated.
     */
    private static final double ML_WEIGHT = 0.65;
    private static final double BEHAVIOR_WEIGHT = 0.35;


    private RiskEngine() {
        // Utility class
    }


    // =========================================================
    // MAIN RISK ANALYSIS
    // =========================================================

    public static RiskEngineResult analyze(
            RiskReading latestReading,
            BehaviorInferenceResult behaviorResult
    ) {

        if (behaviorResult == null) {

            behaviorResult =
                    createEmptyBehaviorResult();
        }


        // =====================================================
        // BEHAVIOR SCORE
        // =====================================================

        int behaviorScore =
                clampScore(
                        behaviorResult.behaviorScore
                );


        // =====================================================
        // NO ML DATA CASE
        // =====================================================

        if (latestReading == null) {

            int finalScore =
                    behaviorScore;


            int finalLevel =
                    scoreToRiskLevel(
                            finalScore
                    );


            List<String> factors =
                    buildCriticalFactors(
                            null,
                            behaviorResult
                    );


            return new RiskEngineResult(

                    finalLevel,
                    finalScore,

                    -1,
                    0.0,
                    0.0,

                    behaviorScore,
                    behaviorResult.overallLevel,

                    factors,

                    false
            );
        }


        // =====================================================
        // BUILD ML FEATURE VECTOR
        // =====================================================

        double[] features =
                new double[]{

                        latestReading.appCategoryScore,
                        latestReading.keywordMatches,
                        latestReading.scrollRate,

                        latestReading.isHighRiskApp,
                        latestReading.rapidScrollFlag,
                        latestReading.isNightTime,

                        latestReading.hasUrlDetected,
                        latestReading.hasVideoTitleDetected
                };


        // =====================================================
        // RUN ACTUAL SURYA ML CLASSIFIER
        // =====================================================

        double[] probabilities =
                WellbeingClassifier.score(
                        features
                );


        // =====================================================
        // VALIDATE OUTPUT
        // =====================================================

        if (probabilities == null ||
                probabilities.length < 3) {

            int finalScore =
                    behaviorScore;


            int finalLevel =
                    scoreToRiskLevel(
                            finalScore
                    );


            return new RiskEngineResult(

                    finalLevel,
                    finalScore,

                    -1,
                    0.0,
                    0.0,

                    behaviorScore,
                    behaviorResult.overallLevel,

                    buildCriticalFactors(
                            latestReading,
                            behaviorResult
                    ),

                    false
            );
        }


        // =====================================================
        // ML CLASS
        // =====================================================

        int mlRiskLevel =
                getArgMax(
                        probabilities
                );


        double mlConfidencePercent =
                clampProbability(
                        probabilities[mlRiskLevel]
                ) * 100.0;


        // =====================================================
        // CONTINUOUS ML RISK SCORE
        // =====================================================

        /*
         * LOW probability contributes 0 risk points.
         *
         * MEDIUM probability contributes up to 50.
         *
         * HIGH probability contributes up to 100.
         *
         * Example:
         *
         * LOW    0.10
         * MEDIUM 0.30
         * HIGH   0.60
         *
         * ML risk score =
         *
         * 0.30 * 50
         * +
         * 0.60 * 100
         *
         * = 75
         */

        double lowProbability =
                clampProbability(
                        probabilities[0]
                );


        double mediumProbability =
                clampProbability(
                        probabilities[1]
                );


        double highProbability =
                clampProbability(
                        probabilities[2]
                );


        double probabilityTotal =
                lowProbability
                        +
                        mediumProbability
                        +
                        highProbability;


        /*
         * Normalise defensively in case generated classifier
         * probabilities contain rounding differences.
         */
        if (probabilityTotal > 0) {

            lowProbability /=
                    probabilityTotal;

            mediumProbability /=
                    probabilityTotal;

            highProbability /=
                    probabilityTotal;
        }


        double mlRiskScore =
                (mediumProbability * 50.0)
                        +
                        (highProbability * 100.0);


        // =====================================================
        // COMBINED RISK SCORE
        // =====================================================

        int finalRiskScore =
                (int) Math.round(

                        (mlRiskScore * ML_WEIGHT)

                                +

                                (behaviorScore * BEHAVIOR_WEIGHT)
                );


        finalRiskScore =
                clampScore(
                        finalRiskScore
                );


        // =====================================================
        // FINAL LEVEL
        // =====================================================

        int finalRiskLevel =
                scoreToRiskLevel(
                        finalRiskScore
                );


        // =====================================================
        // CRITICAL FACTORS
        // =====================================================

        List<String> criticalFactors =
                buildCriticalFactors(
                        latestReading,
                        behaviorResult
                );


        return new RiskEngineResult(

                finalRiskLevel,
                finalRiskScore,

                mlRiskLevel,
                mlConfidencePercent,
                mlRiskScore,

                behaviorScore,
                behaviorResult.overallLevel,

                criticalFactors,

                true
        );
    }


    // =========================================================
    // CRITICAL FACTORS
    // =========================================================

    private static List<String> buildCriticalFactors(
            RiskReading reading,
            BehaviorInferenceResult behavior
    ) {

        /*
         * LinkedHashSet prevents duplicate messages
         * while preserving display order.
         */
        Set<String> factors =
                new LinkedHashSet<>();


        // =====================================================
        // BEHAVIOR RULE FACTORS
        // =====================================================

        if (behavior != null) {

            if (behavior.lateNightPattern) {

                factors.add(
                        "Significant late-night phone usage"
                );
            }


            if (behavior.prolongedSessions) {

                factors.add(
                        "Long continuous usage session"
                );
            }


            if (behavior.fragmentedUsage) {

                factors.add(
                        "Frequent short and fragmented sessions"
                );
            }


            if (behavior.frequentAppSwitching) {

                factors.add(
                        "High app-switching frequency"
                );
            }


            if (behavior.distractionHeavy) {

                factors.add(
                        "High social, entertainment, or gaming usage"
                );
            }
        }


        // =====================================================
        // LATEST ACCESSIBILITY / ML INPUT FACTORS
        // =====================================================

        if (reading != null) {

            if (reading.isNightTime > 0) {

                factors.add(
                        "Latest activity occurred during late-night hours"
                );
            }


            if (reading.rapidScrollFlag > 0) {

                factors.add(
                        "Rapid scrolling detected"
                );
            }


            if (reading.keywordMatches > 0) {

                factors.add(
                        "Stress-related keywords detected in visible content"
                );
            }


            if (reading.isHighRiskApp > 0) {

                factors.add(
                        "Latest activity occurred in a high-risk app category"
                );
            }


            if (reading.hasVideoTitleDetected > 0) {

                factors.add(
                        "Possible passive video-content pattern detected"
                );
            }
        }


        // =====================================================
        // FALLBACK
        // =====================================================

        if (factors.isEmpty()) {

            factors.add(
                    "No strong risk factors detected in the available data"
            );
        }


        return new ArrayList<>(
                factors
        );
    }


    // =========================================================
    // SCORE -> LEVEL
    // =========================================================

    private static int scoreToRiskLevel(
            int score
    ) {

        if (score >= 67) {

            return RiskEngineResult.LEVEL_HIGH;
        }


        if (score >= 34) {

            return RiskEngineResult.LEVEL_MEDIUM;
        }


        return RiskEngineResult.LEVEL_LOW;
    }


    // =========================================================
    // ARGMAX
    // =========================================================

    private static int getArgMax(
            double[] values
    ) {

        int maxIndex = 0;


        for (int i = 1;
             i < values.length;
             i++) {

            if (values[i]
                    > values[maxIndex]) {

                maxIndex = i;
            }
        }


        return maxIndex;
    }


    // =========================================================
    // SAFETY HELPERS
    // =========================================================

    private static int clampScore(
            int score
    ) {

        return Math.max(
                0,
                Math.min(
                        100,
                        score
                )
        );
    }


    private static double clampProbability(
            double probability
    ) {

        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        probability
                )
        );
    }


    // =========================================================
    // EMPTY BEHAVIOR RESULT
    // =========================================================

    private static BehaviorInferenceResult
    createEmptyBehaviorResult() {

        List<String> patterns =
                new ArrayList<>();


        patterns.add(
                "Behavioral metrics are not yet available."
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
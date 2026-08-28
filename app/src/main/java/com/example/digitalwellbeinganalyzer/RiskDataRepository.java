package com.example.digitalwellbeinganalyzer;

import android.content.Context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RiskDataRepository {

    private final RiskReadingDao dao;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();


    // =========================================================
    // CALLBACKS
    // =========================================================

    public interface ConfidenceCallback {
        void onResult(double confidencePercent);
    }

    public interface CategoryBreakdownCallback {
        void onResult(Map<String, Long> categoryTotals);
    }

    public interface HourlyActivityCallback {
        void onResult(List<HourlyActivitySummary> hourlyData);
    }

    public interface RecommendationCallback {
        void onResult(String recommendation);
    }

    public interface ScoreCallback {
        void onResult(int score);
    }

    public interface RiskReadingCallback {
        void onResult(RiskReading reading);
    }

    public interface DailySummaryCallback {
        void onResult(List<DailyRiskSummary> summaries);
    }

    public interface RiskReadingListCallback {
        void onResult(List<RiskReading> readings);
    }

    public interface RiskCountCallback {
        void onResult(int count);
    }

    public interface BehaviorMetricsCallback {
        void onResult(BehaviorMetrics metrics);
    }

    public interface ContextualUsageCallback {
        void onResult(ContextualUsageMetrics metrics);
    }

    public interface BehaviorInferenceCallback {
        void onResult(BehaviorInferenceResult result);
    }

    public interface FinalRiskCallback {
        void onResult(RiskEngineResult result);
    }

    public interface InsightsCallback {
        void onResult(InsightsResult insights);
    }

    public interface SmartRecommendationCallback {
        void onResult(RecommendationResult recommendation);
    }


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public RiskDataRepository(Context context) {

        Context appContext =
                context.getApplicationContext();

        AppDatabase db =
                AppDatabase.getInstance(appContext);

        this.dao =
                db.riskReadingDao();
    }


    // =========================================================
    // HOURLY ACTIVITY
    // =========================================================

    public void getHourlyActivityLast7Days(
            HourlyActivityCallback callback
    ) {

        executor.execute(() -> {

            long sevenDaysAgo =
                    System.currentTimeMillis()
                            - (7L * 24L * 60L * 60L * 1000L);

            List<HourlyActivitySummary> data =
                    dao.getHourlyActivitySince(
                            sevenDaysAgo
                    );

            callback.onResult(data);
        });
    }


    // =========================================================
    // ORIGINAL RULE-BASED RECOMMENDATION
    // =========================================================

    public void getRecommendation(
            RecommendationCallback callback
    ) {

        executor.execute(() -> {

            RiskReading latest =
                    dao.getLatest();

            String tip =
                    RecommendationEngine
                            .getRecommendation(
                                    latest
                            );

            callback.onResult(tip);
        });
    }


    // =========================================================
    // CATEGORY BREAKDOWN
    // =========================================================

    public void getCategoryBreakdownToday(
            Context context,
            CategoryBreakdownCallback callback
    ) {

        Context appContext =
                context.getApplicationContext();

        executor.execute(() -> {

            Map<String, Long> categoryTotals =
                    AppCategoryClassifier
                            .getCategoryUsageToday(
                                    appContext
                            );

            callback.onResult(
                    categoryTotals
            );
        });
    }


    // =========================================================
    // ML CONFIDENCE
    // =========================================================

    public void getLatestConfidence(
            ConfidenceCallback callback
    ) {

        executor.execute(() -> {

            RiskReading latest =
                    dao.getLatest();

            if (latest == null) {

                callback.onResult(0.0);

                return;
            }


            double[] features =
                    new double[]{

                            latest.appCategoryScore,
                            latest.keywordMatches,
                            latest.scrollRate,
                            latest.isHighRiskApp,
                            latest.rapidScrollFlag,
                            latest.isNightTime,
                            latest.hasUrlDetected,
                            latest.hasVideoTitleDetected
                    };


            double[] probabilities =
                    WellbeingClassifier
                            .score(features);


            double confidence =
                    probabilities[
                            latest.riskLevel
                            ] * 100.0;


            callback.onResult(
                    confidence
            );
        });
    }


    // =========================================================
    // WELLBEING SCORE
    // =========================================================

    public void getWellbeingScore(
            ScoreCallback callback
    ) {

        executor.execute(() -> {

            List<RiskReading> recent =
                    dao.getRecent(50);


            if (recent.isEmpty()) {

                callback.onResult(100);

                return;
            }


            double totalPenalty =
                    0.0;


            for (RiskReading reading :
                    recent) {

                if (reading.riskLevel == 1) {

                    totalPenalty += 1.0;

                } else if (
                        reading.riskLevel == 2
                ) {

                    totalPenalty += 2.5;
                }
            }


            double averagePenalty =
                    totalPenalty /
                            recent.size();


            int score =
                    (int) Math.max(
                            0,
                            Math.min(
                                    100,
                                    100
                                            - (averagePenalty
                                            * 40)
                            )
                    );


            callback.onResult(
                    score
            );
        });
    }


    // =========================================================
    // DAILY SUMMARY
    // =========================================================

    public void getLast7DaysSummary(
            DailySummaryCallback callback
    ) {

        executor.execute(() -> {

            long sevenDaysAgo =
                    System.currentTimeMillis()
                            - (7L * 24L * 60L * 60L * 1000L);


            List<DailyRiskSummary> summaries =
                    dao.getDailySummarySince(
                            sevenDaysAgo
                    );


            callback.onResult(
                    summaries
            );
        });
    }


    // =========================================================
    // LATEST RISK
    // =========================================================

    public void getLatestRisk(
            RiskReadingCallback callback
    ) {

        executor.execute(() -> {

            RiskReading latest =
                    dao.getLatest();

            callback.onResult(
                    latest
            );
        });
    }


    // =========================================================
    // RECENT HISTORY
    // =========================================================

    public void getRecentHistory(
            int limit,
            RiskReadingListCallback callback
    ) {

        executor.execute(() -> {

            List<RiskReading> readings =
                    dao.getRecent(
                            limit
                    );

            callback.onResult(
                    readings
            );
        });
    }


    // =========================================================
    // FULL HISTORY
    // =========================================================

    public void getAllHistory(
            RiskReadingListCallback callback
    ) {

        executor.execute(() -> {

            List<RiskReading> readings =
                    dao.getAll();

            callback.onResult(
                    readings
            );
        });
    }


    // =========================================================
    // RISK LEVEL COUNT
    // =========================================================

    public void getRiskLevelCount(
            int riskLevel,
            RiskCountCallback callback
    ) {

        executor.execute(() -> {

            int count =
                    dao.getCountByRiskLevel(
                            riskLevel
                    );

            callback.onResult(
                    count
            );
        });
    }


    // =========================================================
    // BEHAVIOR METRICS
    // =========================================================

    public void getBehaviorMetricsToday(
            Context context,
            BehaviorMetricsCallback callback
    ) {

        Context appContext =
                context.getApplicationContext();

        executor.execute(() -> {

            BehaviorMetrics metrics =
                    BehaviorMetricsCalculator
                            .calculateToday(
                                    appContext
                            );


            callback.onResult(
                    metrics
            );
        });
    }


    // =========================================================
    // CONTEXTUAL USAGE
    // =========================================================

    public void getContextualUsageToday(
            Context context,
            ContextualUsageCallback callback
    ) {

        Context appContext =
                context.getApplicationContext();

        executor.execute(() -> {

            ContextualUsageMetrics metrics =
                    ContextualAnalysisCalculator
                            .calculateToday(
                                    appContext
                            );


            callback.onResult(
                    metrics
            );
        });
    }


    // =========================================================
    // BEHAVIORAL INFERENCE
    // =========================================================

    public void getBehaviorInferenceToday(
            Context context,
            BehaviorInferenceCallback callback
    ) {

        Context appContext =
                context.getApplicationContext();

        executor.execute(() -> {

            BehaviorMetrics behavior =
                    BehaviorMetricsCalculator
                            .calculateToday(
                                    appContext
                            );


            ContextualUsageMetrics contextual =
                    ContextualAnalysisCalculator
                            .calculateToday(
                                    appContext
                            );


            BehaviorInferenceResult result =
                    BehaviorRuleEngine.analyze(
                            behavior,
                            contextual
                    );


            callback.onResult(
                    result
            );
        });
    }


    // =========================================================
    // FINAL COMBINED RISK
    // =========================================================

    public void getFinalRiskToday(
            Context context,
            FinalRiskCallback callback
    ) {

        Context appContext =
                context.getApplicationContext();

        executor.execute(() -> {

            RiskReading latest =
                    dao.getLatest();


            BehaviorMetrics behaviorMetrics =
                    BehaviorMetricsCalculator
                            .calculateToday(
                                    appContext
                            );


            ContextualUsageMetrics contextualMetrics =
                    ContextualAnalysisCalculator
                            .calculateToday(
                                    appContext
                            );


            BehaviorInferenceResult behaviorResult =
                    BehaviorRuleEngine.analyze(
                            behaviorMetrics,
                            contextualMetrics
                    );


            RiskEngineResult finalResult =
                    RiskEngine.analyze(
                            latest,
                            behaviorResult
                    );


            callback.onResult(
                    finalResult
            );
        });
    }


    // =========================================================
    // INSIGHTS
    // =========================================================

    public void getInsightsToday(
            Context context,
            InsightsCallback callback
    ) {

        Context appContext =
                context.getApplicationContext();

        executor.execute(() -> {

            InsightsResult insights =
                    InsightsEngine.generateToday(
                            appContext
                    );


            callback.onResult(
                    insights
            );
        });
    }


    // =========================================================
    // SMART RECOMMENDATION
    // =========================================================

    public void getSmartRecommendationToday(
            Context context,
            SmartRecommendationCallback callback
    ) {

        Context appContext =
                context.getApplicationContext();

        executor.execute(() -> {

            BehaviorMetrics behaviorMetrics =
                    BehaviorMetricsCalculator
                            .calculateToday(
                                    appContext
                            );


            ContextualUsageMetrics contextualMetrics =
                    ContextualAnalysisCalculator
                            .calculateToday(
                                    appContext
                            );


            BehaviorInferenceResult behaviorInference =
                    BehaviorRuleEngine.analyze(
                            behaviorMetrics,
                            contextualMetrics
                    );


            RiskReading latest =
                    dao.getLatest();


            RiskEngineResult riskResult =
                    RiskEngine.analyze(
                            latest,
                            behaviorInference
                    );


            InsightsResult insights =
                    InsightsEngine.generateToday(
                            appContext
                    );


            RecommendationResult recommendation =
                    RecommendationEngine.generate(
                            riskResult,
                            behaviorInference,
                            contextualMetrics,
                            insights
                    );


            callback.onResult(
                    recommendation
            );
        });
    }


    // =========================================================
    // CLEANUP
    // =========================================================

    public void shutdown() {

        executor.shutdownNow();
    }
}
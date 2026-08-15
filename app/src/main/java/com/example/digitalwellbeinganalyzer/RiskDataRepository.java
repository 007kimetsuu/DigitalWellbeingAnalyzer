package com.example.digitalwellbeinganalyzer;

import android.content.Context;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RiskDataRepository {

    private final RiskReadingDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface ConfidenceCallback {
        void onResult(double confidencePercent);
    }
    public interface CategoryBreakdownCallback {
        void onResult(java.util.Map<String, Long> categoryTotals);
    }
    public interface HourlyActivityCallback {
        void onResult(List<HourlyActivitySummary> hourlyData);
    }

    public void getHourlyActivityLast7Days(HourlyActivityCallback callback) {
        executor.execute(() -> {
            long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
            List<HourlyActivitySummary> data = dao.getHourlyActivitySince(sevenDaysAgo);
            callback.onResult(data);
        });
    }
    public interface RecommendationCallback {
        void onResult(String recommendation);
    }

    public void getRecommendation(RecommendationCallback callback) {
        executor.execute(() -> {
            RiskReading latest = dao.getLatest();
            String tip = RecommendationEngine.getRecommendation(latest);
            callback.onResult(tip);
        });
    }
    public void getCategoryBreakdownToday(android.content.Context context, CategoryBreakdownCallback callback) {
        executor.execute(() -> {
            java.util.Map<String, Long> appUsage = UsageStatsHelper.getAllAppUsageToday(context);
            java.util.Map<String, Long> categoryTotals = AppCategoryClassifier.aggregateByCategory(appUsage);
            callback.onResult(categoryTotals);
        });
    }
    public void getLatestConfidence(ConfidenceCallback callback) {
        executor.execute(() -> {
            RiskReading latest = dao.getLatest();
            if (latest == null) {
                callback.onResult(0.0);
                return;
            }
            // Re-run classifier to get probability array for the stored feature vector
            double[] features = new double[] {
                    latest.appCategoryScore, latest.keywordMatches, latest.scrollRate,
                    latest.isHighRiskApp, latest.rapidScrollFlag, latest.isNightTime,
                    latest.hasUrlDetected, latest.hasVideoTitleDetected
            };
            double[] probabilities = WellbeingClassifier.score(features);
            double confidence = probabilities[latest.riskLevel] * 100;
            callback.onResult(confidence);
        });
    }
    public interface ScoreCallback {
        void onResult(int score);
    }

    public void getWellbeingScore(ScoreCallback callback) {
        executor.execute(() -> {
            List<RiskReading> recent = dao.getRecent(50); // last 50 readings
            if (recent.isEmpty()) {
                callback.onResult(100);
                return;
            }

            double totalPenalty = 0;
            for (RiskReading r : recent) {
                if (r.riskLevel == 1) totalPenalty += 1.0;      // MEDIUM
                else if (r.riskLevel == 2) totalPenalty += 2.5; // HIGH — weighted heavier
            }

            double avgPenalty = totalPenalty / recent.size();
            int score = (int) Math.max(0, Math.min(100, 100 - (avgPenalty * 40)));
            callback.onResult(score);
        });
    }
    public interface RiskReadingCallback {
        void onResult(RiskReading reading);
    }
    public interface DailySummaryCallback {
        void onResult(List<DailyRiskSummary> summaries);
    }

    public void getLast7DaysSummary(DailySummaryCallback callback) {
        executor.execute(() -> {
            long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
            List<DailyRiskSummary> summaries = dao.getDailySummarySince(sevenDaysAgo);
            callback.onResult(summaries);
        });
    }

    public interface RiskReadingListCallback {
        void onResult(List<RiskReading> readings);
    }

    public interface RiskCountCallback {
        void onResult(int count);
    }

    public RiskDataRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.dao = db.riskReadingDao();
    }

    // Latest risk reading — for the "current status" card
    public void getLatestRisk(RiskReadingCallback callback) {
        executor.execute(() -> {
            RiskReading latest = dao.getLatest();
            callback.onResult(latest);
        });
    }

    // Recent history — for the timeline list
    public void getRecentHistory(int limit, RiskReadingListCallback callback) {
        executor.execute(() -> {
            List<RiskReading> readings = dao.getRecent(limit);
            callback.onResult(readings);
        });
    }

    // Full history — for charts covering all recorded data
    public void getAllHistory(RiskReadingListCallback callback) {
        executor.execute(() -> {
            List<RiskReading> readings = dao.getAll();
            callback.onResult(readings);
        });
    }

    // Count of readings at a given risk level — for pie/bar charts
    public void getRiskLevelCount(int riskLevel, RiskCountCallback callback) {
        executor.execute(() -> {
            int count = dao.getCountByRiskLevel(riskLevel);
            callback.onResult(count);
        });
    }
}
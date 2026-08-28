package com.example.digitalwellbeinganalyzer;

public class BehaviorMetrics {

    public final long totalScreenTimeMs;

    public final int sessionCount;
    public final long averageSessionDurationMs;
    public final long longestSessionMs;

    public final int shortSessionCount;
    public final double fragmentationPercent;

    public final int appSwitchCount;
    public final int repeatedAppOpenings;
    public final int uniqueAppsUsed;

    public final long lateNightUsageMs;
    public final int lateNightSessionCount;

    public final long productiveUsageMs;
    public final long distractingUsageMs;
    public final double productivityPercent;

    public BehaviorMetrics(
            long totalScreenTimeMs,
            int sessionCount,
            long averageSessionDurationMs,
            long longestSessionMs,
            int shortSessionCount,
            double fragmentationPercent,
            int appSwitchCount,
            int repeatedAppOpenings,
            int uniqueAppsUsed,
            long lateNightUsageMs,
            int lateNightSessionCount,
            long productiveUsageMs,
            long distractingUsageMs,
            double productivityPercent
    ) {

        this.totalScreenTimeMs = totalScreenTimeMs;

        this.sessionCount = sessionCount;
        this.averageSessionDurationMs = averageSessionDurationMs;
        this.longestSessionMs = longestSessionMs;

        this.shortSessionCount = shortSessionCount;
        this.fragmentationPercent = fragmentationPercent;

        this.appSwitchCount = appSwitchCount;
        this.repeatedAppOpenings = repeatedAppOpenings;
        this.uniqueAppsUsed = uniqueAppsUsed;

        this.lateNightUsageMs = lateNightUsageMs;
        this.lateNightSessionCount = lateNightSessionCount;

        this.productiveUsageMs = productiveUsageMs;
        this.distractingUsageMs = distractingUsageMs;
        this.productivityPercent = productivityPercent;
    }
}
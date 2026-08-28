package com.example.digitalwellbeinganalyzer;

public class InsightsResult {

    public final String primaryTitle;
    public final String primaryDescription;

    public final double weeklyChangePercent;
    public final boolean weeklyComparisonAvailable;
    public final String weeklyDescription;

    public final long deepFocusMs;

    public final int pickupCount;
    public final boolean pickupDataAvailable;

    public final int consistencyStreakDays;
    public final String consistencyTitle;
    public final String consistencyDescription;

    public InsightsResult(
            String primaryTitle,
            String primaryDescription,
            double weeklyChangePercent,
            boolean weeklyComparisonAvailable,
            String weeklyDescription,
            long deepFocusMs,
            int pickupCount,
            boolean pickupDataAvailable,
            int consistencyStreakDays,
            String consistencyTitle,
            String consistencyDescription
    ) {

        this.primaryTitle = primaryTitle;
        this.primaryDescription = primaryDescription;

        this.weeklyChangePercent = weeklyChangePercent;
        this.weeklyComparisonAvailable = weeklyComparisonAvailable;
        this.weeklyDescription = weeklyDescription;

        this.deepFocusMs = deepFocusMs;

        this.pickupCount = pickupCount;
        this.pickupDataAvailable = pickupDataAvailable;

        this.consistencyStreakDays = consistencyStreakDays;
        this.consistencyTitle = consistencyTitle;
        this.consistencyDescription = consistencyDescription;
    }
}
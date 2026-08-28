package com.example.digitalwellbeinganalyzer;

import java.util.List;

public class ContextualUsageMetrics {

    public final List<ContextualSession> sessions;

    public final long earlyMorningUsageMs;
    public final long daytimeUsageMs;
    public final long eveningUsageMs;
    public final long lateNightUsageMs;

    public final int peakUsageHour;

    public final boolean weekend;

    public final String dominantCategory;

    public final long productiveUsageMs;
    public final long distractingUsageMs;
    public final long neutralUsageMs;

    public ContextualUsageMetrics(
            List<ContextualSession> sessions,
            long earlyMorningUsageMs,
            long daytimeUsageMs,
            long eveningUsageMs,
            long lateNightUsageMs,
            int peakUsageHour,
            boolean weekend,
            String dominantCategory,
            long productiveUsageMs,
            long distractingUsageMs,
            long neutralUsageMs
    ) {

        this.sessions = sessions;

        this.earlyMorningUsageMs = earlyMorningUsageMs;
        this.daytimeUsageMs = daytimeUsageMs;
        this.eveningUsageMs = eveningUsageMs;
        this.lateNightUsageMs = lateNightUsageMs;

        this.peakUsageHour = peakUsageHour;

        this.weekend = weekend;

        this.dominantCategory = dominantCategory;

        this.productiveUsageMs = productiveUsageMs;
        this.distractingUsageMs = distractingUsageMs;
        this.neutralUsageMs = neutralUsageMs;
    }


    public static class ContextualSession {

        public final String packageName;
        public final String category;

        public final long startTime;
        public final long endTime;
        public final long durationMs;

        public final int startHour;
        public final int dayOfWeek;

        public final boolean weekend;
        public final boolean lateNight;

        public final boolean productive;
        public final boolean distracting;

        public ContextualSession(
                String packageName,
                String category,
                long startTime,
                long endTime,
                long durationMs,
                int startHour,
                int dayOfWeek,
                boolean weekend,
                boolean lateNight,
                boolean productive,
                boolean distracting
        ) {

            this.packageName = packageName;
            this.category = category;

            this.startTime = startTime;
            this.endTime = endTime;
            this.durationMs = durationMs;

            this.startHour = startHour;
            this.dayOfWeek = dayOfWeek;

            this.weekend = weekend;
            this.lateNight = lateNight;

            this.productive = productive;
            this.distracting = distracting;
        }
    }
}
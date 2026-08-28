package com.example.digitalwellbeinganalyzer;

public class RecommendationResult {

    public static final String GUIDE_SCREEN_TIME = "SCREEN_TIME";
    public static final String GUIDE_BREAKS = "BREAKS";
    public static final String GUIDE_LATE_NIGHT = "LATE_NIGHT";
    public static final String GUIDE_PATTERNS = "PATTERNS";

    public final String title;
    public final String message;

    public final String guideKey;

    public final String reason;

    public final int priority;

    public RecommendationResult(
            String title,
            String message,
            String guideKey,
            String reason,
            int priority
    ) {

        this.title = title;
        this.message = message;

        this.guideKey = guideKey;

        this.reason = reason;

        this.priority = priority;
    }
}
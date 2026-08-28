package com.example.digitalwellbeinganalyzer;

import java.util.List;

public class BehaviorInferenceResult {

    public final int behaviorScore;

    public final String overallLevel;

    public final List<String> detectedPatterns;

    public final boolean fragmentedUsage;
    public final boolean prolongedSessions;
    public final boolean lateNightPattern;
    public final boolean distractionHeavy;
    public final boolean frequentAppSwitching;

    public BehaviorInferenceResult(
            int behaviorScore,
            String overallLevel,
            List<String> detectedPatterns,
            boolean fragmentedUsage,
            boolean prolongedSessions,
            boolean lateNightPattern,
            boolean distractionHeavy,
            boolean frequentAppSwitching
    ) {

        this.behaviorScore = behaviorScore;

        this.overallLevel = overallLevel;

        this.detectedPatterns = detectedPatterns;

        this.fragmentedUsage = fragmentedUsage;
        this.prolongedSessions = prolongedSessions;
        this.lateNightPattern = lateNightPattern;
        this.distractionHeavy = distractionHeavy;
        this.frequentAppSwitching = frequentAppSwitching;
    }
}
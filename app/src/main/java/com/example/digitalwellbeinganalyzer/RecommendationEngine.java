package com.example.digitalwellbeinganalyzer;

public class RecommendationEngine {

    public static String getRecommendation(RiskReading latest) {
        if (latest == null) {
            return "Start using your phone normally so we can build your profile.";
        }

        if (latest.isNightTime > 0 && latest.riskLevel >= 1) {
            return "Late-night usage detected. Consider activating Focus Shield and setting a wind-down reminder before 11 PM.";
        }

        if (latest.rapidScrollFlag > 0 && latest.scrollRate > 8) {
            return "Rapid scrolling detected — a sign of passive, low-intent browsing. Try a 10-minute break to reset.";
        }

        if (latest.isHighRiskApp > 0 && latest.riskLevel == 2) {
            return "Extended high-risk app usage detected. Our model suggests activating Deep Work mode to regain focus.";
        }

        if (latest.hasVideoTitleDetected > 0 && latest.riskLevel >= 1) {
            return "Passive video consumption detected. Consider switching to an active task for the next session.";
        }

        if (latest.riskLevel == 0) {
            return "You're maintaining healthy usage patterns. Keep it up!";
        }

        return "Moderate risk detected. Take a short break and reassess your current activity.";
    }
}
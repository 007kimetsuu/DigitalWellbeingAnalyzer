package com.example.digitalwellbeinganalyzer;

import java.util.List;

public class RiskEngineResult {

    public static final int LEVEL_LOW = 0;
    public static final int LEVEL_MEDIUM = 1;
    public static final int LEVEL_HIGH = 2;

    public final int finalRiskLevel;
    public final int finalRiskScore;

    public final int mlRiskLevel;
    public final double mlConfidencePercent;
    public final double mlRiskScore;

    public final int behaviorScore;
    public final String behaviorLevel;

    public final List<String> criticalFactors;

    public final boolean mlAvailable;

    public RiskEngineResult(
            int finalRiskLevel,
            int finalRiskScore,
            int mlRiskLevel,
            double mlConfidencePercent,
            double mlRiskScore,
            int behaviorScore,
            String behaviorLevel,
            List<String> criticalFactors,
            boolean mlAvailable
    ) {

        this.finalRiskLevel = finalRiskLevel;
        this.finalRiskScore = finalRiskScore;

        this.mlRiskLevel = mlRiskLevel;
        this.mlConfidencePercent = mlConfidencePercent;
        this.mlRiskScore = mlRiskScore;

        this.behaviorScore = behaviorScore;
        this.behaviorLevel = behaviorLevel;

        this.criticalFactors = criticalFactors;

        this.mlAvailable = mlAvailable;
    }
}
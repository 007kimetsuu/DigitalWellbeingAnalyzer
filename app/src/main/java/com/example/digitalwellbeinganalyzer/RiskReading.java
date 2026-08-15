package com.example.digitalwellbeinganalyzer;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "risk_readings")
public class RiskReading {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public long timestamp;
    public String packageName;
    public int riskLevel;

    public double appCategoryScore;
    public double keywordMatches;
    public double scrollRate;
    public double isHighRiskApp;
    public double rapidScrollFlag;
    public double isNightTime;
    public double hasUrlDetected;
    public double hasVideoTitleDetected;
    public long sessionDurationMs;

    public RiskReading(long timestamp, String packageName, int riskLevel,
                       double appCategoryScore, double keywordMatches, double scrollRate,
                       double isHighRiskApp, double rapidScrollFlag, double isNightTime,
                       double hasUrlDetected, double hasVideoTitleDetected, long sessionDurationMs) {
        this.timestamp = timestamp;
        this.packageName = packageName;
        this.riskLevel = riskLevel;
        this.appCategoryScore = appCategoryScore;
        this.keywordMatches = keywordMatches;
        this.scrollRate = scrollRate;
        this.isHighRiskApp = isHighRiskApp;
        this.rapidScrollFlag = rapidScrollFlag;
        this.isNightTime = isNightTime;
        this.hasUrlDetected = hasUrlDetected;
        this.hasVideoTitleDetected = hasVideoTitleDetected;
        this.sessionDurationMs = sessionDurationMs;
    }
}
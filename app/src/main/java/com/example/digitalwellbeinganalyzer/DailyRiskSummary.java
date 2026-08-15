package com.example.digitalwellbeinganalyzer;

import androidx.room.ColumnInfo;

public class DailyRiskSummary {
    @ColumnInfo(name = "day")
    public String day; // format: "2026-08-14"

    @ColumnInfo(name = "readingCount")
    public int readingCount;

    @ColumnInfo(name = "avgRiskLevel")
    public double avgRiskLevel;

    @ColumnInfo(name = "highRiskCount")
    public int highRiskCount;
}
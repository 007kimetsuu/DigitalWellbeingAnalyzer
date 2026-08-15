package com.example.digitalwellbeinganalyzer;

import androidx.room.ColumnInfo;

public class HourlyActivitySummary {
    @ColumnInfo(name = "hour")
    public int hour; // 0-23

    @ColumnInfo(name = "readingCount")
    public int readingCount;

    @ColumnInfo(name = "avgRiskLevel")
    public double avgRiskLevel;
}
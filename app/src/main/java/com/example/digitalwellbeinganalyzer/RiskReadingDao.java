package com.example.digitalwellbeinganalyzer;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface RiskReadingDao {

    @Insert
    void insert(RiskReading reading);

    @Query("SELECT * FROM risk_readings ORDER BY timestamp DESC LIMIT :limit")
    List<RiskReading> getRecent(int limit);

    @Query("SELECT * FROM risk_readings ORDER BY timestamp DESC")
    List<RiskReading> getAll();

    @Query("SELECT COUNT(*) FROM risk_readings WHERE riskLevel = :riskLevel")
    int getCountByRiskLevel(int riskLevel);

    @Query("SELECT * FROM risk_readings ORDER BY timestamp DESC LIMIT 1")
    RiskReading getLatest();
    @Query("SELECT " +
            "strftime('%Y-%m-%d', timestamp/1000, 'unixepoch') as day, " +
            "COUNT(*) as readingCount, " +
            "AVG(riskLevel) as avgRiskLevel, " +
            "SUM(CASE WHEN riskLevel = 2 THEN 1 ELSE 0 END) as highRiskCount " +
            "FROM risk_readings " +
            "WHERE timestamp >= :sinceTimestamp " +
            "GROUP BY day " +
            "ORDER BY day ASC")
    List<DailyRiskSummary> getDailySummarySince(long sinceTimestamp);
    @Query("SELECT " +
            "CAST(strftime('%H', timestamp/1000, 'unixepoch') AS INTEGER) as hour, " +
            "COUNT(*) as readingCount, " +
            "AVG(riskLevel) as avgRiskLevel " +
            "FROM risk_readings " +
            "WHERE timestamp >= :sinceTimestamp " +
            "GROUP BY hour " +
            "ORDER BY hour ASC")
    List<HourlyActivitySummary> getHourlyActivitySince(long sinceTimestamp);
}
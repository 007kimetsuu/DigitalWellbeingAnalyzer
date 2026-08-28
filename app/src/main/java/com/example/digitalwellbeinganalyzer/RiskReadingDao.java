package com.example.digitalwellbeinganalyzer;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RiskReadingDao {

    // =========================================================
    // INSERT
    // =========================================================

    @Insert
    void insert(RiskReading reading);


    // =========================================================
    // BASIC HISTORY
    // =========================================================

    @Query(
            "SELECT * FROM risk_readings " +
                    "ORDER BY timestamp DESC " +
                    "LIMIT :limit"
    )
    List<RiskReading> getRecent(int limit);


    @Query(
            "SELECT * FROM risk_readings " +
                    "ORDER BY timestamp DESC"
    )
    List<RiskReading> getAll();


    @Query(
            "SELECT * FROM risk_readings " +
                    "ORDER BY timestamp DESC " +
                    "LIMIT 1"
    )
    RiskReading getLatest();


    // =========================================================
    // RISK COUNTS
    // =========================================================

    @Query(
            "SELECT COUNT(*) FROM risk_readings " +
                    "WHERE riskLevel = :riskLevel"
    )
    int getCountByRiskLevel(int riskLevel);


    // =========================================================
    // DAILY SUMMARY
    // Uses PHONE LOCAL TIME instead of UTC.
    // =========================================================

    @Query(
            "SELECT " +
                    "strftime('%Y-%m-%d', timestamp / 1000, " +
                    "'unixepoch', 'localtime') AS day, " +

                    "COUNT(*) AS readingCount, " +

                    "AVG(riskLevel) AS avgRiskLevel, " +

                    "SUM(CASE " +
                    "WHEN riskLevel = 2 THEN 1 " +
                    "ELSE 0 END) AS highRiskCount " +

                    "FROM risk_readings " +

                    "WHERE timestamp >= :sinceTimestamp " +

                    "GROUP BY day " +

                    "ORDER BY day ASC"
    )
    List<DailyRiskSummary> getDailySummarySince(
            long sinceTimestamp
    );


    // =========================================================
    // HOURLY SUMMARY
    // Aggregates the same hour-of-day across the requested range.
    // Uses PHONE LOCAL TIME instead of UTC.
    // =========================================================

    @Query(
            "SELECT " +

                    "CAST(" +
                    "strftime('%H', timestamp / 1000, " +
                    "'unixepoch', 'localtime') " +
                    "AS INTEGER" +
                    ") AS hour, " +

                    "COUNT(*) AS readingCount, " +

                    "AVG(riskLevel) AS avgRiskLevel " +

                    "FROM risk_readings " +

                    "WHERE timestamp >= :sinceTimestamp " +

                    "GROUP BY hour " +

                    "ORDER BY hour ASC"
    )
    List<HourlyActivitySummary> getHourlyActivitySince(
            long sinceTimestamp
    );


    // =========================================================
    // STORAGE MAINTENANCE
    // Removes raw readings older than a chosen timestamp.
    // =========================================================

    @Query(
            "DELETE FROM risk_readings " +
                    "WHERE timestamp < :beforeTimestamp"
    )
    int deleteOlderThan(
            long beforeTimestamp
    );
}
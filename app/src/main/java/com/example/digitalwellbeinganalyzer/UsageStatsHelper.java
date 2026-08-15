package com.example.digitalwellbeinganalyzer;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Process;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class UsageStatsHelper {

    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.getPackageName()
        );
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static long getTodayForegroundTime(Context context, String packageName) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (24 * 60 * 60 * 1000); // last 24 hours

        List<UsageStats> statsList = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        long totalTime = 0;
        if (statsList != null) {
            for (UsageStats stats : statsList) {
                if (stats.getPackageName().equals(packageName)) {
                    totalTime += stats.getTotalTimeInForeground();
                }
            }
        }
        return totalTime; // milliseconds
    }

    public static Map<String, Long> getAllAppUsageToday(Context context) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (24 * 60 * 60 * 1000);

        List<UsageStats> statsList = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        Map<String, Long> usageMap = new TreeMap<>();
        if (statsList != null) {
            for (UsageStats stats : statsList) {
                if (stats.getTotalTimeInForeground() > 0) {
                    usageMap.merge(stats.getPackageName(), stats.getTotalTimeInForeground(), Long::sum);
                }
            }
        }
        return usageMap;
    }
}
package com.example.digitalwellbeinganalyzer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.example.digitalwellbeinganalyzer.views.DashboardActivity;

public class InterventionManager {

    private static final String CHANNEL_ID =
            "mindtrack_interventions";

    private static final String CHANNEL_NAME =
            "MindTrack Wellbeing Alerts";

    private static final String PREFS_NAME =
            "mindtrack_intervention_prefs";


    // =========================================================
    // USER SETTINGS
    // =========================================================

    private static final String KEY_NOTIFICATIONS_ENABLED =
            "notifications_enabled";

    private static final String KEY_REMINDER_FREQUENCY_MINUTES =
            "reminder_frequency_minutes";

    private static final String KEY_BREAK_DURATION_MINUTES =
            "break_duration_minutes";


    private static final boolean
            DEFAULT_NOTIFICATIONS_ENABLED = true;

    private static final int
            DEFAULT_REMINDER_FREQUENCY_MINUTES = 120;

    private static final int
            DEFAULT_BREAK_DURATION_MINUTES = 5;


    // =========================================================
    // INTERVENTION HISTORY
    // =========================================================

    private static final String KEY_BREAK =
            "last_break_notification";

    private static final String KEY_LONG_SESSION =
            "last_long_session_notification";

    private static final String KEY_LATE_NIGHT =
            "last_late_night_notification";

    private static final String KEY_RISK =
            "last_risk_notification";

    private static final String KEY_RECOMMENDATION =
            "last_recommendation_notification";


    // =========================================================
    // THRESHOLDS
    // =========================================================

    /*
     * Project heuristics — not clinical thresholds.
     */

    private static final long BREAK_THRESHOLD_MS =
            30L * 60L * 1000L;

    private static final long LONG_SESSION_THRESHOLD_MS =
            60L * 60L * 1000L;


    // =========================================================
    // FIXED COOLDOWNS
    // =========================================================

    private static final long LONG_SESSION_COOLDOWN_MS =
            60L * 60L * 1000L;

    private static final long LATE_NIGHT_COOLDOWN_MS =
            2L * 60L * 60L * 1000L;

    private static final long RISK_COOLDOWN_MS =
            60L * 60L * 1000L;

    private static final long RECOMMENDATION_COOLDOWN_MS =
            2L * 60L * 60L * 1000L;


    private InterventionManager() {
        // Utility class
    }


    // =========================================================
    // PREFERENCES
    // =========================================================

    private static SharedPreferences getPreferences(
            Context context
    ) {

        return context.getApplicationContext()
                .getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );
    }


    // =========================================================
    // NOTIFICATION SETTING
    // =========================================================

    public static boolean areNotificationsEnabled(
            Context context
    ) {

        if (context == null) {
            return false;
        }


        return getPreferences(context)
                .getBoolean(
                        KEY_NOTIFICATIONS_ENABLED,
                        DEFAULT_NOTIFICATIONS_ENABLED
                );
    }


    public static void setNotificationsEnabled(
            Context context,
            boolean enabled
    ) {

        if (context == null) {
            return;
        }


        getPreferences(context)
                .edit()
                .putBoolean(
                        KEY_NOTIFICATIONS_ENABLED,
                        enabled
                )
                .apply();
    }


    // =========================================================
    // REMINDER FREQUENCY SETTING
    // =========================================================

    public static int getReminderFrequencyMinutes(
            Context context
    ) {

        if (context == null) {

            return DEFAULT_REMINDER_FREQUENCY_MINUTES;
        }


        return getPreferences(context)
                .getInt(
                        KEY_REMINDER_FREQUENCY_MINUTES,
                        DEFAULT_REMINDER_FREQUENCY_MINUTES
                );
    }


    public static void setReminderFrequencyMinutes(
            Context context,
            int minutes
    ) {

        if (context == null) {
            return;
        }


        /*
         * Supported Profile choices will be constrained,
         * but clamp defensively as well.
         */
        int safeMinutes =
                Math.max(
                        30,
                        Math.min(
                                360,
                                minutes
                        )
                );


        getPreferences(context)
                .edit()
                .putInt(
                        KEY_REMINDER_FREQUENCY_MINUTES,
                        safeMinutes
                )
                .apply();
    }


    // =========================================================
    // BREAK DURATION SETTING
    // =========================================================

    public static int getBreakDurationMinutes(
            Context context
    ) {

        if (context == null) {

            return DEFAULT_BREAK_DURATION_MINUTES;
        }


        return getPreferences(context)
                .getInt(
                        KEY_BREAK_DURATION_MINUTES,
                        DEFAULT_BREAK_DURATION_MINUTES
                );
    }


    public static void setBreakDurationMinutes(
            Context context,
            int minutes
    ) {

        if (context == null) {
            return;
        }


        int safeMinutes =
                Math.max(
                        1,
                        Math.min(
                                30,
                                minutes
                        )
                );


        getPreferences(context)
                .edit()
                .putInt(
                        KEY_BREAK_DURATION_MINUTES,
                        safeMinutes
                )
                .apply();
    }


    // =========================================================
    // CHANNEL
    // =========================================================

    public static void createNotificationChannel(
            Context context
    ) {

        if (context == null) {
            return;
        }


        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            NotificationManager manager =
                    (NotificationManager)
                            context.getSystemService(
                                    Context.NOTIFICATION_SERVICE
                            );


            if (manager == null) {
                return;
            }


            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_DEFAULT
                    );


            channel.setDescription(
                    "MindTrack reminders and digital wellbeing interventions."
            );


            manager.createNotificationChannel(
                    channel
            );
        }
    }


    // =========================================================
    // PERMISSION + USER SETTING
    // =========================================================

    public static boolean canShowNotifications(
            Context context
    ) {

        if (context == null) {
            return false;
        }


        if (!areNotificationsEnabled(context)) {
            return false;
        }


        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU) {

            return ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }


        return true;
    }


    // =========================================================
    // MAIN EVALUATION
    // =========================================================

    public static void evaluateReading(
            Context context,
            RiskReading reading
    ) {

        if (context == null ||
                reading == null ||
                !canShowNotifications(context)) {

            return;
        }


        createNotificationChannel(
                context
        );


        int breakDurationMinutes =
                getBreakDurationMinutes(
                        context
                );


        long reminderCooldownMs =
                getReminderFrequencyMinutes(
                        context
                )
                        * 60L
                        * 1000L;


        // =====================================================
        // 1. BREAK REMINDER
        // =====================================================

        if (reading.sessionDurationMs
                >= BREAK_THRESHOLD_MS) {

            notifyIfAllowed(

                    context,

                    KEY_BREAK,
                    reminderCooldownMs,

                    1001,

                    "Time for a Short Break",

                    "You've been in a continuous phone session for around 30 minutes. Consider taking a "
                            + breakDurationMinutes
                            + "-minute break."
            );
        }


        // =====================================================
        // 2. LONG SESSION
        // =====================================================

        if (reading.sessionDurationMs
                >= LONG_SESSION_THRESHOLD_MS) {

            notifyIfAllowed(

                    context,

                    KEY_LONG_SESSION,
                    LONG_SESSION_COOLDOWN_MS,

                    1002,

                    "Long Session Detected",

                    "Your current phone session has continued for over an hour. Consider stepping away for around "
                            + breakDurationMinutes
                            + " minutes."
            );
        }


        // =====================================================
        // 3. LATE-NIGHT WARNING
        // =====================================================

        if (reading.isNightTime > 0 &&
                reading.sessionDurationMs
                        >= 15L * 60L * 1000L) {

            notifyIfAllowed(

                    context,

                    KEY_LATE_NIGHT,
                    LATE_NIGHT_COOLDOWN_MS,

                    1003,

                    "Late-Night Usage",

                    "MindTrack detected continued phone use during late-night hours. Consider beginning your wind-down routine."
            );
        }


        // =====================================================
        // 4. ELEVATED ML RISK
        // =====================================================

        if (reading.riskLevel >= 2) {

            notifyIfAllowed(

                    context,

                    KEY_RISK,
                    RISK_COOLDOWN_MS,

                    1004,

                    "Elevated Usage Risk",

                    "Your latest behavioral reading was classified as high risk. Review your current activity and consider taking a break."
            );
        }
    }


    // =========================================================
    // SMART RECOMMENDATION
    // =========================================================

    public static void showSmartRecommendation(
            Context context,
            RecommendationResult recommendation
    ) {

        if (context == null ||
                recommendation == null ||
                !canShowNotifications(context)) {

            return;
        }


        if (recommendation.priority < 2) {
            return;
        }


        createNotificationChannel(
                context
        );


        notifyIfAllowed(

                context,

                KEY_RECOMMENDATION,
                RECOMMENDATION_COOLDOWN_MS,

                1005,

                recommendation.title,

                recommendation.message
        );
    }


    // =========================================================
    // THROTTLED NOTIFICATION
    // =========================================================

    private static void notifyIfAllowed(
            Context context,
            String preferenceKey,
            long cooldownMs,
            int notificationId,
            String title,
            String message
    ) {

        SharedPreferences preferences =
                getPreferences(
                        context
                );


        long previousNotification =
                preferences.getLong(
                        preferenceKey,
                        0L
                );


        long now =
                System.currentTimeMillis();


        if ((now - previousNotification)
                < cooldownMs) {

            return;
        }


        showNotification(
                context,
                notificationId,
                title,
                message
        );


        preferences.edit()
                .putLong(
                        preferenceKey,
                        now
                )
                .apply();
    }


    // =========================================================
    // DISPLAY NOTIFICATION
    // =========================================================

    private static void showNotification(
            Context context,
            int notificationId,
            String title,
            String message
    ) {

        if (!canShowNotifications(context)) {
            return;
        }


        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );


        if (manager == null) {
            return;
        }


        Intent dashboardIntent =
                new Intent(
                        context,
                        DashboardActivity.class
                );


        dashboardIntent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        );


        PendingIntent pendingIntent =
                PendingIntent.getActivity(

                        context,

                        notificationId,

                        dashboardIntent,

                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );


        Notification.Builder builder;


        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            builder =
                    new Notification.Builder(
                            context,
                            CHANNEL_ID
                    );

        } else {

            builder =
                    new Notification.Builder(
                            context
                    );
        }


        builder
                .setSmallIcon(
                        R.mipmap.ic_launcher
                )
                .setContentTitle(
                        title
                )
                .setContentText(
                        message
                )
                .setStyle(
                        new Notification.BigTextStyle()
                                .bigText(message)
                )
                .setContentIntent(
                        pendingIntent
                )
                .setAutoCancel(
                        true
                );


        manager.notify(
                notificationId,
                builder.build()
        );
    }
}
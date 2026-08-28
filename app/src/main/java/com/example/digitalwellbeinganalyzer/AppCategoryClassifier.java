package com.example.digitalwellbeinganalyzer;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppCategoryClassifier {

    // =========================================================
    // CATEGORIES
    // =========================================================

    public static final String CATEGORY_EDUCATION = "Education";
    public static final String CATEGORY_PRODUCTIVITY = "Productivity";
    public static final String CATEGORY_SOCIAL = "Social";
    public static final String CATEGORY_COMMUNICATION = "Communication";
    public static final String CATEGORY_ENTERTAINMENT = "Entertainment";
    public static final String CATEGORY_GAMING = "Gaming";
    public static final String CATEGORY_UTILITIES = "Utilities";
    public static final String CATEGORY_OTHER = "Other";


    // =========================================================
    // CURATED PACKAGE OVERRIDES
    // =========================================================

    /*
     * These lists exist because Android's ApplicationInfo.category
     * is not always populated consistently by every app.
     */

    private static final Set<String> EDUCATION_APPS =
            new HashSet<>(Arrays.asList(

                    "com.google.android.apps.classroom",
                    "com.google.android.apps.docs.editors.docs",
                    "com.google.android.apps.docs.editors.sheets",
                    "com.google.android.apps.docs.editors.slides",

                    "org.khanacademy.android",
                    "com.duolingo",
                    "com.udemy.android",
                    "com.coursera.android",
                    "com.linkedin.android.learning",
                    "com.byjus.thelearningapp",
                    "com.unacademyapp"
            ));


    private static final Set<String> PRODUCTIVITY_APPS =
            new HashSet<>(Arrays.asList(

                    "com.google.android.apps.docs",
                    "com.microsoft.office.word",
                    "com.microsoft.office.excel",
                    "com.microsoft.office.powerpoint",

                    "com.microsoft.office.officehubrow",
                    "com.microsoft.teams",

                    "com.google.android.keep",
                    "com.evernote",
                    "com.todoist",

                    "com.google.android.calendar",
                    "com.google.android.apps.tasks",

                    "notion.id"
            ));


    private static final Set<String> SOCIAL_APPS =
            new HashSet<>(Arrays.asList(

                    "com.instagram.android",
                    "com.facebook.katana",
                    "com.facebook.lite",

                    "com.twitter.android",
                    "com.x.android",

                    "com.zhiliaoapp.musically",
                    "com.snapchat.android",

                    "com.reddit.frontpage",
                    "com.linkedin.android"
            ));


    private static final Set<String> COMMUNICATION_APPS =
            new HashSet<>(Arrays.asList(

                    "com.whatsapp",
                    "com.whatsapp.w4b",

                    "org.telegram.messenger",
                    "org.thunderdog.challegram",

                    "com.discord",
                    "com.google.android.apps.messaging",

                    "com.google.android.gm",

                    "com.microsoft.office.outlook",

                    "us.zoom.videomeetings",
                    "com.skype.raider"
            ));


    private static final Set<String> ENTERTAINMENT_APPS =
            new HashSet<>(Arrays.asList(

                    "com.google.android.youtube",
                    "com.google.android.apps.youtube.music",

                    "com.netflix.mediaclient",
                    "com.spotify.music",

                    "in.startv.hotstar",
                    "com.amazon.avod.thirdpartyclient",

                    "tv.twitch.android.app",
                    "com.google.android.videos"
            ));


    private static final Set<String> GAMING_APPS =
            new HashSet<>(Arrays.asList(

                    "com.roblox.client",

                    "com.supercell.clashofclans",
                    "com.supercell.clashroyale",

                    "com.activision.callofduty.shooter",
                    "com.tencent.ig",

                    "com.dts.freefireth",
                    "com.dts.freefiremax",

                    "com.mojang.minecraftpe",

                    "com.ea.gp.fifamobile"
            ));

    private static final Set<String> OTHER_APPS =
            new HashSet<>(Arrays.asList(

                    "com.android.chrome",
                    "org.mozilla.firefox",
                    "com.microsoft.emmx",
                    "com.opera.browser",
                    "com.brave.browser",
                    "com.sec.android.app.sbrowser"
            ));
    private static final Set<String> UTILITY_APPS =
            new HashSet<>(Arrays.asList(

                    "com.android.settings",
                    "com.android.calculator2",
                    "com.google.android.calculator",


                    "com.google.android.apps.maps",

                    "com.google.android.apps.photos",
                    "com.google.android.files",

                    "com.google.android.deskclock"
            ));


    // =========================================================
    // PRIMARY CLASSIFICATION METHOD
    // =========================================================

    /**
     * Recommended classifier.
     *
     * Uses:
     *
     * 1. Curated package overrides
     * 2. Android ApplicationInfo category where available
     * 3. Safe fallback to Other
     */
    public static String getCategory(
            Context context,
            String packageName
    ) {

        if (packageName == null ||
                packageName.trim().isEmpty()) {

            return CATEGORY_OTHER;
        }


        // -----------------------------------------------------
        // 1. CURATED OVERRIDES
        // -----------------------------------------------------

        String knownCategory =
                getKnownPackageCategory(
                        packageName
                );

        if (knownCategory != null) {
            return knownCategory;
        }


        // -----------------------------------------------------
        // 2. ANDROID APP CATEGORY
        // -----------------------------------------------------

        if (context != null &&
                Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.O) {

            try {

                PackageManager packageManager =
                        context.getPackageManager();

                ApplicationInfo info =
                        packageManager.getApplicationInfo(
                                packageName,
                                0
                        );

                switch (info.category) {

                    case ApplicationInfo.CATEGORY_GAME:

                        return CATEGORY_GAMING;


                    case ApplicationInfo.CATEGORY_SOCIAL:

                        return CATEGORY_SOCIAL;


                    case ApplicationInfo.CATEGORY_VIDEO:

                    case ApplicationInfo.CATEGORY_AUDIO:

                        return CATEGORY_ENTERTAINMENT;


                    case ApplicationInfo.CATEGORY_PRODUCTIVITY:

                        return CATEGORY_PRODUCTIVITY;


                    case ApplicationInfo.CATEGORY_MAPS:

                    case ApplicationInfo.CATEGORY_IMAGE:

                        return CATEGORY_UTILITIES;


                    case ApplicationInfo.CATEGORY_NEWS:

                        return CATEGORY_OTHER;


                    default:

                        break;
                }

            } catch (PackageManager.NameNotFoundException ignored) {

                // Package metadata unavailable.
                // Fall through to Other.
            }
        }


        return CATEGORY_OTHER;
    }


    // =========================================================
    // BACKWARD-COMPATIBLE CLASSIFIER
    // =========================================================

    /**
     * Kept because older backend code may still call
     * getCategory(packageName).
     *
     * This method can only use curated mappings because
     * it has no Context / PackageManager access.
     */
    public static String getCategory(
            String packageName
    ) {

        if (packageName == null ||
                packageName.trim().isEmpty()) {

            return CATEGORY_OTHER;
        }

        String category =
                getKnownPackageCategory(
                        packageName
                );

        return category != null
                ? category
                : CATEGORY_OTHER;
    }


    // =========================================================
    // KNOWN PACKAGE LOOKUP
    // =========================================================

    private static String getKnownPackageCategory(
            String packageName
    ) {

        if (EDUCATION_APPS.contains(packageName)) {
            return CATEGORY_EDUCATION;
        }

        if (PRODUCTIVITY_APPS.contains(packageName)) {
            return CATEGORY_PRODUCTIVITY;
        }

        if (SOCIAL_APPS.contains(packageName)) {
            return CATEGORY_SOCIAL;
        }

        if (COMMUNICATION_APPS.contains(packageName)) {
            return CATEGORY_COMMUNICATION;
        }

        if (ENTERTAINMENT_APPS.contains(packageName)) {
            return CATEGORY_ENTERTAINMENT;
        }

        if (GAMING_APPS.contains(packageName)) {
            return CATEGORY_GAMING;
        }
        if (OTHER_APPS.contains(packageName)) {
            return CATEGORY_OTHER;
        }
        if (UTILITY_APPS.contains(packageName)) {
            return CATEGORY_UTILITIES;
        }

        return null;
    }


    // =========================================================
    // CREATE EMPTY CATEGORY MAP
    // =========================================================

    private static Map<String, Long>
    createEmptyCategoryMap() {

        Map<String, Long> totals =
                new HashMap<>();

        totals.put(
                CATEGORY_EDUCATION,
                0L
        );

        totals.put(
                CATEGORY_PRODUCTIVITY,
                0L
        );

        totals.put(
                CATEGORY_SOCIAL,
                0L
        );

        totals.put(
                CATEGORY_COMMUNICATION,
                0L
        );

        totals.put(
                CATEGORY_ENTERTAINMENT,
                0L
        );

        totals.put(
                CATEGORY_GAMING,
                0L
        );

        totals.put(
                CATEGORY_UTILITIES,
                0L
        );

        totals.put(
                CATEGORY_OTHER,
                0L
        );

        return totals;
    }


    // =========================================================
    // OLD MAP-BASED AGGREGATION
    // =========================================================

    /**
     * Preserved for compatibility with Surya's repository.
     *
     * Because this method has no Context, only curated package
     * mappings can be used.
     */
    public static Map<String, Long> aggregateByCategory(
            Map<String, Long> appUsageMap
    ) {

        Map<String, Long> totals =
                createEmptyCategoryMap();

        if (appUsageMap == null) {
            return totals;
        }

        for (Map.Entry<String, Long> entry :
                appUsageMap.entrySet()) {

            String category =
                    getCategory(
                            entry.getKey()
                    );

            totals.merge(
                    category,
                    entry.getValue(),
                    Long::sum
            );
        }

        return totals;
    }


    // =========================================================
    // CONTEXT-AWARE MAP AGGREGATION
    // =========================================================

    public static Map<String, Long> aggregateByCategory(
            Context context,
            Map<String, Long> appUsageMap
    ) {

        Map<String, Long> totals =
                createEmptyCategoryMap();

        if (appUsageMap == null) {
            return totals;
        }

        for (Map.Entry<String, Long> entry :
                appUsageMap.entrySet()) {

            String category =
                    getCategory(
                            context,
                            entry.getKey()
                    );

            totals.merge(
                    category,
                    entry.getValue(),
                    Long::sum
            );
        }

        return totals;
    }


    // =========================================================
    // SESSION-BASED AGGREGATION
    // =========================================================

    public static Map<String, Long> aggregateByCategory(
            Context context,
            List<UsageStatsHelper.AppUsageItem> appUsageItems
    ) {

        Map<String, Long> totals =
                createEmptyCategoryMap();

        if (appUsageItems == null) {
            return totals;
        }


        for (UsageStatsHelper.AppUsageItem item :
                appUsageItems) {

            if (item == null) {
                continue;
            }

            String category =
                    getCategory(
                            context,
                            item.getPackageName()
                    );

            totals.merge(
                    category,
                    item.getTotalUsageMs(),
                    Long::sum
            );
        }

        return totals;
    }


    // =========================================================
    // TODAY CATEGORY USAGE
    // =========================================================

    /**
     * Uses our new session-based data layer.
     */
    public static Map<String, Long> getCategoryUsageToday(
            Context context
    ) {

        List<UsageStatsHelper.AppUsageItem> apps =
                UsageStatsHelper
                        .getMostUsedAppsToday(
                                context
                        );

        return aggregateByCategory(
                context,
                apps
        );
    }
}
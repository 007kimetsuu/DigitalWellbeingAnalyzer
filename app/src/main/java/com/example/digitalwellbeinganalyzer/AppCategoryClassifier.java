package com.example.digitalwellbeinganalyzer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AppCategoryClassifier {

    public static final String CATEGORY_STUDY = "Study";
    public static final String CATEGORY_SOCIAL = "Social";
    public static final String CATEGORY_ENTERTAINMENT = "Entertainment";
    public static final String CATEGORY_MISC = "Misc";

    private static final Set<String> STUDY_APPS = new HashSet<>(Arrays.asList(
            "com.google.android.apps.docs", "com.microsoft.office.word",
            "com.microsoft.office.excel", "com.microsoft.office.powerpoint",
            "com.google.android.apps.classroom", "com.microsoft.teams",
            "us.zoom.videomeetings", "com.evernote"
    ));

    private static final Set<String> SOCIAL_APPS = new HashSet<>(Arrays.asList(
            "com.instagram.android", "com.twitter.android", "com.facebook.katana",
            "com.zhiliaoapp.musically", "com.snapchat.android", "com.whatsapp",
            "org.telegram.messenger", "com.linkedin.android"
    ));

    private static final Set<String> ENTERTAINMENT_APPS = new HashSet<>(Arrays.asList(
            "com.google.android.youtube", "com.netflix.mediaclient", "com.spotify.music",
            "com.reddit.frontpage", "com.google.android.videos", "in.startv.hotstar",
            "com.amazon.avod.thirdpartyclient"
    ));

    public static String getCategory(String packageName) {
        if (packageName == null) return CATEGORY_MISC;
        if (STUDY_APPS.contains(packageName)) return CATEGORY_STUDY;
        if (SOCIAL_APPS.contains(packageName)) return CATEGORY_SOCIAL;
        if (ENTERTAINMENT_APPS.contains(packageName)) return CATEGORY_ENTERTAINMENT;
        return CATEGORY_MISC;
    }

    // Aggregates a map of packageName -> time(ms) into category -> total time(ms)
    public static Map<String, Long> aggregateByCategory(Map<String, Long> appUsageMap) {
        Map<String, Long> categoryTotals = new HashMap<>();
        categoryTotals.put(CATEGORY_STUDY, 0L);
        categoryTotals.put(CATEGORY_SOCIAL, 0L);
        categoryTotals.put(CATEGORY_ENTERTAINMENT, 0L);
        categoryTotals.put(CATEGORY_MISC, 0L);

        for (Map.Entry<String, Long> entry : appUsageMap.entrySet()) {
            String category = getCategory(entry.getKey());
            categoryTotals.merge(category, entry.getValue(), Long::sum);
        }
        return categoryTotals;
    }
}
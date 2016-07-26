package com.davidgassner.android.balloonpopper.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {

    private static final String PREFS_GLOBAL = "prefs_global";
    private static final String PREF_CURRENT_SCORE = "pref_current_score";
    private static final String PREF_CURRENT_LEVEL = "pref_current_level";
    private static final String PREF_TOP_SCORE = "pref_top_score";
    private static final String PREF_MOST_LEVELS = "pref_most_levels";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(
                PREFS_GLOBAL, Context.MODE_PRIVATE);
    }

//  Setters and getters for global preferences
    public static void setCurrentScore(Context context, int score) {
        SharedPreferences.Editor editor =
                getPreferences(context).edit();
        editor.putInt(PREF_CURRENT_SCORE, score);
        editor.apply();
    }

    public static int getCurrentScore(Context context) {
        return getPreferences(context).getInt(PREF_CURRENT_SCORE, 0);
    }

    public static void setCurrentLevel(Context context, int level) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(PREF_CURRENT_LEVEL, level);
        editor.apply();
    }

    public static int getCurrentLevel(Context context) {
        return getPreferences(context).getInt(PREF_CURRENT_LEVEL, 0);
    }

    public static boolean isTopScore(Context context, int newScore) {
        int topScore = getPreferences(context).getInt(PREF_TOP_SCORE, 0);
        return newScore > topScore;
    }

    public static int getTopScore(Context context) {
        return getPreferences(context).getInt(PREF_TOP_SCORE, 0);
    }

    public static void setTopScore(Context context, int score) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(PREF_TOP_SCORE, score);
        editor.apply();
    }

    public static boolean isMostLevels(Context context, int levels) {
        int mostLevels = getPreferences(context).getInt(PREF_MOST_LEVELS, 0);
        return levels > mostLevels;
    }

    public static int getMostLevels(Context context) {
        return getPreferences(context).getInt(PREF_MOST_LEVELS, 0);
    }

    public static void setPrefMostLevels(Context context, int levels) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(PREF_MOST_LEVELS, levels);
        editor.apply();
    }

}
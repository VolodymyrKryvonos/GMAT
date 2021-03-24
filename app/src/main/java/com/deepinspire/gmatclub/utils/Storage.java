package com.deepinspire.gmatclub.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * /**
 * Created by Andriy Lykhtey on 2019-11-27.
 */
public class Storage {

    private static String badge = "badge";
    private static String badgeCount = "badgeCount";
    private static String TOKEN = "TOKEN";
    private static String LOGIN = "LOGIN";
    private static String EMAIL = "EMAIL";
    private static String PASSWORD = "PASSWORD";
    private static String GOOGLE_ID_TOKEN = "GOOGLE_ID_TOKEN";
    private static String FACEBOOK_ID_TOKEN = "FACEBOOK_ID_TOKEN";
    private static String GOOGLE_ACCESS_TOKEN = "GOOGLE_ACCESS_TOKEN";
    private static String FACEBOOK_ACCESS_TOKEN = "FACEBOOK_ACCESS_TOKEN";

    public static void saveBadgeCount(Context context, int data){
        SharedPreferences.Editor editor = context.getSharedPreferences(badge, MODE_PRIVATE).edit();
        editor.putInt(badgeCount, data);
        editor.apply();
    }

    public static int getBadgeCount(Context context){
        SharedPreferences prefs = context.getSharedPreferences(badge, MODE_PRIVATE);
        return prefs.getInt(badgeCount, 0);
    }

    public static void saveGoogleIdToken(Context context, String data){
        SharedPreferences.Editor editor = context.getSharedPreferences(TOKEN, MODE_PRIVATE).edit();
        editor.putString(GOOGLE_ID_TOKEN, data);
        editor.apply();
    }

    public static void saveFacebookIdToken(Context context, String data){
        SharedPreferences.Editor editor = context.getSharedPreferences(TOKEN, MODE_PRIVATE).edit();
        editor.putString(FACEBOOK_ID_TOKEN, data);
        editor.apply();
    }

    public static void saveGoogleAccessToken(Context context, String data){
        SharedPreferences.Editor editor = context.getSharedPreferences(TOKEN, MODE_PRIVATE).edit();
        editor.putString(GOOGLE_ACCESS_TOKEN, data);
        editor.apply();
    }

    public static void saveFacebookAccessToken(Context context, String data){
        SharedPreferences.Editor editor = context.getSharedPreferences(TOKEN, MODE_PRIVATE).edit();
        editor.putString(FACEBOOK_ACCESS_TOKEN, data);
        editor.apply();
    }

    public static String getGoogleIdToken(Context context){
        SharedPreferences prefs = context.getSharedPreferences(TOKEN, MODE_PRIVATE);
        return prefs.getString(GOOGLE_ID_TOKEN, "");
    }

    public static String getFacebookIdToken(Context context){
        SharedPreferences prefs = context.getSharedPreferences(TOKEN, MODE_PRIVATE);
        return prefs.getString(FACEBOOK_ID_TOKEN, "");
    }

    public static String getGoogleAccessToken(Context context){
        SharedPreferences prefs = context.getSharedPreferences(TOKEN, MODE_PRIVATE);
        return prefs.getString(GOOGLE_ACCESS_TOKEN, "");
    }

    public static String getFacebookAccessToken(Context context){
        SharedPreferences prefs = context.getSharedPreferences(TOKEN, MODE_PRIVATE);
        return prefs.getString(FACEBOOK_ACCESS_TOKEN, "");
    }

    public static void saveLoginEmail(Context context, String data){
        SharedPreferences.Editor editor = context.getSharedPreferences(LOGIN, MODE_PRIVATE).edit();
        editor.putString(EMAIL, data);
        editor.apply();
    }

    public static void saveLoginPassword(Context context, String data){
        SharedPreferences.Editor editor = context.getSharedPreferences(LOGIN, MODE_PRIVATE).edit();
        editor.putString(PASSWORD, data);
        editor.apply();
    }

    public static String getLoginEmail(Context context){
        SharedPreferences prefs = context.getSharedPreferences(LOGIN, MODE_PRIVATE);
        return prefs.getString(EMAIL, "");
    }

    public static String getLoginPassword(Context context){
        SharedPreferences prefs = context.getSharedPreferences(LOGIN, MODE_PRIVATE);
        return prefs.getString(PASSWORD, "");
    }

}

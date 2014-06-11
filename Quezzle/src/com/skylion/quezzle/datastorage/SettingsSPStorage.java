package com.skylion.quezzle.datastorage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 11.06.14
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */
public class SettingsSPStorage {
    private static final String STORAGE_NAME = "com.skylion.quezzle.SettingsSPStorage.SETTINGS";
    private static final String IS_RELOAD_HINT_SHOWN = "com.skylion.quezzle.SettingsSPStorage.IS_RELOAD_HINT_SHOWN";

    //Don't allow to create this class
    private SettingsSPStorage() {}

    public static boolean isReloadHintShown(Context context) {
        SharedPreferences pref = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(IS_RELOAD_HINT_SHOWN, false);
    }

    public static void setReloadHintShown(Context context, boolean value) {
        SharedPreferences pref = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        try {
            editor.putBoolean(IS_RELOAD_HINT_SHOWN, value);
        } finally {
            editor.commit();
        }
    }
}

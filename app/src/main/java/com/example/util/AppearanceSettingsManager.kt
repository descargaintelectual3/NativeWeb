package com.example.util

import android.content.Context

object AppearanceSettingsManager {
    private const val PREFS_NAME = "webnative_appearance_prefs"
    private const val KEY_DOCK_POSITION = "key_dock_position"
    private const val KEY_DEFAULT_HOME_URL = "key_default_home_url"
    private const val KEY_AUTO_INJECT_COPY = "key_auto_inject_copy"

    const val DOCK_POS_BOTTOM_CENTER = "BOTTOM_CENTER"
    const val DOCK_POS_BOTTOM_RIGHT = "BOTTOM_RIGHT"
    const val DOCK_POS_TOP_RIGHT = "TOP_RIGHT"

    fun getDockPosition(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DOCK_POSITION, DOCK_POS_BOTTOM_CENTER) ?: DOCK_POS_BOTTOM_CENTER
    }

    fun setDockPosition(context: Context, position: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DOCK_POSITION, position)
            .apply()
    }

    fun getDefaultHomeUrl(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DEFAULT_HOME_URL, "https://google.com") ?: "https://google.com"
    }

    fun setDefaultHomeUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DEFAULT_HOME_URL, url)
            .apply()
    }

    fun isAutoInjectCopyEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_AUTO_INJECT_COPY, true)
    }

    fun setAutoInjectCopyEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AUTO_INJECT_COPY, enabled)
            .apply()
    }
}

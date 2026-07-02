package com.example.services

import android.content.Context

object UrgencySettings {
    private const val PREFS_NAME = "wake_alert_urgency_prefs"
    private const val KEY_ENABLED = "urgency_enabled"
    private const val KEY_KEYWORDS = "urgency_keywords"
    private const val KEY_INTENSITY = "urgency_intensity"
    private const val KEY_WHO = "urgency_who"

    private val DEFAULT_KEYWORDS = setOf("socorro", "emergência", "perigo", "preciso de ajuda", "help", "ajuda")

    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, true)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun getKeywords(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Return a mutable copy so we can easily add/delete from it in UI
        val set = prefs.getStringSet(KEY_KEYWORDS, DEFAULT_KEYWORDS) ?: DEFAULT_KEYWORDS
        return set.toSet()
    }

    fun setKeywords(context: Context, keywords: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_KEYWORDS, keywords).apply()
    }

    fun getIntensity(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_INTENSITY, "HIGH") ?: "HIGH"
    }

    fun setIntensity(context: Context, intensity: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_INTENSITY, intensity).apply()
    }

    fun getWhoCanTrigger(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_WHO, "ANYONE") ?: "ANYONE"
    }

    fun setWhoCanTrigger(context: Context, who: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_WHO, who).apply()
    }
}

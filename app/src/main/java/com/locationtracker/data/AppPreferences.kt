package com.locationtracker.data

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val PREF_NAME = "LocationTrackerPrefs"
    private const val KEY_BASE_URL = "baseUrl"
    private const val DEFAULT_BASE_URL = "http://13.201.150.19:3000/api/v1/"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getBaseUrl(context: Context): String {
        return getPreferences(context).getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }

    fun setBaseUrl(context: Context, baseUrl: String) {
        getPreferences(context).edit().putString(KEY_BASE_URL, baseUrl).apply()
    }
}

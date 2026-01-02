package com.locationtracker.utils

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

object Logger {
    private const val PREF_NAME = "app_logs"
    private const val LOG_KEY = "logs"
    private const val MAX_LOG_SIZE = 5000
    
    private var prefs: SharedPreferences? = null
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    fun log(message: String) {
        val timestamp = dateFormat.format(Date())
        val logEntry = "[$timestamp] $message"
        
        val currentLogs = prefs?.getString(LOG_KEY, "") ?: ""
        val newLogs = if (currentLogs.length > MAX_LOG_SIZE) {
            logEntry + "\n" + currentLogs.substring(0, MAX_LOG_SIZE / 2)
        } else {
            logEntry + "\n" + currentLogs
        }
        
        prefs?.edit()?.putString(LOG_KEY, newLogs)?.apply()
    }
    
    fun getLogs(): String {
        return prefs?.getString(LOG_KEY, "App started. Ready to track location.") ?: "App started. Ready to track location."
    }
    
    fun clearLogs() {
        prefs?.edit()?.remove(LOG_KEY)?.apply()
    }
}

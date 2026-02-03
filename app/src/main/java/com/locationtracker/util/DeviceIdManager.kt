package com.locationtracker.util

import android.content.Context
import android.provider.Settings

object DeviceIdManager {
    
    private var cachedDeviceId: String? = null
    
    /**
     * Gets the Android device ID (ANDROID_ID).
     * This ID is unique to each device but can change after factory reset.
     * The ID is cached after first retrieval for better performance.
     * 
     * @param context Application context
     * @return Device ID string, or "unknown" if unable to retrieve
     */
    fun getDeviceId(context: Context): String {
        if (cachedDeviceId == null) {
            try {
                cachedDeviceId = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                ) ?: "unknown"
            } catch (e: Exception) {
                cachedDeviceId = "unknown"
            }
        }
        return cachedDeviceId ?: "unknown"
    }
    
    /**
     * Clears the cached device ID (useful for testing or if device ID changes)
     */
    fun clearCache() {
        cachedDeviceId = null
    }
}

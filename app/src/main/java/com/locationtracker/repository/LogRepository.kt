package com.locationtracker.repository

import android.content.Context
import com.locationtracker.data.AppDatabase
import com.locationtracker.data.LogEntry
import java.util.concurrent.TimeUnit

class LogRepository(context: Context) {
    private val logDao = AppDatabase.getDatabase(context).logDao()

    suspend fun insertLog(status: String, message: String) {
        val logEntry = LogEntry(
            timestamp = System.currentTimeMillis(),
            status = status,
            message = message
        )
        logDao.insert(logEntry)
    }

    suspend fun getAllLogs(): List<LogEntry> {
        return logDao.getAllLogs()
    }

    suspend fun cleanOldLogs() {
        val cutoffTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)
        logDao.deleteOldLogs(cutoffTime)
    }
}

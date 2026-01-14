package com.locationtracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LogDao {
    @Insert
    suspend fun insert(logEntry: LogEntry)

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<LogEntry>

    @Query("DELETE FROM log_entries WHERE timestamp < :cutoffTime")
    suspend fun deleteOldLogs(cutoffTime: Long)
}

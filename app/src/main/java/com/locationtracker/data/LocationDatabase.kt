package com.locationtracker.data

import androidx.room.*

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lat: Double,
    val lng: Double,
    val accuracy: Float,
    val timestamp: Long,
    val synced: Boolean = false
)

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(location: LocationEntity): Long

    @Query("SELECT * FROM locations WHERE synced = 0")
    suspend fun getUnsyncedLocations(): List<LocationEntity>

    @Query("UPDATE locations SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("DELETE FROM locations WHERE synced = 1 AND timestamp < :cutoffTime")
    suspend fun deleteOldSyncedLocations(cutoffTime: Long)
}

@Database(entities = [LocationEntity::class], version = 1)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}

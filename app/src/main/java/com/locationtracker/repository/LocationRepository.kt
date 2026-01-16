package com.locationtracker.repository

import android.content.Context
import androidx.room.Room
import com.locationtracker.api.NetworkClient
import com.locationtracker.data.LocationData
import com.locationtracker.data.LocationDatabase
import com.locationtracker.data.LocationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class LocationRepository(
    private val context: Context,
    private val logRepository: LogRepository
) {

    private val database = Room.databaseBuilder(
        context.applicationContext,
        LocationDatabase::class.java,
        "location_database"
    ).fallbackToDestructiveMigration().build()

    private val locationDao = database.locationDao()

    private val apiService by lazy {
        NetworkClient.create(context)
    }

    suspend fun saveLocation(locationData: LocationData) {
        withContext(Dispatchers.IO) {
            val entity = LocationEntity(
                latitude = locationData.lat,
                longitude = locationData.lng,
                accuracy = locationData.accuracy,
                timestamp = locationData.timestamp
            )
            locationDao.insert(entity)
        }
    }

    suspend fun getUnsyncedCount(): Int {
        return withContext(Dispatchers.IO) {
            locationDao.getUnsyncedCount()
        }
    }

    suspend fun syncLocations() {
        withContext(Dispatchers.IO) {
            val unsyncedLocations = locationDao.getUnsyncedLocations()
            val BATCH_THRESHOLD = 5
            val syncedIds = mutableListOf<Long>()

            if (unsyncedLocations.size >= BATCH_THRESHOLD) {
                val chunks = unsyncedLocations.chunked(BATCH_THRESHOLD)
                for (chunk in chunks) {
                    try {
                        val locationDataList = chunk.map { location ->
                            LocationData(
                                lat = location.latitude,
                                lng = location.longitude,
                                trip_details = "---",
                                accuracy = location.accuracy,
                                timestamp = location.timestamp,
                                id = location.id
                            )
                        }

                        val response = apiService.sendLocations(locationDataList)
                        if (response.isSuccessful) {
                            syncedIds.addAll(chunk.map { it.id })
                        } else {
                            logRepository.insertLog(
                                "ERROR",
                                "API error (code: ${response.code()}): ${response.message()}"
                            )
                        }
                    } catch (e: IOException) {
                        logRepository.insertLog("ERROR", "Network error during sync: ${e.message}")
                    } catch (e: Exception) {
                        logRepository.insertLog("ERROR", "Unexpected error during sync: ${e.message}")
                    }
                }
            }

            if (syncedIds.isNotEmpty()) {
                locationDao.markAsSynced(syncedIds)
            }

            // Clean up old synced locations (older than 24 hours)
            val cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            locationDao.deleteOldSyncedLocations(cutoffTime)
        }
    }
}

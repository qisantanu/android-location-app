package com.locationtracker.repository

import android.content.Context
import androidx.room.Room
import com.locationtracker.api.LocationApiService
import com.locationtracker.data.LocationData
import com.locationtracker.data.LocationDatabase
import com.locationtracker.data.LocationEntity
import com.locationtracker.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LocationRepository(context: Context) {
    
    init {
        Logger.init(context)
    }
    
    private val database = Room.databaseBuilder(
        context.applicationContext,
        LocationDatabase::class.java,
        "location_database"
    ).build()
    
    private val locationDao = database.locationDao()
    
    private val apiService = Retrofit.Builder()
        .baseUrl("http://192.168.29.181:3000/api/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(LocationApiService::class.java)

    suspend fun saveLocation(locationData: LocationData) {
        withContext(Dispatchers.IO) {
            try {
                val entity = LocationEntity(
                    lat = locationData.lat,
                    lng = locationData.lng,
                    accuracy = locationData.accuracy,
                    timestamp = locationData.timestamp
                )
                locationDao.insert(entity)
                Logger.log("Location saved to database")
            } catch (e: Exception) {
                Logger.log("Database save error: ${e.message}")
            }
        }
    }

    suspend fun syncLocations() {
        withContext(Dispatchers.IO) {
            try {
                val unsyncedLocations = locationDao.getUnsyncedLocations()
                Logger.log("Found ${unsyncedLocations.size} unsynced locations")
                
                if (unsyncedLocations.isEmpty()) return@withContext
                
                val syncedIds = mutableListOf<Long>()
                
                for (location in unsyncedLocations) {
                    try {
                        val locationData = LocationData(
                            lat = location.lat,
                            lng = location.lng,
                            trip_details = "---",
                            accuracy = location.accuracy,
                            timestamp = location.timestamp
                        )
                        
                        val response = apiService.sendLocation(locationData)
                        if (response.isSuccessful) {
                            syncedIds.add(location.id)
                            Logger.log("API call successful for location ${location.id}")
                        } else {
                            Logger.log("API call failed: ${response.code()} - ${response.message()}")
                        }
                    } catch (e: Exception) {
                        Logger.log("API call error: ${e.message}")
                    }
                }
                
                if (syncedIds.isNotEmpty()) {
                    locationDao.markAsSynced(syncedIds)
                    Logger.log("Marked ${syncedIds.size} locations as synced")
                }
                
                // Clean up old synced locations (older than 24 hours)
                val cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                locationDao.deleteOldSyncedLocations(cutoffTime)
                
            } catch (e: Exception) {
                Logger.log("Sync error: ${e.message}")
            }
        }
    }
}

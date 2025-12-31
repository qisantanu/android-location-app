package com.locationtracker.repository

import android.content.Context
import androidx.room.Room
import com.locationtracker.api.LocationApiService
import com.locationtracker.data.LocationData
import com.locationtracker.data.LocationDatabase
import com.locationtracker.data.LocationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LocationRepository(context: Context) {
    
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
            val entity = LocationEntity(
                latitude = locationData.latitude,
                longitude = locationData.longitude,
                accuracy = locationData.accuracy,
                timestamp = locationData.timestamp
            )
            locationDao.insert(entity)
        }
    }

    suspend fun syncLocations() {
        withContext(Dispatchers.IO) {
            try {
                val unsyncedLocations = locationDao.getUnsyncedLocations()
                val syncedIds = mutableListOf<Long>()
                
                for (location in unsyncedLocations) {
                    try {
                        val locationData = LocationData(
                            lat = location.latitude,
                            lng = location.longitude,
                            trip_details = "---",
                            accuracy = location.accuracy,
                            timestamp = location.timestamp
                        )
                        
                        val response = apiService.sendLocation(locationData)
                        if (response.isSuccessful) {
                            syncedIds.add(location.id)
                        }
                    } catch (e: Exception) {
                        // Continue with next location on individual failure
                    }
                }
                
                if (syncedIds.isNotEmpty()) {
                    locationDao.markAsSynced(syncedIds)
                }
                
                // Clean up old synced locations (older than 24 hours)
                val cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                locationDao.deleteOldSyncedLocations(cutoffTime)
                
            } catch (e: Exception) {
                // Log error but don't crash
            }
        }
    }
}

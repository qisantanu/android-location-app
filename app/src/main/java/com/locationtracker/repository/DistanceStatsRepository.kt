package com.locationtracker.repository

import android.content.Context
import com.locationtracker.api.DistanceStatsApiService
import com.locationtracker.data.DistanceStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DistanceStatsRepository(context: Context) {
    
    private val apiService = Retrofit.Builder()
        .baseUrl("http://192.168.29.181:3000/api/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DistanceStatsApiService::class.java)

    suspend fun getDistanceStats(): Result<DistanceStats> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDistanceStats()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to fetch distance stats: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

package com.locationtracker.repository

import android.content.Context
import com.locationtracker.api.InfoApiService
import com.locationtracker.api.NetworkClient
import com.locationtracker.data.InfoItem
import com.locationtracker.repository.LogRepository

class InfoRepository(private val context: Context, private val logRepository: LogRepository) {

    private val infoApiService by lazy {
        NetworkClient.create(context, InfoApiService::class.java)
    }

    suspend fun fetchInfo(): List<InfoItem> {
        return try {
            val response = infoApiService.getInfo()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { (label, value) ->
                    InfoItem(label, value)
                }
            } else {
                logRepository.insertLog("ERROR", "Info API Error: ${response.code()} - ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            logRepository.insertLog("ERROR", "Info API Exception: ${e.message}")
            emptyList()
        }
    }
}

package com.locationtracker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.locationtracker.api.NetworkClient
import com.locationtracker.data.LatestInfoResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

class TrackingViewModel : ViewModel() {

    private val _distance = MutableStateFlow(0)
    val distance: StateFlow<Int> = _distance

    private val _location = MutableStateFlow("Unknown")
    val location: StateFlow<String> = _location

    private val _remainingDistance = MutableStateFlow(0)
    val remainingDistance: StateFlow<Int> = _remainingDistance

    private var isTracking = false
    private var estimationJob: Job? = null
    private var apiSyncJob: Job? = null
    private var apiService: com.locationtracker.api.LocationApiService? = null

    fun startTracking(context: Context) {
        if (isTracking) return
        
        isTracking = true
        _distance.value = 0
        _location.value = "Unknown"
        _remainingDistance.value = 0
        
        // Initialize API service once
        apiService = NetworkClient.create(context)
        
        // Start estimation loop - increments every 30 seconds by 150m (5m/s)
        estimationJob = viewModelScope.launch {
            while (isActive && isTracking) {
                delay(30000) // 30 seconds
                _distance.value += 150 // 5m/s * 30s = 150m
            }
        }
        
        // Start API sync - fetch authoritative data periodically
        apiSyncJob = viewModelScope.launch {
            while (isActive && isTracking) {
                try {
                    apiService?.let { service ->
                        val response = service.getLatestInfo()
                        if (response.isSuccessful) {
                            response.body()?.let { latestInfo ->
                                onApiUpdate(latestInfo.distance, latestInfo.locationName, latestInfo.remainingDistance)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Silently handle API errors - estimation continues
                }
                delay(60000) // Sync every 60 seconds
            }
        }
    }

    fun stopTracking(context: Context) {
        isTracking = false
        estimationJob?.cancel()
        estimationJob = null
        
        // Fetch latest data one more time before stopping
        viewModelScope.launch {
            try {
                val apiService = NetworkClient.create(context)
                val response = apiService.getLatestInfo()
                if (response.isSuccessful) {
                    response.body()?.let { latestInfo ->
                        onApiUpdate(latestInfo.distance, latestInfo.locationName, latestInfo.remainingDistance)
                    }
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
        
        apiSyncJob?.cancel()
        apiSyncJob = null
    }

    fun onApiUpdate(newDist: Int, newLoc: String, newRemainingDist: Int) {
        _distance.value = newDist // Overwrite with authoritative data
        _location.value = newLoc
        _remainingDistance.value = newRemainingDist
    }
}

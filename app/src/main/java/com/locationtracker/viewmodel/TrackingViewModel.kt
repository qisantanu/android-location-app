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
    private var apiService: com.locationtracker.api.LocationApiService? = null

    fun startTracking(context: Context) {
        if (isTracking) return
        
        isTracking = true
        _distance.value = 0
        _location.value = "Unknown"
        _remainingDistance.value = 0
        
        // Initialize API service once
        apiService = NetworkClient.create(context)
    }

    fun stopTracking(context: Context) {
        isTracking = false
    }
}

package com.locationtracker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.locationtracker.api.NetworkClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InfoViewModel : ViewModel() {

    private val _infoData = MutableStateFlow<Map<String, String>>(emptyMap())
    val infoData: StateFlow<Map<String, String>> = _infoData

    private var apiService: com.locationtracker.api.LocationApiService? = null

    fun fetchInfo(context: Context) {
        // Initialize API service if it hasn't been already
        if (apiService == null) {
            apiService = NetworkClient.create(context)
        }

        viewModelScope.launch {
            try {
                apiService?.let { service ->
                    val response = service.getLatestInfo()
                    if (response.isSuccessful) {
                        response.body()?.let { latestInfo ->
                            val dataMap = mutableMapOf<String, String>()
                            dataMap["Distance"] = "${latestInfo.distance} m"
                            dataMap["Location Name"] = latestInfo.locationName
                            dataMap["Remaining Distance"] = "${latestInfo.remainingDistance} m"
                            _infoData.value = dataMap
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle API errors
            }
        }
    }
}

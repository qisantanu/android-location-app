package com.locationtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.locationtracker.data.DistanceStats
import com.locationtracker.repository.DistanceStatsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DistanceStatsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = DistanceStatsRepository(application)
    
    private val _distanceStats = MutableLiveData<DistanceStats?>(null)
    val distanceStats: LiveData<DistanceStats?> = _distanceStats
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    
    private val _lastUpdated = MutableLiveData<Long>(0L)
    val lastUpdated: LiveData<Long> = _lastUpdated
    
    private val REFRESH_INTERVAL = 5 * 60 * 1000L // 5 minutes in milliseconds
    
    init {
        startAutoRefresh()
    }
    
    fun fetchDistanceStats() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = repository.getDistanceStats()
            
            result.onSuccess { stats ->
                _distanceStats.value = stats
                _lastUpdated.value = System.currentTimeMillis()
                _isLoading.value = false
            }
            
            result.onFailure { exception ->
                _error.value = exception.message ?: "Unknown error occurred"
                _isLoading.value = false
            }
        }
    }
    
    private fun startAutoRefresh() {
        viewModelScope.launch {
            // Fetch immediately on first load
            fetchDistanceStats()
            
            // Then refresh every 5 minutes
            while (isActive) {
                delay(REFRESH_INTERVAL)
                if (isActive) {
                    fetchDistanceStats()
                }
            }
        }
    }
    
    fun manualRefresh() {
        fetchDistanceStats()
    }
}

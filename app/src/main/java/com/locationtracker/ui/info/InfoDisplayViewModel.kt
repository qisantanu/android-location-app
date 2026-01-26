package com.locationtracker.ui.info

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.locationtracker.data.InfoItem
import com.locationtracker.repository.InfoRepository
import com.locationtracker.repository.LogRepository
import kotlinx.coroutines.launch

class InfoDisplayViewModel(application: Application) : AndroidViewModel(application) {

    private val infoRepository: InfoRepository
    private val logRepository: LogRepository

    private val _infoData = MutableLiveData<List<InfoItem>>()
    val infoData: LiveData<List<InfoItem>> = _infoData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        logRepository = LogRepository(application.applicationContext)
        infoRepository = InfoRepository(application.applicationContext, logRepository)
        fetchInfo()
    }

    fun fetchInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = infoRepository.fetchInfo()
                _infoData.value = result
                if (result.isEmpty()) {
                    _errorMessage.value = "No data received or API error."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch information: ${e.message}"
                logRepository.insertLog("ERROR", "ViewModel fetchInfo error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}

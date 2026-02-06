package com.locationtracker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.locationtracker.api.NetworkClient
import com.locationtracker.data.SettingItem
import com.locationtracker.data.UpdateSettingRequest
import com.locationtracker.ui.settings.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var settingsApiService: com.locationtracker.api.SettingsApiService? = null

    fun init(context: Context) {
        settingsApiService = NetworkClient.createSettingsApi(context)
        loadSettings(context)
    }

    private fun loadSettings(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val apiService = settingsApiService ?: NetworkClient.createSettingsApi(context)
                val settings = apiService.getSettings()
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load settings: ${e.message}"
                )
            }
        }
    }

    fun selectSetting(setting: SettingItem) {
        _uiState.value = _uiState.value.copy(
            selectedSetting = setting,
            isEditDialogVisible = true
        )
    }

    fun dismissEditDialog() {
        _uiState.value = _uiState.value.copy(
            isEditDialogVisible = false,
            selectedSetting = null
        )
    }

    fun updateSetting(context: Context, newValue: String) {
        val selectedSetting = _uiState.value.selectedSetting ?: return
        
        if (newValue.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Value cannot be blank"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val apiService = settingsApiService ?: NetworkClient.createSettingsApi(context)
                val response = apiService.updateSetting(
                    selectedSetting.id,
                    UpdateSettingRequest(newValue)
                )
                
                if (response.isSuccessful) {
                    // Update the list in-place to save bandwidth
                    val updatedSettings = _uiState.value.settings.map { setting ->
                        if (setting.id == selectedSetting.id) {
                            setting.copy(value = newValue)
                        } else {
                            setting
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        settings = updatedSettings,
                        isLoading = false,
                        isEditDialogVisible = false,
                        selectedSetting = null,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to update setting: ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to update setting: ${e.message}"
                )
            }
        }
    }

    fun refreshSettings(context: Context) {
        loadSettings(context)
    }
}

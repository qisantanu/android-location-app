package com.locationtracker.ui.settings

import com.locationtracker.data.SettingItem

data class SettingsUiState(
    val settings: List<SettingItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedSetting: SettingItem? = null,
    val isEditDialogVisible: Boolean = false
)

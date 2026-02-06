package com.locationtracker.ui.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.locationtracker.data.SettingItem
import com.locationtracker.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    context: Context = LocalContext.current
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.settings.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.errorMessage != null && uiState.settings.isEmpty() -> {
                    ErrorMessage(
                        message = uiState.errorMessage,
                        onRetry = { viewModel.refreshSettings(context) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    SettingsList(
                        settings = uiState.settings,
                        onSettingClick = { viewModel.selectSetting(it) },
                        isLoading = uiState.isLoading
                    )
                }
            }

            // Edit Dialog (BottomSheet)
            if (uiState.isEditDialogVisible && uiState.selectedSetting != null) {
                EditSettingDialog(
                    setting = uiState.selectedSetting,
                    onDismiss = { viewModel.dismissEditDialog() },
                    onSave = { newValue ->
                        viewModel.updateSetting(context, newValue)
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsList(
    settings: List<SettingItem>,
    onSettingClick: (SettingItem) -> Unit,
    isLoading: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(settings) { setting ->
            SettingItemRow(
                setting = setting,
                onClick = { onSettingClick(setting) }
            )
        }

        if (isLoading && settings.isNotEmpty()) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SettingItemRow(
    setting: SettingItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = setting.key,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = setting.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun EditSettingDialog(
    setting: SettingItem,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newValue by remember { mutableStateOf(setting.value) }
    var isValueValid by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${setting.key}") },
        text = {
            Column {
                OutlinedTextField(
                    value = newValue,
                    onValueChange = {
                        newValue = it
                        isValueValid = it.isNotBlank()
                    },
                    label = { Text("New Value") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !isValueValid && newValue.isBlank()
                )
                if (!isValueValid && newValue.isBlank()) {
                    Text(
                        text = "Value cannot be blank",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newValue.isNotBlank()) {
                        onSave(newValue)
                    }
                },
                enabled = newValue.isNotBlank() && newValue != setting.value
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

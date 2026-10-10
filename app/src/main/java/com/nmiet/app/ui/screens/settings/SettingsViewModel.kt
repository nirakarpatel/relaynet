package com.nmiet.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.domain.model.SyncStatus
import com.nmiet.app.domain.repository.ContentRepository
import com.nmiet.app.permissions.NearbyPermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: ContentRepository,
    private val context: Context
) : ViewModel() {

    val syncStatus: StateFlow<SyncStatus> = repository.getSyncStatusFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncStatus()
        )

    private val _hasPermissions = MutableStateFlow(NearbyPermissionManager.hasNearbyPermissions(context))
    val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(NearbyPermissionManager.isBluetoothEnabled(context))
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    fun refreshHardwareState() {
        _hasPermissions.value = NearbyPermissionManager.hasNearbyPermissions(context)
        _isBluetoothEnabled.value = NearbyPermissionManager.isBluetoothEnabled(context)
    }

    fun toggleNetworkOnline(isOnline: Boolean) {
        repository.setSimulatedNetworkOnline(isOnline)
    }

    fun resetToDemoCatalog() {
        viewModelScope.launch {
            repository.resetToInitialDemoData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllCache()
        }
    }
}

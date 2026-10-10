package com.nmiet.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.Board
import com.nmiet.app.domain.model.LearnerProfile
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

    val profile: StateFlow<LearnerProfile> = repository.getLearnerProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LearnerProfile())

    val syncStatus: StateFlow<SyncStatus> = repository.getSyncStatusFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus())

    private val _hasPermissions = MutableStateFlow(NearbyPermissionManager.hasNearbyPermissions(context))
    val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(NearbyPermissionManager.isBluetoothEnabled(context))
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    fun updateBoard(board: Board) {
        val current = profile.value
        viewModelScope.launch {
            repository.updateLearnerProfile(current.copy(board = board))
        }
    }

    fun updateGrade(grade: Int) {
        val current = profile.value
        viewModelScope.launch {
            repository.updateLearnerProfile(current.copy(classGrade = grade))
        }
    }

    fun updateLanguage(lang: AppLanguage) {
        val current = profile.value
        viewModelScope.launch {
            repository.updateLearnerProfile(current.copy(preferredLanguage = lang))
        }
    }

    fun refreshHardwareState() {
        _hasPermissions.value = NearbyPermissionManager.hasNearbyPermissions(context)
        _isBluetoothEnabled.value = NearbyPermissionManager.isBluetoothEnabled(context)
    }

    fun toggleNetworkOnline(isOnline: Boolean) {
        repository.setSimulatedNetworkOnline(isOnline)
    }

    fun resetToDemoCatalog() {
        viewModelScope.launch {
            repository.resetToPilotDemoPacks()
        }
    }
}

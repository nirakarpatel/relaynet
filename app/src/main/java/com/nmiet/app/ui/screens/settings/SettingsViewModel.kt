package com.nmiet.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.data.local.LanguageManager
import com.nmiet.app.data.repository.UserPreferencesRepository
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
    private val context: Context,
    val languageManager: LanguageManager = LanguageManager.getInstance(context),
    val userPreferencesRepository: UserPreferencesRepository = UserPreferencesRepository.getInstance(context)
) : ViewModel() {

    val currentLanguage: StateFlow<AppLanguage> = languageManager.currentLanguage

    val profile: StateFlow<LearnerProfile> = userPreferencesRepository.learnerProfile

    val syncStatus: StateFlow<SyncStatus> = repository.getSyncStatusFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus())

    private val _hasPermissions = MutableStateFlow(NearbyPermissionManager.hasNearbyPermissions(context))
    val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(NearbyPermissionManager.isBluetoothEnabled(context))
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    // Adaptive Storage Breakdown State
    private val _audioCacheSizeMb = MutableStateFlow(12.8)
    val audioCacheSizeMb: StateFlow<Double> = _audioCacheSizeMb.asStateFlow()

    private val _coreDataSizeMb = MutableStateFlow(1.4)
    val coreDataSizeMb: StateFlow<Double> = _coreDataSizeMb.asStateFlow()

    private val _diagramsCacheSizeMb = MutableStateFlow(2.1)
    val diagramsCacheSizeMb: StateFlow<Double> = _diagramsCacheSizeMb.asStateFlow()

    // Sync Queue Status
    private val _pendingSyncAttemptsCount = MutableStateFlow(3)
    val pendingSyncAttemptsCount: StateFlow<Int> = _pendingSyncAttemptsCount.asStateFlow()

    // NVIDIA NIM / Cloud AI API Key State
    private val apiPrefs = context.getSharedPreferences("relaynet_api_prefs", Context.MODE_PRIVATE)
    private val _nvidiaApiKey = MutableStateFlow(apiPrefs.getString("nvidia_api_key", "") ?: "")
    val nvidiaApiKey: StateFlow<String> = _nvidiaApiKey.asStateFlow()

    init {
        repository.setApiKey(_nvidiaApiKey.value)
    }

    fun saveNvidiaApiKey(key: String) {
        val trimmed = key.trim()
        _nvidiaApiKey.value = trimmed
        apiPrefs.edit().putString("nvidia_api_key", trimmed).apply()
        repository.setApiKey(trimmed)
    }

    fun clearNvidiaApiKey() {
        _nvidiaApiKey.value = ""
        apiPrefs.edit().remove("nvidia_api_key").apply()
        repository.setApiKey("")
    }

    fun clearDiagramCache() {
        _diagramsCacheSizeMb.value = 0.0
    }

    fun updateBoard(board: Board) {
        userPreferencesRepository.updateEducationalBoard(board.name)
        val current = profile.value
        viewModelScope.launch {
            repository.updateLearnerProfile(current.copy(educationalBoard = board.name))
        }
    }

    fun updateEducationalBoard(board: String) {
        userPreferencesRepository.updateEducationalBoard(board)
        val current = profile.value
        viewModelScope.launch {
            repository.updateLearnerProfile(current.copy(educationalBoard = board))
        }
    }

    fun updateGrade(grade: Int) {
        val gradeStr = "Class $grade"
        userPreferencesRepository.updateClassGrade(gradeStr)
        val current = profile.value
        viewModelScope.launch {
            repository.updateLearnerProfile(current.copy(classGrade = gradeStr))
        }
    }

    fun updateClassGrade(grade: String) {
        userPreferencesRepository.updateClassGrade(grade)
        val current = profile.value
        viewModelScope.launch {
            repository.updateLearnerProfile(current.copy(classGrade = grade))
        }
    }

    fun updateLearnerName(name: String) {
        userPreferencesRepository.updateStudentName(name)
        val current = profile.value
        viewModelScope.launch {
            repository.updateLearnerProfile(current.copy(studentName = name))
        }
    }

    fun resetLearnerProfile() {
        userPreferencesRepository.resetProfile()
        viewModelScope.launch {
            repository.updateLearnerProfile(
                LearnerProfile(
                    studentName = "Soumya Ranjan",
                    educationalBoard = "ODISHA_STATE",
                    classGrade = "Class 9",
                    languageMode = AppLanguage.BILINGUAL_EN_HI,
                    studentId = "student_pilot_01"
                )
            )
        }
    }

    fun updateLanguage(lang: AppLanguage) {
        languageManager.setLanguage(lang)
        userPreferencesRepository.updateLanguageMode(lang)
        val current = profile.value
        viewModelScope.launch {
            repository.updateLearnerProfile(current.copy(languageMode = lang))
        }
    }

    fun refreshHardwareState() {
        _hasPermissions.value = NearbyPermissionManager.hasNearbyPermissions(context)
        _isBluetoothEnabled.value = NearbyPermissionManager.isBluetoothEnabled(context)
    }

    fun toggleOfflineSimulationSwitch(forceOffline: Boolean) {
        // Toggle simulated network status (force offline = !isOnline)
        repository.setSimulatedNetworkOnline(!forceOffline)
    }

    fun clearAudioCache() {
        _audioCacheSizeMb.value = 0.0
    }

    fun triggerCloudSync() {
        if (syncStatus.value.isOnline) {
            _pendingSyncAttemptsCount.value = 0
        }
    }

    fun syncPendingAttempts() {
        triggerCloudSync()
    }

    fun resetToDemoCatalog() {
        viewModelScope.launch {
            repository.resetToPilotDemoPacks()
            _audioCacheSizeMb.value = 12.8
            _diagramsCacheSizeMb.value = 2.1
            _pendingSyncAttemptsCount.value = 3
        }
    }
}

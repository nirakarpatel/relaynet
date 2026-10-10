package com.nmiet.app.ui.screens.curriculum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.domain.model.CurriculumNode
import com.nmiet.app.domain.model.LearnerProfile
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.SyncStatus
import com.nmiet.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CurriculumViewModel(
    private val repository: ContentRepository
) : ViewModel() {

    val profile: StateFlow<LearnerProfile> = repository.getLearnerProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LearnerProfile())

    val syncStatus: StateFlow<SyncStatus> = repository.getSyncStatusFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus())

    private val _selectedSubject = MutableStateFlow("All")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    val learningPacks: StateFlow<List<LearningPack>> = combine(
        repository.getLearningPacksFlow(),
        _selectedSubject
    ) { packs, subject ->
        if (subject == "All") packs
        else packs.filter { it.subject.contains(subject, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCurriculumNodes: StateFlow<List<CurriculumNode>> = repository.getCurriculumNodesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.resetToPilotDemoPacks()
        }
    }

    fun onSubjectSelected(subject: String) {
        _selectedSubject.value = subject
    }
}

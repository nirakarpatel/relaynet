package com.nmiet.app.ui.screens.mastery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.data.local.LanguageManager
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.QuizAttempt
import com.nmiet.app.domain.model.SyncStatus
import com.nmiet.app.domain.model.TopicMastery
import com.nmiet.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MasteryViewModel(
    private val repository: ContentRepository,
    val languageManager: LanguageManager? = null
) : ViewModel() {

    val currentLanguage: StateFlow<AppLanguage> = languageManager?.currentLanguage
        ?: repository.getLearnerProfileFlow()
            .map { it.preferredLanguage }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.BILINGUAL_EN_OR)

    val topicMasteryList: StateFlow<List<TopicMastery>> = repository.getTopicMasteryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizAttempts: StateFlow<List<QuizAttempt>> = repository.getRecentQuizAttemptsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncStatus: StateFlow<SyncStatus> = repository.getSyncStatusFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus())
}

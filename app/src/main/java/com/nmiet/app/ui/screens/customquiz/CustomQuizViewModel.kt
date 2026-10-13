package com.nmiet.app.ui.screens.customquiz

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.domain.model.CustomQuizConfig
import com.nmiet.app.domain.model.CustomQuizQuestion
import com.nmiet.app.domain.model.QuizDifficulty
import com.nmiet.app.domain.model.QuizSourceType
import com.nmiet.app.domain.model.SyncStatus
import com.nmiet.app.domain.repository.ContentRepository
import com.nmiet.app.domain.usecase.QuizGeneratorEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CustomQuizUiState(
    val isConfiguring: Boolean = true,
    val isGenerating: Boolean = false,
    val config: CustomQuizConfig = CustomQuizConfig(),
    val questions: List<CustomQuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerRevealed: Boolean = false,
    val isCompleted: Boolean = false,
    val correctCount: Int = 0,
    val answeredQuestions: List<Pair<CustomQuizQuestion, Int>> = emptyList(), // question to chosen index
    val weakConcepts: List<String> = emptyList(),
    val masteredConcepts: List<String> = emptyList(),
    val error: String? = null
) {
    val totalCount: Int get() = questions.size
    val currentQuestion: CustomQuizQuestion? get() = questions.getOrNull(currentIndex)
    val scorePercent: Int get() = if (totalCount == 0) 0 else ((correctCount.toFloat() / totalCount.toFloat()) * 100).toInt()
}

class CustomQuizViewModel(
    private val repository: ContentRepository,
    private val generatorEngine: QuizGeneratorEngine = QuizGeneratorEngine(repository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomQuizUiState())
    val uiState: StateFlow<CustomQuizUiState> = _uiState.asStateFlow()

    val syncStatus: StateFlow<SyncStatus> = repository.getSyncStatusFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus())

    fun setSourceType(type: QuizSourceType) {
        _uiState.value = _uiState.value.copy(
            config = _uiState.value.config.copy(sourceType = type)
        )
    }

    fun setQuestionCount(count: Int) {
        _uiState.value = _uiState.value.copy(
            config = _uiState.value.config.copy(questionCount = count)
        )
    }

    fun setDifficulty(diff: QuizDifficulty) {
        _uiState.value = _uiState.value.copy(
            config = _uiState.value.config.copy(difficulty = diff)
        )
    }

    fun setSpecificTopicTag(tag: String) {
        _uiState.value = _uiState.value.copy(
            config = _uiState.value.config.copy(
                sourceType = QuizSourceType.WEAK_TOPIC,
                specificTopicTag = tag
            )
        )
    }

    fun attachTextbookPhoto(bitmap: Bitmap?) {
        _uiState.value = _uiState.value.copy(
            config = _uiState.value.config.copy(
                sourceType = QuizSourceType.TEXTBOOK_PHOTO,
                customPhoto = bitmap
            )
        )
    }

    fun startQuizGeneration() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true, isConfiguring = false)
            val questions = generatorEngine.generateQuiz(
                config = _uiState.value.config,
                isOnline = syncStatus.value.isOnline
            )
            _uiState.value = _uiState.value.copy(
                isGenerating = false,
                questions = questions,
                currentIndex = 0,
                selectedOptionIndex = null,
                isAnswerRevealed = false,
                isCompleted = false,
                correctCount = 0,
                answeredQuestions = emptyList(),
                weakConcepts = emptyList(),
                masteredConcepts = emptyList()
            )
        }
    }

    fun selectOption(index: Int) {
        val state = _uiState.value
        if (state.isAnswerRevealed || state.currentQuestion == null) return

        val q = state.currentQuestion ?: return
        val isCorrect = index == q.correctIndex
        val updatedCorrect = if (isCorrect) state.correctCount + 1 else state.correctCount
        val updatedAnswers = state.answeredQuestions + (q to index)

        _uiState.value = state.copy(
            selectedOptionIndex = index,
            isAnswerRevealed = true,
            correctCount = updatedCorrect,
            answeredQuestions = updatedAnswers
        )
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.currentIndex + 1 < state.totalCount) {
            _uiState.value = state.copy(
                currentIndex = state.currentIndex + 1,
                selectedOptionIndex = null,
                isAnswerRevealed = false
            )
        } else {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        val state = _uiState.value
        val weak = mutableSetOf<String>()
        val mastered = mutableSetOf<String>()

        state.answeredQuestions.forEach { (q, chosen) ->
            if (chosen == q.correctIndex) {
                mastered.add(q.conceptTag)
            } else {
                weak.add(q.conceptTag)
            }
        }

        val finalWeak = weak.toList()
        val finalMastered = mastered.filter { it !in weak }.toList()

        _uiState.value = state.copy(
            isCompleted = true,
            weakConcepts = finalWeak,
            masteredConcepts = finalMastered
        )

        // Save Attempt to Room SQLite with isSynced = false
        viewModelScope.launch {
            repository.submitQuizAttempt(
                topicId = "relaynet_class9_science_photosynthesis_v1",
                topicName = "Generated Quiz: ${state.config.sourceType.displayName} (${state.config.difficulty.displayName})",
                scorePercent = state.scorePercent,
                correctCount = state.correctCount,
                totalCount = state.totalCount,
                weakConcepts = finalWeak
            )
        }
    }

    fun retakeFailedQuestions() {
        val state = _uiState.value
        val failed = state.answeredQuestions.filter { (q, chosen) -> chosen != q.correctIndex }.map { it.first }
        if (failed.isNotEmpty()) {
            _uiState.value = state.copy(
                questions = failed,
                currentIndex = 0,
                selectedOptionIndex = null,
                isAnswerRevealed = false,
                isCompleted = false,
                correctCount = 0,
                answeredQuestions = emptyList()
            )
        } else {
            startQuizGeneration()
        }
    }

    fun resetToConfiguration() {
        _uiState.value = _uiState.value.copy(
            isConfiguring = true,
            isCompleted = false,
            currentIndex = 0,
            selectedOptionIndex = null,
            isAnswerRevealed = false
        )
    }
}

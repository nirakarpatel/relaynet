package com.nmiet.app.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.QuizQuestion
import com.nmiet.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuizViewModel(
    private val repository: ContentRepository,
    val packId: String
) : ViewModel() {

    private val _learningPack = MutableStateFlow<LearningPack?>(null)
    val learningPack: StateFlow<LearningPack?> = _learningPack.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
    val selectedOptionIndex: StateFlow<Int?> = _selectedOptionIndex.asStateFlow()

    private val _isAnswerSubmitted = MutableStateFlow(false)
    val isAnswerSubmitted: StateFlow<Boolean> = _isAnswerSubmitted.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.ODIA)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _correctCount = MutableStateFlow(0)
    val correctCount: StateFlow<Int> = _correctCount.asStateFlow()

    private val _weakConcepts = MutableStateFlow<MutableList<String>>(mutableListOf())
    val weakConcepts: StateFlow<List<String>> = _weakConcepts.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished: StateFlow<Boolean> = _isQuizFinished.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getLearningPackByIdFlow(packId).collect { pack ->
                _learningPack.value = pack
            }
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun selectOption(index: Int) {
        if (!_isAnswerSubmitted.value) {
            _selectedOptionIndex.value = index
        }
    }

    fun submitAnswer() {
        val pack = _learningPack.value ?: return
        val question = pack.quizQuestions.getOrNull(_currentIndex.value) ?: return
        val selected = _selectedOptionIndex.value ?: return

        _isAnswerSubmitted.value = true

        if (selected == question.correctIndex) {
            _correctCount.value += 1
        } else {
            val list = _weakConcepts.value.toMutableList()
            if (!list.contains(question.conceptTag)) {
                list.add(question.conceptTag)
            }
            _weakConcepts.value = list
        }
    }

    fun nextQuestion() {
        val pack = _learningPack.value ?: return
        if (_currentIndex.value < pack.quizQuestions.size - 1) {
            _currentIndex.value += 1
            _selectedOptionIndex.value = null
            _isAnswerSubmitted.value = false
        } else {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        val pack = _learningPack.value ?: return
        val total = pack.quizQuestions.size
        val scorePercent = if (total > 0) ((_correctCount.value.toFloat() / total) * 100).toInt() else 0

        viewModelScope.launch {
            repository.submitQuizAttempt(
                topicId = pack.packId,
                topicName = pack.titleEnglish,
                scorePercent = scorePercent,
                correctCount = _correctCount.value,
                totalCount = total,
                weakConcepts = _weakConcepts.value
            )
            _isQuizFinished.value = true
        }
    }
}

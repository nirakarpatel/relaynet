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
    val packId: String,
    val topicTag: String? = null
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

    val isTargetedTopicQuiz: Boolean = !topicTag.isNullOrBlank()

    init {
        viewModelScope.launch {
            repository.getLearningPackByIdFlow(packId).collect { rawPack ->
                if (rawPack != null) {
                    val pack = if (!topicTag.isNullOrBlank()) {
                        val targetedQuestions = buildTargetedQuestions(rawPack.quizQuestions, topicTag)
                        rawPack.copy(
                            titleEnglish = "${rawPack.titleEnglish} • Targeted: $topicTag",
                            quizQuestions = targetedQuestions
                        )
                    } else {
                        rawPack
                    }
                    _learningPack.value = pack
                }
            }
        }
    }

    private fun buildTargetedQuestions(original: List<QuizQuestion>, tag: String): List<QuizQuestion> {
        val matches = original.filter {
            it.conceptTag.contains("photolysis", ignoreCase = true) ||
            it.conceptTag.contains("chloroplast", ignoreCase = true) ||
            it.questionEnglish.contains("photolysis", ignoreCase = true) ||
            it.questionEnglish.contains("light", ignoreCase = true) ||
            it.conceptTag.equals(tag, ignoreCase = true)
        }
        val result = matches.toMutableList()
        // Ensure 3 isolated questions for targeted diagnostic practice
        if (result.size < 3) {
            val supplemental = listOf(
                QuizQuestion(
                    id = "q_targeted_photolysis_eq",
                    questionOdia = "ଫୋଟୋଲିସିସ୍ (Water photolysis) ପ୍ରକ୍ରିୟାରେ ନିମ୍ନଲିଖିତ ମଧ୍ୟରୁ କେଉଁଟି ସଠିକ୍ ରାସାୟନିକ ବିଭାଜନ?",
                    questionEnglish = "Which of the following correctly represents the photolysis of water in light reactions?",
                    optionsOdia = listOf(
                        "A) CO₂ + H₂O ──> Glucose",
                        "B) 2H₂O ──[Light/Chlorophyll]──> 4H⁺ + 4e⁻ + O₂↑",
                        "C) O₂ + 4e⁻ ──> 2H₂O",
                        "D) Glucose ──> Pyruvate + Energy"
                    ),
                    optionsEnglish = listOf(
                        "A) CO₂ + H₂O ──> Glucose",
                        "B) 2H₂O ──[Light/Chlorophyll]──> 4H⁺ + 4e⁻ + O₂↑",
                        "C) O₂ + 4e⁻ ──> 2H₂O",
                        "D) Glucose ──> Pyruvate + Energy"
                    ),
                    correctIndex = 1,
                    explanationOdia = "ସୌରଶକ୍ତି ଉପସ୍ଥିତିରେ ଜଳ ଅଣୁ ବିଭାଜିତ ହୋଇ ପ୍ରୋଟନ୍ (H⁺), ଇଲେକ୍ଟ୍ରନ୍ (e⁻) ଏବଂ ଅମ୍ଳଜାନ (O₂) ଗ୍ୟାସ୍ ପ୍ରଦାନ କରେ।",
                    explanationEnglish = "Water splitting photolysis under radiant light generates protons, electrons, and by-product oxygen gas.",
                    conceptTag = "PHOTOLYSIS_WATER",
                    misconceptionHintOdia = "⚠️ Water is oxidized and split into protons, electrons, and O₂.",
                    misconceptionHintEnglish = "⚠️ Water is oxidized and split into protons, electrons, and O₂."
                )
            )
            for (q in supplemental) {
                if (result.none { it.id == q.id }) {
                    result.add(q)
                }
                if (result.size >= 3) break
            }
        }
        return result.take(3)
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
                topicId = packId,
                topicName = if (isTargetedTopicQuiz) "Targeted: Photolysis & Light Reactions" else pack.titleEnglish,
                scorePercent = scorePercent,
                correctCount = _correctCount.value,
                totalCount = total,
                weakConcepts = _weakConcepts.value
            )
            _isQuizFinished.value = true
        }
    }
}

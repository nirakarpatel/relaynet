package com.nmiet.app.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.data.local.LanguageManager
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
    val topicTag: String? = null,
    val languageManager: LanguageManager? = null
) : ViewModel() {

    private val _learningPack = MutableStateFlow<LearningPack?>(null)
    val learningPack: StateFlow<LearningPack?> = _learningPack.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
    val selectedOptionIndex: StateFlow<Int?> = _selectedOptionIndex.asStateFlow()

    private val _isAnswerSubmitted = MutableStateFlow(false)
    val isAnswerSubmitted: StateFlow<Boolean> = _isAnswerSubmitted.asStateFlow()

    private val _language = MutableStateFlow(languageManager?.currentLanguage?.value ?: AppLanguage.BILINGUAL_EN_OR)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _correctCount = MutableStateFlow(0)
    val correctCount: StateFlow<Int> = _correctCount.asStateFlow()

    private val _weakConcepts = MutableStateFlow<MutableList<String>>(mutableListOf())
    val weakConcepts: StateFlow<List<String>> = _weakConcepts.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished: StateFlow<Boolean> = _isQuizFinished.asStateFlow()

    data class DiagnosticBanner(
        val title: String,
        val messageEnglish: String,
        val messageOdia: String,
        val messageHindi: String,
        val conceptTag: String
    )

    private val _diagnosticBanner = MutableStateFlow<DiagnosticBanner?>(null)
    val diagnosticBanner: StateFlow<DiagnosticBanner?> = _diagnosticBanner.asStateFlow()

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
            _diagnosticBanner.value = null
        } else {
            val list = _weakConcepts.value.toMutableList()
            if (!list.contains(question.conceptTag)) {
                list.add(question.conceptTag)
            }
            _weakConcepts.value = list

            // Check for diagnostic interception
            val isBrainPack = packId == "class9_bio_human_brain"
            when {
                // Question 2: Cerebellar balance failure (Initiation vs. Coordination)
                (isBrainPack && _currentIndex.value == 1) || question.conceptTag == "CEREBELLAR_COORDINATION" -> {
                    _diagnosticBanner.value = DiagnosticBanner(
                        title = "⚠️ DIAGNOSTIC INTERCEPTOR: Cerebellum vs. Cerebrum Distinction",
                        messageEnglish = "CRITICAL DISTINCTION: Muscle movement initiation is driven consciously by the Cerebrum (motor cortex), but fine-tuning, balance equilibrium, and posture are coordinated by the Cerebellum. Cerebellar failure leads to ataxia (loss of balance and stumbling), NOT an inability to generate thoughts or decide to walk!",
                        messageOdia = "ସେରେବ୍ରମ୍ ମାଂସପେଶୀ ଗତି ଆରମ୍ଭ କରିବା ପାଇଁ ନିଷ୍ପତ୍ତି ନିଏ, କିନ୍ତୁ ଭାରସାମ୍ୟ ଓ ସନ୍ତୁଳନ ସେରେବେଲମ୍ ଦ୍ୱାରା ନିୟନ୍ତ୍ରିତ ହୁଏ। ସେରେବେଲମ୍ କ୍ଷତିଗ୍ରସ୍ତ ହେଲେ ବ୍ୟକ୍ତି ଝୁଣ୍ଟି ପଡିଥାଏ।",
                        messageHindi = "महत्वपूर्ण अंतर: गति की शुरुआत सेरेब्रम द्वारा होती है, किंतु शारीरिक संतुलन और स्थिरता सेरेबेलम द्वारा समन्वित होती है। सेरेबेलम की विफलता से संतुलन बिगड़ता है।",
                        conceptTag = "CEREBELLAR_COORDINATION"
                    )
                }
                // Question 3: Autonomic life support (Brainstem spinal cord bridge)
                (isBrainPack && _currentIndex.value == 2) || question.conceptTag == "BRAINSTEM_AUTONOMIC" -> {
                    _diagnosticBanner.value = DiagnosticBanner(
                        title = "⚠️ DIAGNOSTIC INTERCEPTOR: Brainstem Connection to Spinal Cord",
                        messageEnglish = "CRITICAL PATHWAY: The Brainstem serves as the vital columnar bridge connecting the brain directly to the spinal cord. It regulates involuntary, autonomic life-support functions—cardiac rhythm, respiration cadence, and peristalsis—24/7 without requiring conscious thought!",
                        messageOdia = "ବ୍ରେନ୍-ଷ୍ଟେମ୍ ମସ୍ତିଷ୍କକୁ ସିଧାସଳଖ ମେରୁଦଣ୍ଡ (Spinal Cord) ସହ ଯୋଡେ ଏବଂ ହୃଦସ୍ପନ୍ଦନ ଓ ଶ୍ୱାସକ୍ରିୟା ଭଳି ଅନୈଚ୍ଛିକ ଜୀବନ ରକ୍ଷାକାରୀ କାର୍ଯ୍ୟ ପରିଚାଳନା କରେ।",
                        messageHindi = "महत्वपूर्ण संरचना: ब्रेनस्टेम मस्तिष्क को रीढ़ की हड्डी से जोड़ता है और हृदय गति व श्वसन जैसी अनैच्छिक जीवन रक्षक क्रियाओं को नियंत्रित करता है।",
                        conceptTag = "BRAINSTEM_AUTONOMIC"
                    )
                }
                // Question 5: Wattage output (12-25 W LED lightbulb comparison)
                (isBrainPack && _currentIndex.value == 4) || question.conceptTag == "NEURAL_BIOELECTRICITY" -> {
                    _diagnosticBanner.value = DiagnosticBanner(
                        title = "⚠️ DIAGNOSTIC INTERCEPTOR: 12–25 W LED Lightbulb Output",
                        messageEnglish = "SCIENTIFIC FACT: The human brain continuously generates between 12 W and 25 W of bioelectrical energy through ionic synaptic action potentials. This electrical power is sufficient to illuminate a low-voltage LED bulb!",
                        messageOdia = "ମାନବ ମସ୍ତିଷ୍କ ନିରନ୍ତର ୧୨ ରୁ ୨୫ ୱାଟ୍ ବିଦ୍ୟୁତ୍ ଶକ୍ତି ଉତ୍ପନ୍ନ କରେ, ଯାହା ଗୋଟିଏ ଏଲ୍.ଇ.ଡି (LED) ବଲ୍ବ ଜଳାଇବା ପାଇଁ ଯଥେଷ୍ଟ!",
                        messageHindi = "वैज्ञानिक तथ्य: मानव मस्तिष्क लगातार 12 से 25 वाट विद्युत ऊर्जा उत्पन्न करता है—जो एक कम वोल्टेज वाले एलईडी बल्ब को जलाने के लिए पर्याप्त है!",
                        conceptTag = "NEURAL_BIOELECTRICITY"
                    )
                }
                else -> {
                    _diagnosticBanner.value = null
                }
            }
        }
    }

    fun nextQuestion() {
        val pack = _learningPack.value ?: return
        if (_currentIndex.value < pack.quizQuestions.size - 1) {
            _currentIndex.value += 1
            _selectedOptionIndex.value = null
            _isAnswerSubmitted.value = false
            _diagnosticBanner.value = null
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

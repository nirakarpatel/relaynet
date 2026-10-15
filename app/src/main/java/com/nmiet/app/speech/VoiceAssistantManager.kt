package com.nmiet.app.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.nmiet.app.data.local.AppDatabase
import com.nmiet.app.data.local.LanguageManager
import com.nmiet.app.data.remote.FluxDiagramService
import com.nmiet.app.data.remote.GeminiKeyRotator
import com.nmiet.app.data.remote.GeminiMultimodalService
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.StudyLevel
import com.nmiet.app.domain.repository.ContentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

sealed interface VoiceUiState {
    data object Idle : VoiceUiState

    data class Listening(
        val partialText: String = "",
        val soundLevelRms: Float = 0f,
        val languageLabel: String = ""
    ) : VoiceUiState

    data class Thinking(
        val recognizedQuery: String,
        val activeKeyIndex: Int = 1
    ) : VoiceUiState

    data class Processing(
        val recognizedText: String
    ) : VoiceUiState

    data class Speaking(
        val query: String,
        val answerText: String,
        val textEnglish: String = answerText,
        val textOdia: String = answerText,
        val isPaused: Boolean = false,
        val soundLevelRms: Float = 0f,
        val generatedDiagramBase64: String? = null
    ) : VoiceUiState

    data class Paused(
        val query: String,
        val answerText: String,
        val textEnglish: String = answerText,
        val textOdia: String = answerText,
        val generatedDiagramBase64: String? = null
    ) : VoiceUiState

    data class ActionExecuted(
        val recognizedText: String,
        val actionTitle: String,
        val actionOdia: String,
        val targetRoute: String?
    ) : VoiceUiState

    data class Error(val errorMessage: String) : VoiceUiState
}

data class SpeechLangConfig(
    val speechLocale: String,
    val additionalLocales: Array<String>,
    val ttsLocale: Locale,
    val fallbackTtsLocale: Locale = Locale("en", "IN"),
    val promptLanguageMode: String,
    val displayLabel: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SpeechLangConfig
        return speechLocale == other.speechLocale &&
                additionalLocales.contentEquals(other.additionalLocales) &&
                ttsLocale == other.ttsLocale &&
                fallbackTtsLocale == other.fallbackTtsLocale &&
                promptLanguageMode == other.promptLanguageMode &&
                displayLabel == other.displayLabel
    }

    override fun hashCode(): Int {
        var result = speechLocale.hashCode()
        result = 31 * result + additionalLocales.contentHashCode()
        result = 31 * result + ttsLocale.hashCode()
        result = 31 * result + fallbackTtsLocale.hashCode()
        result = 31 * result + promptLanguageMode.hashCode()
        result = 31 * result + displayLabel.hashCode()
        return result
    }
}

class VoiceAssistantManager(
    private val context: Context,
    private val repository: ContentRepository,
    private val languageManager: LanguageManager,
    private val geminiKeyRotator: GeminiKeyRotator = GeminiKeyRotator.getInstance(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job()),
    private val geminiMultimodalService: GeminiMultimodalService = GeminiMultimodalService(geminiKeyRotator),
    private val fluxDiagramService: FluxDiagramService = FluxDiagramService()
) : RecognitionListener, TextToSpeech.OnInitListener {

    private val _uiState = MutableStateFlow<VoiceUiState>(VoiceUiState.Idle)
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var isOdiaTtsSupported = false
    private var currentSpokenText: String = ""

    init {
        initTts()
    }

    fun getSpeechLanguageConfig(): SpeechLangConfig {
        return when (languageManager.currentLanguage.value) {
            AppLanguage.ENGLISH_ONLY -> SpeechLangConfig(
                speechLocale = "en-IN",
                additionalLocales = arrayOf("en-US"),
                ttsLocale = Locale("en", "IN"),
                fallbackTtsLocale = Locale.US,
                promptLanguageMode = "Indian English",
                displayLabel = "English (en-IN)"
            )
            AppLanguage.ODIA_ONLY -> SpeechLangConfig(
                speechLocale = "or-IN",
                additionalLocales = arrayOf("en-IN"),
                ttsLocale = Locale("or", "IN"),
                fallbackTtsLocale = Locale("en", "IN"),
                promptLanguageMode = "native Odia (ଓଡ଼ିଆ)",
                displayLabel = "ଓଡ଼ିଆ (Odia)"
            )
            AppLanguage.HINDI_ONLY -> SpeechLangConfig(
                speechLocale = "hi-IN",
                additionalLocales = arrayOf("en-IN"),
                ttsLocale = Locale("hi", "IN"),
                fallbackTtsLocale = Locale("en", "IN"),
                promptLanguageMode = "Hindi (हिंदी)",
                displayLabel = "हिंदी (Hindi)"
            )
            AppLanguage.BILINGUAL_EN_HI -> SpeechLangConfig(
                speechLocale = "hi-IN",
                additionalLocales = arrayOf("en-IN", "en-US"),
                ttsLocale = Locale("hi", "IN"),
                fallbackTtsLocale = Locale("en", "IN"),
                promptLanguageMode = "clear Hindi with English keywords (conversational Hinglish academic style)",
                displayLabel = "English + हिंदी"
            )
            AppLanguage.BILINGUAL_EN_OR -> SpeechLangConfig(
                speechLocale = "or-IN",
                additionalLocales = arrayOf("en-IN", "en-US"),
                ttsLocale = Locale("or", "IN"),
                fallbackTtsLocale = Locale("en", "IN"),
                promptLanguageMode = "native Odia with English technical terms",
                displayLabel = "English + ଓଡ଼ିଆ"
            )
        }
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("VoiceAssistantManager", "Failed to init TTS: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            val odiaLocale = Locale("or", "IN")
            val langResult = tts?.isLanguageAvailable(odiaLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
            isOdiaTtsSupported = (langResult == TextToSpeech.LANG_AVAILABLE || langResult == TextToSpeech.LANG_COUNTRY_AVAILABLE)

            val config = getSpeechLanguageConfig()
            applyTtsLocale(config)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    scope.launch {
                        val current = _uiState.value
                        if (current is VoiceUiState.Speaking && !current.isPaused) {
                            _uiState.value = VoiceUiState.Idle
                        }
                    }
                }
                override fun onError(utteranceId: String?) {
                    scope.launch {
                        _uiState.value = VoiceUiState.Idle
                    }
                }
            })
        }
    }

    private fun applyTtsLocale(config: SpeechLangConfig) {
        val primaryAvailable = tts?.isLanguageAvailable(config.ttsLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
        if (primaryAvailable == TextToSpeech.LANG_AVAILABLE || primaryAvailable == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
            tts?.language = config.ttsLocale
        } else {
            tts?.language = config.fallbackTtsLocale
        }
    }

    fun startListening() {
        stopSpeaking()
        if (!com.nmiet.app.permissions.NearbyPermissionManager.hasAudioPermission(context)) {
            _uiState.value = VoiceUiState.Error("Microphone permission (RECORD_AUDIO) is required to use Voice Assistant.")
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _uiState.value = VoiceUiState.Error("Speech Recognition is not available on this device.")
            return
        }

        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(this)
            }

            val config = getSpeechLanguageConfig()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, config.speechLocale)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, config.speechLocale)
                if (config.additionalLocales.isNotEmpty()) {
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", config.additionalLocales)
                }
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
            _uiState.value = VoiceUiState.Listening(
                partialText = "Listening in ${config.displayLabel}...",
                soundLevelRms = 0f,
                languageLabel = config.displayLabel
            )
        } catch (e: Exception) {
            _uiState.value = VoiceUiState.Error("Failed to start voice recognizer: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
    }

    // --- RecognitionListener Callbacks ---

    override fun onReadyForSpeech(params: Bundle?) {
        val config = getSpeechLanguageConfig()
        _uiState.value = VoiceUiState.Listening(
            partialText = "Listening...",
            soundLevelRms = 0f,
            languageLabel = config.displayLabel
        )
    }

    override fun onRmsChanged(rmsdB: Float) {
        val current = _uiState.value
        if (current is VoiceUiState.Listening) {
            _uiState.value = current.copy(soundLevelRms = rmsdB.coerceIn(0f, 10f))
        }
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull() ?: ""
        if (text.isNotBlank()) {
            val config = getSpeechLanguageConfig()
            _uiState.value = VoiceUiState.Listening(
                partialText = text,
                languageLabel = config.displayLabel
            )
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val query = matches?.firstOrNull() ?: ""
        if (query.isNotBlank()) {
            _uiState.value = VoiceUiState.Thinking(
                recognizedQuery = query,
                activeKeyIndex = geminiKeyRotator.getActiveKeyIndex()
            )
            parseVoiceIntent(query)
        } else {
            _uiState.value = VoiceUiState.Idle
        }
    }

    override fun onError(error: Int) {
        val errorMsg = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network offline: please try speaking again"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network connection timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak again."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice recognizer is busy"
            SpeechRecognizer.ERROR_SERVER -> "Speech server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
            else -> "Voice recognition error ($error)"
        }
        _uiState.value = VoiceUiState.Error(errorMsg)
    }

    override fun onBeginningOfSpeech() {}
    override fun onEndOfSpeech() {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    // --- Diagram Intent Detector Helper ---
    fun isDiagramRequest(query: String): Boolean {
        val q = query.lowercase().trim()
        return q.contains("diagram") || q.contains("flowchart") || 
               q.contains("draw") || q.contains("generate image") || 
               q.contains("picture of") || q.contains("bond structure") ||
               q.contains("structure of") || q.contains("चित्र") || q.contains("ଚିତ୍ର")
    }

    // --- Voice Intent Parser & Academic Tutor Route ---

    fun parseVoiceIntent(spokenText: String) {
        val lower = spokenText.lowercase().trim()

        // 1. Navigation: Explicit request to open a specific lesson
        val isExplicitLessonOpen = (lower.startsWith("open lesson") || lower.startsWith("open chapter") ||
                lower.startsWith("ପାଠ ଖୋଲ") || lower.startsWith("ଅଧ୍ୟାୟ ଖୋଲ") ||
                lower.startsWith("पाठ खोलो")) && (lower.contains("photosynthesis") || lower.contains("chapter 4") || lower.contains("ଆଲୋକ ଶ୍ଳେଷଣ"))
        if (isExplicitLessonOpen) {
            val route = "lesson/relaynet_class9_science_photosynthesis_v1"
            _uiState.value = VoiceUiState.ActionExecuted(
                recognizedText = spokenText,
                actionTitle = "Opening Lesson: Chapter 4 Photosynthesis",
                actionOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ଅଧ୍ୟାୟ ଖୋଲାଯାଉଛି...",
                targetRoute = route
            )
            speakUtterance(
                text = "Opening Chapter 4 Photosynthesis lesson reader.",
                config = getSpeechLanguageConfig()
            )
            return
        }

        // 2. Navigation: "Start Quiz" / "କୁଇଜ୍ ଆରମ୍ଭ କର" / "test"
        val isQuizIntent = lower.startsWith("start quiz") || lower.startsWith("open quiz") ||
                lower.startsWith("କୁଇଜ୍ ଆରମ୍ଭ") || lower.startsWith("क्विज शुरू") ||
                lower == "quiz" || lower == "start quiz" || lower == "test"
        if (isQuizIntent) {
            val route = "quiz/relaynet_class9_science_photosynthesis_v1"
            _uiState.value = VoiceUiState.ActionExecuted(
                recognizedText = spokenText,
                actionTitle = "Launching Diagnostic Quiz",
                actionOdia = "ଡାଇଗ୍ନୋଷ୍ଟିକ୍ କୁଇଜ୍ ଆରମ୍ଭ କରାଯାଉଛି...",
                targetRoute = route
            )
            speakUtterance(
                text = "Launching diagnostic self-assessment quiz for Chapter 4.",
                config = getSpeechLanguageConfig()
            )
            return
        }

        // 3. Navigation: "Generate Notes" / "ନୋଟ୍ ତିଆରି କର" / "revision notes"
        val isNotesIntent = lower.startsWith("generate notes") || lower.startsWith("make notes") ||
                lower.startsWith("open notes") || lower.startsWith("ନୋଟ୍ ତିଆରି") || lower == "notes"
        if (isNotesIntent) {
            val route = "notes"
            _uiState.value = VoiceUiState.ActionExecuted(
                recognizedText = spokenText,
                actionTitle = "Generating Structured Revision Notes",
                actionOdia = "ପୁନରାବୃତ୍ତି ନୋଟ୍ ପ୍ରସ୍ତୁତ କରାଯାଉଛି...",
                targetRoute = route
            )
            speakUtterance(
                text = "Generating structured revision notes from curriculum cache.",
                config = getSpeechLanguageConfig()
            )
            return
        }

        // 4. Academic doubt / diagram request -> dynamic multimodal service
        onSpeechRecognized(spokenText)
    }

    fun onSpeechRecognized(spokenQuery: String) {
        _uiState.value = VoiceUiState.Thinking(
            recognizedQuery = spokenQuery,
            activeKeyIndex = geminiKeyRotator.getActiveKeyIndex()
        )

        scope.launch {
            val activeLanguage = languageManager.currentLanguage.value
            val config = getSpeechLanguageConfig()

            var currentGrade = "Class 9"
            var currentBoard = "CBSE / Odisha Board"
            val studyLevel = StudyLevel.INTERMEDIATE
            try {
                val profile = repository.getLearnerProfileFlow().first()
                currentGrade = "Class ${profile.classGrade}"
                currentBoard = profile.board.displayName
            } catch (_: Exception) {}

            // Check for image/diagram request intent first
            if (isDiagramRequest(spokenQuery)) {
                val diagramResult = fluxDiagramService.generateDiagram(
                    prompt = spokenQuery,
                    subjectContext = "$currentGrade $currentBoard Science",
                    language = activeLanguage
                )

                val imageBase64 = diagramResult.getOrNull()
                val caption = if (diagramResult.isSuccess && !imageBase64.isNullOrBlank()) {
                    when (activeLanguage) {
                        AppLanguage.ODIA_ONLY -> "ଏଠାରେ '$spokenQuery' ପାଇଁ ପାଠ୍ୟପୁସ୍ତକ ଚିତ୍ର ପ୍ରସ୍ତୁତ କରାଯାଇଛି:"
                        AppLanguage.HINDI_ONLY -> "यहाँ '$spokenQuery' के लिए पाठ्यपुस्तक का चित्र प्रस्तुत है:"
                        else -> "Here is the scientific textbook diagram for '$spokenQuery':"
                    }
                } else {
                    when (activeLanguage) {
                        AppLanguage.ODIA_ONLY -> "ଚିତ୍ର ପ୍ରସ୍ତୁତ କରିବାରେ ଅସୁବିଧା ହେଲା। ଦୟାକରି ପୁନର୍ବାର ଚେଷ୍ଟା କରନ୍ତୁ।"
                        AppLanguage.HINDI_ONLY -> "चित्र निर्माण में समस्या आई। कृपया पुनः प्रयास करें।"
                        else -> "Could not generate diagram at this moment. Please try again."
                    }
                }

                _uiState.value = VoiceUiState.Speaking(
                    query = spokenQuery,
                    answerText = caption,
                    textEnglish = caption,
                    textOdia = caption,
                    isPaused = false,
                    generatedDiagramBase64 = imageBase64
                )
                speakUtterance(text = caption, config = config)
            } else {
                // Dynamic Academic Solver via Gemini API with rotating keys
                val answerResult = geminiMultimodalService.solveTextQuery(
                    query = spokenQuery,
                    language = activeLanguage,
                    grade = currentGrade,
                    board = currentBoard,
                    studyLevel = studyLevel
                )

                answerResult.onSuccess { answerText ->
                    _uiState.value = VoiceUiState.Speaking(
                        query = spokenQuery,
                        answerText = answerText,
                        textEnglish = answerText,
                        textOdia = answerText,
                        isPaused = false
                    )
                    speakUtterance(text = answerText, config = config)
                }.onFailure { error ->
                    Log.w("VoiceAssistantManager", "Gemini solveTextQuery failed (${error.message}). Falling back to Room SQLite search.")
                    // Graceful offline fallback: ONLY use topic-matching keyword search in Room SQLite, NEVER a hardcoded string!
                    val localFallback = AppDatabase.getInstance(context).learningDao()
                        .searchLessonContent(spokenQuery) ?: "I could not find an answer offline. Please reconnect to cloud AI."
                    _uiState.value = VoiceUiState.Speaking(
                        query = spokenQuery,
                        answerText = localFallback,
                        textEnglish = localFallback,
                        textOdia = localFallback,
                        isPaused = false
                    )
                    speakUtterance(text = localFallback, config = config)
                }
            }
        }
    }

    // --- Text-To-Speech (TTS) Execution & Controls ---

    fun speak(textEnglish: String, textOdia: String) {
        val config = getSpeechLanguageConfig()
        val textToSpeak = if (isOdiaTtsSupported && textOdia.isNotBlank()) textOdia else textEnglish
        _uiState.value = VoiceUiState.Speaking(
            query = "",
            answerText = textToSpeak,
            textEnglish = textEnglish,
            textOdia = textOdia,
            isPaused = false
        )
        speakUtterance(textToSpeak, config)
    }

    private fun speakUtterance(text: String, config: SpeechLangConfig) {
        if (!isTtsReady || tts == null) return
        currentSpokenText = text
        applyTtsLocale(config)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "relaynet_voice_utterance")
    }

    fun pauseSpeaking() {
        tts?.stop()
        val current = _uiState.value
        if (current is VoiceUiState.Speaking) {
            _uiState.value = VoiceUiState.Paused(
                query = current.query,
                answerText = current.answerText,
                textEnglish = current.textEnglish,
                textOdia = current.textOdia
            )
        }
    }

    fun resumeSpeaking() {
        val current = _uiState.value
        if (current is VoiceUiState.Paused) {
            val config = getSpeechLanguageConfig()
            _uiState.value = VoiceUiState.Speaking(
                query = current.query,
                answerText = current.answerText,
                textEnglish = current.textEnglish,
                textOdia = current.textOdia,
                isPaused = false
            )
            speakUtterance(current.answerText, config)
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        val current = _uiState.value
        if (current is VoiceUiState.Speaking || current is VoiceUiState.Thinking || current is VoiceUiState.Paused) {
            _uiState.value = VoiceUiState.Idle
        }
    }

    fun dismiss() {
        stopListening()
        stopSpeaking()
        _uiState.value = VoiceUiState.Idle
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}
    }
}

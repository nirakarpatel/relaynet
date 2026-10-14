package com.nmiet.app.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.nmiet.app.domain.model.TutorSender
import com.nmiet.app.domain.repository.ContentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

sealed interface VoiceUiState {
    data object Idle : VoiceUiState
    data class Listening(val partialText: String = "", val soundLevelRms: Float = 0f) : VoiceUiState
    data class Processing(val recognizedText: String) : VoiceUiState
    data class ActionExecuted(
        val recognizedText: String,
        val actionTitle: String,
        val actionOdia: String,
        val targetRoute: String?
    ) : VoiceUiState
    data class Speaking(
        val textEnglish: String,
        val textOdia: String,
        val isPaused: Boolean = false
    ) : VoiceUiState
    data class Error(val errorMessage: String) : VoiceUiState
}

class VoiceAssistantManager(
    private val context: Context,
    private val repository: ContentRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job())
) : RecognitionListener, TextToSpeech.OnInitListener {

    private val _uiState = MutableStateFlow<VoiceUiState>(VoiceUiState.Idle)
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var isOdiaTtsSupported = false

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (_: Exception) {}
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            val odiaLocale = Locale("or", "IN")
            val langResult = tts?.isLanguageAvailable(odiaLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
            isOdiaTtsSupported = (langResult == TextToSpeech.LANG_AVAILABLE || langResult == TextToSpeech.LANG_COUNTRY_AVAILABLE)

            if (isOdiaTtsSupported) {
                tts?.language = odiaLocale
            } else {
                tts?.language = Locale("en", "IN")
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    scope.launch {
                        if (_uiState.value is VoiceUiState.Speaking) {
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

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "or-IN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "or-IN")
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-IN", "en-US"))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
            _uiState.value = VoiceUiState.Listening(partialText = "Listening in Odia & English...", soundLevelRms = 0f)
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
        _uiState.value = VoiceUiState.Listening(partialText = "Listening...", soundLevelRms = 0f)
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
            _uiState.value = VoiceUiState.Listening(partialText = text)
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val query = matches?.firstOrNull() ?: ""
        if (query.isNotBlank()) {
            _uiState.value = VoiceUiState.Processing(recognizedText = query)
            parseVoiceIntent(query)
        } else {
            _uiState.value = VoiceUiState.Idle
        }
    }

    override fun onError(error: Int) {
        val errorMsg = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network offline: using regional keyword match"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak again."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice recognizer is busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
            else -> "Voice recognition error ($error)"
        }
        _uiState.value = VoiceUiState.Error(errorMsg)
    }

    override fun onBeginningOfSpeech() {}
    override fun onEndOfSpeech() {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    // --- Voice Intent Parser (Command & Control) ---

    fun parseVoiceIntent(spokenText: String) {
        val lower = spokenText.lowercase().trim()

        // 1. "Open Photosynthesis" / "ଆଲୋକ ଶ୍ଳେଷଣ ଖୋଲ"
        if (lower.contains("photosynthesis") || lower.contains("ଆଲୋକ ଶ୍ଳେଷଣ") ||
            lower.contains("open lesson") || lower.contains("ପାଠ ଖୋଲ") || lower.contains("chapter 4")
        ) {
            val route = "lesson/relaynet_class9_science_photosynthesis_v1"
            _uiState.value = VoiceUiState.ActionExecuted(
                recognizedText = spokenText,
                actionTitle = "Opening Lesson: Chapter 4 Photosynthesis",
                actionOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ଅଧ୍ୟାୟ ଖୋଲାଯାଉଛି...",
                targetRoute = route
            )
            speak(
                textEnglish = "Opening Chapter 4 Photosynthesis lesson reader.",
                textOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ଅଧ୍ୟାୟ ଖୋଲାଯାଉଛି।"
            )
            return
        }

        // 2. "Start Quiz" / "କୁଇଜ୍ ଆରମ୍ଭ କର" / "test"
        if (lower.contains("quiz") || lower.contains("କୁଇଜ") || lower.contains("test") ||
            lower.contains("start quiz") || lower.contains("ପରୀକ୍ଷା")
        ) {
            val route = "quiz/relaynet_class9_science_photosynthesis_v1"
            _uiState.value = VoiceUiState.ActionExecuted(
                recognizedText = spokenText,
                actionTitle = "Launching Diagnostic Quiz",
                actionOdia = "ଡାଇଗ୍ନୋଷ୍ଟିକ୍ କୁଇଜ୍ ଆରମ୍ଭ କରାଯାଉଛି...",
                targetRoute = route
            )
            speak(
                textEnglish = "Launching diagnostic self-assessment quiz for Chapter 4.",
                textOdia = "ଆପଣଙ୍କର ଅଭ୍ୟାସ କୁଇଜ୍ ଆରମ୍ଭ ହେଉଛି।"
            )
            return
        }

        // 3. "Generate Notes" / "ନୋଟ୍ ତିଆରି କର" / "revision notes"
        if (lower.contains("notes") || lower.contains("ନୋଟ") || lower.contains("revision") ||
            lower.contains("generate note") || lower.contains("ସାରାଂଶ")
        ) {
            val route = "notes"
            _uiState.value = VoiceUiState.ActionExecuted(
                recognizedText = spokenText,
                actionTitle = "Generating Structured Revision Notes",
                actionOdia = "ପୁନରାବୃତ୍ତି ନୋଟ୍ ପ୍ରସ୍ତୁତ କରାଯାଉଛି...",
                targetRoute = route
            )
            speak(
                textEnglish = "Generating structured revision notes from curriculum cache.",
                textOdia = "ଅଧ୍ୟାୟର ପୁନରାବୃତ୍ତି ନୋଟ୍ ପ୍ରସ୍ତୁତ କରାଯାଉଛି।"
            )
            return
        }

        // 4. Spoken Academic Question -> Route directly to AI Tutor
        scope.launch {
            _uiState.value = VoiceUiState.Processing("Consulting RelayNet AI Tutor for academic explanation...")
            val response = repository.askTutor(questionText = spokenText)
            val odiaText = response.textOdia
            val engText = response.textEnglish

            _uiState.value = VoiceUiState.Speaking(
                textEnglish = engText,
                textOdia = odiaText
            )
            speak(textEnglish = engText, textOdia = odiaText)
        }
    }

    // --- Text-To-Speech (TTS) Execution ---

    fun speak(textEnglish: String, textOdia: String) {
        if (!isTtsReady || tts == null) return

        _uiState.value = VoiceUiState.Speaking(
            textEnglish = textEnglish,
            textOdia = textOdia,
            isPaused = false
        )

        val spokenUtterance = if (isOdiaTtsSupported && textOdia.isNotBlank()) textOdia else textEnglish
        tts?.speak(spokenUtterance, TextToSpeech.QUEUE_FLUSH, null, "voice_assistant_utterance")
    }

    fun pauseSpeaking() {
        tts?.stop()
        val current = _uiState.value
        if (current is VoiceUiState.Speaking) {
            _uiState.value = current.copy(isPaused = true)
        }
    }

    fun resumeSpeaking() {
        val current = _uiState.value
        if (current is VoiceUiState.Speaking) {
            speak(current.textEnglish, current.textOdia)
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        if (_uiState.value is VoiceUiState.Speaking) {
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

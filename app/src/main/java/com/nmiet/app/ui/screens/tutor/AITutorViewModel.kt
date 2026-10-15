package com.nmiet.app.ui.screens.tutor

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.data.local.LanguageManager
import com.nmiet.app.data.remote.GeminiImageService
import com.nmiet.app.data.remote.GeminiKeyRotator
import com.nmiet.app.data.remote.GeminiMultimodalService
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.ContentType
import com.nmiet.app.domain.model.MessageSender
import com.nmiet.app.domain.model.StudyLevel
import com.nmiet.app.domain.model.SyncStatus
import com.nmiet.app.domain.model.TutorChatMessage
import com.nmiet.app.domain.model.TutorMessage
import com.nmiet.app.domain.model.TutorSender
import com.nmiet.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

data class TutorUiState(
    val isGenerating: Boolean = false,
    val statusText: String = "Generating..."
)

class AITutorViewModel(
    private val repository: ContentRepository,
    val languageManager: LanguageManager? = null
) : ViewModel() {

    private val geminiService = GeminiMultimodalService()
    private val geminiImageService = GeminiImageService()

    private val _uiState = MutableStateFlow(TutorUiState())
    val uiState: StateFlow<TutorUiState> = _uiState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<TutorChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<TutorChatMessage>> = _chatMessages.asStateFlow()

    private var lastSentMessage: String? = null
    private var lastSentTime: Long = 0L

    val currentLanguage: StateFlow<AppLanguage> = languageManager?.currentLanguage
        ?: repository.getLearnerProfileFlow()
            .map { it.preferredLanguage }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.BILINGUAL_EN_OR)

    val messages: StateFlow<List<TutorMessage>> = repository.getTutorMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val dbMessages = repository.getTutorMessagesFlow().first()
            if (dbMessages.isNotEmpty() && _chatMessages.value.isEmpty()) {
                _chatMessages.value = dbMessages.map { msg ->
                    val resolvedText = when (currentLanguage.value) {
                        AppLanguage.ENGLISH_ONLY -> msg.textEnglish.ifBlank { msg.textOdia }
                        AppLanguage.ODIA_ONLY -> msg.textOdia.ifBlank { msg.textEnglish }
                        AppLanguage.HINDI_ONLY -> msg.textHindi.ifBlank { msg.textEnglish }
                        else -> msg.textEnglish.ifBlank { msg.textOdia }.ifBlank { msg.textHindi }
                    }
                    TutorChatMessage(
                        id = msg.id,
                        sender = if (msg.sender == TutorSender.USER) MessageSender.USER else MessageSender.TUTOR,
                        text = resolvedText,
                        userUploadedImageUri = null,
                        generatedDiagramBase64 = msg.imageBase64,
                        sourceTag = if (msg.sender == TutorSender.USER) "" else "[Cloud AI]",
                        timestamp = msg.timestamp,
                        imageBitmap = msg.imageBitmap
                    )
                }
            }
        }
    }

    val syncStatus: StateFlow<SyncStatus> = repository.getSyncStatusFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus())

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _attachedImage = MutableStateFlow<Bitmap?>(null)
    val attachedImage: StateFlow<Bitmap?> = _attachedImage.asStateFlow()

    private val _attachedImageUri = MutableStateFlow<Uri?>(null)
    val attachedImageUri: StateFlow<Uri?> = _attachedImageUri.asStateFlow()

    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isGeneratingVisual = MutableStateFlow(false)
    val isGeneratingVisual: StateFlow<Boolean> = _isGeneratingVisual.asStateFlow()

    private val _isScanningImage = MutableStateFlow(false)
    val isScanningImage: StateFlow<Boolean> = _isScanningImage.asStateFlow()

    private val _offlineNotice = MutableStateFlow<String?>(null)
    val offlineNotice: StateFlow<String?> = _offlineNotice.asStateFlow()

    // TextToSpeech State
    private var tts: TextToSpeech? = null
    private val _isTtsSpeaking = MutableStateFlow(false)
    val isTtsSpeaking: StateFlow<Boolean> = _isTtsSpeaking.asStateFlow()

    private val _isTtsPaused = MutableStateFlow(false)
    val isTtsPaused: StateFlow<Boolean> = _isTtsPaused.asStateFlow()

    private val _currentlySpeakingMessageId = MutableStateFlow<String?>(null)
    val currentlySpeakingMessageId: StateFlow<String?> = _currentlySpeakingMessageId.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun onImageCaptured(bitmap: Bitmap?, uri: Uri? = null) {
        _attachedImage.value = bitmap
        _attachedImageUri.value = uri
    }

    fun onImageUriSelected(context: Context, uri: Uri?) {
        if (uri == null) return
        _attachedImageUri.value = uri
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                _attachedImage.value = bitmap
            }
        } catch (_: Exception) {}
    }

    fun clearAttachedImage() {
        _attachedImage.value = null
        _attachedImageUri.value = null
    }

    fun clearOfflineNotice() {
        _offlineNotice.value = null
    }

    // 1. USER MESSAGE DISPATCH & DEBOUNCE (Precaution against accidental duplicate dispatches)
    fun sendUserMessage(text: String) {
        val trimmed = text.trim()
        val currentTime = System.currentTimeMillis()
        if (trimmed.isEmpty() || (trimmed == lastSentMessage && currentTime - lastSentTime < 1000)) {
            return // Ignore accidental double clicks / duplicate triggers
        }
        lastSentMessage = trimmed
        lastSentTime = currentTime
        _inputText.value = ""

        // 1. Append user message ONCE
        val userMsg = TutorChatMessage(
            sender = MessageSender.USER,
            text = trimmed,
            sourceTag = "" // Do not show "[Cloud AI] RelayNet Tutor" on the USER bubble!
        )
        _chatMessages.update { it + userMsg }

        // Persist to Room SQLite
        viewModelScope.launch {
            repository.saveTutorMessage(
                TutorMessage(
                    id = userMsg.id,
                    sender = TutorSender.USER,
                    textOdia = trimmed,
                    textEnglish = trimmed,
                    timestamp = userMsg.timestamp
                )
            )
        }

        // 2. Process query
        processQuery(trimmed)
    }

    // 2. QUERY PROCESSOR & DIAGRAM INTENT ROUTER
    private fun processQuery(query: String) {
        val isImageReq = query.lowercase().let { 
            it.contains("diagram") || it.contains("image") || it.contains("draw") || 
            it.contains("structure") || it.contains("flowchart") ||
            it.contains("चित्र") || it.contains("ଚିତ୍ର")
        }

        if (isImageReq) {
            viewModelScope.launch {
                _uiState.update { it.copy(isGenerating = true, statusText = "Generating...") }
                _isGenerating.value = true
                _isGeneratingVisual.value = true

                val result = geminiImageService.generateEducationalDiagram(query)
                result.onSuccess { base64Data ->
                    _uiState.update { it.copy(isGenerating = false) }
                    _isGenerating.value = false
                    _isGeneratingVisual.value = false
                    val tutorMsg = TutorChatMessage(
                        sender = MessageSender.TUTOR,
                        text = "Here is the visual diagram for $query:",
                        generatedDiagramBase64 = base64Data,
                        sourceTag = "[Cloud AI]"
                    )
                    _chatMessages.update { list -> list + tutorMsg }
                    repository.saveTutorMessage(
                        TutorMessage(
                            id = tutorMsg.id,
                            sender = TutorSender.TUTOR,
                            textOdia = tutorMsg.text ?: "",
                            textEnglish = tutorMsg.text ?: "",
                            textHindi = tutorMsg.text ?: "",
                            timestamp = tutorMsg.timestamp,
                            isStudyRelated = true,
                            isCloudAI = true,
                            imageBase64 = base64Data
                        )
                    )
                }.onFailure { _ ->
                    _uiState.update { it.copy(isGenerating = false) }
                    _isGenerating.value = false
                    _isGeneratingVisual.value = false
                    val tutorMsg = TutorChatMessage(
                        sender = MessageSender.TUTOR,
                        text = "Could not generate diagram at this moment. Please check your connectivity and try again.",
                        sourceTag = "[Cloud AI]"
                    )
                    _chatMessages.update { list -> list + tutorMsg }
                    repository.saveTutorMessage(
                        TutorMessage(
                            id = tutorMsg.id,
                            sender = TutorSender.TUTOR,
                            textOdia = tutorMsg.text ?: "",
                            textEnglish = tutorMsg.text ?: "",
                            textHindi = tutorMsg.text ?: "",
                            timestamp = tutorMsg.timestamp,
                            isStudyRelated = true,
                            isCloudAI = true
                        )
                    )
                }
            }
        } else {
            viewModelScope.launch {
                _uiState.update { it.copy(isGenerating = true, statusText = "Generating...") }
                _isGenerating.value = true
                try {
                    val learnerProfile = repository.getLearnerProfileFlow().first()
                    val grade = "Class ${learnerProfile.classGrade}"
                    val board = learnerProfile.board.displayName

                    val result = geminiService.solveTextQuery(
                        query = query,
                        language = currentLanguage.value,
                        grade = grade,
                        board = board,
                        studyLevel = StudyLevel.INTERMEDIATE
                    )

                    val explanation = if (result.isSuccess) {
                        result.getOrNull() ?: ""
                    } else {
                        val langCode = when (currentLanguage.value) {
                            AppLanguage.ENGLISH_ONLY -> "english"
                            AppLanguage.ODIA_ONLY -> "odia"
                            AppLanguage.HINDI_ONLY -> "hindi"
                            AppLanguage.BILINGUAL_EN_HI -> "bilingual_hi"
                            AppLanguage.BILINGUAL_EN_OR -> "bilingual_or"
                        }
                        val localMsg = repository.askTutor(
                            questionText = query,
                            imageBitmap = null,
                            language = langCode,
                            insertUserMessage = false
                        )
                        localMsg.getPrimaryText(currentLanguage.value)
                    }

                    val tutorMsg = TutorChatMessage(
                        sender = MessageSender.TUTOR,
                        text = explanation,
                        sourceTag = "[Cloud AI]"
                    )
                    _chatMessages.update { list -> list + tutorMsg }
                    repository.saveTutorMessage(
                        TutorMessage(
                            id = tutorMsg.id,
                            sender = TutorSender.TUTOR,
                            textOdia = explanation,
                            textEnglish = explanation,
                            textHindi = explanation,
                            timestamp = tutorMsg.timestamp,
                            isCloudAI = true,
                            isStudyRelated = true
                        )
                    )
                    speakResponse(explanation, messageId = tutorMsg.id)
                } catch (e: Exception) {
                    val errorMsg = TutorChatMessage(
                        sender = MessageSender.TUTOR,
                        text = "Could not process your question at this moment. Please check your connectivity and try again.",
                        sourceTag = "[Cloud AI]"
                    )
                    _chatMessages.update { list -> list + errorMsg }
                } finally {
                    _uiState.update { it.copy(isGenerating = false) }
                    _isGenerating.value = false
                }
            }
        }
    }

    // 3. PHOTO "SCAN & SOLVE"
    fun sendImageQuery(bitmap: Bitmap, caption: String? = null) {
        val promptText = caption?.trim()?.ifBlank { null } ?: "Analyze and solve this textbook problem / diagram step-by-step."
        _attachedImage.value = null
        _attachedImageUri.value = null
        _inputText.value = ""

        val userMsg = TutorChatMessage(
            sender = MessageSender.USER,
            text = promptText,
            sourceTag = "",
            imageBitmap = bitmap
        )
        _chatMessages.update { it + userMsg }

        viewModelScope.launch {
            repository.saveTutorMessage(
                TutorMessage(
                    id = userMsg.id,
                    sender = TutorSender.USER,
                    textOdia = promptText,
                    textEnglish = promptText,
                    timestamp = userMsg.timestamp,
                    imageBitmap = bitmap
                )
            )

            _uiState.update { it.copy(isGenerating = true, statusText = "Generating...") }
            _isGenerating.value = true
            _isScanningImage.value = true
            try {
                val learnerProfile = repository.getLearnerProfileFlow().first()
                val grade = "Class ${learnerProfile.classGrade}"
                val board = learnerProfile.board.displayName

                val result = geminiService.solveImageProblem(
                    bitmap = bitmap,
                    caption = promptText,
                    language = currentLanguage.value,
                    grade = grade,
                    board = board,
                    studyLevel = StudyLevel.INTERMEDIATE
                )

                val solution = if (result.isSuccess) {
                    result.getOrNull() ?: ""
                } else {
                    val langCode = when (currentLanguage.value) {
                        AppLanguage.ENGLISH_ONLY -> "en"
                        AppLanguage.ODIA_ONLY -> "or"
                        AppLanguage.HINDI_ONLY -> "hi"
                        AppLanguage.BILINGUAL_EN_HI -> "en_hi"
                        AppLanguage.BILINGUAL_EN_OR -> "en_or"
                    }
                    val fallback = repository.scanAndSolveTextbook(
                        imageBitmap = bitmap,
                        userPrompt = promptText,
                        language = langCode,
                        insertUserMessage = false
                    )
                    fallback.getPrimaryText(currentLanguage.value)
                }

                val tutorMsg = TutorChatMessage(
                    sender = MessageSender.TUTOR,
                    text = solution,
                    sourceTag = "[Cloud AI]"
                )
                _chatMessages.update { list -> list + tutorMsg }
                repository.saveTutorMessage(
                    TutorMessage(
                        id = tutorMsg.id,
                        sender = TutorSender.TUTOR,
                        textOdia = solution,
                        textEnglish = solution,
                        textHindi = solution,
                        timestamp = tutorMsg.timestamp,
                        isCloudAI = true,
                        isStudyRelated = true
                    )
                )
                speakResponse(solution, messageId = tutorMsg.id)
            } catch (e: Exception) {
                val errorMsg = TutorChatMessage(
                    sender = MessageSender.TUTOR,
                    text = "Could not analyze the image at this moment. Please check your connectivity and try again.",
                    sourceTag = "[Cloud AI]"
                )
                _chatMessages.update { list -> list + errorMsg }
            } finally {
                _uiState.update { it.copy(isGenerating = false) }
                _isScanningImage.value = false
                _isGenerating.value = false
            }
        }
    }

    fun sendTextMessage(query: String) {
        sendUserMessage(query)
    }

    fun sendDiagramQuery(prompt: String) {
        sendUserMessage(prompt)
    }

    fun requestVisualDiagram(prompt: String) {
        sendUserMessage(prompt)
    }

    fun sendMessage(text: String = _inputText.value) {
        val query = text.trim()
        val image = _attachedImage.value
        if (query.isBlank() && image == null) return

        // 1. Photo Query
        if (image != null) {
            sendImageQuery(image, if (query.isNotBlank()) query else null)
            return
        }

        // 2. Delegate to sendUserMessage
        sendUserMessage(query)
    }

    fun isDiagramRequest(query: String): Boolean {
        val q = query.lowercase().trim()
        return q.contains("diagram") || q.contains("flowchart") || 
               q.contains("draw") || q.contains("generate image") || 
               q.contains("picture of") || q.contains("bond structure") ||
               q.contains("structure of") || q.contains("चित्र") || q.contains("ଚିତ୍ର")
    }

    // VOICE TALK: SpeechRecognizer Integration
    fun startVoiceRecognition(context: Context) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            simulateVoiceInput()
            return
        }

        try {
            stopVoiceRecognition()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)?.apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isRecordingVoice.value = true
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isRecordingVoice.value = false
                    }
                    override fun onError(error: Int) {
                        _isRecordingVoice.value = false
                    }
                    override fun onResults(results: Bundle?) {
                        _isRecordingVoice.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val spokenText = matches[0]
                            _inputText.value = spokenText
                            sendMessage(spokenText)
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _inputText.value = matches[0]
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            if (speechRecognizer == null) {
                simulateVoiceInput()
                return
            }

            val langTag = when (currentLanguage.value) {
                AppLanguage.ODIA_ONLY, AppLanguage.BILINGUAL_EN_OR -> "or-IN"
                AppLanguage.HINDI_ONLY, AppLanguage.BILINGUAL_EN_HI -> "hi-IN"
                AppLanguage.ENGLISH_ONLY -> "en-IN"
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, langTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)
        } catch (t: Throwable) {
            Log.e("AITutorViewModel", "Speech recognition failed: ${t.message}", t)
            _isRecordingVoice.value = false
            simulateVoiceInput()
        }
    }

    fun stopVoiceRecognition() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Throwable) {}
        speechRecognizer = null
        _isRecordingVoice.value = false
    }

    fun toggleVoiceRecognition(context: Context) {
        if (_isRecordingVoice.value) {
            stopVoiceRecognition()
        } else {
            startVoiceRecognition(context)
        }
    }

    fun onVoiceResultReceived(recognizedText: String) {
        if (recognizedText.isNotBlank()) {
            _inputText.value = recognizedText
        }
    }

    // TTS Output
    fun initTts(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val loc = when (currentLanguage.value) {
                        AppLanguage.ODIA_ONLY, AppLanguage.BILINGUAL_EN_OR -> Locale("or", "IN")
                        AppLanguage.HINDI_ONLY, AppLanguage.BILINGUAL_EN_HI -> Locale("hi", "IN")
                        AppLanguage.ENGLISH_ONLY -> Locale("en", "IN")
                    }
                    if (tts?.isLanguageAvailable(loc) == TextToSpeech.LANG_AVAILABLE) {
                        tts?.language = loc
                    } else {
                        tts?.language = Locale("en", "IN")
                    }
                    tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isTtsSpeaking.value = true
                            _isTtsPaused.value = false
                        }
                        override fun onDone(utteranceId: String?) {
                            _isTtsSpeaking.value = false
                            _isTtsPaused.value = false
                            _currentlySpeakingMessageId.value = null
                        }
                        override fun onError(utteranceId: String?) {
                            _isTtsSpeaking.value = false
                            _isTtsPaused.value = false
                            _currentlySpeakingMessageId.value = null
                        }
                    })
                }
            }
        }
    }

    fun speakResponse(text: String, context: Context? = null, messageId: String? = null) {
        if (context != null && tts == null) {
            initTts(context)
        }
        val clean = text
            .replace("**", "")
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .replace("_", "")
            .trim()
        _currentlySpeakingMessageId.value = messageId
        _isTtsSpeaking.value = true
        _isTtsPaused.value = false
        val utteranceId = messageId ?: "tutor_tts_${System.currentTimeMillis()}"
        tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun toggleSpeakMessage(messageId: String, text: String, context: Context? = null) {
        if (_currentlySpeakingMessageId.value == messageId && _isTtsSpeaking.value) {
            pauseTts()
        } else if (_currentlySpeakingMessageId.value == messageId && _isTtsPaused.value) {
            speakResponse(text, context, messageId)
        } else {
            speakResponse(text, context, messageId)
        }
    }

    fun pauseTts() {
        tts?.stop()
        _isTtsSpeaking.value = false
        _isTtsPaused.value = true
    }

    fun stopTts() {
        tts?.stop()
        _isTtsSpeaking.value = false
        _isTtsPaused.value = false
        _currentlySpeakingMessageId.value = null
    }

    fun simulateVoiceInput() {
        _isRecordingVoice.value = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            val demoQuery = when (currentLanguage.value) {
                AppLanguage.ODIA_ONLY -> "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟାରେ ଅମ୍ଳଜାନ କେଉଁଠାରୁ ବାହାରେ?"
                AppLanguage.HINDI_ONLY -> "प्रकाश संश्लेषण में ऑक्सीजन कहाँ से निकलती है?"
                else -> "Where does Oxygen come from in photosynthesis?"
            }
            _inputText.value = demoQuery
            _isRecordingVoice.value = false
        }
    }

    fun attachSampleTextbookPhoto() {
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(46, 125, 50)
            isAntiAlias = true
        }
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawCircle(100f, 100f, 60f, paint)
        paint.color = android.graphics.Color.rgb(255, 193, 7)
        canvas.drawCircle(100f, 100f, 30f, paint)
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 12f
        canvas.drawText("Chloroplast Structure", 20f, 180f, paint)
        _attachedImage.value = bitmap
        _inputText.value = "Explain this chloroplast thylakoid diagram step-by-step"
    }

    override fun onCleared() {
        super.onCleared()
        stopVoiceRecognition()
        tts?.stop()
        tts?.shutdown()
    }
}

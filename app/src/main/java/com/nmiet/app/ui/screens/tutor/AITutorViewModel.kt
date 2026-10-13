package com.nmiet.app.ui.screens.tutor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.domain.model.SyncStatus
import com.nmiet.app.domain.model.TutorMessage
import com.nmiet.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AITutorViewModel(
    private val repository: ContentRepository
) : ViewModel() {

    val messages: StateFlow<List<TutorMessage>> = repository.getTutorMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncStatus: StateFlow<SyncStatus> = repository.getSyncStatusFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus())

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _attachedImage = MutableStateFlow<Bitmap?>(null)
    val attachedImage: StateFlow<Bitmap?> = _attachedImage.asStateFlow()

    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun onImageCaptured(bitmap: Bitmap?) {
        _attachedImage.value = bitmap
    }

    fun onImageUriSelected(context: Context, uri: Uri?) {
        if (uri == null) return
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                _attachedImage.value = bitmap
            }
        } catch (_: Exception) {}
    }

    fun clearAttachedImage() {
        _attachedImage.value = null
    }

    fun sendMessage(text: String = _inputText.value) {
        val query = text.trim()
        val image = _attachedImage.value
        if (query.isBlank() && image == null) return

        val sendQuery = if (query.isNotBlank()) query else "Analyze and solve this textbook problem / diagram."
        _inputText.value = ""
        _attachedImage.value = null

        viewModelScope.launch {
            _isGenerating.value = true
            repository.askTutor(questionText = sendQuery, imageBitmap = image)
            _isGenerating.value = false
        }
    }

    fun simulateVoiceInput() {
        _isRecordingVoice.value = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            _inputText.value = "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟାରେ ଅମ୍ଳଜାନ କେଉଁଠାରୁ ବାହାରେ?"
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
        _inputText.value = "Explain this chloroplast thylakoid diagram"
    }
}

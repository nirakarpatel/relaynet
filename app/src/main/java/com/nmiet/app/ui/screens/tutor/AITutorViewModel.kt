package com.nmiet.app.ui.screens.tutor

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

    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun sendMessage(text: String = _inputText.value) {
        if (text.isBlank()) return
        val query = text.trim()
        _inputText.value = ""

        viewModelScope.launch {
            _isGenerating.value = true
            repository.askTutor(query)
            _isGenerating.value = false
        }
    }

    fun simulateVoiceInput() {
        // Voice workflow from blueprint: Tap microphone -> speech-to-text transcript shown for correction
        _isRecordingVoice.value = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            _inputText.value = "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟାରେ ଅମ୍ଳଜାନ କେଉଁଠାରୁ ବାହାରେ?"
            _isRecordingVoice.value = false
        }
    }
}

package com.nmiet.app.ui.screens.playground

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nmiet.app.ai.llm.DeviceMemoryGuidance
import com.nmiet.app.ai.llm.LocalLlmEngine
import com.nmiet.app.ai.llm.LocalLlmState
import com.nmiet.app.ai.llm.LocalModelInfo
import com.nmiet.app.ai.llm.LocalModelManager
import com.nmiet.app.ai.llm.LocalModelRegistry
import com.nmiet.app.ai.llm.ModelInstallationState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class PlaygroundMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val tokenCount: Int? = null,
    val tokensPerSec: Float? = null,
    val latencyMs: Long? = null,
    val isLocalInference: Boolean = true
)

class LocalAiPlaygroundViewModel(
    val modelManager: LocalModelManager,
    val engine: LocalLlmEngine
) : ViewModel() {

    private val _selectedModel = MutableStateFlow<LocalModelInfo>(LocalModelRegistry.QWEN3_0_6B_NOTHINK)
    val selectedModel: StateFlow<LocalModelInfo> = _selectedModel.asStateFlow()

    val installState: StateFlow<ModelInstallationState> = modelManager.installState
    val engineState: StateFlow<LocalLlmState> = engine.state

    private val _memoryGuidance = MutableStateFlow(modelManager.getDeviceMemoryGuidance())
    val memoryGuidance: StateFlow<DeviceMemoryGuidance> = _memoryGuidance.asStateFlow()

    private val _promptInput = MutableStateFlow("")
    val promptInput: StateFlow<String> = _promptInput.asStateFlow()

    private val _messages = MutableStateFlow<List<PlaygroundMessage>>(emptyList())
    val messages: StateFlow<List<PlaygroundMessage>> = _messages.asStateFlow()

    private var activeStreamingJob: Job? = null
    private var downloadJob: Job? = null

    val isModelLoaded: Boolean
        get() = engine.isModelLoaded()

    fun updatePromptInput(text: String) {
        _promptInput.value = text
    }

    fun selectTestPrompt(preset: String) {
        _promptInput.value = preset
    }

    fun startModelDownload() {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            modelManager.downloadAndInstallModel(_selectedModel.value)
        }
    }

    fun cancelModelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        modelManager.cancelDownload(_selectedModel.value)
    }

    fun importLocalModelFile(file: File) {
        viewModelScope.launch {
            modelManager.importModelFromFile(file, _selectedModel.value)
        }
    }

    fun deleteModel() {
        viewModelScope.launch {
            if (engine.isModelLoaded()) {
                engine.unloadModel()
            }
            modelManager.deleteModel(_selectedModel.value)
        }
    }

    fun loadModel() {
        val file = modelManager.getInstalledModelFile(_selectedModel.value)
        viewModelScope.launch {
            engine.loadModel(file, _selectedModel.value)
        }
    }

    fun unloadModel() {
        viewModelScope.launch {
            engine.unloadModel()
        }
    }

    fun sendPrompt(userPrompt: String = _promptInput.value) {
        val trimmed = userPrompt.trim()
        if (trimmed.isBlank() || !engine.isModelLoaded()) return

        val userMsg = PlaygroundMessage(isUser = true, text = trimmed)
        _messages.value = _messages.value + userMsg
        _promptInput.value = ""

        val botMsgId = java.util.UUID.randomUUID().toString()
        val botPlaceholder = PlaygroundMessage(
            id = botMsgId,
            isUser = false,
            text = "⏳ Generating on-device tokens...",
            isLocalInference = true
        )
        _messages.value = _messages.value + botPlaceholder

        activeStreamingJob?.cancel()
        activeStreamingJob = viewModelScope.launch {
            val responseBuilder = StringBuilder()
            val startMs = System.currentTimeMillis()
            var tokenCount = 0

            try {
                engine.generateStream(trimmed).collect { chunk ->
                    tokenCount++
                    responseBuilder.append(chunk)

                    val elapsedSec = (System.currentTimeMillis() - startMs) / 1000f
                    val speed = if (elapsedSec > 0) tokenCount / elapsedSec else null

                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == botMsgId) {
                            msg.copy(
                                text = responseBuilder.toString(),
                                tokenCount = tokenCount,
                                tokensPerSec = speed,
                                latencyMs = System.currentTimeMillis() - startMs
                            )
                        } else msg
                    }
                }
            } catch (e: Exception) {
                if (responseBuilder.isEmpty()) {
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == botMsgId) {
                            msg.copy(text = "[Inference cancelled or failed: ${e.message}]")
                        } else msg
                    }
                }
            }
        }
    }

    fun cancelActiveGeneration() {
        activeStreamingJob?.cancel()
        activeStreamingJob = null
        engine.cancelGeneration()
    }

    fun clearChat() {
        _messages.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveGeneration()
        cancelModelDownload()
    }
}

class LocalAiPlaygroundViewModelFactory(
    private val modelManager: LocalModelManager,
    private val engine: LocalLlmEngine
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocalAiPlaygroundViewModel(modelManager, engine) as T
    }
}

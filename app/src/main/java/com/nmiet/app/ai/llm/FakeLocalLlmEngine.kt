package com.nmiet.app.ai.llm

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import java.io.File

/**
 * Deterministic Local LLM engine for testing and smoke verification without native binaries.
 */
class FakeLocalLlmEngine(
    private val simulateDelay: Boolean = false,
    private val tokensPerDelayMs: Long = 20L
) : LocalLlmEngine {

    private val _state = MutableStateFlow<LocalLlmState>(LocalLlmState.Unloaded)
    override val state: StateFlow<LocalLlmState> = _state.asStateFlow()

    private var loadedModel: LocalModelInfo? = null
    var shouldFailLoad: Boolean = false
    var failureMessage: String = "Simulated engine load failure"
    var cannedResponse: String? = null

    override fun isModelLoaded(): Boolean = loadedModel != null && _state.value is LocalLlmState.Loaded

    override fun getLoadedModelInfo(): LocalModelInfo? = loadedModel

    override suspend fun loadModel(modelFile: File, modelInfo: LocalModelInfo): Result<Unit> {
        if (!modelFile.exists()) {
            val err = "Model file not found: ${modelFile.absolutePath}"
            _state.value = LocalLlmState.Error(err, canRetry = false)
            return Result.failure(IllegalArgumentException(err))
        }

        if (shouldFailLoad) {
            _state.value = LocalLlmState.Error(failureMessage, canRetry = true)
            return Result.failure(IllegalStateException(failureMessage))
        }

        _state.value = LocalLlmState.Loading(modelInfo.displayName)
        if (simulateDelay) delay(50)

        loadedModel = modelInfo
        _state.value = LocalLlmState.Loaded(modelInfo, loadDurationMs = 120L)
        return Result.success(Unit)
    }

    override fun generateStream(prompt: String): Flow<String> = flow {
        val model = loadedModel
        if (model == null) {
            emit("Error: No on-device model is loaded.")
            _state.value = LocalLlmState.Error("No model loaded", canRetry = false)
            return@flow
        }

        val textToEmit = cannedResponse ?: generateResponseForPrompt(prompt)
        val chunks = textToEmit.split(" ").map { "$it " }

        val startTime = System.currentTimeMillis()
        val builder = StringBuilder()
        var tokenCount = 0

        _state.value = LocalLlmState.Generating(prompt, "", 0)

        try {
            for (chunk in chunks) {
                if (simulateDelay) delay(tokensPerDelayMs)
                tokenCount++
                builder.append(chunk)

                val elapsed = (System.currentTimeMillis() - startTime) / 1000f
                val speed = if (elapsed > 0) tokenCount / elapsed else 25.0f

                _state.value = LocalLlmState.Generating(
                    prompt = prompt,
                    streamedResponse = builder.toString(),
                    tokenCount = tokenCount,
                    timeToFirstTokenMs = 15L,
                    speedTokensPerSec = speed
                )
                emit(chunk)
            }
            _state.value = LocalLlmState.Loaded(model, loadDurationMs = 0L)
        } catch (e: CancellationException) {
            _state.value = LocalLlmState.Loaded(model, loadDurationMs = 0L)
            throw e
        }
    }

    private fun generateResponseForPrompt(prompt: String): String {
        return when {
            prompt.contains("photosynthesis", ignoreCase = true) ->
                "Photosynthesis is the process by which green plants transform sunlight, water (H₂O), and carbon dioxide (CO₂) into glucose (C₆H₁₂O₆) and oxygen (O₂)."
            prompt.contains("ଆଲୋକ ଶ୍ଳେଷଣ", ignoreCase = true) ->
                "ଆଲୋକ ଶ୍ଳେଷଣ ହେଉଛି ସେହି ଜୈବ ରାସାୟନିକ ପ୍ରକ୍ରିୟା ଯାହା ଦ୍ୱାରା ସବୁଜ ଉଦ୍ଭିଦ ସୂର୍ଯ୍ୟାଲୋକ ଏବଂ କ୍ଲୋରୋଫିଲ ସାହାଯ୍ୟରେ ଶର୍କରା ପ୍ରସ୍ତୁତ କରେ।"
            prompt.contains("प्रकाश संश्लेषण", ignoreCase = true) ->
                "प्रकाश संश्लेषण वह प्रक्रिया है जिसके द्वारा हरे पौधे सूर्य के प्रकाश, जल और कार्बन डाइऑक्साइड का उपयोग करके अपना भोजन तैयार करते हैं।"
            else ->
                "Local Qwen3 0.6B response: Processing educational prompt on-device without remote servers. Offline verification successful."
        }
    }

    override fun cancelGeneration() {
        val model = loadedModel
        if (model != null) {
            _state.value = LocalLlmState.Loaded(model, loadDurationMs = 0L)
        } else {
            _state.value = LocalLlmState.Unloaded
        }
    }

    override suspend fun unloadModel() {
        loadedModel = null
        _state.value = LocalLlmState.Unloaded
    }
}

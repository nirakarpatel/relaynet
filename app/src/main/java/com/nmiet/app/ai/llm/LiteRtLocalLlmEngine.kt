package com.nmiet.app.ai.llm

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Production on-device LLM inference engine implemented with Google's official LiteRT-LM runtime.
 *
 * Requirements satisfied:
 * - Threading: All model loading, weight parsing, and token streaming executed strictly on Dispatchers.IO.
 * - Resource Safety: Automatic JNI/native cleanup via engine.close() and conversation.close().
 * - Hardware Backend: CPU backend configured conservatively to prevent out-of-memory or GPU driver crashes on diverse hardware.
 * - Cancellable Streaming: Flow collection supports immediate user cancellation without dangling background compute.
 */
class LiteRtLocalLlmEngine(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : LocalLlmEngine {

    private val _state = MutableStateFlow<LocalLlmState>(LocalLlmState.Unloaded)
    override val state: StateFlow<LocalLlmState> = _state.asStateFlow()

    private var engine: Engine? = null
    private var conversation: Conversation? = null
    private var loadedModelInfo: LocalModelInfo? = null
    private var activeGenerationJob: Job? = null

    override fun isModelLoaded(): Boolean = engine != null && _state.value is LocalLlmState.Loaded

    override fun getLoadedModelInfo(): LocalModelInfo? = loadedModelInfo

    override suspend fun loadModel(modelFile: File, modelInfo: LocalModelInfo): Result<Unit> = withContext(ioDispatcher) {
        if (!modelFile.exists() || modelFile.length() == 0L) {
            val err = "Model file does not exist or is empty at: ${modelFile.absolutePath}"
            _state.value = LocalLlmState.Error(err, canRetry = false)
            return@withContext Result.failure(IllegalArgumentException(err))
        }

        try {
            unloadModel()
            _state.value = LocalLlmState.Loading(modelInfo.displayName)
            val startTime = System.currentTimeMillis()

            val config = EngineConfig(
                modelPath = modelFile.absolutePath,
                backend = Backend.CPU(),
                cacheDir = context.cacheDir.absolutePath
            )

            val newEngine = Engine(config)
            newEngine.initialize()

            engine = newEngine
            conversation = newEngine.createConversation()
            loadedModelInfo = modelInfo

            val duration = System.currentTimeMillis() - startTime
            _state.value = LocalLlmState.Loaded(modelInfo, duration)
            Result.success(Unit)
        } catch (e: Exception) {
            try { engine?.close() } catch (_: Exception) {}
            engine = null
            try { conversation?.close() } catch (_: Exception) {}
            conversation = null
            loadedModelInfo = null
            val message = e.localizedMessage ?: "Failed to initialize LiteRT-LM engine"
            _state.value = LocalLlmState.Error(message, canRetry = true)
            Result.failure(e)
        }
    }

    override fun generateStream(prompt: String): Flow<String> = flow {
        val currentEngine = engine
        val currentConv = conversation
        val info = loadedModelInfo

        if (currentEngine == null || currentConv == null || info == null) {
            emit("Error: No on-device model is currently loaded in memory.")
            _state.value = LocalLlmState.Error("Inference requested before model loaded", canRetry = false)
            return@flow
        }

        val startTime = System.currentTimeMillis()
        var firstTokenTime: Long? = null
        val responseBuilder = StringBuilder()
        var tokenCount = 0

        _state.value = LocalLlmState.Generating(
            prompt = prompt,
            streamedResponse = "",
            tokenCount = 0
        )

        try {
            val responseStream = currentConv.sendMessageAsync(prompt)
            responseStream.collect { message ->
                val chunk = extractTextFromMessage(message)
                if (chunk.isEmpty()) return@collect

                if (firstTokenTime == null) {
                    firstTokenTime = System.currentTimeMillis() - startTime
                }
                tokenCount++
                responseBuilder.append(chunk)

                val elapsedSec = (System.currentTimeMillis() - startTime) / 1000f
                val speed = if (elapsedSec > 0) tokenCount / elapsedSec else null

                _state.value = LocalLlmState.Generating(
                    prompt = prompt,
                    streamedResponse = responseBuilder.toString(),
                    tokenCount = tokenCount,
                    timeToFirstTokenMs = firstTokenTime,
                    speedTokensPerSec = speed
                )
                emit(chunk)
            }

            // Restore state to Loaded once generation finishes
            _state.value = LocalLlmState.Loaded(info, loadDurationMs = 0L)
        } catch (e: CancellationException) {
            if (info != null) {
                _state.value = LocalLlmState.Loaded(info, loadDurationMs = 0L)
            } else {
                _state.value = LocalLlmState.Unloaded
            }
            throw e
        } catch (e: Exception) {
            _state.value = LocalLlmState.Error("Generation error: ${e.message}", canRetry = true)
            emit("\n[Inference interrupted: ${e.message}]")
        }
    }.flowOn(ioDispatcher)

    private fun extractTextFromMessage(message: Message): String {
        val textFromContents = message.contents.contents
            .filterIsInstance<Content.Text>()
            .joinToString("") { it.text }

        if (textFromContents.isNotEmpty()) return textFromContents

        val channelText = message.channels.values.joinToString("")
        if (channelText.isNotEmpty()) return channelText

        return ""
    }

    override fun cancelGeneration() {
        activeGenerationJob?.cancel()
        activeGenerationJob = null
        val info = loadedModelInfo
        if (info != null && engine != null) {
            _state.value = LocalLlmState.Loaded(info, loadDurationMs = 0L)
        } else {
            _state.value = LocalLlmState.Unloaded
        }
    }

    override suspend fun unloadModel() = withContext(ioDispatcher) {
        cancelGeneration()
        try { conversation?.close() } catch (_: Exception) {}
        conversation = null

        try { engine?.close() } catch (_: Exception) {}
        engine = null

        loadedModelInfo = null
        _state.value = LocalLlmState.Unloaded
    }
}

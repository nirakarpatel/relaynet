package com.nmiet.app.ai.llm

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Clean hardware-agnostic abstraction for on-device LLM inference engines.
 */
interface LocalLlmEngine {

    /**
     * Observable state flow of the inference engine.
     */
    val state: StateFlow<LocalLlmState>

    /**
     * Loads the target model from app storage onto the specified execution backend.
     * Must execute off the main thread.
     */
    suspend fun loadModel(modelFile: File, modelInfo: LocalModelInfo): Result<Unit>

    /**
     * Streams tokens asynchronously for a user prompt.
     * Must execute off the main thread.
     */
    fun generateStream(prompt: String): Flow<String>

    /**
     * Cancels an active inference generation in progress.
     */
    fun cancelGeneration()

    /**
     * Unloads the model from memory and releases all native C++/JNI handles.
     */
    suspend fun unloadModel()

    /**
     * Returns true if the model weights are currently loaded and ready for inference.
     */
    fun isModelLoaded(): Boolean

    /**
     * Returns current loaded model info, if any.
     */
    fun getLoadedModelInfo(): LocalModelInfo?
}

package com.nmiet.app.ai.llm

import java.io.File

/**
 * State of the on-device LiteRT-LM inference engine.
 */
sealed interface LocalLlmState {
    data object Unloaded : LocalLlmState

    data class Loading(
        val modelName: String,
        val progressPercent: Int? = null
    ) : LocalLlmState

    data class Loaded(
        val modelInfo: LocalModelInfo,
        val loadDurationMs: Long,
        val memoryUsageMb: Long? = null
    ) : LocalLlmState

    data class Generating(
        val prompt: String,
        val streamedResponse: String,
        val tokenCount: Int,
        val timeToFirstTokenMs: Long? = null,
        val speedTokensPerSec: Float? = null
    ) : LocalLlmState

    data class Error(
        val errorMessage: String,
        val canRetry: Boolean = true
    ) : LocalLlmState
}

/**
 * State of model package installation, verification, and file storage.
 */
sealed interface ModelInstallationState {
    data object NotInstalled : ModelInstallationState

    data class CheckingStorage(
        val requiredBytes: Long,
        val availableBytes: Long
    ) : ModelInstallationState

    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val progressPercent: Int
    ) : ModelInstallationState

    data class VerifyingChecksum(
        val bytesProcessed: Long,
        val totalBytes: Long,
        val expectedSha256: String
    ) : ModelInstallationState

    data class Installed(
        val modelFile: File,
        val verifiedSha256: String,
        val installedSizeBytes: Long
    ) : ModelInstallationState

    data class Failed(
        val errorMessage: String,
        val canRetry: Boolean = true
    ) : ModelInstallationState
}

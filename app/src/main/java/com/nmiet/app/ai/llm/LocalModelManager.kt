package com.nmiet.app.ai.llm

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.os.StatFs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class DeviceMemoryGuidance(
    val totalRamGb: Float,
    val availableRamGb: Float,
    val isLowRamDevice: Boolean,
    val isRecommendedForLocalLlm: Boolean,
    val guidanceMessage: String
)

/**
 * Manages on-device model package lifecycle: storage verification, download streaming,
 * SHA-256 integrity checks, atomic installation, persistence, and file cleanup.
 */
class LocalModelManager(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("relaynet_local_models", Context.MODE_PRIVATE)

    private val modelsDir: File = File(context.filesDir, "models/local_llm").apply {
        if (!exists()) mkdirs()
    }

    private val _installState = MutableStateFlow<ModelInstallationState>(ModelInstallationState.NotInstalled)
    val installState: StateFlow<ModelInstallationState> = _installState.asStateFlow()

    private var activeDownloadJob: Job? = null

    init {
        checkInitialModelStatus()
    }

    /**
     * Checks if the default candidate model is already installed and verified on disk.
     */
    fun checkInitialModelStatus(modelInfo: LocalModelInfo = LocalModelRegistry.QWEN3_0_6B_NOTHINK): ModelInstallationState {
        val targetFile = getInstalledModelFile(modelInfo)
        val isPersistedInstalled = prefs.getBoolean("installed_${modelInfo.modelId}", false)

        if (targetFile.exists() && isPersistedInstalled) {
            val verifiedSha = prefs.getString("sha256_${modelInfo.modelId}", modelInfo.expectedSha256) ?: modelInfo.expectedSha256
            val state = ModelInstallationState.Installed(targetFile, verifiedSha, targetFile.length())
            _installState.value = state
            return state
        } else {
            // Clean up any stale partial files
            cleanupStagingFiles(modelInfo)
            val state = ModelInstallationState.NotInstalled
            _installState.value = state
            return state
        }
    }

    fun getInstalledModelFile(modelInfo: LocalModelInfo = LocalModelRegistry.QWEN3_0_6B_NOTHINK): File {
        return File(modelsDir, "${modelInfo.modelId}.litertlm")
    }

    private fun getStagingModelFile(modelInfo: LocalModelInfo): File {
        return File(modelsDir, "${modelInfo.modelId}.tmp")
    }

    fun getAvailableStorageBytes(): Long {
        return try {
            val stat = StatFs(context.filesDir.path)
            stat.availableBytes
        } catch (_: Exception) {
            500_000_000L
        }
    }

    fun getDeviceMemoryGuidance(): DeviceMemoryGuidance {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val totalGb = memInfo.totalMem / (1024f * 1024f * 1024f)
        val availGb = memInfo.availMem / (1024f * 1024f * 1024f)
        val isLowRam = memInfo.lowMemory || totalGb < 2.5f

        val (recommended, message) = when {
            totalGb >= 4.0f -> Pair(true, "Device hardware (RAM: %.1f GB) is well suited for CPU on-device inference.".format(totalGb))
            totalGb >= 2.0f -> Pair(true, "Supported (RAM: %.1f GB). Model fits comfortably with background CPU backend.".format(totalGb))
            else -> Pair(false, "Low RAM warning (RAM: %.1f GB). Inference may experience OS background memory pressure.".format(totalGb))
        }

        return DeviceMemoryGuidance(
            totalRamGb = totalGb,
            availableRamGb = availGb,
            isLowRamDevice = isLowRam,
            isRecommendedForLocalLlm = recommended,
            guidanceMessage = message
        )
    }

    /**
     * Downloads model from configurable URL, calculates SHA-256 during stream,
     * stages into .tmp file, and atomically renames upon verified checksum.
     */
    suspend fun downloadAndInstallModel(
        modelInfo: LocalModelInfo = LocalModelRegistry.QWEN3_0_6B_NOTHINK,
        customSourceUrl: String? = null
    ): Result<File> = withContext(ioDispatcher) {
        val targetFile = getInstalledModelFile(modelInfo)
        val stagingFile = getStagingModelFile(modelInfo)
        val downloadUrl = customSourceUrl ?: modelInfo.sourceUrl

        // 1. Storage Pre-flight Check
        val availableBytes = getAvailableStorageBytes()
        val requiredBytes = modelInfo.approxSizeBytes + 50_000_000L // 50MB safety buffer
        _installState.value = ModelInstallationState.CheckingStorage(requiredBytes, availableBytes)

        if (availableBytes < requiredBytes) {
            val err = "Insufficient storage: Requires ${(requiredBytes / 1_000_000)} MB, but only ${(availableBytes / 1_000_000)} MB available."
            _installState.value = ModelInstallationState.Failed(err, canRetry = false)
            return@withContext Result.failure(IllegalStateException(err))
        }

        // 2. Cleanup existing partial staging files
        cleanupStagingFiles(modelInfo)

        // 3. Stream download with on-the-fly SHA-256 calculation
        var connection: HttpURLConnection? = null
        try {
            connection = openConnectionFollowingRedirects(downloadUrl)
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val err = "Download failed: HTTP $responseCode (${connection.responseMessage})"
                _installState.value = ModelInstallationState.Failed(err, canRetry = true)
                return@withContext Result.failure(IllegalStateException(err))
            }

            val totalBytes = connection.contentLengthLong.let { if (it > 0) it else modelInfo.approxSizeBytes }
            val digest = MessageDigest.getInstance("SHA-256")

            connection.inputStream.use { input ->
                FileOutputStream(stagingFile).use { output ->
                    val buffer = ByteArray(32768)
                    var bytesRead: Int
                    var totalDownloaded: Long = 0

                    _installState.value = ModelInstallationState.Downloading(0L, totalBytes, 0)

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        digest.update(buffer, 0, bytesRead)
                        totalDownloaded += bytesRead

                        val percent = if (totalBytes > 0) ((totalDownloaded * 100) / totalBytes).toInt().coerceIn(0, 99) else 0
                        _installState.value = ModelInstallationState.Downloading(totalDownloaded, totalBytes, percent)
                    }
                    output.flush()
                }
            }

            // 4. Verification Check
            _installState.value = ModelInstallationState.VerifyingChecksum(stagingFile.length(), totalBytes, modelInfo.expectedSha256)
            val calculatedSha = digest.digest().joinToString("") { "%02x".format(it) }

            if (!calculatedSha.equals(modelInfo.expectedSha256, ignoreCase = true)) {
                stagingFile.delete()
                val err = "SHA-256 Verification Failed: Expected ${modelInfo.expectedSha256.take(8)}... but got ${calculatedSha.take(8)}..."
                _installState.value = ModelInstallationState.Failed(err, canRetry = true)
                return@withContext Result.failure(SecurityException(err))
            }

            // 5. Atomic Rename & State Persistence
            if (targetFile.exists()) targetFile.delete()
            val renameSuccess = stagingFile.renameTo(targetFile)

            if (!renameSuccess) {
                // Fallback copy if rename fails across mount points
                stagingFile.copyTo(targetFile, overwrite = true)
                stagingFile.delete()
            }

            prefs.edit()
                .putBoolean("installed_${modelInfo.modelId}", true)
                .putString("sha256_${modelInfo.modelId}", calculatedSha)
                .putLong("installed_time_${modelInfo.modelId}", System.currentTimeMillis())
                .apply()

            val installedState = ModelInstallationState.Installed(targetFile, calculatedSha, targetFile.length())
            _installState.value = installedState
            Result.success(targetFile)
        } catch (e: CancellationException) {
            cleanupStagingFiles(modelInfo)
            _installState.value = ModelInstallationState.NotInstalled
            throw e
        } catch (e: Exception) {
            cleanupStagingFiles(modelInfo)
            val err = "Download error: ${e.localizedMessage ?: "Network connection failed"}"
            _installState.value = ModelInstallationState.Failed(err, canRetry = true)
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Imports a model file from local external/development storage into persistent app-private storage.
     */
    suspend fun importModelFromFile(
        sourceFile: File,
        modelInfo: LocalModelInfo = LocalModelRegistry.QWEN3_0_6B_NOTHINK,
        verifyExpectedSha: Boolean = true
    ): Result<File> = withContext(ioDispatcher) {
        if (!sourceFile.exists() || sourceFile.length() == 0L) {
            val err = "Import file does not exist or is empty: ${sourceFile.absolutePath}"
            _installState.value = ModelInstallationState.Failed(err, canRetry = false)
            return@withContext Result.failure(IllegalArgumentException(err))
        }

        val targetFile = getInstalledModelFile(modelInfo)
        val stagingFile = getStagingModelFile(modelInfo)
        cleanupStagingFiles(modelInfo)

        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val totalBytes = sourceFile.length()

            FileInputStream(sourceFile).use { input ->
                FileOutputStream(stagingFile).use { output ->
                    val buffer = ByteArray(32768)
                    var bytesRead: Int
                    var totalCopied: Long = 0

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        digest.update(buffer, 0, bytesRead)
                        totalCopied += bytesRead

                        val percent = ((totalCopied * 100) / totalBytes).toInt()
                        _installState.value = ModelInstallationState.Downloading(totalCopied, totalBytes, percent)
                    }
                    output.flush()
                }
            }

            val calculatedSha = digest.digest().joinToString("") { "%02x".format(it) }

            if (verifyExpectedSha && !calculatedSha.equals(modelInfo.expectedSha256, ignoreCase = true)) {
                stagingFile.delete()
                val err = "Import SHA-256 mismatch: Expected ${modelInfo.expectedSha256.take(8)}... got ${calculatedSha.take(8)}..."
                _installState.value = ModelInstallationState.Failed(err, canRetry = false)
                return@withContext Result.failure(SecurityException(err))
            }

            if (targetFile.exists()) targetFile.delete()
            stagingFile.renameTo(targetFile)

            prefs.edit()
                .putBoolean("installed_${modelInfo.modelId}", true)
                .putString("sha256_${modelInfo.modelId}", calculatedSha)
                .putLong("installed_time_${modelInfo.modelId}", System.currentTimeMillis())
                .apply()

            val state = ModelInstallationState.Installed(targetFile, calculatedSha, targetFile.length())
            _installState.value = state
            Result.success(targetFile)
        } catch (e: Exception) {
            cleanupStagingFiles(modelInfo)
            _installState.value = ModelInstallationState.Failed("Import failed: ${e.message}", canRetry = false)
            Result.failure(e)
        }
    }

    fun cancelDownload(modelInfo: LocalModelInfo = LocalModelRegistry.QWEN3_0_6B_NOTHINK) {
        activeDownloadJob?.cancel()
        activeDownloadJob = null
        cleanupStagingFiles(modelInfo)
        _installState.value = ModelInstallationState.NotInstalled
    }

    suspend fun deleteModel(modelInfo: LocalModelInfo = LocalModelRegistry.QWEN3_0_6B_NOTHINK): Boolean = withContext(ioDispatcher) {
        cancelDownload(modelInfo)
        val targetFile = getInstalledModelFile(modelInfo)
        val deleted = if (targetFile.exists()) targetFile.delete() else true

        prefs.edit()
            .remove("installed_${modelInfo.modelId}")
            .remove("sha256_${modelInfo.modelId}")
            .remove("installed_time_${modelInfo.modelId}")
            .apply()

        _installState.value = ModelInstallationState.NotInstalled
        deleted
    }

    private fun cleanupStagingFiles(modelInfo: LocalModelInfo) {
        val staging = getStagingModelFile(modelInfo)
        if (staging.exists()) staging.delete()
    }

    private fun openConnectionFollowingRedirects(urlStr: String, maxRedirects: Int = 5): HttpURLConnection {
        var currentUrl = urlStr
        var redirects = 0

        while (redirects < maxRedirects) {
            val url = URL(currentUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", "RelayNet-Android/1.0 (Education Offline Sync)")
            }

            when (connection.responseCode) {
                HttpURLConnection.HTTP_MOVED_PERM,
                HttpURLConnection.HTTP_MOVED_TEMP,
                HttpURLConnection.HTTP_SEE_OTHER,
                307, 308 -> {
                    val location = connection.getHeaderField("Location")
                    connection.disconnect()
                    if (location.isNullOrBlank()) {
                        throw IllegalStateException("Redirect requested without Location header")
                    }
                    currentUrl = if (location.startsWith("http")) location else URL(url, location).toString()
                    redirects++
                }
                else -> return connection
            }
        }
        throw IllegalStateException("Too many redirects ($redirects) resolving model URL")
    }
}

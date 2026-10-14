package com.nmiet.app.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.os.StatFs
import android.util.Log
import com.nmiet.app.data.local.ZipPackManager
import com.nmiet.app.domain.model.Board
import com.nmiet.app.domain.model.ContentItem
import com.nmiet.app.domain.model.ContentMediaType
import com.nmiet.app.domain.model.DownloadStatus
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.repository.ContentRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * Cloudflare R2 Content Manager for RelayNet.
 * Handles remote catalogue discovery, resilient file downloads, SHA-256 verification,
 * local download persistence across restarts, and offline ZIP pack import.
 */
class R2ContentManager(
    private val context: Context,
    private val repository: ContentRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private const val TAG = "R2ContentManager"
        private const val PREFS_NAME = "relaynet_r2_downloads"
        private const val STORAGE_DIR_NAME = "content_library"
        private const val BUFFER_SIZE = 16384 // 16 KB buffer

        @Volatile
        private var instance: R2ContentManager? = null

        fun getInstance(context: Context, repository: ContentRepository): R2ContentManager {
            return instance ?: synchronized(this) {
                instance ?: R2ContentManager(context.applicationContext, repository).also {
                    instance = it
                }
            }
        }

        fun parseCatalogJson(jsonStr: String, baseUrl: String = R2ContentConfig.DEFAULT_PUBLIC_BASE_URL): List<ContentItem> {
            val list = mutableListOf<ContentItem>()
            val root = if (jsonStr.trim().startsWith("[")) {
                JSONArray(jsonStr)
            } else {
                JSONObject(jsonStr).optJSONArray("items") ?: JSONArray()
            }

            for (i in 0 until root.length()) {
                val obj = root.getJSONObject(i)
                val id = obj.optString("id", "r2_item_$i")
                val title = obj.optString("title", "Resource $i")
                val desc = obj.optString("description", "")
                val typeStr = obj.optString("media_type", obj.optString("type", "REVISION_NOTES"))
                val path = obj.optString("path", obj.optString("remote_url", ""))
                val size = obj.optLong("size_bytes", obj.optLong("size", 0L))
                val sha = obj.optString("sha256_checksum", obj.optString("sha256", ""))
                val grade = obj.optInt("class_grade", 9)
                val subject = obj.optString("subject", "Science")
                val lang = obj.optString("language", "English")

                val mediaType = when (typeStr.uppercase()) {
                    "IMAGE_DIAGRAM", "PNG", "IMAGE" -> ContentMediaType.IMAGE_DIAGRAM
                    "LEARNING_PACK_ZIP", "ZIP", "PACK" -> ContentMediaType.LEARNING_PACK_ZIP
                    "JSON_RESOURCE", "JSON" -> ContentMediaType.JSON_RESOURCE
                    "PDF_TEXTBOOK", "PDF" -> ContentMediaType.PDF_TEXTBOOK
                    "VIDEO_LESSON", "VIDEO" -> ContentMediaType.VIDEO_LESSON
                    else -> ContentMediaType.REVISION_NOTES
                }

                val remoteUrl = if (path.startsWith("http://") || path.startsWith("https://")) {
                    path
                } else {
                    R2ContentConfig.getObjectUrl(path, baseUrl)
                }

                list.add(
                    ContentItem(
                        id = id,
                        title = title,
                        description = desc,
                        mediaType = mediaType,
                        remoteUrl = remoteUrl,
                        sizeBytes = size,
                        sha256Checksum = sha,
                        classGrade = grade,
                        subject = subject,
                        board = Board.ODISHA_STATE,
                        language = lang
                    )
                )
            }
            return list
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val storageDir: File by lazy {
        val dir = File(context.filesDir, STORAGE_DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    private val activeDownloadJobs = ConcurrentHashMap<String, Job>()

    private val _contentItems = MutableStateFlow<List<ContentItem>>(emptyList())
    val contentItems: StateFlow<List<ContentItem>> = _contentItems.asStateFlow()

    private val _catalogStatusMessage = MutableStateFlow("Initializing Cloudflare R2 catalogue...")
    val catalogStatusMessage: StateFlow<String> = _catalogStatusMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _activeBaseUrl = MutableStateFlow(R2ContentConfig.DEFAULT_PUBLIC_BASE_URL)
    val activeBaseUrl: StateFlow<String> = _activeBaseUrl.asStateFlow()

    init {
        // Initialize with verified sample items and reconcile with persistent local storage
        val initialItems = reconcileWithLocalStorage(R2SampleCatalogue.getVerifiedSampleItems())
        _contentItems.value = initialItems
        _catalogStatusMessage.value = "Loaded verified sample catalogue (7 resources available on R2)."
    }

    /**
     * Checks if previously downloaded items exist on disk.
     * Prevents partial or deleted files from appearing as COMPLETED.
     */
    private fun reconcileWithLocalStorage(items: List<ContentItem>): List<ContentItem> {
        return items.map { item ->
            val savedPath = prefs.getString("file_${item.id}", null)
            if (savedPath != null) {
                val file = File(savedPath)
                if (file.exists() && file.isFile && file.length() > 0) {
                    item.copy(
                        downloadStatus = DownloadStatus.COMPLETED,
                        downloadProgressPercent = 100,
                        localFilePath = file.absolutePath,
                        errorMessage = null
                    )
                } else {
                    // File removed or corrupted, clean preference
                    prefs.edit().remove("file_${item.id}").apply()
                    item.copy(
                        downloadStatus = DownloadStatus.NOT_DOWNLOADED,
                        downloadProgressPercent = 0,
                        localFilePath = null
                    )
                }
            } else {
                item.copy(
                    downloadStatus = DownloadStatus.NOT_DOWNLOADED,
                    downloadProgressPercent = 0,
                    localFilePath = null
                )
            }
        }
    }

    /**
     * Fetches remote catalog.json from R2.
     * If missing (HTTP 404) or offline, falls back gracefully to the verified sample catalog.
     */
    fun refreshCatalogue(customBaseUrl: String? = null) {
        scope.launch {
            _isRefreshing.value = true
            val baseUrl = customBaseUrl ?: _activeBaseUrl.value
            _activeBaseUrl.value = baseUrl

            val catalogUrl = R2ContentConfig.getCatalogUrl(baseUrl)
            Log.d(TAG, "Fetching catalogue from: $catalogUrl")

            var remoteItems: List<ContentItem>? = null
            var errorDetail = ""

            try {
                val url = URL(catalogUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 7000
                connection.setRequestProperty("User-Agent", "RelayNet-Android/1.0")

                val code = connection.responseCode
                if (code in 200..299) {
                    val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                    remoteItems = parseCatalogJson(jsonText, baseUrl)
                    _catalogStatusMessage.value = "✅ Remote catalogue synchronized from Cloudflare R2 (${remoteItems.size} items)."
                } else {
                    errorDetail = "HTTP $code ${connection.responseMessage}"
                    _catalogStatusMessage.value = "⚠️ catalog.json not found on R2 ($errorDetail). Using verified sample catalogue."
                }
            } catch (e: Exception) {
                errorDetail = e.message ?: "Connection error"
                _catalogStatusMessage.value = "Offline / Connection error ($errorDetail). Using cached local catalogue."
                Log.w(TAG, "Failed to load remote catalog.json: $errorDetail")
            }

            val baseList = remoteItems ?: R2SampleCatalogue.getVerifiedSampleItems(baseUrl)
            val mergedList = reconcileWithLocalStorage(baseList)

            _contentItems.value = mergedList
            _isRefreshing.value = false
        }
    }


    /**
     * Downloads an item from R2 with on-the-fly SHA-256 calculation, atomic staging rename,
     * and storage quota check.
     */
    fun startDownload(item: ContentItem) {
        val existingJob = activeDownloadJobs[item.id]
        if (existingJob != null && existingJob.isActive) {
            Log.d(TAG, "Download already in progress for ${item.id}")
            return
        }

        val job = scope.launch {
            updateItemState(item.id) {
                it.copy(
                    downloadStatus = DownloadStatus.DOWNLOADING,
                    downloadProgressPercent = 0,
                    errorMessage = null
                )
            }

            val fileName = item.remoteUrl.substringAfterLast("/").takeIf { it.isNotBlank() } ?: "${item.id}.bin"
            val targetFile = File(storageDir, "${item.id}_$fileName")
            val stagingFile = File(storageDir, "${item.id}_${System.currentTimeMillis()}.tmp")

            var connection: HttpURLConnection? = null
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null

            try {
                // 1. Storage Pre-flight Check
                val availableBytes = getAvailableStorageBytes()
                val requiredBytes = if (item.sizeBytes > 0) item.sizeBytes + 2_000_000L else 5_000_000L
                if (availableBytes < requiredBytes) {
                    throw IllegalStateException("Insufficient device storage. Required ${(requiredBytes / 1024)} KB, available ${(availableBytes / 1024)} KB.")
                }

                // 2. Open HTTP Connection
                Log.d(TAG, "Starting download for ${item.id} from ${item.remoteUrl}")
                val url = URL(item.remoteUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 15000
                    setRequestProperty("User-Agent", "RelayNet-Android/1.0")
                    instanceFollowRedirects = true
                }

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    throw IllegalStateException("Download failed with HTTP $responseCode (${connection.responseMessage})")
                }

                val totalLength = connection.contentLengthLong.let { if (it > 0) it else item.sizeBytes }
                val digest = MessageDigest.getInstance("SHA-256")

                inputStream = connection.inputStream
                outputStream = FileOutputStream(stagingFile)

                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                var totalDownloaded: Long = 0
                var lastReportedPercent = -1

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    digest.update(buffer, 0, bytesRead)
                    totalDownloaded += bytesRead

                    val percent = if (totalLength > 0) {
                        ((totalDownloaded * 100) / totalLength).toInt().coerceIn(0, 99)
                    } else 50

                    if (percent != lastReportedPercent) {
                        lastReportedPercent = percent
                        updateItemState(item.id) {
                            it.copy(
                                downloadStatus = DownloadStatus.DOWNLOADING,
                                downloadProgressPercent = percent
                            )
                        }
                    }
                }

                outputStream.flush()
                outputStream.close()
                outputStream = null

                // 3. SHA-256 Checksum Verification (if expected checksum exists)
                val calculatedSha = digest.digest().joinToString("") { "%02x".format(it) }
                Log.d(TAG, "Downloaded ${item.id} (${stagingFile.length()} bytes). Calculated SHA: $calculatedSha")

                if (item.sha256Checksum.isNotBlank()) {
                    if (!calculatedSha.equals(item.sha256Checksum, ignoreCase = true)) {
                        stagingFile.delete()
                        throw SecurityException("SHA-256 checksum mismatch! Expected: ${item.sha256Checksum.take(12)}..., calculated: ${calculatedSha.take(12)}...")
                    }
                }

                // 4. Atomic File Promotion
                if (targetFile.exists()) targetFile.delete()
                val renamed = stagingFile.renameTo(targetFile)
                if (!renamed) {
                    stagingFile.copyTo(targetFile, overwrite = true)
                    stagingFile.delete()
                }

                // 5. Persist to SharedPreferences
                prefs.edit().putString("file_${item.id}", targetFile.absolutePath).apply()

                updateItemState(item.id) {
                    it.copy(
                        downloadStatus = DownloadStatus.COMPLETED,
                        downloadProgressPercent = 100,
                        localFilePath = targetFile.absolutePath,
                        errorMessage = null
                    )
                }

                Log.i(TAG, "Successfully downloaded and verified: ${targetFile.name}")

            } catch (ce: CancellationException) {
                Log.w(TAG, "Download cancelled for ${item.id}")
                stagingFile.delete()
                updateItemState(item.id) {
                    it.copy(
                        downloadStatus = DownloadStatus.NOT_DOWNLOADED,
                        downloadProgressPercent = 0,
                        errorMessage = "Download cancelled"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download failed for ${item.id}: ${e.message}", e)
                stagingFile.delete()
                updateItemState(item.id) {
                    it.copy(
                        downloadStatus = DownloadStatus.FAILED,
                        downloadProgressPercent = 0,
                        errorMessage = e.message ?: "Download failed"
                    )
                }
            } finally {
                outputStream?.close()
                inputStream?.close()
                connection?.disconnect()
                activeDownloadJobs.remove(item.id)
            }
        }

        activeDownloadJobs[item.id] = job
    }

    /**
     * Cancels an active in-flight download.
     */
    fun cancelDownload(itemId: String) {
        val job = activeDownloadJobs[itemId]
        if (job != null && job.isActive) {
            job.cancel()
        }
    }

    /**
     * Deletes a downloaded local file and resets state.
     */
    fun deleteDownloadedItem(item: ContentItem) {
        cancelDownload(item.id)
        item.localFilePath?.let { path ->
            val file = File(path)
            if (file.exists()) file.delete()
        }
        prefs.edit().remove("file_${item.id}").apply()

        updateItemState(item.id) {
            it.copy(
                downloadStatus = DownloadStatus.NOT_DOWNLOADED,
                downloadProgressPercent = 0,
                localFilePath = null,
                errorMessage = null
            )
        }
    }

    /**
     * Reads text from a downloaded text or JSON file.
     */
    suspend fun readTextContent(item: ContentItem): Result<String> = withContext(ioDispatcher) {
        try {
            val path = item.localFilePath ?: throw IllegalStateException("File is not downloaded")
            val file = File(path)
            if (!file.exists()) throw IllegalStateException("Local file not found on disk")
            Result.success(file.readText(Charsets.UTF_8))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves the File handle for an image resource.
     */
    fun getLocalImageFile(item: ContentItem): File? {
        val path = item.localFilePath ?: return null
        val file = File(path)
        return if (file.exists() && file.isFile) file else null
    }

    /**
     * Imports a downloaded ZIP learning pack into the RelayNet curriculum database
     * using ZipPackManager validation.
     */
    suspend fun importLearningPackZip(item: ContentItem): Result<LearningPack> = withContext(ioDispatcher) {
        try {
            val path = item.localFilePath ?: throw IllegalStateException("Learning pack ZIP is not downloaded")
            val zipFile = File(path)
            if (!zipFile.exists()) throw IllegalStateException("Target ZIP file missing from storage")

            val destDir = File(context.filesDir, "packs/extracted_${item.id}")
            val extracted = ZipPackManager.extractAndVerifyPack(zipFile, destDir)

            if (!extracted.isChecksumValid) {
                return@withContext Result.failure(SecurityException("Pack manifest checksum mismatch for ${zipFile.name}"))
            }

            // Save to Room SQLite via repository
            repository.saveIncomingLearningPack(extracted.pack, "Cloudflare R2 Storage")
            Result.success(extracted.pack)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import learning pack: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun updateItemState(itemId: String, transform: (ContentItem) -> ContentItem) {
        _contentItems.update { list ->
            list.map { if (it.id == itemId) transform(it) else it }
        }
    }

    private fun getAvailableStorageBytes(): Long {
        return try {
            val stat = StatFs(context.filesDir.absolutePath)
            stat.availableBytes
        } catch (e: Exception) {
            100_000_000L // 100MB fallback assumption
        }
    }
}

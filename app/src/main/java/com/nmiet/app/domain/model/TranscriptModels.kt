package com.nmiet.app.domain.model

/**
 * A discrete segment of speech within an audio/video recording with exact time bounds.
 */
data class TranscriptSegment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val languageTag: String? = "en",
    val confidence: Float? = null,
    val isUserEdited: Boolean = false
) {
    val durationMs: Long get() = (endMs - startMs).coerceAtLeast(0)

    fun formattedTimeRange(): String {
        val startSec = startMs / 1000
        val endSec = endMs / 1000
        return String.format("%02d:%02d - %02d:%02d", startSec / 60, startSec % 60, endSec / 60, endSec % 60)
    }
}

/**
 * Complete verified transcript document for an educational video.
 */
data class VideoTranscriptDocument(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sourceFileName: String,
    val sourceFileSizeBytes: Long,
    val durationMs: Long,
    val extractedAudioPath: String,
    val languageTag: String,
    val engineVersion: String = "RelayNet-ASR-v1",
    val segments: List<TranscriptSegment>,
    val isReviewed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Speech dataset manifest entry pairing an audio segment with its verified ground-truth transcript.
 */
data class DatasetManifestItem(
    val audioRelativePath: String,
    val text: String,
    val language: String,
    val startSeconds: Double,
    val endSeconds: Double,
    val durationSeconds: Double,
    val sampleRateHz: Int = 16000,
    val licenseConsent: String = "RelayNet Authorized Educational Dataset v1"
)

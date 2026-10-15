package com.nmiet.app.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.UUID

data class ExtractedAudioResult(
    val outputFile: File,
    val durationMs: Long,
    val mimeType: String,
    val sampleRate: Int,
    val channelCount: Int
)

/**
 * Extracts raw audio tracks from video files (MP4, MKV, 3GP) using Android native MediaExtractor and MediaMuxer.
 */
class MediaAudioExtractor(private val context: Context) {

    suspend fun extractAudioFromVideo(videoUri: Uri): Result<ExtractedAudioResult> = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        var pfd: android.os.ParcelFileDescriptor? = null

        try {
            pfd = context.contentResolver.openFileDescriptor(videoUri, "r")
                ?: return@withContext Result.failure(IllegalArgumentException("Cannot open video FileDescriptor for URI: $videoUri"))

            extractor.setDataSource(pfd.fileDescriptor)

            val trackCount = extractor.trackCount
            var audioTrackIndex = -1
            var audioFormat: MediaFormat? = null

            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    audioFormat = format
                    break
                }
            }

            if (audioTrackIndex == -1 || audioFormat == null) {
                return@withContext Result.failure(IllegalStateException("No supported audio track found inside the selected video."))
            }

            extractor.selectTrack(audioTrackIndex)

            val durationUs = if (audioFormat.containsKey(MediaFormat.KEY_DURATION)) {
                audioFormat.getLong(MediaFormat.KEY_DURATION)
            } else {
                0L
            }
            val durationMs = durationUs / 1000
            val mime = audioFormat.getString(MediaFormat.KEY_MIME) ?: "audio/mp4a-latm"
            val sampleRate = if (audioFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE) else 16000
            val channelCount = if (audioFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 1

            // Output container: standard M4A / AAC container
            val outputDir = File(context.cacheDir, "audio_extracts").apply { mkdirs() }
            val outputFile = File(outputDir, "audio_${UUID.randomUUID().toString().take(8)}.m4a")

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxerTrackIndex = muxer.addTrack(audioFormat)
            muxer.start()

            val maxBufferSize = if (audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE).coerceAtLeast(256 * 1024)
            } else {
                256 * 1024
            }

            val buffer = ByteBuffer.allocate(maxBufferSize)
            val bufferInfo = MediaCodec.BufferInfo()

            while (true) {
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)
                if (bufferInfo.size < 0) {
                    break
                }
                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags

                muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                extractor.advance()
            }

            Log.i("MediaAudioExtractor", "Audio extraction complete: ${outputFile.length()} bytes, duration: ${durationMs}ms")
            Result.success(
                ExtractedAudioResult(
                    outputFile = outputFile,
                    durationMs = durationMs,
                    mimeType = mime,
                    sampleRate = sampleRate,
                    channelCount = channelCount
                )
            )
        } catch (e: Exception) {
            Log.e("MediaAudioExtractor", "Failed to extract audio: ${e.message}", e)
            Result.failure(e)
        } finally {
            try { muxer?.stop() } catch (_: Exception) {}
            try { muxer?.release() } catch (_: Exception) {}
            try { extractor.release() } catch (_: Exception) {}
            try { pfd?.close() } catch (_: Exception) {}
        }
    }
}

package com.nmiet.app.data.local

import com.nmiet.app.domain.model.LearningPack
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Robust learning pack ZIP manager implementing Section 9 of RelayNet blueprint:
 * - Validates file format and size limits (prevents zip bombs)
 * - Prevents Zip Slip / path traversal vulnerabilities
 * - Verifies SHA-256 checksums
 * - Extracts and maps manifest & lesson JSONs into Room LearningPack
 */
object ZipPackManager {

    private const val MAX_UNCOMPRESSED_SIZE_BYTES = 50 * 1024 * 1024 // 50 MB safety limit
    private const val MAX_TOTAL_ENTRIES = 500

    data class ExtractedPackResult(
        val pack: LearningPack,
        val manifestJson: String,
        val calculatedSha256: String,
        val isChecksumValid: Boolean
    )

    fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Safely unzips a learning pack with Zip-Slip path traversal protection.
     */
    fun extractAndVerifyPack(zipFile: File, outputDir: File): ExtractedPackResult {
        if (!zipFile.exists() || !zipFile.name.endsWith(".zip", ignoreCase = true)) {
            throw IllegalArgumentException("Target file must be a valid .zip archive: ${zipFile.name}")
        }

        val canonicalDestDir = outputDir.canonicalFile
        if (!canonicalDestDir.exists()) {
            canonicalDestDir.mkdirs()
        }

        var totalBytesRead: Long = 0
        var totalEntries = 0

        var manifestContent: String? = null
        var lessonOdiaContent: String? = null
        var lessonEnglishContent: String? = null
        var diagramLocalPath: String? = null

        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                totalEntries++
                if (totalEntries > MAX_TOTAL_ENTRIES) {
                    throw SecurityException("Pack contains excessive entries (Zip Bomb protection).")
                }

                val targetFile = File(canonicalDestDir, entry.name).canonicalFile
                if (!targetFile.path.startsWith(canonicalDestDir.path)) {
                    throw SecurityException("Security Violation: Zip Slip path traversal detected for entry ${entry.name}")
                }

                if (entry.isDirectory) {
                    targetFile.mkdirs()
                } else {
                    targetFile.parentFile?.mkdirs()
                    FileOutputStream(targetFile).use { fos ->
                        val buffer = ByteArray(4096)
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            totalBytesRead += len
                            if (totalBytesRead > MAX_UNCOMPRESSED_SIZE_BYTES) {
                                throw SecurityException("Archive expands beyond safe limits ($MAX_UNCOMPRESSED_SIZE_BYTES bytes).")
                            }
                            fos.write(buffer, 0, len)
                        }
                    }

                    // Check for key files
                    when {
                        entry.name == "manifest.json" -> manifestContent = targetFile.readText()
                        entry.name == "lesson/odia.json" -> lessonOdiaContent = targetFile.readText()
                        entry.name == "lesson/english.json" -> lessonEnglishContent = targetFile.readText()
                        entry.name.startsWith("diagrams/") && (entry.name.endsWith(".jpg", ignoreCase = true) || entry.name.endsWith(".png", ignoreCase = true) || entry.name.endsWith(".webp", ignoreCase = true)) -> {
                            diagramLocalPath = targetFile.absolutePath
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        val calculatedSha = calculateSha256(zipFile)

        // Parse manifest
        val manifestObj = if (manifestContent != null) JSONObject(manifestContent) else JSONObject()
        val expectedSha = manifestObj.optString("sha256_checksum", "")
        val isChecksumValid = expectedSha.isEmpty() || expectedSha.equals(calculatedSha, ignoreCase = true)

        if (diagramLocalPath == null && manifestObj.has("diagrams")) {
            val diagramsArray = manifestObj.optJSONArray("diagrams")
            if (diagramsArray != null && diagramsArray.length() > 0) {
                val firstDiagram = diagramsArray.optJSONObject(0)
                val relFile = firstDiagram?.optString("file", "") ?: ""
                if (relFile.isNotEmpty()) {
                    val f = File(canonicalDestDir, relFile)
                    if (f.exists()) {
                        diagramLocalPath = f.absolutePath
                    }
                }
            }
        }

        val packId = manifestObj.optString("pack_id", "relaynet_extracted_${System.currentTimeMillis()}")
        val boardStr = manifestObj.optString("board", "ODISHA_STATE")
        val grade = manifestObj.optInt("class_grade", 9)
        val subject = manifestObj.optString("subject", "Science (Biology)")
        val versionInt = manifestObj.optInt("version", 1)

        val odiaObj = if (lessonOdiaContent != null) JSONObject(lessonOdiaContent) else JSONObject()
        val engObj = if (lessonEnglishContent != null) JSONObject(lessonEnglishContent) else JSONObject()

        val boardEnum = when (boardStr.uppercase()) {
            "CBSE" -> com.nmiet.app.domain.model.Board.CBSE
            "CISCE_ICSE", "ICSE" -> com.nmiet.app.domain.model.Board.CISCE_ICSE
            else -> com.nmiet.app.domain.model.Board.ODISHA_STATE
        }

        val lessonData = com.nmiet.app.domain.model.LessonData(
            summaryOdia = odiaObj.optString("overview", "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା ସାରାଂଶ"),
            summaryEnglish = engObj.optString("overview", "Overview of photosynthesis"),
            equationOrFormula = odiaObj.optString("equation", "6CO₂ + 12H₂O ──► C₆H₁₂O₆ + 6H₂O + 6O₂"),
            sections = emptyList()
        )

        val learningPack = LearningPack(
            packId = packId,
            titleOdia = odiaObj.optString("title", "ଆଲୋକ ଶ୍ଳେଷଣ ପାଠ"),
            titleEnglish = engObj.optString("title", "Photosynthesis Lesson"),
            board = boardEnum,
            classGrade = grade,
            subject = subject,
            chapterOdia = "ଅଧ୍ୟାୟ ୧: ପୋଷଣ",
            chapterEnglish = "Chapter 1: Nutrition",
            topicOdia = "ଆଲୋକ ଶ୍ଳେଷଣ",
            topicEnglish = "Photosynthesis",
            version = versionInt,
            lesson = lessonData,
            quizQuestions = emptyList(),
            sizeBytes = zipFile.length(),
            checksumSha256 = calculatedSha,
            isDownloaded = true,
            isReceivedViaMesh = false,
            peerSenderName = null,
            diagramLocalPath = diagramLocalPath
        )

        return ExtractedPackResult(
            pack = learningPack,
            manifestJson = manifestContent ?: "{}",
            calculatedSha256 = calculatedSha,
            isChecksumValid = isChecksumValid
        )
    }
}

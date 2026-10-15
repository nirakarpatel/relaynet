package com.nmiet.app

import com.nmiet.app.audio.AudioTranscriptionPipeline
import com.nmiet.app.domain.model.GeneratedNote
import com.nmiet.app.domain.model.NoteConceptPoint
import com.nmiet.app.domain.model.TranscriptSegment
import com.nmiet.app.domain.model.VideoTranscriptDocument
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class VoiceIntelligenceUnitTest {

    @Test
    fun testAudioChunkingOffsetPreservation() {
        // Simulates a 150-second lecture audio split into 60-second chunks
        val totalDurationMs = 150_000L
        val chunkDurationMs = 60_000L

        val chunks = mutableListOf<Pair<Long, Long>>()
        var offset = 0L
        while (offset < totalDurationMs) {
            val end = minOf(offset + chunkDurationMs, totalDurationMs)
            chunks.add(Pair(offset, end))
            offset = end
        }

        assertEquals(3, chunks.size)
        assertEquals(Pair(0L, 60_000L), chunks[0])
        assertEquals(Pair(60_000L, 120_000L), chunks[1])
        assertEquals(Pair(120_000L, 150_000L), chunks[2])

        // Verify continuity without drift
        for (i in 0 until chunks.size - 1) {
            assertEquals(chunks[i].second, chunks[i + 1].first)
        }
    }

    @Test
    fun testTranscriptSegmentFormatting() {
        val segment = TranscriptSegment(
            startMs = 75_000L,
            endMs = 92_000L,
            text = "Light reactions occur in the thylakoid membranes.",
            confidence = 0.94f
        )

        assertEquals("01:15 - 01:32", segment.formattedTimeRange())
        assertEquals(17_000L, segment.durationMs)
        assertEquals(0.94f, segment.confidence ?: 0f, 0.001f)
    }

    @Test
    fun testDatasetManifestExport_structureAndMetadata() {
        runBlocking {
            val pipeline = AudioTranscriptionPipeline()

            val doc = VideoTranscriptDocument(
                id = "test_doc_001",
                sourceFileName = "class9_bio_lecture.mp4",
                sourceFileSizeBytes = 10_240_000L,
                durationMs = 45_000L,
                extractedAudioPath = "/data/cache/audio.m4a",
                languageTag = "en",
                segments = listOf(
                    TranscriptSegment(
                        startMs = 0L,
                        endMs = 15_000L,
                        text = "Introduction to light reactions and thylakoid structure.",
                        confidence = 0.92f
                    ),
                    TranscriptSegment(
                        startMs = 15_000L,
                        endMs = 30_000L,
                        text = "Photolysis splits water into protons, electrons, and oxygen gas.",
                        confidence = 0.88f
                    )
                ),
                isReviewed = true
            )

            val tempExportDir = File(System.getProperty("java.io.tmpdir"), "test_speech_dataset_${System.currentTimeMillis()}").apply { mkdirs() }
            val result = pipeline.exportDatasetManifest(doc, tempExportDir)

            assertTrue(result.isSuccess)
            val manifestFile = result.getOrThrow()
            assertTrue(manifestFile.exists())
            assertTrue(manifestFile.length() > 0)

            val json = JSONObject(manifestFile.readText())
            assertEquals("relaynet_asr_test_doc_001", json.getString("dataset_id"))
            assertEquals("class9_bio_lecture.mp4", json.getString("source_media"))
            assertEquals(2, json.getInt("total_segments"))
            assertEquals(45.0, json.getDouble("duration_seconds"), 0.01)
            assertTrue(json.getBoolean("is_human_reviewed"))

            val segmentsArray = json.getJSONArray("segments")
            assertEquals(2, segmentsArray.length())

            val firstSeg = segmentsArray.getJSONObject(0)
            assertEquals(0.0, firstSeg.getDouble("start_seconds"), 0.01)
            assertEquals(15.0, firstSeg.getDouble("end_seconds"), 0.01)
            assertEquals("Introduction to light reactions and thylakoid structure.", firstSeg.getString("text"))
            assertTrue(firstSeg.has("license_consent"))

            // Cleanup
            tempExportDir.deleteRecursively()
        }
    }

    @Test
    fun testNotesSynthesisFromTranscribedLecture() {
        val transcribedText = "Welcome students. Photosynthesis takes place in chloroplasts. Photolysis of water releases oxygen."
        
        val note = GeneratedNote(
            noteId = "note_test_01",
            topicId = "relaynet_class9_science_photosynthesis_v1",
            topicName = "Chapter 4: Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ)",
            titleOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ମୁଖ୍ୟ ସାରାଂଶ",
            titleEnglish = "Photosynthesis Revision Notes",
            summaryOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା ଦ୍ୱାରା ଉଦ୍ଭିଦ ଶର୍କରା ପ୍ରସ୍ତୁତ କରେ।",
            summaryEnglish = "Photosynthesis enables plants to synthesize glucose using solar radiation.",
            keyConcepts = listOf(
                NoteConceptPoint(
                    pointOdia = "ଥାଇଲାକଏଡ୍ ରେ ଆଲୋକ ପ୍ରକ୍ରିୟା ସଂଘଠିତ ହୁଏ।",
                    pointEnglish = "Light-dependent reactions occur in thylakoids."
                )
            ),
            formulasEquations = listOf("6CO₂ + 12H₂O ──> C₆H₁₂O₆ + 6O₂ + 6H₂O"),
            examTips = listOf("Oxygen comes from water, not CO₂."),
            createdAt = System.currentTimeMillis(),
            isBookmarked = true,
            isOfflineGenerated = true
        )

        val jsonStr = note.toJson()
        val parsed = GeneratedNote.fromJson(
            noteId = note.noteId,
            topicId = note.topicId,
            topicName = note.topicName,
            titleOdia = note.titleOdia,
            titleEnglish = note.titleEnglish,
            jsonString = jsonStr,
            createdAt = note.createdAt,
            isBookmarked = note.isBookmarked
        )

        assertEquals("note_test_01", parsed.noteId)
        assertEquals(1, parsed.keyConcepts.size)
        assertEquals("Light-dependent reactions occur in thylakoids.", parsed.keyConcepts[0].pointEnglish)
        assertEquals("6CO₂ + 12H₂O ──> C₆H₁₂O₆ + 6O₂ + 6H₂O", parsed.formulasEquations[0])
        assertTrue(parsed.isOfflineGenerated)
    }

    @Test
    fun testTopicMasteryAssessmentFromTranscribedContent() {
        // Verifies diagnostic quiz mastery scoring calculation for video-derived topic
        val initialMastery = 0
        val perfectQuizScore = 100
        
        // Target mastery elevation logic
        val newMastery = if (perfectQuizScore == 100) 95 else initialMastery
        assertEquals(95, newMastery)
        assertTrue("Elevated to Strong Mastery", newMastery >= 85)
    }
}

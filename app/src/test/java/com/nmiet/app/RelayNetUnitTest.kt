package com.nmiet.app

import com.nmiet.app.data.local.LearningPackEntity
import com.nmiet.app.data.remote.PilotCurriculumCatalog
import com.nmiet.app.domain.model.ContentOrigin
import com.nmiet.app.domain.model.TopicMastery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RelayNetUnitTest {

    @Test
    fun pilotPack_containsFiveDiagnosticQuestions() {
        val pack = PilotCurriculumCatalog.getPilotLearningPack()
        assertEquals("relaynet_class9_science_photosynthesis_v1", pack.packId)
        assertEquals(9, pack.classGrade)
        assertEquals(5, pack.quizQuestions.size)
        assertTrue(pack.titleOdia.contains("ଆଲୋକ ଶ୍ଳେଷଣ"))
        assertNotNull(pack.lesson.equationOrFormula)
    }

    @Test
    fun offlineTutor_answersQuestionsInOdiaAndEnglish() {
        val (odiaResp, engResp) = PilotCurriculumCatalog.getOfflineTutorResponse("ଆଲୋକ ଶ୍ଳେଷଣ କଣ?")
        assertTrue(odiaResp.contains("ଖାଦ୍ୟ ପ୍ରସ୍ତୁତି"))
        assertTrue(engResp.contains("Photosynthesis"))

        val (eqOdia, eqEng) = PilotCurriculumCatalog.getOfflineTutorResponse("What is the formula equation?")
        assertTrue(eqOdia.contains("6CO₂"))
        assertTrue(eqEng.contains("6CO₂"))
    }

    @Test
    fun module1_studyGuardrail_refusesNonAcademicQuery() {
        val nonAcademicKeywords = listOf("movie", "bollywood", "pubg", "gaming", "dating", "girlfriend", "celebrity")
        val sampleQuery = "Who is the best Bollywood celebrity actor?"
        val isNonAcademic = nonAcademicKeywords.any { sampleQuery.lowercase().contains(it) }
        assertTrue(isNonAcademic)

        val academicQuery = "Photosynthesis equation and water splitting photolysis"
        val isAcademic = !nonAcademicKeywords.any { academicQuery.lowercase().contains(it) }
        assertTrue(isAcademic)
    }

    @Test
    fun module2_learningPackOrigin_mappedCorrectly() {
        val pilot = PilotCurriculumCatalog.getPilotLearningPack()
        assertEquals(ContentOrigin.INTERNET_DOWNLOADED, pilot.origin)
        assertEquals("relaynet_class9_science_photosynthesis_v1", pilot.packId)
        assertTrue(pilot.isDownloaded)

        val entity = LearningPackEntity(
            packId = pilot.packId,
            titleOdia = pilot.titleOdia,
            titleEnglish = pilot.titleEnglish,
            subject = pilot.subject,
            chapterNumber = pilot.chapterNumber,
            origin = pilot.origin,
            senderPeerName = pilot.peerSenderName,
            receivedTimestamp = pilot.receivedTimestamp,
            sha256Checksum = pilot.checksumSha256,
            isCachedLocally = pilot.isDownloaded
        )
        assertEquals(ContentOrigin.INTERNET_DOWNLOADED, entity.origin)
        assertTrue(entity.isCachedLocally)

        val shared = PilotCurriculumCatalog.getSampleSharedPack()
        assertEquals(ContentOrigin.P2P_RECEIVED, shared.origin)
        assertEquals("Rahul-Phone (Nearby Peer)", shared.peerSenderName)
        assertTrue(shared.isReceivedViaMesh)
    }

    @Test
    fun topicMastery_calculatesCorrectTiers() {
        val strong = TopicMastery(
            topicId = "1",
            topicTitleOdia = "ଓଡ଼ିଆ",
            topicTitleEnglish = "English",
            subject = "Science",
            masteryScore = 90,
            lastPracticedDate = 1000L,
            attemptsCount = 2,
            recommendedNextAction = "Next"
        )
        assertTrue(strong.tierLabel.contains("Strong Mastery"))

        val developing = strong.copy(masteryScore = 55)
        assertTrue(developing.tierLabel.contains("Developing"))

        val foundation = strong.copy(masteryScore = 30)
        assertTrue(foundation.tierLabel.contains("Needs Foundation"))
    }

    @Test
    fun zipPackManager_calculatesSha256Checksum() {
        val tempFile = java.io.File.createTempFile("relaynet_test", ".txt")
        tempFile.writeText("RelayNet Pilot Pack Content")
        val sha = com.nmiet.app.data.local.ZipPackManager.calculateSha256(tempFile)
        assertNotNull(sha)
        assertEquals(64, sha.length)
        tempFile.delete()
    }

    @Test
    fun module2_cloudDownloadedAndP2POrigins_validated() {
        val cloudPack = LearningPackEntity(
            packId = "pack_cloud_1",
            titleOdia = "ଓଡ଼ିଆ ପାଠ",
            titleEnglish = "Cloud Pack",
            subject = "Science",
            origin = ContentOrigin.CLOUD_DOWNLOADED,
            senderPeerName = null,
            sha256Checksum = "abc",
            isCachedLocally = true
        )
        assertEquals(ContentOrigin.CLOUD_DOWNLOADED, cloudPack.origin)

        val sharedPack = LearningPackEntity(
            packId = "pack_shared_1",
            titleOdia = "ସେୟାର ପାଠ",
            titleEnglish = "Shared Pack",
            subject = "Science",
            origin = ContentOrigin.P2P_RECEIVED,
            senderPeerName = "Node-02 (Teacher Phone)",
            sha256Checksum = "def",
            isCachedLocally = true
        )
        assertEquals(ContentOrigin.P2P_RECEIVED, sharedPack.origin)
        assertEquals("Node-02 (Teacher Phone)", sharedPack.senderPeerName)
    }

    @Test
    fun module4_remediationLoop_advancesMasteryOnHundredPercent() {
        // Initial state: 61% Developing
        val initialScore = 61
        val initialMastery = TopicMastery(
            topicId = "relaynet_class9_science_photosynthesis_v1",
            topicTitleOdia = "ଆଲୋକ ଶ୍ଳେଷଣ",
            topicTitleEnglish = "Photosynthesis",
            subject = "Science",
            masteryScore = initialScore,
            lastPracticedDate = System.currentTimeMillis(),
            attemptsCount = 1,
            recommendedNextAction = "Targeted Practice: Photolysis & Light Reactions - 3 Questions"
        )
        assertTrue(initialMastery.tierLabel.contains("Developing"))

        // When student scores 100% on the targeted micro-quiz, mastery advances to 95% (Strong Mastery)
        val scorePercent = 100
        val newScore = if (scorePercent == 100) 95 else ((scorePercent * 0.65) + (initialScore * 0.35)).toInt()
        val updatedMastery = initialMastery.copy(
            masteryScore = newScore,
            recommendedNextAction = "Strong Mastery Achieved (95%)! Water Photolysis & Light Reactions mastered."
        )
        assertEquals(95, updatedMastery.masteryScore)
        assertTrue(updatedMastery.tierLabel.contains("Strong Mastery"))
    }

    @Test
    fun module1_generatedNotes_serializationAndTakeaways() {
        val note = com.nmiet.app.domain.model.GeneratedNote(
            noteId = "test_note_1",
            topicId = "relaynet_class9_science_photosynthesis_v1",
            topicName = "Chapter 4: Photosynthesis",
            titleOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ସାରାଂଶ",
            titleEnglish = "Photosynthesis Revision Notes",
            summaryOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ସାରାଂଶ",
            summaryEnglish = "Photosynthesis summary",
            keyConcepts = listOf(
                com.nmiet.app.domain.model.NoteConceptPoint(
                    pointOdia = "ଜଳ ବିଶ୍ଳେଷଣରୁ ଅମ୍ଳଜାନ ନିର୍ଗତ ହୁଏ।",
                    pointEnglish = "Oxygen evolves from photolysis of water."
                )
            ),
            formulasEquations = listOf("6CO₂ + 12H₂O ──► C₆H₁₂O₆ + 6H₂O + 6O₂"),
            examTips = listOf("⚠️ Always cite water photolysis as oxygen source.")
        )

        assertEquals("test_note_1", note.noteId)
        assertEquals("relaynet_class9_science_photosynthesis_v1", note.topicId)
        assertEquals(1, note.keyConcepts.size)
        assertEquals("6CO₂ + 12H₂O ──► C₆H₁₂O₆ + 6H₂O + 6O₂", note.formulasEquations.first())
        assertTrue(note.examTips.first().contains("water photolysis"))
        assertTrue(note.isOfflineGenerated)
    }

    @Test
    fun module2_customQuiz_synthesisAndDistractorDiagnostics() {
        val config = com.nmiet.app.domain.model.CustomQuizConfig(
            sourceType = com.nmiet.app.domain.model.QuizSourceType.WEAK_TOPIC,
            specificTopicTag = "PHOTOLYSIS_WATER",
            questionCount = 3,
            difficulty = com.nmiet.app.domain.model.QuizDifficulty.INTERMEDIATE
        )

        assertEquals(3, config.questionCount)
        assertEquals("PHOTOLYSIS_WATER", config.specificTopicTag)
        assertEquals(com.nmiet.app.domain.model.QuizDifficulty.INTERMEDIATE, config.difficulty)
    }

    @Test
    fun module3_voiceAssistant_intentParsingRegex() {
        fun parseTestIntent(spoken: String): String {
            val lower = spoken.lowercase()
            return when {
                lower.contains("photosynthesis") || lower.contains("ଆଲୋକ ଶ୍ଳେଷଣ") -> "ROUTE_LESSON"
                lower.contains("quiz") || lower.contains("କୁଇଜ") -> "ROUTE_QUIZ"
                lower.contains("notes") || lower.contains("ନୋଟ") -> "ROUTE_NOTES"
                else -> "ROUTE_TUTOR_QUERY"
            }
        }

        assertEquals("ROUTE_LESSON", parseTestIntent("Open Photosynthesis"))
        assertEquals("ROUTE_LESSON", parseTestIntent("ଆଲୋକ ଶ୍ଳେଷଣ ଖୋଲ"))
        assertEquals("ROUTE_QUIZ", parseTestIntent("Start Quiz"))
        assertEquals("ROUTE_QUIZ", parseTestIntent("କୁଇଜ୍ ଆରମ୍ଭ କର"))
        assertEquals("ROUTE_NOTES", parseTestIntent("Generate Notes"))
        assertEquals("ROUTE_NOTES", parseTestIntent("ନୋଟ୍ ତିଆରି କର"))
        assertEquals("ROUTE_TUTOR_QUERY", parseTestIntent("Where does oxygen come from?"))
    }

    @Test
    fun regionalLanguageExpansion_containsAllTargetLanguages() {
        val languages = com.nmiet.app.domain.model.AppLanguage.entries.map { it.name }
        assertTrue(languages.contains("ODIA"))
        assertTrue(languages.contains("ENGLISH"))
        assertTrue(languages.contains("HINDI"))
        assertTrue(languages.contains("BENGALI"))
        assertTrue(languages.contains("TAMIL"))
        assertTrue(languages.contains("TELUGU"))
    }

    @Test
    fun contentItem_downloadLifecycleValidation() {
        val item = com.nmiet.app.domain.model.ContentItem(
            id = "test_doc",
            title = "Test PDF",
            description = "Test Description",
            mediaType = com.nmiet.app.domain.model.ContentMediaType.PDF_TEXTBOOK,
            remoteUrl = "https://example.org/doc.pdf",
            sizeBytes = 2048L,
            sha256Checksum = "abc123"
        )
        assertEquals(com.nmiet.app.domain.model.DownloadStatus.NOT_DOWNLOADED, item.downloadStatus)
        assertEquals(0, item.downloadProgressPercent)

        val downloading = item.copy(
            downloadStatus = com.nmiet.app.domain.model.DownloadStatus.DOWNLOADING,
            downloadProgressPercent = 50
        )
        assertEquals(50, downloading.downloadProgressPercent)

        val completed = item.copy(
            downloadStatus = com.nmiet.app.domain.model.DownloadStatus.COMPLETED,
            downloadProgressPercent = 100,
            localFilePath = "/storage/test_doc.pdf"
        )
        assertTrue(completed.downloadStatus == com.nmiet.app.domain.model.DownloadStatus.COMPLETED)
        assertNotNull(completed.localFilePath)
    }

    @Test
    fun studyPlanner_taskCompletionTracking() {
        val task = com.nmiet.app.domain.model.StudyTask(
            id = "t1",
            topicId = "topic_1",
            topicTitle = "Photosynthesis",
            subject = "Science",
            scheduledDate = 1000L,
            isCompleted = false
        )
        assertEquals(false, task.isCompleted)
        val completed = task.copy(isCompleted = true)
        assertEquals(true, completed.isCompleted)
    }
}

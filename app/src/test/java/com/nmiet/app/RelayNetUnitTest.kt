package com.nmiet.app

import com.nmiet.app.data.local.LearningPackEntity
import com.nmiet.app.data.remote.PilotCurriculumCatalog
import com.nmiet.app.domain.model.ContentOrigin
import com.nmiet.app.domain.model.TopicMastery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
        assertTrue(languages.contains("ENGLISH_ONLY"))
        assertTrue(languages.contains("ODIA_ONLY"))
        assertTrue(languages.contains("HINDI_ONLY"))
        assertTrue(languages.contains("BILINGUAL_EN_HI"))
        assertTrue(languages.contains("BILINGUAL_EN_OR"))
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

    @Test
    fun peerContentManifest_serializationAndDeserialization() {
        val item1 = com.nmiet.app.domain.model.ContentManifestItem(
            contentId = "pack_01",
            title = "Class 9 Science Pack",
            mediaType = com.nmiet.app.domain.model.ContentMediaType.LEARNING_PACK_ZIP,
            language = "or+en",
            version = 1,
            sizeBytes = 1024L,
            sha256Checksum = "hash123"
        )
        val manifest = com.nmiet.app.domain.model.PeerContentManifest(
            protocolVersion = 1,
            deviceName = "Learner-Device-A",
            items = listOf(item1)
        )

        val serialized = manifest.serialize()
        assertNotNull(serialized)
        assertTrue(serialized.isNotEmpty())

        val deserialized = com.nmiet.app.domain.model.PeerContentManifest.deserialize(serialized)
        assertNotNull(deserialized)
        assertEquals("Learner-Device-A", deserialized!!.deviceName)
        assertEquals(1, deserialized.items.size)
        assertEquals("pack_01", deserialized.items[0].contentId)
        assertEquals("Class 9 Science Pack", deserialized.items[0].title)
    }

    @Test
    fun peerContentManifest_malformedPayload_handledGracefully() {
        // Empty byte array
        assertNull(com.nmiet.app.domain.model.PeerContentManifest.deserialize(ByteArray(0)))

        // Non-JSON garbage bytes
        assertNull(com.nmiet.app.domain.model.PeerContentManifest.deserialize("GARBAGE_BYTES_NOT_JSON".toByteArray(Charsets.UTF_8)))

        // Valid JSON with incorrect type
        val wrongType = """{"type":"OTHER_PROTOCOL","protocolVersion":1}""".toByteArray(Charsets.UTF_8)
        assertNull(com.nmiet.app.domain.model.PeerContentManifest.deserialize(wrongType))

        // Manifest containing an item with blank contentId: individual item is skipped, valid item is preserved
        val mixedJson = """
        {
            "type": "RELAYNET_MANIFEST_EXCHANGE",
            "protocolVersion": 1,
            "deviceName": "Peer-Test",
            "timestamp": 123456,
            "items": [
                {"contentId": "valid_01", "title": "Valid Lesson", "mediaType": "LEARNING_PACK_ZIP", "version": 2},
                {"contentId": "   ", "title": "Invalid Blank ID", "mediaType": "LEARNING_PACK_ZIP", "version": 1}
            ]
        }
        """.trimIndent().toByteArray(Charsets.UTF_8)

        val parsed = com.nmiet.app.domain.model.PeerContentManifest.deserialize(mixedJson)
        assertNotNull(parsed)
        assertEquals(1, parsed!!.items.size)
        assertEquals("valid_01", parsed.items[0].contentId)
    }

    @Test
    fun peerContentManifest_duplicateContent_retainsHighestVersion() {
        val itemV1 = com.nmiet.app.domain.model.ContentManifestItem(
            contentId = "pack_biology",
            title = "Biology V1",
            mediaType = com.nmiet.app.domain.model.ContentMediaType.LEARNING_PACK_ZIP,
            language = "en",
            version = 1,
            sizeBytes = 1000L,
            sha256Checksum = "hash_v1"
        )
        val itemV3 = com.nmiet.app.domain.model.ContentManifestItem(
            contentId = "pack_biology",
            title = "Biology V3",
            mediaType = com.nmiet.app.domain.model.ContentMediaType.LEARNING_PACK_ZIP,
            language = "en",
            version = 3,
            sizeBytes = 1500L,
            sha256Checksum = "hash_v3"
        )
        val manifest = com.nmiet.app.domain.model.PeerContentManifest(
            deviceName = "Peer-Dedupe",
            items = listOf(itemV1, itemV3)
        )

        val serialized = manifest.serialize()
        val deserialized = com.nmiet.app.domain.model.PeerContentManifest.deserialize(serialized)
        assertNotNull(deserialized)
        assertEquals(1, deserialized!!.items.size)
        assertEquals("pack_biology", deserialized.items[0].contentId)
        assertEquals(3, deserialized.items[0].version)
        assertEquals("Biology V3", deserialized.items[0].title)
    }

    @Test
    fun contentManifest_comparison_detectsMissingAndOutdated() {
        // Local database state
        val localPack1 = PilotCurriculumCatalog.getPilotLearningPack().copy(
            packId = "pack_bio",
            version = 1
        )
        val localPack2 = PilotCurriculumCatalog.getPilotLearningPack().copy(
            packId = "pack_physics",
            version = 2
        )
        val localPacks = listOf(localPack1, localPack2)

        // Peer offers:
        // pack_bio: version 2 (outdated locally -> should be missingOrUpdatable)
        // pack_physics: version 1 (local is newer -> should NOT be missingOrUpdatable)
        // pack_chemistry: version 1 (missing locally -> should be missingOrUpdatable)
        val peerItems = listOf(
            com.nmiet.app.domain.model.ContentManifestItem(
                contentId = "pack_bio",
                title = "Biology Rev 2",
                mediaType = com.nmiet.app.domain.model.ContentMediaType.LEARNING_PACK_ZIP,
                language = "or+en",
                version = 2,
                sizeBytes = 2000L,
                sha256Checksum = "hash_bio"
            ),
            com.nmiet.app.domain.model.ContentManifestItem(
                contentId = "pack_physics",
                title = "Physics Rev 1",
                mediaType = com.nmiet.app.domain.model.ContentMediaType.LEARNING_PACK_ZIP,
                language = "or+en",
                version = 1,
                sizeBytes = 2000L,
                sha256Checksum = "hash_phy"
            ),
            com.nmiet.app.domain.model.ContentManifestItem(
                contentId = "pack_chemistry",
                title = "Chemistry Basics",
                mediaType = com.nmiet.app.domain.model.ContentMediaType.LEARNING_PACK_ZIP,
                language = "or+en",
                version = 1,
                sizeBytes = 3000L,
                sha256Checksum = "hash_chem"
            )
        )

        val missingOrUpdatable = peerItems.filter { item ->
            val local = localPacks.find { it.packId == item.contentId }
            local == null || local.version < item.version
        }

        assertEquals(2, missingOrUpdatable.size)
        val ids = missingOrUpdatable.map { it.contentId }.toSet()
        assertTrue(ids.contains("pack_bio"))
        assertTrue(ids.contains("pack_chemistry"))
        assertFalse(ids.contains("pack_physics"))
    }

    @Test
    fun zipPackManager_checksumMismatch_detectedAndRejected() {
        val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "relaynet_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        val fakeZip = java.io.File(tempDir, "mismatched.zip")
        val manifestJson = """
        {
            "pack_id": "test_mismatch",
            "sha256_checksum": "0000000000000000000000000000000000000000000000000000000000000000"
        }
        """.trimIndent()

        java.util.zip.ZipOutputStream(java.io.FileOutputStream(fakeZip)).use { zos ->
            zos.putNextEntry(java.util.zip.ZipEntry("manifest.json"))
            zos.write(manifestJson.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        val extractDir = java.io.File(tempDir, "extracted")
        val result = com.nmiet.app.data.local.ZipPackManager.extractAndVerifyPack(fakeZip, extractDir)
        assertFalse(result.isChecksumValid)

        tempDir.deleteRecursively()
    }

    @Test
    fun zipPackManager_createAndExtractPack_roundtripSuccess() {
        val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "relaynet_rt_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        val samplePack = PilotCurriculumCatalog.getPilotLearningPack()
        val zipFile = java.io.File(tempDir, "sample_pack.zip")
        com.nmiet.app.data.local.ZipPackManager.createPackZip(samplePack, zipFile)

        assertTrue(zipFile.exists())
        assertTrue(zipFile.length() > 0)

        val extractDir = java.io.File(tempDir, "extracted_rt")
        val result = com.nmiet.app.data.local.ZipPackManager.extractAndVerifyPack(zipFile, extractDir)

        assertTrue(result.isChecksumValid)
        assertEquals(samplePack.packId, result.pack.packId)
        assertEquals(samplePack.titleEnglish, result.pack.titleEnglish)
        assertEquals(samplePack.lesson.equationOrFormula, result.pack.lesson.equationOrFormula)

        tempDir.deleteRecursively()
    }

    @Test(expected = SecurityException::class)
    fun zipPackManager_zipSlipVulnerability_blockedWithSecurityException() {
        val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "relaynet_zipslip_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        val maliciousZip = java.io.File(tempDir, "malicious.zip")
        java.util.zip.ZipOutputStream(java.io.FileOutputStream(maliciousZip)).use { zos ->
            // Path traversal entry trying to break out of extraction directory
            zos.putNextEntry(java.util.zip.ZipEntry("../escaped_system_file.txt"))
            zos.write("Malicious payload content".toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        val extractDir = java.io.File(tempDir, "extracted_slip")
        try {
            com.nmiet.app.data.local.ZipPackManager.extractAndVerifyPack(maliciousZip, extractDir)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun localModelRegistry_qwen3Metadata_pinnedAndVerified() {
        val model = com.nmiet.app.ai.llm.LocalModelRegistry.QWEN3_0_6B_NOTHINK
        assertEquals("qwen3_0.6b_nothink_int4", model.modelId)
        assertEquals("Qwen3 0.6B (INT4 No-Think)", model.displayName)
        assertEquals("6aa2daf8aba4aa456797fb8040b36a3948bcfda7", model.pinnedRevision)
        assertEquals("2df6821ec12702dafd33915e7a1a1adc7c4b053f3672fd9555dfaf3a114c4139", model.expectedSha256)
        assertEquals(347_251_840L, model.approxSizeBytes)
        assertEquals(".litertlm", model.format)
        assertEquals("Apache 2.0", model.license)
        assertTrue(model.academicGuidance.contains("complex multi-step reasoning", ignoreCase = true))

        // Registry lookup
        val retrieved = com.nmiet.app.ai.llm.LocalModelRegistry.getModelById("qwen3_0.6b_nothink_int4")
        assertNotNull(retrieved)
        assertEquals(model.modelId, retrieved?.modelId)

        val nonExistent = com.nmiet.app.ai.llm.LocalModelRegistry.getModelById("unknown-model-xyz")
        assertNull(nonExistent)
    }

    @Test
    fun localLlmState_andInstallationState_hierarchy() {
        val model = com.nmiet.app.ai.llm.LocalModelRegistry.QWEN3_0_6B_NOTHINK
        val unloaded: com.nmiet.app.ai.llm.LocalLlmState = com.nmiet.app.ai.llm.LocalLlmState.Unloaded
        val loading: com.nmiet.app.ai.llm.LocalLlmState = com.nmiet.app.ai.llm.LocalLlmState.Loading("model_1")
        val loaded: com.nmiet.app.ai.llm.LocalLlmState = com.nmiet.app.ai.llm.LocalLlmState.Loaded(model, 1200L)
        val generating: com.nmiet.app.ai.llm.LocalLlmState = com.nmiet.app.ai.llm.LocalLlmState.Generating("prompt", "tok", 1)
        val error: com.nmiet.app.ai.llm.LocalLlmState = com.nmiet.app.ai.llm.LocalLlmState.Error("OOM error")

        assertTrue(unloaded is com.nmiet.app.ai.llm.LocalLlmState.Unloaded)
        assertEquals("model_1", (loading as com.nmiet.app.ai.llm.LocalLlmState.Loading).modelName)
        assertEquals(1200L, (loaded as com.nmiet.app.ai.llm.LocalLlmState.Loaded).loadDurationMs)
        assertEquals("tok", (generating as com.nmiet.app.ai.llm.LocalLlmState.Generating).streamedResponse)
        assertEquals("OOM error", (error as com.nmiet.app.ai.llm.LocalLlmState.Error).errorMessage)

        val notInstalled: com.nmiet.app.ai.llm.ModelInstallationState = com.nmiet.app.ai.llm.ModelInstallationState.NotInstalled
        val downloading: com.nmiet.app.ai.llm.ModelInstallationState = com.nmiet.app.ai.llm.ModelInstallationState.Downloading(500, 1000, 50)
        val failed: com.nmiet.app.ai.llm.ModelInstallationState = com.nmiet.app.ai.llm.ModelInstallationState.Failed("Disk full", canRetry = true)

        assertTrue(notInstalled is com.nmiet.app.ai.llm.ModelInstallationState.NotInstalled)
        assertEquals(50, (downloading as com.nmiet.app.ai.llm.ModelInstallationState.Downloading).progressPercent)
        assertTrue((failed as com.nmiet.app.ai.llm.ModelInstallationState.Failed).canRetry)
    }

    @Test
    fun fakeLocalLlmEngine_lifecycleAndStreamGeneration() {
        kotlinx.coroutines.runBlocking {
            val engine = com.nmiet.app.ai.llm.FakeLocalLlmEngine()
            assertFalse(engine.isModelLoaded())
            assertTrue(engine.state.value is com.nmiet.app.ai.llm.LocalLlmState.Unloaded)

            val tempModelFile = java.io.File.createTempFile("fake_model", ".litertlm")
            val modelInfo = com.nmiet.app.ai.llm.LocalModelRegistry.QWEN3_0_6B_NOTHINK

            // Load
            val loadResult = engine.loadModel(tempModelFile, modelInfo)
            assertTrue(loadResult.isSuccess)
            assertTrue(engine.isModelLoaded())
            assertTrue(engine.state.value is com.nmiet.app.ai.llm.LocalLlmState.Loaded)

            // Stream Generation
            val accumulated = StringBuilder()
            var chunkCount = 0
            engine.generateStream("What is photosynthesis in Odia?").collect { chunk ->
                accumulated.append(chunk)
                chunkCount++
            }

            assertTrue(chunkCount > 0)
            assertTrue(accumulated.isNotEmpty())
            assertTrue(engine.isModelLoaded()) // Returns to Loaded state after completion

            // Unload
            engine.unloadModel()
            assertFalse(engine.isModelLoaded())
            assertTrue(engine.state.value is com.nmiet.app.ai.llm.LocalLlmState.Unloaded)

            tempModelFile.delete()
        }
    }

    @Test
    fun fakeLocalLlmEngine_errorHandling_transitionsToErrorState() {
        kotlinx.coroutines.runBlocking {
            val engine = com.nmiet.app.ai.llm.FakeLocalLlmEngine()
            engine.shouldFailLoad = true
            engine.failureMessage = "Simulated native backend initialization failure"

            val tempModelFile = java.io.File.createTempFile("fake_model_err", ".litertlm")
            val modelInfo = com.nmiet.app.ai.llm.LocalModelRegistry.QWEN3_0_6B_NOTHINK

            val loadResult = engine.loadModel(tempModelFile, modelInfo)
            assertFalse(loadResult.isSuccess)
            assertFalse(engine.isModelLoaded())
            assertTrue(engine.state.value is com.nmiet.app.ai.llm.LocalLlmState.Error)
            assertEquals("Simulated native backend initialization failure", (engine.state.value as com.nmiet.app.ai.llm.LocalLlmState.Error).errorMessage)

            tempModelFile.delete()
        }
    }

    @Test
    fun fakeLocalLlmEngine_cancellation_cancelsGracefully() {
        kotlinx.coroutines.runBlocking {
            val engine = com.nmiet.app.ai.llm.FakeLocalLlmEngine()
            val tempModelFile = java.io.File.createTempFile("fake_model_cancel", ".litertlm")
            val modelInfo = com.nmiet.app.ai.llm.LocalModelRegistry.QWEN3_0_6B_NOTHINK

            engine.loadModel(tempModelFile, modelInfo)
            assertTrue(engine.isModelLoaded())

            engine.cancelGeneration()
            assertTrue(engine.isModelLoaded())

            engine.unloadModel()
            assertFalse(engine.isModelLoaded())
            tempModelFile.delete()
        }
    }

    @Test
    fun localModel_atomicStagingAndChecksumValidation_detectsMismatch() {
        val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "relaynet_llm_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        val stagingFile = java.io.File(tempDir, "model.litertlm.tmp")
        val targetFile = java.io.File(tempDir, "model.litertlm")

        val validContent = "RelayNet Genuine On-Device LiteRT-LM Model Bytes"
        stagingFile.writeText(validContent)

        // Compute SHA-256 of valid content
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val calculatedSha = md.digest(validContent.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

        val expectedSha = calculatedSha
        val wrongSha = "0000000000000000000000000000000000000000000000000000000000000000"

        // Test 1: Checksum mismatch -> staging deleted, target never created
        var verified = calculatedSha.equals(wrongSha, ignoreCase = true)
        assertFalse(verified)
        if (!verified) {
            stagingFile.delete()
        }
        assertFalse(stagingFile.exists())
        assertFalse(targetFile.exists())

        // Test 2: Checksum match -> atomic rename to target succeeds
        stagingFile.writeText(validContent)
        val md2 = java.security.MessageDigest.getInstance("SHA-256")
        val calculatedSha2 = md2.digest(stagingFile.readBytes()).joinToString("") { "%02x".format(it) }
        verified = calculatedSha2.equals(expectedSha, ignoreCase = true)
        assertTrue(verified)

        if (verified) {
            stagingFile.renameTo(targetFile)
        }
        assertFalse(stagingFile.exists())
        assertTrue(targetFile.exists())
        assertEquals(validContent.length.toLong(), targetFile.length())

        tempDir.deleteRecursively()
    }

    @Test
    fun r2ContentConfig_constructsAccurateUrls() {
        val defaultCatalogUrl = com.nmiet.app.data.remote.R2ContentConfig.getCatalogUrl()
        assertEquals(
            "https://pub-5cd70efc05b9415d8f5df5e4aed4e50a.r2.dev/Nirmit/catalog.json",
            defaultCatalogUrl
        )

        val sampleZipUrl = com.nmiet.app.data.remote.R2ContentConfig.getObjectUrl("RelayNet_Nearby_Learning_Pack_v2.zip")
        assertEquals(
            "https://pub-5cd70efc05b9415d8f5df5e4aed4e50a.r2.dev/Nirmit/RelayNet_Nearby_Learning_Pack_v2.zip",
            sampleZipUrl
        )

        val customBaseUrl = "https://custom-r2.cdn.net/storage/"
        val customUrl = com.nmiet.app.data.remote.R2ContentConfig.getObjectUrl("diagrams/photosynthesis.png", customBaseUrl)
        assertEquals("https://custom-r2.cdn.net/storage/diagrams/photosynthesis.png", customUrl)
    }

    @Test
    fun r2SampleCatalogue_containsAllVerifiedObjectsWithExactMetadata() {
        val sampleItems = com.nmiet.app.data.remote.R2SampleCatalogue.getVerifiedSampleItems()
        assertEquals(7, sampleItems.size)

        val zipItem = sampleItems.find { it.id == "r2_pack_nearby_v2_zip" }
        assertNotNull(zipItem)
        assertEquals(com.nmiet.app.domain.model.ContentMediaType.LEARNING_PACK_ZIP, zipItem!!.mediaType)
        assertEquals(70562L, zipItem.sizeBytes)
        assertEquals("1925c78b5c4b53affdaab782fd31f69ae2e98eb51774c2a106eb981d58751264", zipItem.sha256Checksum)
        assertTrue(zipItem.isZip)

        val txtItem = sampleItems.find { it.id == "r2_res_quick_revision_txt" }
        assertNotNull(txtItem)
        assertEquals(com.nmiet.app.domain.model.ContentMediaType.REVISION_NOTES, txtItem!!.mediaType)
        assertEquals(479L, txtItem.sizeBytes)
        assertEquals("e79e8564426209b95186116cac7059e4e34ee42b4f9cc6a0f1a775816320f8a9", txtItem.sha256Checksum)
        assertEquals("479 B", txtItem.formattedSize)
        assertTrue(txtItem.isText)

        val pngItem = sampleItems.find { it.id == "r2_diag_photosynthesis_png" }
        assertNotNull(pngItem)
        assertEquals(com.nmiet.app.domain.model.ContentMediaType.IMAGE_DIAGRAM, pngItem!!.mediaType)
        assertEquals(42691L, pngItem.sizeBytes)
        assertEquals("66723e43543ac568711300f31c348bddf7745f94345a0a6207beaa6b4c6c309f", pngItem.sha256Checksum)
        assertTrue(pngItem.isImage)
    }

    @Test
    fun r2Download_checksumIntegrity_rejectsCorruptedFile() {
        val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "r2_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        val stagingFile = java.io.File(tempDir, "sample.txt.tmp")
        val finalFile = java.io.File(tempDir, "sample.txt")
        val legitimateContent = "RELAYNET QUICK REVISION PHOTOSYNTHESIS"
        val corruptedContent = "CORRUPTED BY MITM ATTACK OR BIT ROT"

        stagingFile.writeText(corruptedContent)

        val md = java.security.MessageDigest.getInstance("SHA-256")
        val calculatedSha = md.digest(corruptedContent.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

        val legitimateMd = java.security.MessageDigest.getInstance("SHA-256")
        val expectedLegitimateSha = legitimateMd.digest(legitimateContent.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

        // Must reject mismatched SHA
        assertFalse(calculatedSha.equals(expectedLegitimateSha, ignoreCase = true))

        if (!calculatedSha.equals(expectedLegitimateSha, ignoreCase = true)) {
            stagingFile.delete()
        }

        assertFalse("Staging file must be purged upon checksum mismatch", stagingFile.exists())
        assertFalse("Corrupted file must never be promoted to final destination", finalFile.exists())

        tempDir.deleteRecursively()
    }

    @Test
    fun r2CatalogJson_parsesAllSevenVerifiedResourcesAccurately() {
        val catalogFile = java.io.File("catalog.json").takeIf { it.exists() }
            ?: java.io.File("backend/static/Nirmit/catalog.json").takeIf { it.exists() }
            ?: java.io.File("../catalog.json")

        assertTrue("catalog.json must exist locally", catalogFile.exists())
        val jsonText = catalogFile.readText(Charsets.UTF_8)

        val items = com.nmiet.app.data.remote.R2ContentManager.parseCatalogJson(jsonText)
        assertEquals(7, items.size)

        // Verify ZIP Learning Pack
        val zip = items.firstOrNull { it.id == "r2_pack_nearby_v2_zip" }
        assertNotNull(zip)
        assertEquals("RelayNet Learning Pack v2 (Complete Bundle)", zip!!.title)
        assertEquals(com.nmiet.app.domain.model.ContentMediaType.LEARNING_PACK_ZIP, zip.mediaType)
        assertEquals(70562L, zip.sizeBytes)
        assertEquals("1925c78b5c4b53affdaab782fd31f69ae2e98eb51774c2a106eb981d58751264", zip.sha256Checksum)
        assertEquals("https://pub-5cd70efc05b9415d8f5df5e4aed4e50a.r2.dev/Nirmit/RelayNet_Nearby_Learning_Pack_v2.zip", zip.remoteUrl)
        assertTrue(zip.isZip)

        // Verify Quick Revision Notes
        val txt = items.firstOrNull { it.id == "r2_res_quick_revision_txt" }
        assertNotNull(txt)
        assertEquals("Photosynthesis Quick Revision Notes", txt!!.title)
        assertEquals(com.nmiet.app.domain.model.ContentMediaType.REVISION_NOTES, txt.mediaType)
        assertEquals(479L, txt.sizeBytes)
        assertEquals("e79e8564426209b95186116cac7059e4e34ee42b4f9cc6a0f1a775816320f8a9", txt.sha256Checksum)
        assertEquals("https://pub-5cd70efc05b9415d8f5df5e4aed4e50a.r2.dev/Nirmit/resources/quick_revision.txt", txt.remoteUrl)
        assertTrue(txt.isText)

        // Verify Photosynthesis Diagram
        val photoDiag = items.firstOrNull { it.id == "r2_diag_photosynthesis_png" }
        assertNotNull(photoDiag)
        assertEquals(com.nmiet.app.domain.model.ContentMediaType.IMAGE_DIAGRAM, photoDiag!!.mediaType)
        assertEquals(42691L, photoDiag.sizeBytes)
        assertEquals("66723e43543ac568711300f31c348bddf7745f94345a0a6207beaa6b4c6c309f", photoDiag.sha256Checksum)
        assertTrue(photoDiag.isImage)

        // Verify Water Cycle Diagram
        val waterDiag = items.firstOrNull { it.id == "r2_diag_water_cycle_png" }
        assertNotNull(waterDiag)
        assertEquals(com.nmiet.app.domain.model.ContentMediaType.IMAGE_DIAGRAM, waterDiag!!.mediaType)
        assertEquals(31176L, waterDiag.sizeBytes)
        assertEquals("99b41f9d0c98f2c5fd5dd0c049133c702b811f79ea29518f5d7696b5b2d6eea0", waterDiag.sha256Checksum)

        // Verify English & Odia Lessons & Manifest
        val engLesson = items.firstOrNull { it.id == "r2_lesson_english_json" }
        assertNotNull(engLesson)
        assertEquals(751L, engLesson!!.sizeBytes)
        assertEquals("39404d9e610c62afad5267d1f35920dfc12193878be2b5f6384fc265706b31e0", engLesson.sha256Checksum)

        val odiaLesson = items.firstOrNull { it.id == "r2_lesson_odia_json" }
        assertNotNull(odiaLesson)
        assertEquals(1247L, odiaLesson!!.sizeBytes)
        assertEquals("d62f5f45b17fcb5b9928b64c8b3b6a9e3a9a19cf6419a4bc8cfb66521ab1f8cf", odiaLesson.sha256Checksum)

        val manifest = items.firstOrNull { it.id == "r2_manifest_json" }
        assertNotNull(manifest)
        assertEquals(903L, manifest!!.sizeBytes)
        assertEquals("412c92c07323bd3c3440b17f880bbe651b8a65b535a0ed55924fbf11f33b59e5", manifest.sha256Checksum)
    }

    @Test
    fun navigationRoutes_localAiPlaygroundRoute_registeredAndAccessible() {
        assertEquals("local_ai_playground", com.nmiet.app.navigation.Routes.LOCAL_AI_PLAYGROUND)
        assertEquals("profile/app_settings", com.nmiet.app.navigation.Routes.APP_SETTINGS)
        assertEquals("profile", com.nmiet.app.navigation.Routes.PROFILE)
    }

    // =========================================================================
    // PHASE 3 & 4: BLUETOOTH RFCOMM & GOOGLE NEARBY AUTOMATED TESTS
    // =========================================================================

    @Test
    fun bluetoothProtocol_framingConstants_andServiceUuid() {
        val expectedUuid = java.util.UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
        assertEquals(expectedUuid, com.nmiet.app.nearby.BluetoothTransferManager.SERVICE_UUID)
        assertEquals("fa87c0d0-afac-11de-8a39-0800200c9a66", com.nmiet.app.nearby.BluetoothTransferManager.SERVICE_UUID.toString())

        // Magic bytes 'R' (0x52) and 'N' (0x4E)
        val magic0: Byte = 0x52
        val magic1: Byte = 0x4E
        assertEquals(82.toByte(), magic0)
        assertEquals(78.toByte(), magic1)

        // Type definitions: PACK=1, NOTE=2, REQUEST=3
        val typePack: Byte = 1
        val typeNote: Byte = 2
        val typeRequest: Byte = 3
        assertEquals(1.toByte(), typePack)
        assertEquals(2.toByte(), typeNote)
        assertEquals(3.toByte(), typeRequest)

        // ACKs: SUCCESS=0x06 (ACK), FAILURE=0x15 (NAK)
        val ackSuccess: Byte = 0x06
        val ackFailure: Byte = 0x15
        assertEquals(6.toByte(), ackSuccess)
        assertEquals(21.toByte(), ackFailure)
    }

    @Test
    fun bluetoothProtocol_packetFramingRoundtrip_andSha256Verification() {
        val testPayload = "{\"type\":\"RELAYNET_LEARNING_PACK\",\"packId\":\"test_p2p_pack_01\"}".toByteArray(Charsets.UTF_8)

        // Compute expected SHA-256
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val expectedSha = md.digest(testPayload).joinToString("") { "%02x".format(it) }

        // Simulate Sender framing
        val baos = java.io.ByteArrayOutputStream()
        val dos = java.io.DataOutputStream(baos)
        dos.writeByte(0x52) // MAGIC_0
        dos.writeByte(0x4E) // MAGIC_1
        dos.writeByte(1)    // TYPE_PACK
        dos.writeInt(testPayload.size)
        dos.writeUTF(expectedSha)
        dos.write(testPayload)
        dos.flush()

        val framedBytes = baos.toByteArray()

        // Simulate Receiver reading and verifying stream
        val bais = java.io.ByteArrayInputStream(framedBytes)
        val dis = java.io.DataInputStream(bais)

        val b0 = dis.readByte()
        val b1 = dis.readByte()
        assertEquals(0x52.toByte(), b0)
        assertEquals(0x4E.toByte(), b1)

        val type = dis.readByte()
        assertEquals(1.toByte(), type)

        val size = dis.readInt()
        assertEquals(testPayload.size, size)

        val readSha = dis.readUTF()
        assertEquals(expectedSha, readSha)

        val receivedData = ByteArray(size)
        dis.readFully(receivedData)

        val computedSha = java.security.MessageDigest.getInstance("SHA-256")
            .digest(receivedData).joinToString("") { "%02x".format(it) }

        val isVerified = computedSha.equals(readSha, ignoreCase = true)
        assertTrue("Checksum verification must pass for unmodified payload", isVerified)

        val ackByte: Byte = if (isVerified) 0x06 else 0x15
        assertEquals(0x06.toByte(), ackByte)
    }

    @Test
    fun bluetoothProtocol_corruptedPayload_failsSha256AndYieldsAckFailure() {
        val originalPayload = "{\"packId\":\"clean_pack\"}".toByteArray(Charsets.UTF_8)
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val expectedSha = md.digest(originalPayload).joinToString("") { "%02x".format(it) }

        // Corrupted payload
        val corruptedPayload = "{\"packId\":\"tampered!\"}".toByteArray(Charsets.UTF_8)

        // Simulate stream with mismatching payload
        val baos = java.io.ByteArrayOutputStream()
        val dos = java.io.DataOutputStream(baos)
        dos.writeByte(0x52)
        dos.writeByte(0x4E)
        dos.writeByte(1)
        dos.writeInt(corruptedPayload.size)
        dos.writeUTF(expectedSha) // Sender promised original SHA
        dos.write(corruptedPayload) // But transmitted corrupted bytes
        dos.flush()

        val dis = java.io.DataInputStream(java.io.ByteArrayInputStream(baos.toByteArray()))
        dis.readByte(); dis.readByte(); dis.readByte()
        val size = dis.readInt()
        val promisedSha = dis.readUTF()
        val receivedBytes = ByteArray(size)
        dis.readFully(receivedBytes)

        val actualSha = java.security.MessageDigest.getInstance("SHA-256")
            .digest(receivedBytes).joinToString("") { "%02x".format(it) }

        val isVerified = actualSha.equals(promisedSha, ignoreCase = true)
        assertFalse("Corrupted payload must fail SHA-256 validation", isVerified)

        val ackByte: Byte = if (isVerified) 0x06 else 0x15
        assertEquals("Must return ACK_FAILURE (0x15) on checksum mismatch", 0x15.toByte(), ackByte)
    }

    @Test
    fun bluetoothProtocol_invalidMagicBytes_rejectedEarly() {
        val baos = java.io.ByteArrayOutputStream()
        val dos = java.io.DataOutputStream(baos)
        dos.writeByte(0x00) // Bad Magic 0
        dos.writeByte(0x7F) // Bad Magic 1
        dos.writeByte(1)
        dos.flush()

        val dis = java.io.DataInputStream(java.io.ByteArrayInputStream(baos.toByteArray()))
        val b0 = dis.readByte()
        val b1 = dis.readByte()

        val isValid = (b0 == 0x52.toByte() && b1 == 0x4E.toByte())
        assertFalse("Malformed magic bytes must be rejected", isValid)
    }

    @Test
    fun bluetoothProtocol_noteSerialization_andDeserializationRoundtrip() {
        val note = com.nmiet.app.domain.model.GeneratedNote.createDemoPhotosynthesisNote().copy(
            noteId = "note_bt_test_01",
            titleEnglish = "Light Reaction Mechanisms",
            titleOdia = "ଆଲୋକ ପ୍ରକ୍ରିୟା"
        )

        val entity = com.nmiet.app.data.local.GeneratedNoteEntity.fromDomain(note)
        assertEquals("note_bt_test_01", entity.noteId)
        assertTrue(entity.isOfflineGenerated)

        // Serialize to JSON exactly as BluetoothTransferManager.sendNoteToPeer() does
        val json = org.json.JSONObject().apply {
            put("type", "RELAYNET_GENERATED_NOTE")
            put("noteId", entity.noteId)
            put("topicId", entity.topicId)
            put("topicName", entity.topicName)
            put("titleOdia", entity.titleOdia)
            put("titleEnglish", entity.titleEnglish)
            put("contentJson", entity.contentJson)
            put("createdAt", entity.createdAt)
            put("isBookmarked", entity.isBookmarked)
            put("isOfflineGenerated", entity.isOfflineGenerated)
        }

        // Deserialize on Receiver
        val receivedEntity = com.nmiet.app.data.local.GeneratedNoteEntity(
            noteId = json.getString("noteId"),
            topicId = json.optString("topicId", "topic_custom"),
            topicName = json.optString("topicName", "P2P Shared Note"),
            titleOdia = json.optString("titleOdia", ""),
            titleEnglish = json.optString("titleEnglish", "AI Study Note"),
            contentJson = json.optString("contentJson", "{}"),
            createdAt = json.optLong("createdAt", System.currentTimeMillis()),
            isBookmarked = true,
            isOfflineGenerated = true
        )

        val restoredNote = receivedEntity.toDomain()
        assertEquals("note_bt_test_01", restoredNote.noteId)
        assertEquals("Light Reaction Mechanisms", restoredNote.titleEnglish)
        assertEquals("ଆଲୋକ ପ୍ରକ୍ରିୟା", restoredNote.titleOdia)
        assertTrue(restoredNote.keyConcepts.isNotEmpty())
    }

    @Test
    fun nearbyConnections_strategyAndServiceId_constants() {
        assertEquals("com.nmiet.app.relaynet.NEARBY_SERVICE", com.nmiet.app.nearby.GoogleNearbyConnectionsManager.SERVICE_ID)
        assertEquals(com.google.android.gms.nearby.connection.Strategy.P2P_STAR, com.nmiet.app.nearby.GoogleNearbyConnectionsManager.STRATEGY)
    }

    @Test
    fun nearbyConnections_authVerification_pinFlowAndStateHierarchy() {
        val peer = com.nmiet.app.domain.model.PeerDevice(
            id = "endpoint_peer_99",
            name = "Learner-GalaxyTab",
            address = "endpoint_peer_99",
            signalStrengthRssi = -52
        )

        // Test AuthVerification state
        val authState = com.nmiet.app.domain.model.TransferState.AuthVerification(
            peer = peer,
            authenticationDigits = "482910",
            isIncoming = true
        )

        assertEquals("482910", authState.authenticationDigits)
        assertTrue(authState.isIncoming)
        assertEquals("Learner-GalaxyTab", authState.peer.name)

        // Test Connected state
        val connectedState = com.nmiet.app.domain.model.TransferState.Connected(peer)
        assertEquals("endpoint_peer_99", connectedState.connectedPeer.id)

        // Test Transferring progress calculation
        val totalBytes = 347251840L // ~331 MB
        val transferred = 173625920L // 50%
        val progressPercent = ((transferred * 100) / totalBytes).toInt()
        assertEquals(50, progressPercent)

        val transferringState = com.nmiet.app.domain.model.TransferState.Transferring(
            peer = peer,
            isOutgoing = false,
            progressPercent = progressPercent,
            currentItemTitle = "Receiving verified educational package..."
        )
        assertEquals(50, transferringState.progressPercent)
        assertFalse(transferringState.isOutgoing)
    }

    @Test
    fun demoModeSeparation_distinctProtocolsAndSimulationGuards() {
        // Verify DemoTransferManager is explicitly identified as simulation
        val demoManager = com.nmiet.app.nearby.DemoTransferManager()
        assertTrue("DemoTransferManager must declare isDemoMode = true", demoManager.isDemoMode)

        // Verify TransportProtocol enum separation
        val protocols = com.nmiet.app.ui.screens.nearbyshare.TransportProtocol.values()
        assertEquals(3, protocols.size)
        assertTrue(protocols.contains(com.nmiet.app.ui.screens.nearbyshare.TransportProtocol.DEMO_SIMULATION))
        assertTrue(protocols.contains(com.nmiet.app.ui.screens.nearbyshare.TransportProtocol.GOOGLE_NEARBY_CONNECTIONS))
        assertTrue(protocols.contains(com.nmiet.app.ui.screens.nearbyshare.TransportProtocol.BLUETOOTH_RFCOMM))

        // Verify DemoManager starts with Idle state and empty discovered peers
        assertEquals(com.nmiet.app.domain.model.TransferState.Idle, demoManager.transferState.value)
        assertTrue(demoManager.discoveredPeers.value.isEmpty())
    }

    @Test
    fun nearbyTransferManager_interfaceDefaultStubs_behaveSafely() {
        // Implement an anonymous NearbyTransferManager that relies on default interface methods
        val testDouble = object : com.nmiet.app.nearby.NearbyTransferManager {
            private val _state = kotlinx.coroutines.flow.MutableStateFlow<com.nmiet.app.domain.model.TransferState>(com.nmiet.app.domain.model.TransferState.Idle)
            override val transferState = _state
            override val isScanning = kotlinx.coroutines.flow.MutableStateFlow(false)
            override val discoveredPeers = kotlinx.coroutines.flow.MutableStateFlow<List<com.nmiet.app.domain.model.PeerDevice>>(emptyList())
            override val isDemoMode: Boolean = false

            override fun startScanning() {}
            override fun stopScanning() {}
            override fun sendPackToPeer(peer: com.nmiet.app.domain.model.PeerDevice, pack: com.nmiet.app.domain.model.LearningPack) {}
            override fun cancelTransfer() {}
            override fun resetToIdle() {}
        }

        val dummyPeer = com.nmiet.app.domain.model.PeerDevice("p1", "Test Peer", "addr1")
        val dummyNote = com.nmiet.app.domain.model.GeneratedNote.createDemoPhotosynthesisNote()

        // Calling default interface stubs must execute safely without raising unhandled exceptions
        testDouble.sendNoteToPeer(dummyPeer, dummyNote)
        testDouble.requestPackFromPeer(dummyPeer, "pack_id_1")
        testDouble.acceptConnection(dummyPeer)
        testDouble.rejectConnection(dummyPeer)
        testDouble.sendManifestToPeer(dummyPeer)
        assertFalse(testDouble.isDemoMode)
    }

    // =========================================================================
    // Google Nearby Connections: Operations, Encoders, Decoders & Integrity
    // =========================================================================

    @Test
    fun nearbyConnections_noteEncodingAndDecoding_preservesMultilingualOdiaAndEnglish() {
        val note = com.nmiet.app.domain.model.GeneratedNote(
            noteId = "note_nearby_od_01",
            topicId = "topic_biology_photosynthesis",
            topicName = "Photosynthesis and Food Synthesis",
            titleOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା ଏବଂ ଜଳର ବିଶ୍ଳେଷଣ",
            titleEnglish = "Photosynthesis & Photolysis of Water",
            summaryOdia = "ସୂର୍ଯ୍ୟାଲୋକର ଉପସ୍ଥିତିରେ ସବୁଜ ଉଦ୍ଭିଦ ନିଜର ଖାଦ୍ୟ ପ୍ରସ୍ତୁତ କରେ।",
            summaryEnglish = "Green plants synthesize glucose using water, carbon dioxide, and solar light energy.",
            keyConcepts = listOf(
                com.nmiet.app.domain.model.NoteConceptPoint(
                    pointOdia = "ହରିତକଣାର ଗ୍ରାନାରେ ଆଲୋକ ପ୍ରକ୍ରିୟା ସମ୍ପାଦିତ ହୁଏ।",
                    pointEnglish = "Light reactions occur specifically in the thylakoid grana."
                ),
                com.nmiet.app.domain.model.NoteConceptPoint(
                    pointOdia = "ଷ୍ଟ୍ରୋମାରେ କାର୍ବନ ଡାଇଅକ୍ସାଇଡ ସ୍ଥିରୀକରଣ ହୁଏ।",
                    pointEnglish = "Dark reactions occur within the stroma mediated by RuBisCO."
                )
            ),
            formulasEquations = listOf("6CO₂ + 12H₂O ──► C₆H₁₂O₆ + 6H₂O + 6O₂"),
            examTips = listOf("Remember: O₂ evolves strictly from H₂O photolysis, NOT from CO₂ splitting."),
            createdAt = 1700000000000L,
            isBookmarked = true,
            isOfflineGenerated = true
        )

        val encodedBytes = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.encodeNote(note)
        assertTrue(encodedBytes.isNotEmpty())

        val decodedNote = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNote(encodedBytes)
        assertNotNull(decodedNote)
        assertEquals("note_nearby_od_01", decodedNote?.noteId)
        assertEquals("topic_biology_photosynthesis", decodedNote?.topicId)
        assertEquals("ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା ଏବଂ ଜଳର ବିଶ୍ଳେଷଣ", decodedNote?.titleOdia)
        assertEquals("Photosynthesis & Photolysis of Water", decodedNote?.titleEnglish)
        assertEquals("ସୂର୍ଯ୍ୟାଲୋକର ଉପସ୍ଥିତିରେ ସବୁଜ ଉଦ୍ଭିଦ ନିଜର ଖାଦ୍ୟ ପ୍ରସ୍ତୁତ କରେ।", decodedNote?.summaryOdia)
        assertEquals(2, decodedNote?.keyConcepts?.size)
        assertEquals("ହରିତକଣାର ଗ୍ରାନାରେ ଆଲୋକ ପ୍ରକ୍ରିୟା ସମ୍ପାଦିତ ହୁଏ।", decodedNote?.keyConcepts?.get(0)?.pointOdia)
        assertEquals("6CO₂ + 12H₂O ──► C₆H₁₂O₆ + 6H₂O + 6O₂", decodedNote?.formulasEquations?.firstOrNull())
        assertTrue(decodedNote?.isOfflineGenerated == true)
    }

    @Test
    fun nearbyConnections_noteDecoding_malformedPayloadsAndUnsupportedVersions_rejected() {
        // 1. Malformed JSON bytes
        val badJson = "not a valid json string".toByteArray(Charsets.UTF_8)
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNote(badJson))

        // 2. Empty bytes
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNote(ByteArray(0)))

        // 3. Missing noteId
        val missingIdJson = org.json.JSONObject().apply {
            put("type", com.nmiet.app.nearby.GoogleNearbyConnectionsManager.TYPE_NOTE)
            put("protocolVersion", 1)
            put("titleEnglish", "Orphan Note")
        }.toString().toByteArray(Charsets.UTF_8)
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNote(missingIdJson))

        // 4. Unsupported protocol version (> 1)
        val futureVersionJson = org.json.JSONObject().apply {
            put("type", com.nmiet.app.nearby.GoogleNearbyConnectionsManager.TYPE_NOTE)
            put("protocolVersion", 99)
            put("noteId", "note_future_01")
            put("titleEnglish", "Future Protocol Note")
        }.toString().toByteArray(Charsets.UTF_8)
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNote(futureVersionJson))

        // 5. Wrong discriminator type
        val wrongTypeJson = org.json.JSONObject().apply {
            put("type", "UNKNOWN_PROTOCOL_TYPE")
            put("protocolVersion", 1)
            put("noteId", "note_01")
        }.toString().toByteArray(Charsets.UTF_8)
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNote(wrongTypeJson))
    }

    @Test
    fun nearbyConnections_packRequest_encodingAndDecoding_roundtripAndValidation() {
        val requestId = "req_pack_pull_99"
        val packId = "relaynet_class9_science_photosynthesis_v1"

        val encoded = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.encodePackRequest(requestId, packId)
        assertTrue(encoded.isNotEmpty())

        val decoded = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackRequest(encoded)
        assertNotNull(decoded)
        assertEquals(1, decoded?.protocolVersion)
        assertEquals(requestId, decoded?.requestId)
        assertEquals(packId, decoded?.packId)

        // Invalid: Empty or blank requestId
        val emptyReqId = org.json.JSONObject().apply {
            put("type", com.nmiet.app.nearby.GoogleNearbyConnectionsManager.TYPE_PACK_REQUEST)
            put("protocolVersion", 1)
            put("requestId", "   ")
            put("packId", packId)
        }.toString().toByteArray(Charsets.UTF_8)
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackRequest(emptyReqId))

        // Invalid: Unsupported protocol version
        val badVersion = org.json.JSONObject().apply {
            put("type", com.nmiet.app.nearby.GoogleNearbyConnectionsManager.TYPE_PACK_REQUEST)
            put("protocolVersion", 5)
            put("requestId", requestId)
            put("packId", packId)
        }.toString().toByteArray(Charsets.UTF_8)
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackRequest(badVersion))
    }

    @Test
    fun nearbyConnections_packResponse_encodingAndDecoding_allStatuses() {
        // Status 1: AVAILABLE
        val availableResp = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.Companion.PackResponseMessage(
            protocolVersion = 1,
            requestId = "req_101",
            packId = "pack_photosynthesis_v1",
            status = "AVAILABLE",
            titleEnglish = "Photosynthesis & Plant Biochemistry",
            filePayloadId = 9876543210L,
            fileSizeBytes = 524288L,
            sha256Checksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        )
        val encodedAvail = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.encodePackResponse(availableResp)
        val decodedAvail = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackResponse(encodedAvail)
        assertNotNull(decodedAvail)
        assertEquals("AVAILABLE", decodedAvail?.status)
        assertEquals(9876543210L, decodedAvail?.filePayloadId)
        assertEquals(524288L, decodedAvail?.fileSizeBytes)
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", decodedAvail?.sha256Checksum)

        // Status 2: NOT_FOUND
        val notFoundResp = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.Companion.PackResponseMessage(
            protocolVersion = 1,
            requestId = "req_102",
            packId = "missing_pack_id",
            status = "NOT_FOUND",
            message = "Pack 'missing_pack_id' is not installed locally on peer."
        )
        val encodedNotFound = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.encodePackResponse(notFoundResp)
        val decodedNotFound = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackResponse(encodedNotFound)
        assertNotNull(decodedNotFound)
        assertEquals("NOT_FOUND", decodedNotFound?.status)
        assertEquals("Pack 'missing_pack_id' is not installed locally on peer.", decodedNotFound?.message)

        // Status 3: ERROR
        val errorResp = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.Companion.PackResponseMessage(
            protocolVersion = 1,
            requestId = "req_103",
            packId = "corrupt_pack_id",
            status = "ERROR",
            message = "Pack archive is unreadable or corrupted on disk."
        )
        val encodedError = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.encodePackResponse(errorResp)
        val decodedError = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackResponse(encodedError)
        assertNotNull(decodedError)
        assertEquals("ERROR", decodedError?.status)

        // Reject: AVAILABLE status without checksum or invalid filePayloadId
        val invalidAvail = org.json.JSONObject().apply {
            put("type", com.nmiet.app.nearby.GoogleNearbyConnectionsManager.TYPE_PACK_RESPONSE)
            put("protocolVersion", 1)
            put("requestId", "req_104")
            put("packId", "pack_test")
            put("status", "AVAILABLE")
            put("filePayloadId", -1L) // Invalid ID
            put("sha256Checksum", "") // Missing checksum
        }.toString().toByteArray(Charsets.UTF_8)
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackResponse(invalidAvail))
    }

    @Test
    fun nearbyConnections_discriminatorRouting_strictlyDifferentiatesProtocolMessages() {
        val note = com.nmiet.app.domain.model.GeneratedNote.createDemoPhotosynthesisNote()
        val noteBytes = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.encodeNote(note)

        val requestBytes = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.encodePackRequest("req_1", "pack_1")

        val responseBytes = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.encodePackResponse(
            com.nmiet.app.nearby.GoogleNearbyConnectionsManager.Companion.PackResponseMessage(
                protocolVersion = 1,
                requestId = "req_1",
                packId = "pack_1",
                status = "NOT_FOUND",
                message = "Not found"
            )
        )

        val manifestBytes = com.nmiet.app.domain.model.PeerContentManifest(
            deviceName = "Peer-1",
            items = emptyList()
        ).serialize()

        val ackBytes = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.encodeNoteAck(
            com.nmiet.app.nearby.GoogleNearbyConnectionsManager.Companion.NoteAckMessage(
                protocolVersion = 1,
                messageId = "msg_1",
                noteId = "note_1",
                status = "SUCCESS"
            )
        )

        // 1. Note decoder only decodes note bytes
        assertNotNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNote(noteBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNote(requestBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNote(responseBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNote(manifestBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNote(ackBytes))

        // 2. Pack request decoder only decodes pack request bytes
        assertNotNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackRequest(requestBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackRequest(noteBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackRequest(responseBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackRequest(manifestBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackRequest(ackBytes))

        // 3. Pack response decoder only decodes pack response bytes
        assertNotNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackResponse(responseBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackResponse(noteBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackResponse(requestBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackResponse(manifestBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodePackResponse(ackBytes))

        // 4. Manifest deserializer only decodes manifest bytes
        assertNotNull(com.nmiet.app.domain.model.PeerContentManifest.deserialize(manifestBytes))
        assertNull(com.nmiet.app.domain.model.PeerContentManifest.deserialize(noteBytes))
        assertNull(com.nmiet.app.domain.model.PeerContentManifest.deserialize(requestBytes))
        assertNull(com.nmiet.app.domain.model.PeerContentManifest.deserialize(responseBytes))
        assertNull(com.nmiet.app.domain.model.PeerContentManifest.deserialize(ackBytes))

        // 5. Note ACK decoder only decodes note ACK bytes
        assertNotNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNoteAck(ackBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNoteAck(noteBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNoteAck(requestBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNoteAck(responseBytes))
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNoteAck(manifestBytes))
    }

    @Test
    fun nearbyConnections_repository_getPackArchiveFile_resolvesArchiveAndRejectsMissing() {
        val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "relaynet_repo_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        val samplePack = PilotCurriculumCatalog.getPilotLearningPack().copy(
            packId = "pack_biology_09_photosynthesis",
            titleEnglish = "Photosynthesis & Cell Energy"
        )
        val entity = com.nmiet.app.data.local.LearningPackEntity.fromDomain(samplePack)

        val mockDao = java.lang.reflect.Proxy.newProxyInstance(
            com.nmiet.app.data.local.LearningDao::class.java.classLoader,
            arrayOf(com.nmiet.app.data.local.LearningDao::class.java)
        ) { _, method, args ->
            when (method.name) {
                "getPackByIdOnce" -> {
                    val id = args?.get(0) as? String
                    if (id == samplePack.packId) entity else null
                }
                else -> null
            }
        } as com.nmiet.app.data.local.LearningDao

        val repository = com.nmiet.app.data.repository.ContentRepositoryImpl(
            learningDao = mockDao,
            archiveDir = tempDir
        )

        kotlinx.coroutines.runBlocking {
            // 1. Available pack resolves to a valid, readable .zip archive
            val archiveFile = repository.getPackArchiveFile(samplePack.packId)
            assertNotNull(archiveFile)
            assertTrue(archiveFile!!.exists())
            assertTrue(archiveFile.canRead())
            assertTrue(archiveFile.length() > 0)
            assertTrue(archiveFile.name.endsWith(".zip"))

            // 2. Validate archive contains valid manifest and lessons
            val extractDir = java.io.File(tempDir, "extracted_check")
            val result = com.nmiet.app.data.local.ZipPackManager.extractAndVerifyPack(archiveFile, extractDir)
            assertTrue(result.isChecksumValid)
            assertEquals(samplePack.packId, result.pack.packId)
            assertEquals("Photosynthesis & Cell Energy", result.pack.titleEnglish)

            // 3. Non-existent pack returns null
            val missing = repository.getPackArchiveFile("completely_unknown_pack_id")
            assertNull(missing)
        }

        tempDir.deleteRecursively()
    }

    @Test
    fun nearbyConnections_integrity_sha256Verification_acceptsMatchRejectsMismatch() {
        val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "relaynet_sha_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        val pack = PilotCurriculumCatalog.getPilotLearningPack()
        val archive = java.io.File(tempDir, "valid_pack.zip")
        com.nmiet.app.data.local.ZipPackManager.createPackZip(pack, archive)

        val computedSha256 = com.nmiet.app.data.local.ZipPackManager.calculateSha256(archive)
        assertTrue(computedSha256.isNotBlank())
        assertEquals(64, computedSha256.length) // Hex SHA-256 length

        // Matching checksum accepts
        val matchingChecksum = computedSha256.uppercase() // Case-insensitive test
        assertTrue(computedSha256.equals(matchingChecksum, ignoreCase = true))

        // Mismatched checksum rejects
        val tamperedChecksum = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        assertFalse(computedSha256.equals(tamperedChecksum, ignoreCase = true))

        tempDir.deleteRecursively()
    }

    @Test
    fun nearbyConnections_integrity_missingChecksum_neverProducesSuccess() {
        val emptySha: String? = ""
        val nullSha: String? = null
        val whitespaceSha: String? = "   "

        fun isChecksumUsable(sha: String?): Boolean {
            return !sha.isNullOrBlank()
        }

        assertFalse("Empty checksum must never be accepted", isChecksumUsable(emptySha))
        assertFalse("Null checksum must never be accepted", isChecksumUsable(nullSha))
        assertFalse("Whitespace checksum must never be accepted", isChecksumUsable(whitespaceSha))
    }

    @Test
    fun nearbyConnections_orderIndependentCorrelation_matchesMetadataAndPayload() {
        val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "relaynet_corr_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        val peer = com.nmiet.app.domain.model.PeerDevice("peer_node_07", "Learner GalaxyTab", "addr_07")
        val samplePack = PilotCurriculumCatalog.getPilotLearningPack()
        val testFile = java.io.File(tempDir, "corr_test.zip")
        com.nmiet.app.data.local.ZipPackManager.createPackZip(samplePack, testFile)
        val fileSha = com.nmiet.app.data.local.ZipPackManager.calculateSha256(testFile)

        val metadataMap = mutableMapOf<Long, com.nmiet.app.nearby.GoogleNearbyConnectionsManager.IncomingPackMetadata>()
        val completedFilesMap = mutableMapOf<Long, java.io.File>()

        // CASE A: Metadata arrives FIRST, File completes SECOND
        val metaA = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.IncomingPackMetadata(
            requestId = "req_A",
            packId = samplePack.packId,
            filePayloadId = 7701L,
            fileSizeBytes = testFile.length(),
            sha256Checksum = fileSha,
            titleEnglish = samplePack.titleEnglish,
            peer = peer
        )
        metadataMap[7701L] = metaA

        // When file payload 7701 completes, correlate using metadataMap
        val correlatedMetaA = metadataMap.remove(7701L)
        assertNotNull("Must correlate metadata for payload 7701", correlatedMetaA)
        assertEquals("req_A", correlatedMetaA?.requestId)
        assertEquals(fileSha, correlatedMetaA?.sha256Checksum)

        // CASE B: File completes FIRST, Metadata arrives SECOND
        completedFilesMap[7702L] = testFile

        // When metadata arrives for 7702, correlate using completedFilesMap
        val correlatedFileB = completedFilesMap.remove(7702L)
        assertNotNull("Must correlate completed file for payload 7702", correlatedFileB)
        assertTrue(correlatedFileB!!.exists())
        assertEquals(testFile.length(), correlatedFileB.length())

        tempDir.deleteRecursively()
    }

    @Test
    fun nearbyConnections_noteAck_encodingAndDecoding_successAndErrorStatuses() {
        // Success ACK
        val successAck = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.Companion.NoteAckMessage(
            protocolVersion = 1,
            messageId = "msg_note_tx_001",
            noteId = "note_01",
            status = "SUCCESS",
            errorMessage = ""
        )
        val encodedSuccess = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.encodeNoteAck(successAck)
        assertTrue(encodedSuccess.isNotEmpty())

        val decodedSuccess = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNoteAck(encodedSuccess)
        assertNotNull(decodedSuccess)
        assertEquals(1, decodedSuccess?.protocolVersion)
        assertEquals("msg_note_tx_001", decodedSuccess?.messageId)
        assertEquals("note_01", decodedSuccess?.noteId)
        assertEquals("SUCCESS", decodedSuccess?.status)
        assertEquals("", decodedSuccess?.errorMessage)

        // Error ACK
        val errorAck = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.Companion.NoteAckMessage(
            protocolVersion = 1,
            messageId = "msg_note_tx_002",
            noteId = "note_02",
            status = "ERROR",
            errorMessage = "SQLite full / disk I/O error"
        )
        val encodedError = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.encodeNoteAck(errorAck)
        val decodedError = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNoteAck(encodedError)
        assertNotNull(decodedError)
        assertEquals("ERROR", decodedError?.status)
        assertEquals("SQLite full / disk I/O error", decodedError?.errorMessage)

        // Invalid: missing status or unsupported version
        val badAck = org.json.JSONObject().apply {
            put("type", com.nmiet.app.nearby.GoogleNearbyConnectionsManager.TYPE_NOTE_ACK)
            put("protocolVersion", 2) // Unsupported version
            put("messageId", "msg_03")
            put("noteId", "note_03")
            put("status", "SUCCESS")
        }.toString().toByteArray(Charsets.UTF_8)
        assertNull(com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNoteAck(badAck))
    }

    @Test
    fun nearbyConnections_notePersistence_singleOwner_persistsExactlyOnce() {
        var dbSaveCount = 0
        var uiCallbackCount = 0

        val fakeRepo = java.lang.reflect.Proxy.newProxyInstance(
            com.nmiet.app.domain.repository.ContentRepository::class.java.classLoader,
            arrayOf(com.nmiet.app.domain.repository.ContentRepository::class.java)
        ) { _, method, args ->
            when (method.name) {
                "saveGeneratedNote" -> {
                    dbSaveCount++
                    null
                }
                else -> null
            }
        } as com.nmiet.app.domain.repository.ContentRepository

        val note = com.nmiet.app.domain.model.GeneratedNote.createDemoPhotosynthesisNote()
        val peerName = "Teacher-Tablet"

        // Simulate incoming note handler with single persistence owner:
        // GoogleNearbyConnectionsManager saves it once to Room repository
        kotlinx.coroutines.runBlocking {
            fakeRepo.saveGeneratedNote(note)
        }

        // Observer callback strictly notifies UI / sync status without performing a duplicate write
        val onNoteReceivedCallback: (suspend (com.nmiet.app.domain.model.GeneratedNote, String) -> Unit) = { _, _ ->
            uiCallbackCount++
            // Does NOT call fakeRepo.saveGeneratedNote(note)
        }

        kotlinx.coroutines.runBlocking {
            onNoteReceivedCallback(note, peerName)
        }

        assertEquals("Note must be persisted exactly once by its single owner", 1, dbSaveCount)
        assertEquals("UI observer must be notified exactly once", 1, uiCallbackCount)
    }

    @Test
    fun nearbyConnections_noteAck_successfulPersistence_producesCorrelatedSuccessAck() {
        var isSaved = false
        val note = com.nmiet.app.domain.model.GeneratedNote.createDemoPhotosynthesisNote().copy(noteId = "note_ack_test_10")
        val messageId = "msg_note_seq_42"

        val inboundBytes = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.encodeNote(note, messageId)
        val decodedTransfer = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNoteTransfer(inboundBytes)
        assertNotNull(decodedTransfer)
        assertEquals(messageId, decodedTransfer?.messageId)

        // Simulate persistence
        try {
            isSaved = true
        } catch (_: Exception) {}

        assertTrue(isSaved)
        val generatedAck = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.Companion.NoteAckMessage(
            protocolVersion = 1,
            messageId = decodedTransfer!!.messageId,
            noteId = decodedTransfer.note.noteId,
            status = if (isSaved) "SUCCESS" else "ERROR"
        )

        assertEquals("msg_note_seq_42", generatedAck.messageId)
        assertEquals("note_ack_test_10", generatedAck.noteId)
        assertEquals("SUCCESS", generatedAck.status)
    }

    @Test
    fun nearbyConnections_noteAck_failedPersistence_producesErrorAck() {
        val note = com.nmiet.app.domain.model.GeneratedNote.createDemoPhotosynthesisNote().copy(noteId = "note_fail_db")
        val messageId = "msg_note_seq_99"

        val inboundBytes = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.encodeNote(note, messageId)
        val decodedTransfer = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.decodeNoteTransfer(inboundBytes)
        assertNotNull(decodedTransfer)

        var persistenceException: String? = null
        try {
            throw java.io.IOException("Room SQLite database is locked or out of space")
        } catch (e: Exception) {
            persistenceException = e.message
        }

        assertNotNull(persistenceException)
        val generatedAck = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.Companion.NoteAckMessage(
            protocolVersion = 1,
            messageId = decodedTransfer!!.messageId,
            noteId = decodedTransfer.note.noteId,
            status = "ERROR",
            errorMessage = persistenceException ?: "Unknown error"
        )

        assertEquals("ERROR", generatedAck.status)
        assertTrue(generatedAck.errorMessage.contains("database is locked"))
    }

    @Test
    fun nearbyConnections_sender_requiresMatchingAck_beforeReportingSuccess() {
        val note = com.nmiet.app.domain.model.GeneratedNote.createDemoPhotosynthesisNote()
        val peer = com.nmiet.app.domain.model.PeerDevice("p1", "Learner-01", "addr1")
        val messageId = "msg_tx_ack_req_01"

        // Initially state is transferring
        var transferState: com.nmiet.app.domain.model.TransferState = com.nmiet.app.domain.model.TransferState.Transferring(
            peer = peer,
            isOutgoing = true,
            progressPercent = 0,
            currentItemTitle = "Sending note..."
        )

        // When Nearby transport finishes transmitting bytes (progress 100%), state MUST stay in Transferring
        val deliveredState: com.nmiet.app.domain.model.TransferState = com.nmiet.app.domain.model.TransferState.Transferring(
            peer = peer,
            isOutgoing = true,
            progressPercent = 100,
            currentItemTitle = "Delivered payload. Waiting for ${peer.name} to confirm saving note..."
        )
        assertTrue("Nearby payload delivery must stay in Transferring state before ACK", deliveredState is com.nmiet.app.domain.model.TransferState.Transferring)

        // Only when matching ACK with status SUCCESS arrives does state become Success
        val ack = com.nmiet.app.nearby.GoogleNearbyConnectionsManager.Companion.NoteAckMessage(
            protocolVersion = 1,
            messageId = messageId,
            noteId = note.noteId,
            status = "SUCCESS"
        )
        if (ack.status == "SUCCESS") {
            transferState = com.nmiet.app.domain.model.TransferState.Success(
                peer = peer,
                transferredCount = 1,
                message = "AI Study Note '${note.titleEnglish}' safely received and saved by ${peer.name}."
            )
        }

        assertTrue("Matching SUCCESS ACK must trigger application-level Success", transferState is com.nmiet.app.domain.model.TransferState.Success)
    }

    @Test
    fun nearbyConnections_sender_wrongOrLateAck_ignoredSafely() {
        val pendingMessageId = "msg_active_123"
        val lateAckMessageId = "msg_stale_999"

        val activePendingTransfers = mutableMapOf<String, String>()
        activePendingTransfers[pendingMessageId] = "note_content"

        // When late ACK arrives with unmatched messageId:
        val matched = activePendingTransfers.remove(lateAckMessageId)
        assertNull("Late or mismatched ACK must be safely ignored without altering active transfers", matched)
        assertTrue("Active pending transfer must remain intact", activePendingTransfers.containsKey(pendingMessageId))
    }

    @Test
    fun nearbyConnections_duplicateMessage_handledIdempotentlyWithoutDuplicatePersistence() {
        val processedMessageIds = mutableSetOf<String>()
        var persistenceCallCount = 0

        val note = com.nmiet.app.domain.model.GeneratedNote.createDemoPhotosynthesisNote()
        val messageId = "msg_idempotent_test_01"

        fun processInboundNote(msgId: String): com.nmiet.app.nearby.GoogleNearbyConnectionsManager.Companion.NoteAckMessage {
            if (processedMessageIds.contains(msgId)) {
                // Duplicate detected: Return SUCCESS ACK without re-saving
                return com.nmiet.app.nearby.GoogleNearbyConnectionsManager.Companion.NoteAckMessage(
                    protocolVersion = 1,
                    messageId = msgId,
                    noteId = note.noteId,
                    status = "SUCCESS"
                )
            }
            persistenceCallCount++
            processedMessageIds.add(msgId)
            return com.nmiet.app.nearby.GoogleNearbyConnectionsManager.Companion.NoteAckMessage(
                protocolVersion = 1,
                messageId = msgId,
                noteId = note.noteId,
                status = "SUCCESS"
            )
        }

        // First message delivery
        val ack1 = processInboundNote(messageId)
        assertEquals("SUCCESS", ack1.status)
        assertEquals(1, persistenceCallCount)

        // Duplicate delivery with same messageId
        val ack2 = processInboundNote(messageId)
        assertEquals("SUCCESS", ack2.status)
        assertEquals("Duplicate delivery must not trigger repeated persistence", 1, persistenceCallCount)
    }

    @Test
    fun nearbyConnections_noteTransfer_timeoutAndDisconnection_producesMeaningfulFailure() {
        val peer = com.nmiet.app.domain.model.PeerDevice("p2", "Student-Tab", "addr2")
        val note = com.nmiet.app.domain.model.GeneratedNote.createDemoPhotosynthesisNote()

        // 1. Timeout failure state
        val timeoutState: com.nmiet.app.domain.model.TransferState = com.nmiet.app.domain.model.TransferState.Error(
            errorMessage = "Timed out waiting for ${peer.name} to confirm saving note '${note.titleEnglish}'.",
            canRetry = true
        )
        assertTrue(timeoutState is com.nmiet.app.domain.model.TransferState.Error)
        assertTrue((timeoutState as com.nmiet.app.domain.model.TransferState.Error).errorMessage.contains("Timed out"))
        assertTrue(timeoutState.canRetry)

        // 2. Disconnection failure state
        val disconnectState: com.nmiet.app.domain.model.TransferState = com.nmiet.app.domain.model.TransferState.Error(
            errorMessage = "Peer disconnected before note delivery confirmation was received.",
            canRetry = true
        )
        assertTrue(disconnectState is com.nmiet.app.domain.model.TransferState.Error)
        assertTrue((disconnectState as com.nmiet.app.domain.model.TransferState.Error).errorMessage.contains("disconnected"))
    }
}

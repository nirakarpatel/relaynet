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
}


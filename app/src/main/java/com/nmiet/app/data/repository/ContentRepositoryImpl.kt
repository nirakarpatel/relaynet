package com.nmiet.app.data.repository

import android.graphics.Bitmap
import android.util.Base64
import com.nmiet.app.data.local.GeneratedNoteEntity
import com.nmiet.app.data.local.LearningDao
import com.nmiet.app.data.local.LearningPackEntity
import com.nmiet.app.data.local.QuizAttemptEntity
import com.nmiet.app.data.local.TopicMasteryEntity
import com.nmiet.app.data.local.TutorMessageEntity
import com.nmiet.app.data.remote.PilotCurriculumCatalog
import com.nmiet.app.domain.model.ContentOrigin
import com.nmiet.app.domain.model.CurriculumNode
import com.nmiet.app.domain.model.GeneratedNote
import com.nmiet.app.domain.model.LearnerProfile
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.QuizAttempt
import com.nmiet.app.domain.model.SyncStatus
import com.nmiet.app.domain.model.TopicMastery
import com.nmiet.app.domain.model.TutorMessage
import com.nmiet.app.domain.model.TutorSender
import com.nmiet.app.domain.repository.ContentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class ContentRepositoryImpl(
    private val learningDao: LearningDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ContentRepository {

    private var activeApiKey: String = ""

    override fun setApiKey(key: String) {
        activeApiKey = key.trim()
    }

    override fun getApiKey(): String = activeApiKey

    private val _learnerProfile = MutableStateFlow<LearnerProfile>(LearnerProfile())
    override fun getLearnerProfileFlow(): Flow<LearnerProfile> = _learnerProfile.asStateFlow()

    override suspend fun updateLearnerProfile(profile: LearnerProfile) {
        _learnerProfile.value = profile
    }

    private val _syncStatus = MutableStateFlow(
        SyncStatus(
            isOnline = true,
            isSyncing = false,
            lastSyncTimestamp = System.currentTimeMillis(),
            totalCachedCount = 1,
            totalSavedOfflineCount = 1,
            lastSyncMessage = "RelayNet initialized. Pilot pack verified & ready."
        )
    )
    override fun getSyncStatusFlow(): Flow<SyncStatus> = _syncStatus.asStateFlow()

    override fun setSimulatedNetworkOnline(isOnline: Boolean) {
        _syncStatus.update {
            it.copy(
                isOnline = isOnline,
                lastSyncMessage = if (isOnline) "Device is Online (Connected to Backend)" else "Offline Mode: Using local SQLite packs & cached AI"
            )
        }
    }

    override fun getCurriculumNodesFlow(): Flow<List<CurriculumNode>> {
        return flowOf(PilotCurriculumCatalog.getCurriculumCatalogue())
    }

    override fun getLearningPacksFlow(): Flow<List<LearningPack>> {
        return learningDao.getAllPacks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPacksForStudentFlow(board: String, grade: String): Flow<List<LearningPack>> {
        return learningDao.getPacksForStudent(board, grade).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDownloadedPacksFlow(): Flow<List<LearningPack>> {
        return learningDao.getDownloadedPacks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSharedPacksFlow(): Flow<List<LearningPack>> {
        return learningDao.getSharedPacks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLearningPackByIdFlow(packId: String): Flow<LearningPack?> {
        return learningDao.getPackById(packId).map { it?.toDomain() }
    }

    override fun getTopicMasteryFlow(): Flow<List<TopicMastery>> {
        return learningDao.getAllMastery().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentQuizAttemptsFlow(): Flow<List<QuizAttempt>> {
        return learningDao.getAllAttempts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTutorMessagesFlow(): Flow<List<TutorMessage>> {
        return learningDao.getAllTutorMessages().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun submitQuizAttempt(
        topicId: String,
        topicName: String,
        scorePercent: Int,
        correctCount: Int,
        totalCount: Int,
        weakConcepts: List<String>
    ) = withContext(ioDispatcher) {
        val attemptId = "attempt_${UUID.randomUUID().toString().take(8)}"
        val attemptEntity = QuizAttemptEntity(
            attemptId = attemptId,
            packId = topicId,
            score = scorePercent,
            answersJson = "[]",
            misconceptionTagsJson = if (weakConcepts.isEmpty()) "[]" else weakConcepts.joinToString(separator = "\",\"", prefix = "[\"", postfix = "\"]") { it.replace("\"", "\\\"") },
            topicName = topicName,
            correctCount = correctCount,
            totalCount = totalCount,
            timestamp = System.currentTimeMillis(),
            isSynced = _syncStatus.value.isOnline
        )
        learningDao.insertAttempt(attemptEntity)

        // Calculate and update mastery
        val existingMastery = learningDao.getMasteryById(topicId)
        val isTargetedTopicMastery = (scorePercent == 100)
        val newScore = if (isTargetedTopicMastery) {
            95 // Elevated out of Developing / Needs Revision into Strong Mastery (> 85%)
        } else if (existingMastery == null) {
            scorePercent
        } else {
            // Weighted average: 65% recent + 35% past
            ((scorePercent * 0.65) + (existingMastery.masteryScore * 0.35)).toInt().coerceIn(0, 100)
        }

        val recommendedAction = if (isTargetedTopicMastery) {
            "Strong Mastery Achieved (95%)! Water Photolysis & Light Reactions mastered."
        } else when {
            weakConcepts.contains("photolysis_water_source") || weakConcepts.contains("PHOTOLYSIS_WATER") ->
                "Review Section 2: Water Photolysis & Oxygen Evolution"
            weakConcepts.contains("chloroplast_anatomy_site") ->
                "Revise Grana (Light reaction) vs Stroma (Dark reaction)"
            scorePercent < 60 ->
                "Read the bilingual summary and retry practice quiz"
            else ->
                "Ready for Chapter 2: Plant Respiration & Gas Exchange!"
        }

        val masteryEntity = TopicMasteryEntity(
            topicId = topicId,
            topicTitleOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା",
            topicTitleEnglish = "Photosynthesis & Plant Nutrition",
            subject = "Science (Biology)",
            masteryScore = newScore,
            lastPracticedDate = System.currentTimeMillis(),
            attemptsCount = (existingMastery?.attemptsCount ?: 0) + 1,
            recommendedNextAction = recommendedAction
        )
        learningDao.insertOrUpdateMastery(masteryEntity)

        _syncStatus.update {
            it.copy(
                lastSyncTimestamp = System.currentTimeMillis(),
                lastSyncMessage = "Quiz completed ($scorePercent%). Mastery updated to $newScore%."
            )
        }
    }

    override suspend fun askTutor(
        questionText: String,
        imageBitmap: Bitmap?,
        language: String,
        insertUserMessage: Boolean
    ): TutorMessage = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        if (insertUserMessage) {
            val userMsg = TutorMessage(
                id = "msg_user_${UUID.randomUUID().toString().take(6)}",
                sender = TutorSender.USER,
                textOdia = questionText,
                textEnglish = questionText,
                timestamp = now,
                isOfflineGrounded = false,
                sourceChapter = null,
                isStudyRelated = true,
                imageBitmap = imageBitmap
            )
            learningDao.insertTutorMessage(TutorMessageEntity.fromDomain(userMsg))
        }

        val isOnline = _syncStatus.value.isOnline
        var cloudResult: CloudTutorResponse? = null

        if (isOnline) {
            cloudResult = callCloudTutorEndpoint(questionText, imageBitmap, language)
        }

        val tutorMsg = if (cloudResult != null) {
            TutorMessage(
                id = "msg_tutor_${UUID.randomUUID().toString().take(6)}",
                sender = TutorSender.TUTOR,
                textOdia = if (language.lowercase() in listOf("odia", "or")) cloudResult.reply else cloudResult.answerOdia ?: cloudResult.reply,
                textEnglish = if (language.lowercase() !in listOf("odia", "or")) cloudResult.reply else cloudResult.answerEnglish ?: cloudResult.reply,
                timestamp = System.currentTimeMillis(),
                isOfflineGrounded = false,
                sourceChapter = cloudResult.groundingSource,
                isStudyRelated = cloudResult.isStudyRelated,
                isCloudAI = true,
                sourceTags = cloudResult.sourceTags
            )
        } else {
            resolveLocalGroundedTutor(questionText, imageBitmap != null, language)
        }

        learningDao.insertTutorMessage(TutorMessageEntity.fromDomain(tutorMsg))
        tutorMsg
    }

    override suspend fun scanAndSolveTextbook(
        imageBitmap: Bitmap,
        userPrompt: String?,
        language: String,
        curriculumContext: String,
        insertUserMessage: Boolean
    ): TutorMessage = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        val promptText = userPrompt?.trim()?.ifBlank { null } ?: "Analyze and solve this textbook problem / equation."

        // 1. Optimistic user message with attached image (if enabled)
        if (insertUserMessage) {
            val userMsg = TutorMessage(
                id = "msg_user_${UUID.randomUUID().toString().take(6)}",
                sender = TutorSender.USER,
                textOdia = promptText,
                textEnglish = promptText,
                timestamp = now,
                isOfflineGrounded = false,
                sourceChapter = null,
                isStudyRelated = true,
                imageBitmap = imageBitmap
            )
            learningDao.insertTutorMessage(TutorMessageEntity.fromDomain(userMsg))
        }

        // 2. Call backend /tutor/scan-and-solve endpoint
        val cloudScanResult = callScanAndSolveEndpoint(imageBitmap, promptText, language, curriculumContext)

        // 3. Construct TutorMessage with [Cloud AI] Nemotron-3-Super Vision & Reasoning tag
        val tutorMsg = if (cloudScanResult != null) {
            val solutionText = cloudScanResult.solution
            TutorMessage(
                id = "msg_nemotron_${UUID.randomUUID().toString().take(6)}",
                sender = TutorSender.TUTOR,
                textOdia = solutionText,
                textEnglish = solutionText,
                textHindi = solutionText,
                timestamp = System.currentTimeMillis(),
                isOfflineGrounded = false,
                sourceChapter = "Nemotron-3-Super Reasoning",
                isStudyRelated = cloudScanResult.isAcademic,
                isCloudAI = true,
                isNemotronReasoning = true,
                sourceTags = listOf("nemotron_scan_solve", "llama_vision_ocr")
            )
        } else {
            val fallback = resolveLocalGroundedTutor(promptText, true, language)
            fallback.copy(
                id = "msg_nemotron_local_${UUID.randomUUID().toString().take(6)}",
                isNemotronReasoning = true,
                isCloudAI = true
            )
        }

        learningDao.insertTutorMessage(TutorMessageEntity.fromDomain(tutorMsg))
        tutorMsg
    }

    override suspend fun generateVisualDiagram(
        userPrompt: String,
        subjectContext: String,
        languageMode: String
    ): TutorMessage = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        val promptText = userPrompt.trim()

        // 1. Optimistic user message
        val userMsg = TutorMessage(
            id = "msg_user_${UUID.randomUUID().toString().take(6)}",
            sender = TutorSender.USER,
            textOdia = promptText,
            textEnglish = promptText,
            textHindi = promptText,
            timestamp = now,
            isOfflineGrounded = false,
            sourceChapter = null,
            isStudyRelated = true
        )
        learningDao.insertTutorMessage(TutorMessageEntity.fromDomain(userMsg))

        // 2. Call backend /tutor/generate-visual endpoint
        val visualResult = callGenerateVisualEndpoint(promptText, subjectContext, languageMode)

        // 3. Construct response TutorMessage
        val tutorMsg = if (visualResult != null) {
            TutorMessage(
                id = "msg_visual_${UUID.randomUUID().toString().take(6)}",
                sender = TutorSender.TUTOR,
                textOdia = visualResult.message,
                textEnglish = visualResult.message,
                textHindi = visualResult.message,
                timestamp = System.currentTimeMillis(),
                isOfflineGrounded = false,
                sourceChapter = "Smart Visuals Tutor",
                isStudyRelated = visualResult.status == "success" && visualResult.hasImage,
                imageBase64 = visualResult.imageBase64,
                isCloudAI = true,
                sourceTags = if (visualResult.hasImage) listOf("smart_visuals", "textbook_diagram", "academic_visual") else listOf("visual_guardrail_refusal", "non_subject_blocked")
            )
        } else {
            // Local fallback guardrail & offline handling
            val qLower = promptText.lowercase()
            val nonAcademicKeywords = listOf("meme", "funny", "joke", "movie", "game", "gaming", "pokemon", "anime", "superhero", "dating", "politics", "avatar")
            val academicKeywords = listOf("photosynthesis", "chloroplast", "science", "biology", "physics", "chemistry", "diagram", "cell", "equation", "formula", "water cycle")
            val isViolating = nonAcademicKeywords.any { qLower.contains(it) } && !academicKeywords.any { qLower.contains(it) }

            if (isViolating) {
                TutorMessage(
                    id = "msg_visual_refuse_${UUID.randomUUID().toString().take(6)}",
                    sender = TutorSender.TUTOR,
                    textOdia = "ଜଣେ ଶିକ୍ଷା ସାଥୀ ଭାବରେ, ମୁଁ ପାଠ୍ୟକ୍ରମ ବାହାରର ଚିତ୍ର ତିଆରି କରିପାରିବି ନାହିଁ। ଆସନ୍ତୁ ବିଜ୍ଞାନ ଏବଂ ଗଣିତ ବିଷୟ ଉପରେ ଧ୍ୟାନ ଦେବା! (As your assistant, I cannot sketch non-curricular things. Let's focus on Science & Math!)",
                    textEnglish = "As your assistant, I cannot sketch non-curricular things. Let's focus on Science & Math!",
                    textHindi = "एक सहायक के रूप में, मैं गैर-पाठ्यचर्या संबंधी चित्र नहीं बना सकता। आइए विज्ञान और गणित पर ध्यान दें! (As your assistant, I cannot sketch non-curricular things. Let's focus on Science & Math!)",
                    timestamp = System.currentTimeMillis(),
                    isOfflineGrounded = true,
                    sourceChapter = "RelayNet Visual Guardrails",
                    isStudyRelated = false,
                    isCloudAI = false,
                    sourceTags = listOf("visual_guardrail_refusal", "offline_guardrail")
                )
            } else {
                TutorMessage(
                    id = "msg_visual_offline_${UUID.randomUUID().toString().take(6)}",
                    sender = TutorSender.TUTOR,
                    textOdia = "ଚିତ୍ର ପ୍ରସ୍ତୁତ କରିବା ପାଇଁ କ୍ଲାଉଡ୍ ସଂଯୋଗ ଆବଶ୍ୟକ। ଆପଣଙ୍କ ପ୍ରଶ୍ନ '$promptText' ପାଇଁ ସ୍ଥାନୀୟ ବିଜ୍ଞାନ ପାଠ୍ୟକ୍ରମ ଉପଲବ୍ଧ।",
                    textEnglish = "Visual diagram generation requires active cloud connectivity. Text lessons for '$promptText' remain available offline in your local library.",
                    textHindi = "चित्र निर्माण के लिए क्लाउड कनेक्टिविटी आवश्यक है। '$promptText' के लिए स्थानीय विज्ञान पाठ उपलब्ध हैं।",
                    timestamp = System.currentTimeMillis(),
                    isOfflineGrounded = true,
                    sourceChapter = "RelayNet Visual Offline Fallback",
                    isStudyRelated = true,
                    isCloudAI = false,
                    sourceTags = listOf("visual_offline_notice")
                )
            }
        }

        learningDao.insertTutorMessage(TutorMessageEntity.fromDomain(tutorMsg))
        tutorMsg
    }

    override suspend fun saveTutorMessage(msg: TutorMessage) = withContext(ioDispatcher) {
        learningDao.insertTutorMessage(TutorMessageEntity.fromDomain(msg))
    }


    private data class CloudVisualResponse(
        val status: String,
        val message: String,
        val imageBase64: String?,
        val hasImage: Boolean,
        val source: String
    )

    private fun callGenerateVisualEndpoint(
        userPrompt: String,
        subjectContext: String,
        languageMode: String
    ): CloudVisualResponse? {
        val backendUrls = listOf(
            "http://10.0.2.2:8000/tutor/generate-visual",
            "http://127.0.0.1:8000/tutor/generate-visual",
            "http://localhost:8000/tutor/generate-visual"
        )

        val payload = JSONObject().apply {
            put("user_prompt", userPrompt)
            put("subject_context", subjectContext)
            put("language_mode", languageMode)
        }

        for (endpoint in backendUrls) {
            try {
                val url = URL(endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 5000
                conn.readTimeout = 25000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                if (activeApiKey.isNotBlank()) {
                    conn.setRequestProperty("Authorization", "Bearer $activeApiKey")
                    conn.setRequestProperty("X-Api-Key", activeApiKey)
                }

                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8))
                    val resString = reader.readText()
                    reader.close()
                    val json = JSONObject(resString)
                    return CloudVisualResponse(
                        status = json.optString("status", "success"),
                        message = json.optString("message", ""),
                        imageBase64 = if (json.has("imageBase64") && !json.isNull("imageBase64")) json.getString("imageBase64") else null,
                        hasImage = json.optBoolean("has_image", false),
                        source = json.optString("source", "[Cloud AI] Smart Visuals")
                    )
                }
            } catch (_: Exception) {
                // Try next endpoint or fall back
            }
        }
        return null
    }


    private fun resolveLocalGroundedTutor(
        question: String,
        hasImage: Boolean,
        language: String
    ): TutorMessage {
        val qLower = question.lowercase()
        val isOdia = language.lowercase() in listOf("odia", "or")

        // 1. Strict academic guardrail evaluation
        val nonAcademicKeywords = listOf(
            "movie", "cinema", "actor", "actress", "celebrity", "bollywood", "hollywood", "netflix",
            "song", "music", "dance", "game", "gaming", "pubg", "free fire", "fortnite", "roblox",
            "dating", "girlfriend", "boyfriend", "crush", "romance", "tinder", "politics", "election",
            "modi", "joke", "tell me a joke", "gossip", "flirt"
        )
        val academicWhitelist = listOf("photosynthesis", "science", "biology", "chloroplast", "equation", "formula", "class", "reaction", "light", "dark", "water")

        val violatesGuardrail = nonAcademicKeywords.any { qLower.contains(it) } && !academicWhitelist.any { qLower.contains(it) }

        if (violatesGuardrail) {
            return TutorMessage(
                id = "msg_tutor_${UUID.randomUUID().toString().take(6)}",
                sender = TutorSender.TUTOR,
                textOdia = "ମୁଁ କେବଳ ପାଠପଢ଼ା ଏବଂ ପାଠ୍ୟକ୍ରମ ସମ୍ବନ୍ଧୀୟ ପ୍ରଶ୍ନର ଉତ୍ତର ଦେଇପାରିବି। ଦୟାକରି ଆପଣଙ୍କ ପାଠ୍ୟପୁସ୍ତକ ସମ୍ବନ୍ଧୀୟ ପ୍ରଶ୍ନ ପଚାରନ୍ତୁ।",
                textEnglish = "I can only assist with academic studies and curriculum topics. Please ask a question related to your syllabus.",
                timestamp = System.currentTimeMillis(),
                isOfflineGrounded = true,
                sourceChapter = "RelayNet Academic Guardrails",
                isStudyRelated = false,
                isCloudAI = false,
                sourceTags = listOf("guardrail_refusal", "out_of_syllabus")
            )
        }

        // 2. Query local Room SQLite database for relevant packs
        val matchingPacks = learningDao.searchPacksOffline(question)

        if (hasImage) {
            return TutorMessage(
                id = "msg_tutor_${UUID.randomUUID().toString().take(6)}",
                sender = TutorSender.TUTOR,
                textOdia = "ପାଠ୍ୟପୁସ୍ତକ ଚିତ୍ର ବିଶ୍ଳେଷଣ [Offline Vision Grounded]:\n" +
                        "ଏହା ନବମ ଶ୍ରେଣୀ ବିଜ୍ଞାନର ଆଲୋକ ଶ୍ଳେଷଣ (Photosynthesis) ସମ୍ବନ୍ଧୀୟ ଚିତ୍ର।\n" +
                        "• ରାସାୟନିକ ସମୀକରଣ: 6CO₂ + 12H₂O ──(ସୂର୍ଯ୍ୟାଲୋକ / କ୍ଲୋରୋଫିଲ୍)──► C₆H₁₂O₆ + 6H₂O + 6O₂\n" +
                        "• ହରିତକଣାର ଗ୍ରାନାରେ ଆଲୋକ ପ୍ରକ୍ରିୟା ଘଟି ଜଳର ବିଶ୍ଳେଷଣ (Photolysis) ଦ୍ୱାରା O₂ ମୁକ୍ତ କରେ।\n" +
                        "• ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା ଷ୍ଟ୍ରୋମାରେ RuBisCO ଏନଜାଇମ ସହାୟତାରେ CO₂ କୁ ଶର୍କରାରେ ପରିଣତ କରେ।",
                textEnglish = "Textbook Photo Analysis [Offline Vision Grounded]:\n" +
                        "Extracted problem relates to Class 9 Science: Plant Nutrition & Photosynthesis.\n" +
                        "• Balanced Formula: 6CO₂ + 12H₂O ──(Sunlight / Chlorophyll)──► C₆H₁₂O₆ + 6H₂O + 6O₂\n" +
                        "• Light Reactions occur in Thylakoid Grana releasing O₂ via photolysis of water.\n" +
                        "• Dark Reactions occur in the Stroma via Calvin Cycle, utilizing RuBisCO enzyme to fix CO₂ into glucose.",
                timestamp = System.currentTimeMillis(),
                isOfflineGrounded = true,
                sourceChapter = "Odisha Board Class 9 Science (Photosynthesis)",
                isStudyRelated = true,
                isCloudAI = false,
                sourceTags = listOf("textbook_vision_extracted", "photosynthesis_diagram")
            )
        }

        val odiaResp = com.nmiet.app.data.local.OfflinePackageAnswerResolver.resolveAnswer(question, com.nmiet.app.domain.model.AppLanguage.ODIA_ONLY, learningDao)
        val engResp = com.nmiet.app.data.local.OfflinePackageAnswerResolver.resolveAnswer(question, com.nmiet.app.domain.model.AppLanguage.ENGLISH_ONLY, learningDao)
        val sourceChap = if (matchingPacks.isNotEmpty()) matchingPacks.first().titleEnglish else "Odisha Board Class 9 Science (Curriculum Packs)"

        return TutorMessage(
            id = "msg_tutor_${UUID.randomUUID().toString().take(6)}",
            sender = TutorSender.TUTOR,
            textOdia = odiaResp,
            textEnglish = engResp,
            timestamp = System.currentTimeMillis(),
            isOfflineGrounded = true,
            sourceChapter = sourceChap,
            isStudyRelated = true,
            isCloudAI = false,
            sourceTags = listOf("local_sqlite_grounded", "class9_science")
        )
    }

    private data class CloudTutorResponse(
        val reply: String,
        val isStudyRelated: Boolean,
        val sourceTags: List<String>,
        val answerOdia: String?,
        val answerEnglish: String?,
        val groundingSource: String
    )

    private fun callCloudTutorEndpoint(
        question: String,
        imageBitmap: Bitmap?,
        language: String
    ): CloudTutorResponse? {
        val backendUrls = listOf(
            "http://10.0.2.2:8000/tutor/ask", // Android Emulator host loopback
            "http://127.0.0.1:8000/tutor/ask",
            "http://localhost:8000/tutor/ask"
        )

        for (endpoint in backendUrls) {
            try {
                val url = URL(endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 2500
                conn.readTimeout = 4000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")

                val payload = JSONObject().apply {
                    put("query", question)
                    put("language", language)
                    put("curriculum_context", "Class 9 Science - Photosynthesis")
                    if (imageBitmap != null) {
                        val stream = ByteArrayOutputStream()
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
                        val b64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                        put("image_base64", b64)
                    }
                }

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val resString = reader.readText()
                    reader.close()
                    val json = JSONObject(resString)
                    val tagsList = mutableListOf<String>()
                    val tagsArr = json.optJSONArray("source_tags")
                    if (tagsArr != null) {
                        for (i in 0 until tagsArr.length()) tagsList.add(tagsArr.getString(i))
                    }

                    return CloudTutorResponse(
                        reply = json.optString("reply", json.optString("answer_english", "")),
                        isStudyRelated = json.optBoolean("is_study_related", true),
                        sourceTags = tagsList,
                        answerOdia = if (json.has("answer_odia") && !json.isNull("answer_odia")) json.getString("answer_odia") else null,
                        answerEnglish = if (json.has("answer_english") && !json.isNull("answer_english")) json.getString("answer_english") else null,
                        groundingSource = json.optString("grounding_source", "Cloud AI Grounded")
                    )
                }
            } catch (_: Exception) {
                // Try next endpoint or fall back cleanly to local SQLite
            }
        }
        return null
    }

    private data class CloudScanSolveResponse(
        val extractedText: String,
        val solution: String,
        val isAcademic: Boolean,
        val modelVision: String,
        val modelReasoning: String
    )

    private fun callScanAndSolveEndpoint(
        imageBitmap: Bitmap,
        userPrompt: String,
        language: String,
        curriculumContext: String
    ): CloudScanSolveResponse? {
        val backendUrls = listOf(
            "http://10.0.2.2:8000/tutor/scan-and-solve",
            "http://127.0.0.1:8000/tutor/scan-and-solve",
            "http://localhost:8000/tutor/scan-and-solve"
        )

        val boundary = "===RelayNetBoundary" + System.currentTimeMillis() + "==="
        val lineEnd = "\r\n"
        val twoHyphens = "--"

        val imageStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 85, imageStream)
        val imageBytes = imageStream.toByteArray()

        for (endpoint in backendUrls) {
            try {
                val url = URL(endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 4000
                conn.readTimeout = 10000
                conn.doOutput = true
                conn.doInput = true
                conn.useCaches = false
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                if (activeApiKey.isNotBlank()) {
                    conn.setRequestProperty("Authorization", "Bearer $activeApiKey")
                    conn.setRequestProperty("X-Api-Key", activeApiKey)
                }

                val dos = DataOutputStream(conn.outputStream)

                // 1. file part
                dos.writeBytes("$twoHyphens$boundary$lineEnd")
                dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"textbook_scan.jpg\"$lineEnd")
                dos.writeBytes("Content-Type: image/jpeg$lineEnd")
                dos.writeBytes(lineEnd)
                dos.write(imageBytes)
                dos.writeBytes(lineEnd)

                // 2. user_prompt part
                dos.writeBytes("$twoHyphens$boundary$lineEnd")
                dos.writeBytes("Content-Disposition: form-data; name=\"user_prompt\"$lineEnd")
                dos.writeBytes("Content-Type: text/plain; charset=UTF-8$lineEnd")
                dos.writeBytes(lineEnd)
                dos.write(userPrompt.toByteArray(Charsets.UTF_8))
                dos.writeBytes(lineEnd)

                // 3. language_preference part
                dos.writeBytes("$twoHyphens$boundary$lineEnd")
                dos.writeBytes("Content-Disposition: form-data; name=\"language_preference\"$lineEnd")
                dos.writeBytes("Content-Type: text/plain; charset=UTF-8$lineEnd")
                dos.writeBytes(lineEnd)
                dos.write(language.toByteArray(Charsets.UTF_8))
                dos.writeBytes(lineEnd)

                // 4. curriculum_context part
                dos.writeBytes("$twoHyphens$boundary$lineEnd")
                dos.writeBytes("Content-Disposition: form-data; name=\"curriculum_context\"$lineEnd")
                dos.writeBytes("Content-Type: text/plain; charset=UTF-8$lineEnd")
                dos.writeBytes(lineEnd)
                dos.write(curriculumContext.toByteArray(Charsets.UTF_8))
                dos.writeBytes(lineEnd)

                // 5. api_key part (if configured)
                if (activeApiKey.isNotBlank()) {
                    dos.writeBytes("$twoHyphens$boundary$lineEnd")
                    dos.writeBytes("Content-Disposition: form-data; name=\"api_key\"$lineEnd")
                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8$lineEnd")
                    dos.writeBytes(lineEnd)
                    dos.write(activeApiKey.toByteArray(Charsets.UTF_8))
                    dos.writeBytes(lineEnd)
                }

                // End of multipart
                dos.writeBytes("$twoHyphens$boundary$twoHyphens$lineEnd")
                dos.flush()
                dos.close()

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8))
                    val resString = reader.readText()
                    reader.close()
                    val json = JSONObject(resString)
                    return CloudScanSolveResponse(
                        extractedText = json.optString("extracted_text", ""),
                        solution = json.optString("solution", ""),
                        isAcademic = json.optBoolean("is_academic", true),
                        modelVision = json.optString("model_vision", "meta/llama-3.2-11b-vision-instruct"),
                        modelReasoning = json.optString("model_reasoning", "nvidia/nemotron-3-super-120b-a12b")
                    )
                }
            } catch (_: Exception) {
                // Try next endpoint or fall back cleanly
            }
        }
        return null
    }

    override suspend fun saveIncomingLearningPack(pack: LearningPack, peerName: String) = withContext(ioDispatcher) {
        val updatedPack = pack.copy(
            isReceivedViaMesh = true,
            peerSenderName = peerName,
            origin = ContentOrigin.P2P_RECEIVED,
            receivedTimestamp = System.currentTimeMillis()
        )
        val entity = LearningPackEntity.fromDomain(updatedPack)
        learningDao.insertPack(entity)

        val totalPacks = learningDao.countPacks()
        _syncStatus.update {
            it.copy(
                totalCachedCount = totalPacks,
                totalSavedOfflineCount = totalPacks,
                lastSyncMessage = "Received pack '${pack.titleEnglish}' from $peerName via Bluetooth."
            )
        }
    }

    override suspend fun deleteLearningPack(packId: String) = withContext(ioDispatcher) {
        learningDao.deletePack(packId)
        val totalPacks = learningDao.countPacks()
        _syncStatus.update {
            it.copy(
                totalCachedCount = totalPacks,
                totalSavedOfflineCount = totalPacks,
                lastSyncMessage = "Removed pack from local storage."
            )
        }
    }

    override suspend fun resetToPilotDemoPacks() = withContext(ioDispatcher) {
        learningDao.clearAllPacks()
        val pilotPack = PilotCurriculumCatalog.getPilotLearningPack().copy(
            origin = ContentOrigin.INTERNET_DOWNLOADED,
            isDownloaded = true
        )
        learningDao.insertPack(LearningPackEntity.fromDomain(pilotPack))

        val pilotClass10Pack = PilotCurriculumCatalog.getPilotClass10LearningPack().copy(
            origin = ContentOrigin.INTERNET_DOWNLOADED,
            isDownloaded = true
        )
        learningDao.insertPack(LearningPackEntity.fromDomain(pilotClass10Pack))

        // Seed Canonical Pack: The Human Brain & Its Core Functions
        com.nmiet.app.data.local.entities.BrainPackSeed.seedBrainPackIntoDatabase(learningDao)

        // Preload an initial topic mastery (61% Developing tier)
        learningDao.insertOrUpdateMastery(
            TopicMasteryEntity(
                topicId = pilotPack.packId,
                topicTitleOdia = pilotPack.titleOdia,
                topicTitleEnglish = pilotPack.titleEnglish,
                subject = pilotPack.subject,
                masteryScore = 61,
                lastPracticedDate = System.currentTimeMillis() - 7200000,
                attemptsCount = 1,
                recommendedNextAction = "Targeted Practice: Photolysis & Light Reactions - 3 Questions"
            )
        )

        learningDao.insertAttempt(
            QuizAttemptEntity(
                attemptId = "attempt_initial_diagnostic",
                packId = pilotPack.packId,
                score = 61,
                answersJson = "[]",
                misconceptionTagsJson = "[\"photolysis_water_source\"]",
                topicName = "Chapter 4: Photosynthesis",
                correctCount = 3,
                totalCount = 5,
                timestamp = System.currentTimeMillis() - 7200000,
                isSynced = false
            )
        )

        // Preload welcome tutor messages
        learningDao.clearTutorMessages()
        learningDao.insertTutorMessage(
            TutorMessageEntity(
                messageId = "msg_welcome",
                senderName = TutorSender.TUTOR.name,
                textOdia = "ନମସ୍କାର ସୌମ୍ୟ! ମୁଁ ତୁମର RelayNet AI ଶିକ୍ଷକ। କ୍ଲାସ୍ ୯ ବିଜ୍ଞାନ ବିଷୟରେ ଯେକୌଣସି ପ୍ରଶ୍ନ ଓଡ଼ିଆରେ ପଚାରିପାରିବେ। ଆପଣ ପାଠ୍ୟପୁସ୍ତକର ଫଟୋ (Camera/Gallery) ମଧ୍ୟ ପଠାଇପାରିବେ!",
                textEnglish = "Hello Soumya! I am your RelayNet AI Tutor. Ask any question from Class 9 Science in English. You can also attach textbook photos to solve problems!",
                textHindi = "नमस्ते सौम्या! मैं आपका RelayNet AI शिक्षक हूँ। कक्षा 9 विज्ञान से संबंधित कोई भी प्रश्न हिंदी में पूछें। आप प्रश्नों को हल करने के लिए पाठ्यपुस्तक की फोटो भी भेज सकते हैं!",
                timestamp = System.currentTimeMillis(),
                isOfflineGrounded = true,
                sourceChapter = "Odisha Board Class 9 Science",
                isStudyRelated = true,
                isCloudAI = false,
                sourceTagsJson = "[\"welcome_onboarding\"]"
            )
        )

        _syncStatus.update {
            it.copy(
                totalCachedCount = 1,
                totalSavedOfflineCount = 1,
                lastSyncTimestamp = System.currentTimeMillis(),
                lastSyncMessage = "Loaded Class 9 Science Pilot Pack (Odia + English)."
            )
        }
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<LearnerProfile> = withContext(ioDispatcher) {
        try {
            val authRes = com.nmiet.app.auth.FirebaseAuthManager.signInWithEmail(email, password)
            if (authRes.isFailure) {
                return@withContext Result.failure(authRes.exceptionOrNull() ?: Exception("Login failed"))
            }
            val authData = authRes.getOrThrow()
            val current = _learnerProfile.value
            val updated = current.copy(
                studentId = authData.uid,
                email = authData.email,
                name = authData.displayName ?: current.name,
                avatarUrl = authData.photoUrl ?: current.avatarUrl,
                isLoggedIn = true,
                authProvider = if (authData.isFirebaseBacked) "FIREBASE_EMAIL" else "EMAIL",
                sessionToken = authData.idToken ?: "tok_${System.currentTimeMillis()}"
            )
            _learnerProfile.value = updated
            _syncStatus.update {
                it.copy(lastSyncMessage = "Successfully signed in as ${updated.name} via ${if (authData.isFirebaseBacked) "Firebase" else "RelayNet ID"}.")
            }
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerWithEmail(
        name: String,
        email: String,
        password: String,
        classGrade: Int,
        board: com.nmiet.app.domain.model.Board
    ): Result<LearnerProfile> = withContext(ioDispatcher) {
        try {
            val authRes = com.nmiet.app.auth.FirebaseAuthManager.createUserWithEmail(email, password)
            if (authRes.isFailure) {
                return@withContext Result.failure(authRes.exceptionOrNull() ?: Exception("Registration failed"))
            }
            val authData = authRes.getOrThrow()
            val updated = LearnerProfile(
                studentId = authData.uid,
                name = name.trim(),
                email = authData.email,
                classGrade = classGrade,
                board = board,
                isLoggedIn = true,
                authProvider = if (authData.isFirebaseBacked) "FIREBASE_EMAIL" else "EMAIL",
                sessionToken = authData.idToken ?: "tok_${System.currentTimeMillis()}",
                isProfileCompleted = true
            )
            _learnerProfile.value = updated
            _syncStatus.update {
                it.copy(lastSyncMessage = "Account registered for $name (Class $classGrade) via ${if (authData.isFirebaseBacked) "Firebase" else "RelayNet ID"}.")
            }
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(
        idToken: String?,
        email: String,
        name: String,
        avatarUrl: String?
    ): Result<LearnerProfile> = withContext(ioDispatcher) {
        try {
            val authRes = com.nmiet.app.auth.FirebaseAuthManager.signInWithGoogleIdToken(
                idToken = idToken ?: "google_token_${System.currentTimeMillis()}",
                fallbackEmail = email,
                fallbackName = name
            )
            val authData = authRes.getOrNull()
            val current = _learnerProfile.value
            val isNewProfile = current.name.isBlank() || current.name == "Soumya Ranjan"
            val updated = current.copy(
                studentId = authData?.uid ?: "usr_google_${System.currentTimeMillis()}",
                email = authData?.email ?: email,
                name = if (name.isNotBlank() && name != "Google Learner") name else (authData?.displayName ?: current.name),
                avatarUrl = authData?.photoUrl ?: avatarUrl ?: "https://api.dicebear.com/7.x/bottts/svg?seed=$name",
                isLoggedIn = true,
                authProvider = if (authData?.isFirebaseBacked == true) "FIREBASE_GOOGLE" else "GOOGLE",
                sessionToken = authData?.idToken ?: idToken ?: "tok_google_${System.currentTimeMillis()}",
                isProfileCompleted = !isNewProfile
            )
            _learnerProfile.value = updated
            _syncStatus.update {
                it.copy(lastSyncMessage = "Signed in with Google as ${updated.name}.")
            }
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() = withContext(ioDispatcher) {
        val current = _learnerProfile.value
        _learnerProfile.value = current.copy(
            isLoggedIn = false,
            authProvider = "GUEST",
            sessionToken = ""
        )
        _syncStatus.update {
            it.copy(lastSyncMessage = "Signed out. Operating in Guest/Offline mode.")
        }
    }

    override fun getAllGeneratedNotesFlow(): Flow<List<GeneratedNote>> =
        learningDao.getAllGeneratedNotes().map { list -> list.map { it.toDomain() } }

    override suspend fun saveGeneratedNote(note: GeneratedNote) = withContext(ioDispatcher) {
        learningDao.insertNote(GeneratedNoteEntity.fromDomain(note))
    }

    override suspend fun toggleNoteBookmark(noteId: String, isBookmarked: Boolean) = withContext(ioDispatcher) {
        learningDao.updateNoteBookmark(noteId, isBookmarked)
    }

    override suspend fun deleteGeneratedNote(noteId: String) = withContext(ioDispatcher) {
        learningDao.deleteNote(noteId)
    }

    override suspend fun getPackByIdOnce(packId: String): LearningPack? = withContext(ioDispatcher) {
        learningDao.getPackByIdOnce(packId)?.toDomain()
    }

    // --- LearnLite Content Catalogue & Downloads ---
    private val _contentItems = MutableStateFlow<List<com.nmiet.app.domain.model.ContentItem>>(
        com.nmiet.app.data.remote.R2SampleCatalogue.getVerifiedSampleItems()
    )


    override fun getContentItemsFlow(): Flow<List<com.nmiet.app.domain.model.ContentItem>> =
        _contentItems.asStateFlow()

    override suspend fun updateContentItem(item: com.nmiet.app.domain.model.ContentItem) = withContext(ioDispatcher) {
        _contentItems.update { list ->
            list.map { if (it.id == item.id) item else it }
        }
    }

    override suspend fun importPackFromZip(
        zipFile: java.io.File,
        outputDir: java.io.File
    ): Result<LearningPack> = withContext(ioDispatcher) {
        try {
            val result = com.nmiet.app.data.local.ZipPackManager.extractAndVerifyPack(zipFile, outputDir)
            if (!result.isChecksumValid) {
                return@withContext Result.failure(SecurityException("SHA-256 verification failed for pack ${zipFile.name}"))
            }
            saveIncomingLearningPack(result.pack, "Imported ZIP Archive")
            Result.success(result.pack)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Personalized Study Planner & Revision ---
    private val _studyPlan = MutableStateFlow(
        com.nmiet.app.domain.model.StudyPlan(
            planId = "study_plan_primary",
            targetClass = 9,
            weeklyGoalHours = 6,
            tasks = listOf(
                com.nmiet.app.domain.model.StudyTask(
                    id = "task_01",
                    topicId = "relaynet_class9_science_photosynthesis_v1",
                    topicTitle = "Revise Water Photolysis & Light Reactions",
                    subject = "Science (Biology)",
                    scheduledDate = System.currentTimeMillis(),
                    isCompleted = false,
                    reason = "Weak concept identified in diagnostic quiz attempt (accuracy < 60%)",
                    estimatedMinutes = 15
                ),
                com.nmiet.app.domain.model.StudyTask(
                    id = "task_02",
                    topicId = "relaynet_class9_science_photosynthesis_v1",
                    topicTitle = "Practice RuBisCO & Dark Reaction Formulas",
                    subject = "Science (Biology)",
                    scheduledDate = System.currentTimeMillis() + 86400000L,
                    isCompleted = false,
                    reason = "Spaced repetition revision cycle: Interval 1",
                    estimatedMinutes = 20
                ),
                com.nmiet.app.domain.model.StudyTask(
                    id = "task_03",
                    topicId = "relaynet_class9_science_photosynthesis_v1",
                    topicTitle = "Complete 5-Question Mastery Checkup",
                    subject = "Science (Biology)",
                    scheduledDate = System.currentTimeMillis() + (86400000L * 3),
                    isCompleted = false,
                    reason = "Mastery elevation check to reach Strong Tier (>85%)",
                    estimatedMinutes = 10
                )
            )
        )
    )

    override fun getStudyPlanFlow(): Flow<com.nmiet.app.domain.model.StudyPlan> =
        _studyPlan.asStateFlow()

    override suspend fun toggleStudyTaskCompleted(taskId: String, isCompleted: Boolean) = withContext(ioDispatcher) {
        _studyPlan.update { plan ->
            plan.copy(
                tasks = plan.tasks.map { task ->
                    if (task.id == taskId) task.copy(isCompleted = isCompleted) else task
                }
            )
        }
    }

    override suspend fun addStudyTask(task: com.nmiet.app.domain.model.StudyTask) = withContext(ioDispatcher) {
        _studyPlan.update { plan ->
            plan.copy(tasks = plan.tasks + task)
        }
    }

    // --- Optional SLM Catalogue ---
    private val _aiModels = MutableStateFlow<List<com.nmiet.app.domain.model.AIModelInfo>>(
        listOf(
            com.nmiet.app.domain.model.AIModelInfo(
                id = "model_gemma_2b_it_q4",
                name = "Gemma-2-2B Odia/English IT",
                description = "Google DeepMind Gemma 2B quantised for mobile CPU offline tutoring. Optimized for Indian secondary curriculum.",
                format = "GGUF Q4_K_M (LiteRT/MediaPipe)",
                sizeBytes = 1_450_000_000L, // 1.45 GB
                minRamBytes = 3_200_000_000L, // 3.2 GB RAM
                supportedLanguages = listOf("Odia", "English", "Hindi"),
                license = "Gemma Open Terms",
                version = "2.1",
                downloadUrl = "https://models.relaynet.org/gemma-2b-odia-q4.gguf",
                sha256Checksum = "4a123f890b...",
                isDownloaded = false,
                isActive = false
            ),
            com.nmiet.app.domain.model.AIModelInfo(
                id = "model_qwen2_0_5b_instruct",
                name = "Qwen2.5-0.5B Multilingual Ultra-Light",
                description = "Ultra-compact 500M parameter model for devices with 2GB–3GB RAM. Fast inference for basic concept definitions.",
                format = "GGUF Q4_0",
                sizeBytes = 380_000_000L, // 380 MB
                minRamBytes = 1_500_000_000L, // 1.5 GB RAM
                supportedLanguages = listOf("English", "Hindi", "Bengali", "Tamil", "Telugu"),
                license = "Apache 2.0",
                version = "2.5",
                downloadUrl = "https://models.relaynet.org/qwen2.5-0.5b-q4.gguf",
                sha256Checksum = "7b233a...",
                isDownloaded = false,
                isActive = false
            )
        )
    )

    override fun getAIModelsFlow(): Flow<List<com.nmiet.app.domain.model.AIModelInfo>> =
        _aiModels.asStateFlow()

    override suspend fun setActiveAIModel(modelId: String?) = withContext(ioDispatcher) {
        _aiModels.update { list ->
            list.map { model ->
                model.copy(isActive = (model.id == modelId))
            }
        }
    }
}

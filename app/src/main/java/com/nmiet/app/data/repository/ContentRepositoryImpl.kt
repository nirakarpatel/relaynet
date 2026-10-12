package com.nmiet.app.data.repository

import com.nmiet.app.data.local.LearningDao
import com.nmiet.app.data.local.LearningPackEntity
import com.nmiet.app.data.local.QuizAttemptEntity
import com.nmiet.app.data.local.TopicMasteryEntity
import com.nmiet.app.data.local.TutorMessageEntity
import com.nmiet.app.data.remote.PilotCurriculumCatalog
import com.nmiet.app.domain.model.CurriculumNode
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
import java.util.UUID

class ContentRepositoryImpl(
    private val learningDao: LearningDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ContentRepository {

    private val _learnerProfile = MutableStateFlow(LearnerProfile())
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
        val newScore = if (existingMastery == null) {
            scorePercent
        } else {
            // Weighted average: 65% recent + 35% past
            ((scorePercent * 0.65) + (existingMastery.masteryScore * 0.35)).toInt().coerceIn(0, 100)
        }

        val recommendedAction = when {
            weakConcepts.contains("photolysis_water_source") ->
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

    override suspend fun askTutor(questionText: String): TutorMessage = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        val userMsg = TutorMessageEntity(
            messageId = "msg_user_${UUID.randomUUID().toString().take(6)}",
            senderName = TutorSender.USER.name,
            textOdia = questionText,
            textEnglish = questionText,
            timestamp = now,
            isOfflineGrounded = false,
            sourceChapter = null
        )
        learningDao.insertTutorMessage(userMsg)

        val isOnline = _syncStatus.value.isOnline
        val (odiaResp, engResp) = PilotCurriculumCatalog.getOfflineTutorResponse(questionText)

        val tutorMsg = TutorMessageEntity(
            messageId = "msg_tutor_${UUID.randomUUID().toString().take(6)}",
            senderName = TutorSender.TUTOR.name,
            textOdia = odiaResp,
            textEnglish = engResp,
            timestamp = now + 400,
            isOfflineGrounded = !isOnline,
            sourceChapter = "Odisha Board Class 9 Science: Chapter 1 (Nutrition in Plants)"
        )
        learningDao.insertTutorMessage(tutorMsg)

        tutorMsg.toDomain()
    }

    override suspend fun saveIncomingLearningPack(pack: LearningPack, peerName: String) = withContext(ioDispatcher) {
        val entity = LearningPackEntity.fromDomain(pack.copy(isReceivedViaMesh = true, peerSenderName = peerName))
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
        val pilotPack = PilotCurriculumCatalog.getPilotLearningPack()
        learningDao.insertPack(LearningPackEntity.fromDomain(pilotPack))

        // Preload an initial topic mastery
        learningDao.insertOrUpdateMastery(
            TopicMasteryEntity(
                topicId = pilotPack.packId,
                topicTitleOdia = pilotPack.titleOdia,
                topicTitleEnglish = pilotPack.titleEnglish,
                subject = pilotPack.subject,
                masteryScore = 75,
                lastPracticedDate = System.currentTimeMillis() - 86400000,
                attemptsCount = 1,
                recommendedNextAction = "Complete quiz on Light & Dark reactions to reach Strong Mastery (>85%)"
            )
        )

        // Preload welcome tutor messages
        learningDao.clearTutorMessages()
        learningDao.insertTutorMessage(
            TutorMessageEntity(
                messageId = "msg_welcome",
                senderName = TutorSender.TUTOR.name,
                textOdia = "ନମସ୍କାର ସୌମ୍ୟ! ମୁଁ ତୁମର RelayNet AI ଶିକ୍ଷକ। କ୍ଲାସ୍ ୯ ବିଜ୍ଞାନ ବିଷୟରେ ଯେକୌଣସି ପ୍ରଶ୍ନ ଓଡ଼ିଆ କିମ୍ବା ଇଂରାଜୀରେ ପଚାରିପାରିବ। (ଇଣ୍ଟରନେଟ୍ ନଥିଲେ ମଧ୍ୟ ମୁଁ ଉତ୍ତର ଦେଇପାରିବି!)",
                textEnglish = "Hello Soumya! I am your RelayNet AI Tutor. You can ask any question from Class 9 Science in Odia or English. Even without internet, I can answer from downloaded packs!",
                timestamp = System.currentTimeMillis(),
                isOfflineGrounded = true,
                sourceChapter = "Odisha Board Class 9 Science"
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
}

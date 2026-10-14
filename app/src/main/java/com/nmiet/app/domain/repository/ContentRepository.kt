package com.nmiet.app.domain.repository

import android.graphics.Bitmap
import com.nmiet.app.domain.model.CurriculumNode
import com.nmiet.app.domain.model.LearnerProfile
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.QuizAttempt
import com.nmiet.app.domain.model.SyncStatus
import com.nmiet.app.domain.model.TopicMastery
import com.nmiet.app.domain.model.TutorMessage
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    fun getLearnerProfileFlow(): Flow<LearnerProfile>
    suspend fun updateLearnerProfile(profile: LearnerProfile)

    fun getCurriculumNodesFlow(): Flow<List<CurriculumNode>>
    fun getLearningPacksFlow(): Flow<List<LearningPack>>
    fun getPacksForStudentFlow(board: String, grade: String): Flow<List<LearningPack>>
    fun getDownloadedPacksFlow(): Flow<List<LearningPack>>
    fun getSharedPacksFlow(): Flow<List<LearningPack>>
    fun getLearningPackByIdFlow(packId: String): Flow<LearningPack?>
    fun getTopicMasteryFlow(): Flow<List<TopicMastery>>
    fun getRecentQuizAttemptsFlow(): Flow<List<QuizAttempt>>
    fun getTutorMessagesFlow(): Flow<List<TutorMessage>>
    fun getSyncStatusFlow(): Flow<SyncStatus>

    suspend fun submitQuizAttempt(
        topicId: String,
        topicName: String,
        scorePercent: Int,
        correctCount: Int,
        totalCount: Int,
        weakConcepts: List<String>
    )

    suspend fun askTutor(
        questionText: String,
        imageBitmap: Bitmap? = null,
        language: String = "odia"
    ): TutorMessage

    suspend fun scanAndSolveTextbook(
        imageBitmap: Bitmap,
        userPrompt: String? = null,
        language: String = "en_or",
        curriculumContext: String = "Class 9 Science - Biology / Life Science"
    ): TutorMessage

    suspend fun saveIncomingLearningPack(pack: LearningPack, peerName: String)
    suspend fun deleteLearningPack(packId: String)
    suspend fun resetToPilotDemoPacks()
    fun setSimulatedNetworkOnline(isOnline: Boolean)

    suspend fun loginWithEmail(email: String, password: String): Result<LearnerProfile>
    suspend fun registerWithEmail(name: String, email: String, password: String, classGrade: Int, board: com.nmiet.app.domain.model.Board): Result<LearnerProfile>
    suspend fun loginWithGoogle(idToken: String?, email: String, name: String, avatarUrl: String?): Result<LearnerProfile>
    suspend fun logout()

    fun getAllGeneratedNotesFlow(): Flow<List<com.nmiet.app.domain.model.GeneratedNote>>
    suspend fun saveGeneratedNote(note: com.nmiet.app.domain.model.GeneratedNote)
    suspend fun toggleNoteBookmark(noteId: String, isBookmarked: Boolean)
    suspend fun deleteGeneratedNote(noteId: String)
    suspend fun getPackByIdOnce(packId: String): LearningPack?

    // LearnLite Content Catalogue & Downloads
    fun getContentItemsFlow(): Flow<List<com.nmiet.app.domain.model.ContentItem>>
    suspend fun updateContentItem(item: com.nmiet.app.domain.model.ContentItem)
    suspend fun importPackFromZip(zipFile: java.io.File, outputDir: java.io.File): Result<LearningPack>

    // Personalized Study Planner & Revision
    fun getStudyPlanFlow(): Flow<com.nmiet.app.domain.model.StudyPlan>
    suspend fun toggleStudyTaskCompleted(taskId: String, isCompleted: Boolean)
    suspend fun addStudyTask(task: com.nmiet.app.domain.model.StudyTask)

    // Optional SLM Catalogue
    fun getAIModelsFlow(): Flow<List<com.nmiet.app.domain.model.AIModelInfo>>
    suspend fun setActiveAIModel(modelId: String?)

    // Cloud / NVIDIA AI API Key management
    fun setApiKey(key: String)
    fun getApiKey(): String
}

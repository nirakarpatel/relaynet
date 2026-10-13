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

    suspend fun saveIncomingLearningPack(pack: LearningPack, peerName: String)
    suspend fun deleteLearningPack(packId: String)
    suspend fun resetToPilotDemoPacks()
    fun setSimulatedNetworkOnline(isOnline: Boolean)

    fun getAllGeneratedNotesFlow(): Flow<List<com.nmiet.app.domain.model.GeneratedNote>>
    suspend fun saveGeneratedNote(note: com.nmiet.app.domain.model.GeneratedNote)
    suspend fun toggleNoteBookmark(noteId: String, isBookmarked: Boolean)
    suspend fun deleteGeneratedNote(noteId: String)
    suspend fun getPackByIdOnce(packId: String): LearningPack?
}

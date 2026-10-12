package com.nmiet.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningDao {

    // --- Learner Profile ---
    @Query("SELECT * FROM learner_profile WHERE profileId = :id LIMIT 1")
    fun getProfileById(id: String = "primary_learner"): Flow<LearnerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProfile(profile: LearnerProfileEntity)

    // --- Learning Packs ---
    @Query("SELECT * FROM learning_packs ORDER BY grade ASC, subject ASC")
    fun getAllPacks(): Flow<List<LearningPackEntity>>

    @Query("SELECT * FROM learning_packs WHERE packId = :packId LIMIT 1")
    fun getPackById(packId: String): Flow<LearningPackEntity?>

    @Query("SELECT * FROM learning_packs WHERE packId = :packId LIMIT 1")
    fun getPackByIdOnce(packId: String): LearningPackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPack(pack: LearningPackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllPacks(packs: List<LearningPackEntity>)

    @Query("DELETE FROM learning_packs WHERE packId = :packId")
    fun deletePack(packId: String)

    @Query("DELETE FROM learning_packs")
    fun clearAllPacks()

    @Query("SELECT COUNT(*) FROM learning_packs")
    fun countPacks(): Int

    // --- Quiz Attempts ---
    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC")
    fun getAllAttempts(): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttempt(attempt: QuizAttemptEntity)

    // --- Topic Mastery ---
    @Query("SELECT * FROM topic_mastery ORDER BY masteryScore DESC")
    fun getAllMastery(): Flow<List<TopicMasteryEntity>>

    @Query("SELECT * FROM topic_mastery WHERE topicId = :topicId LIMIT 1")
    fun getMasteryById(topicId: String): TopicMasteryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateMastery(mastery: TopicMasteryEntity)

    // --- Tutor Chat History ---
    @Query("SELECT * FROM tutor_messages ORDER BY timestamp ASC")
    fun getAllTutorMessages(): Flow<List<TutorMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTutorMessage(message: TutorMessageEntity)

    @Query("DELETE FROM tutor_messages")
    fun clearTutorMessages()
}

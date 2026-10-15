package com.nmiet.app

import com.nmiet.app.data.local.LearnerProfileEntity
import com.nmiet.app.data.local.LearningDao
import com.nmiet.app.data.local.LearningPackEntity
import com.nmiet.app.data.local.OfflinePackageAnswerResolver
import com.nmiet.app.data.local.QuizAttemptEntity
import com.nmiet.app.data.local.TopicMasteryEntity
import com.nmiet.app.data.local.TutorMessageEntity
import com.nmiet.app.data.local.GeneratedNoteEntity
import com.nmiet.app.data.remote.PilotCurriculumCatalog
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.ContentOrigin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflinePackageAnswerTest {

    private class TestLearningDao(val testPacks: List<LearningPackEntity>) : LearningDao {
        override fun getProfileById(id: String): Flow<LearnerProfileEntity?> = flowOf(null)
        override fun insertProfile(profile: LearnerProfileEntity) {}
        override fun getAllPacks(): Flow<List<LearningPackEntity>> = flowOf(testPacks)
        override fun getPacksForStudent(board: String, grade: String): Flow<List<LearningPackEntity>> = flowOf(testPacks)
        override fun getDownloadedPacks(): Flow<List<LearningPackEntity>> = flowOf(testPacks)
        override fun getSharedPacks(): Flow<List<LearningPackEntity>> = flowOf(emptyList())
        override fun getPacksByOrigin(origin: ContentOrigin): Flow<List<LearningPackEntity>> = flowOf(testPacks)
        override fun searchPacksOffline(query: String): List<LearningPackEntity> = testPacks
        override fun getAllPacksOnce(): List<LearningPackEntity> = testPacks
        override fun getPackById(packId: String): Flow<LearningPackEntity?> = flowOf(testPacks.firstOrNull { it.packId == packId })
        override fun getPackByIdOnce(packId: String): LearningPackEntity? = testPacks.firstOrNull { it.packId == packId }
        override fun insertPack(pack: LearningPackEntity) {}
        override fun insertAllPacks(packs: List<LearningPackEntity>) {}
        override fun deletePack(packId: String) {}
        override fun clearAllPacks() {}
        override fun countPacks(): Int = testPacks.size
        override fun getAllAttempts(): Flow<List<QuizAttemptEntity>> = flowOf(emptyList())
        override fun insertAttempt(attempt: QuizAttemptEntity) {}
        override fun getAllMastery(): Flow<List<TopicMasteryEntity>> = flowOf(emptyList())
        override fun getMasteryById(topicId: String): TopicMasteryEntity? = null
        override fun insertOrUpdateMastery(mastery: TopicMasteryEntity) {}
        override fun getAllTutorMessages(): Flow<List<TutorMessageEntity>> = flowOf(emptyList())
        override fun insertTutorMessage(message: TutorMessageEntity) {}
        override fun clearTutorMessages() {}
        override fun getAllGeneratedNotes(): Flow<List<GeneratedNoteEntity>> = flowOf(emptyList())
        override fun getNoteById(noteId: String): Flow<GeneratedNoteEntity?> = flowOf(null)
        override fun getNoteByTopicId(topicId: String): GeneratedNoteEntity? = null
        override fun insertNote(note: GeneratedNoteEntity) {}
        override fun updateNoteBookmark(noteId: String, isBookmarked: Boolean) {}
        override fun deleteNote(noteId: String) {}
    }

    private fun createDao(): LearningDao {
        val packs = listOf(
            LearningPackEntity.fromDomain(PilotCurriculumCatalog.getPilotLearningPack()),
            LearningPackEntity.fromDomain(com.nmiet.app.data.local.entities.BrainPackSeed.createLearningPack()),
            LearningPackEntity.fromDomain(PilotCurriculumCatalog.getPilotClass10LearningPack())
        )
        return TestLearningDao(packs)
    }

    @Test
    fun testKeyTermPhotolysisAnswer() {
        val dao = createDao()
        val answer = OfflinePackageAnswerResolver.resolveAnswer(
            query = "What is photolysis of water?",
            language = AppLanguage.ENGLISH_ONLY,
            learningDao = dao
        )
        assertTrue("Expected photolysis definition, got: $answer", answer.contains("Photolysis", ignoreCase = true) || answer.contains("splitting of water", ignoreCase = true))
    }

    @Test
    fun testBrainCerebrumAnswer() {
        val dao = createDao()
        val answer = OfflinePackageAnswerResolver.resolveAnswer(
            query = "What does the cerebrum do?",
            language = AppLanguage.ENGLISH_ONLY,
            learningDao = dao
        )
        assertTrue("Expected cerebrum info, got: $answer", answer.contains("Cerebrum", ignoreCase = true) || answer.contains("brain mass", ignoreCase = true) || answer.contains("thought", ignoreCase = true))
    }

    @Test
    fun testPhotosynthesisEquationAnswer() {
        val dao = createDao()
        val answer = OfflinePackageAnswerResolver.resolveAnswer(
            query = "What is the equation for photosynthesis?",
            language = AppLanguage.ENGLISH_ONLY,
            learningDao = dao
        )
        assertTrue("Expected equation, got: $answer", answer.contains("CO₂") || answer.contains("formula", ignoreCase = true) || answer.contains("equation", ignoreCase = true))
    }

    @Test
    fun testBrainElectricalPowerAnswer() {
        val dao = createDao()
        val answer = OfflinePackageAnswerResolver.resolveAnswer(
            query = "How much electrical power does the human brain produce?",
            language = AppLanguage.ENGLISH_ONLY,
            learningDao = dao
        )
        println("DEBUG_ANSWER: $answer")
        assertTrue("Expected watts info, got: $answer", answer.contains("Watts", ignoreCase = true) || answer.contains("12 to 25", ignoreCase = true) || answer.contains("bulb", ignoreCase = true) || answer.contains("power", ignoreCase = true))
    }

    @Test
    fun testDynamicGuidanceForUnknownOfflineQuery() {
        val dao = createDao()
        val answer = OfflinePackageAnswerResolver.resolveAnswer(
            query = "Who was the emperor of France in 1804?",
            language = AppLanguage.ENGLISH_ONLY,
            learningDao = dao
        )
        assertTrue("Expected curriculum guidance, got: $answer", answer.contains("Offline", ignoreCase = true) || answer.contains("Cloud AI", ignoreCase = true))
    }
}
